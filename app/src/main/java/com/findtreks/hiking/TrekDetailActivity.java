/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.findtreks.hiking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.findtreks.hiking.model.Register;
import com.findtreks.hiking.model.Trek;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.findtreks.hiking.adapter.RegisterAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.findtreks.hiking.Globals.COLLECTION_NAME;

public class TrekDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot>, RegisterTrekDialogFragment.RegisterListener {

    private static final String TAG = "TrekDetail";

    public static final String KEY_TREK_ID = "key_trek_id";

    @BindView(R.id.trek_image)
    ImageView mImageView;

    @BindView(R.id.trek_name)
    TextView mNameView;

    @BindView(R.id.trek_num_ratings)
    TextView mNumRatingsView;

    @BindView(R.id.trek_date)
    TextView mDateView;

    @BindView(R.id.trek_city)
    TextView mCityView;

    @BindView(R.id.trek_category)
    TextView mCategoryView;

    @BindView(R.id.view_empty_ratings)
    ViewGroup mEmptyView;

    @BindView(R.id.recycler_ratings)
    RecyclerView mRatingsRecycler;

    @BindView(R.id.fab_show_whatsapp)
    View mFabWhatsappGroup;
    //private RegisterTrekDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mTreksRef;
    private ListenerRegistration mTrekRegistration;

    private RegisterAdapter mRegisterAdapter;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trek_detail);
        ButterKnife.bind(this);

        storageReference = FirebaseStorage.getInstance().getReference();
        // Get restaurant ID from extras
        String trekId = getIntent().getExtras().getString(KEY_TREK_ID);
        if (trekId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_TREK_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
        mTreksRef = mFirestore.collection(COLLECTION_NAME).document(trekId);

        // Get ratings
        Query ratingsQuery = mTreksRef
                .collection("registration")

                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRegisterAdapter = new RegisterAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };

        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mRegisterAdapter);

        //mRatingDialog = new RegisterTrekDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        mRegisterAdapter.startListening();
        mTrekRegistration = mTreksRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mRegisterAdapter.stopListening();

        if (mTrekRegistration != null) {
            mTrekRegistration.remove();
            mTrekRegistration = null;
        }
    }

    private Task<Void> registerTrek(final DocumentReference trekRef,
                                    final Register register, final String registerId) {
        // Create reference for new register, for use inside the transaction
        DocumentReference ratingRef;
        if (registerId != null){
            ratingRef = trekRef.collection("registration")
                    .document(registerId);
        }else{
            ratingRef = trekRef.collection("registration")
                    .document();
        }
        final DocumentReference registerRefInsertUpdate = ratingRef;

        // In a transaction, add the new register and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {

                Trek trek = transaction.get(trekRef)
                        .toObject(Trek.class);


                // Compute new number of ratings
                if (registerId == null){
                    int newNumRatings = trek.getNumRegistered() + 1;
                    // Set new trek info
                    trek.setNumRegistered(newNumRatings);
                }

                // Commit to Firestore
                transaction.set(trekRef, trek);
                if (registerId!=null){
                    Map<String,Object> ratingMap = new HashMap<>();

                    ratingMap.put("text", register.getText());
                    ratingMap.put("coming", register.getComing());

                    transaction.update(registerRefInsertUpdate, ratingMap);
                }else{
                    transaction.set(registerRefInsertUpdate, register);
                }



                return null;
            }
        });
    }

    /**
     * Listener for the Trek document ({@link #mTreksRef}).
     */
    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e);
            return;
        }

        onTrekLoaded(snapshot.toObject(Trek.class));
    }

    private void onTrekLoaded(Trek trek) {

        TrekApplication trekApplication = (TrekApplication)(this.getApplication().getApplicationContext());

        String region = trekApplication.getRegionsTranslationReversedMap().get(trek.getCity());
        String category = trekApplication.getCategoriesTranslationReversedMap().get(trek.getCategory());

        mNameView.setText(trek.getName());
        mNumRatingsView.setText(getString(R.string.fmt_num_registerd, trek.getNumRegistered()));
        mCityView.setText(region);
        mDateView.setText(new SimpleDateFormat("EEE, dd/MM/yyyy")
                .format(new Date(trek.getTrekStartDate())));
        if (trek.getWhatsappGroup()!= null){
            mFabWhatsappGroup.setEnabled(true);
            mFabWhatsappGroup.setTag(trek.getWhatsappGroup());
        }else{
            mFabWhatsappGroup.setEnabled(false);
            mFabWhatsappGroup.setTag(null);
        }


        storageReference.child("images/" + trek.getPhoto())
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Picasso.get().load(uri).into(mImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
       /* // Background image
        Glide.with(mImageView.getContext())
                .load(trek.getPhoto())
                .into(mImageView);*/
    }

    @OnClick(R.id.trek_button_back)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.fab_show_whatsapp)
    public void onWhatsappChatClicked(View view) {
        String tagUrl = view.getTag().toString();
        if (tagUrl != null && tagUrl.startsWith("https://chat.whatsapp.com/")) {
            tagUrl.replace("https://chat.whatsapp.com/","");
            Intent intentWhatsapp = new Intent(Intent.ACTION_VIEW);

            String url = String.format("%s%s", "https://chat.whatsapp.com/", tagUrl);
            intentWhatsapp.setData(Uri.parse(url));
            intentWhatsapp.setPackage("com.whatsapp");
            startActivity(intentWhatsapp);
        }
    }
    @OnClick(R.id.fab_show_register_dialog)
    public void onAddRatingClicked(View view) {
        final DocumentReference ratingRef = mTreksRef.collection("registration")
                .document();
        Query userRating= mTreksRef
                .collection("registration")
                .whereEqualTo("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRating.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String text = null;
                String registerId = null;
                boolean isComing = true;
                if(task.isComplete()){
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                    if (documents.size()>0){
                        Map<String, Object> ratingMap  = documents.get(0).getData();

                        text = (String) ratingMap.get("text");
                        isComing = (Boolean) ratingMap.get("coming");
                        registerId = documents.get(0).getId();
                    }
                    newInstance(text, isComing, registerId).show(getSupportFragmentManager(), RegisterTrekDialogFragment.TAG);
                }
            }
        });


        //mRatingDialog.show(getSupportFragmentManager(), RegisterTrekDialogFragment.TAG);
    }

    @Override
    public void onRegister(Register register, String ratingId) {
        // In a transaction, add the new register and update the aggregate totals
        registerTrek(mTreksRef, register, ratingId)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Register added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add register failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add register",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public static RegisterTrekDialogFragment newInstance(String text, boolean isComing, String registerId) {
        RegisterTrekDialogFragment f = new RegisterTrekDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("text", text);
        args.putBoolean("coming", isComing);
        args.putString("registerId", registerId);
        f.setArguments(args);

        return f;
    }

}
