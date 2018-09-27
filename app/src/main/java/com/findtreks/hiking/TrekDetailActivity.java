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

import com.findtreks.hiking.model.Trek;
import com.google.android.gms.common.data.ObjectDataBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.findtreks.hiking.adapter.RatingAdapter;
import com.findtreks.hiking.model.Rating;
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
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static com.findtreks.hiking.Globals.COLLECTION_NAME;

public class TrekDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener {

    private static final String TAG = "RestaurantDetail";

    public static final String KEY_TREK_ID = "key_trek_id";

    @BindView(R.id.trek_image)
    ImageView mImageView;

    @BindView(R.id.trek_name)
    TextView mNameView;

    @BindView(R.id.trek_rating)
    MaterialRatingBar mRatingIndicator;

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

    //private RatingDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mTreksRef;
    private ListenerRegistration mTrekRegistration;

    private RatingAdapter mRatingAdapter;
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
                .collection("ratings")

                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
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
        mRatingsRecycler.setAdapter(mRatingAdapter);

        //mRatingDialog = new RatingDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        mRatingAdapter.startListening();
        mTrekRegistration = mTreksRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        if (mTrekRegistration != null) {
            mTrekRegistration.remove();
            mTrekRegistration = null;
        }
    }

    private Task<Void> addRating(final DocumentReference trekRef,
                                 final Rating rating, final String ratingId) {
        // Create reference for new rating, for use inside the transaction
        DocumentReference ratingRef;
        if (ratingId != null){
            ratingRef = trekRef.collection("ratings")
                    .document(ratingId);
        }else{
            ratingRef = trekRef.collection("ratings")
                    .document();
        }
        final DocumentReference ratingRefInsertUpdate = ratingRef;

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {

                Trek trek = transaction.get(trekRef)
                        .toObject(Trek.class);


                // Compute new number of ratings
                int newNumRatings = trek.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = trek.getAvgRating() *
                        trek.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) /
                        newNumRatings;

                // Set new trek info
                trek.setNumRatings(newNumRatings);
                trek.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(trekRef, trek);
                if (ratingId!=null){
                    Map<String,Object> ratingMap = new HashMap<>();

                    ratingMap.put("rating",rating.getRating());
                    ratingMap.put("text",rating.getText());
                    transaction.update(ratingRefInsertUpdate, ratingMap);
                }else{
                    transaction.set(ratingRefInsertUpdate, rating);
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
        mRatingIndicator.setRating((float) trek.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, trek.getNumRatings()));
        mCityView.setText(region);
        mDateView.setText(new SimpleDateFormat("EEE, dd/MM/yyyy")
                .format(new Date(trek.getTrekStartDate())));
        mCategoryView.setText(category);


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

    @OnClick(R.id.fab_show_rating_dialog)
    public void onAddRatingClicked(View view) {
        final DocumentReference ratingRef = mTreksRef.collection("ratings")
                .document();
        Query userRating= mTreksRef
                .collection("ratings")
                .whereEqualTo("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRating.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String text = null;
                Double rating = null;
                String ratingId = null;
                if(task.isComplete()){
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                    if (documents.size()>0){
                        Map<String, Object> ratingMap  = documents.get(0).getData();
                        rating = (Double)ratingMap.get("rating");
                        text = (String) ratingMap.get("text");
                        ratingId = documents.get(0).getId();
                    }
                    newInstance(rating, text, ratingId).show(getSupportFragmentManager(), RatingDialogFragment.TAG);
                }
            }
        });


        //mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onRating(Rating rating, String ratingId) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mTreksRef, rating, ratingId)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
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
    public static RatingDialogFragment newInstance(Double rating,String text,String ratingId) {
        RatingDialogFragment f = new RatingDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDouble("rating", rating == null ? 0 : rating.doubleValue());
        args.putString("text", text);
        args.putString("ratingId", ratingId);
        f.setArguments(args);

        return f;
    }

}
