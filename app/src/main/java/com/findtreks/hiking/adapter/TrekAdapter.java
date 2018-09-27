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
 package com.findtreks.hiking.adapter;

import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.findtreks.hiking.R;
import com.findtreks.hiking.TrekApplication;
import com.findtreks.hiking.model.Trek;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of Restaurants.
 */
public class TrekAdapter extends FirestoreAdapter<TrekAdapter.ViewHolder> {

    public interface OnTrekSelectedListener {

        void onTrekSelected(DocumentSnapshot restaurant);

    }

    private OnTrekSelectedListener mListener;


    public TrekAdapter(Query query, OnTrekSelectedListener listener) {
        super(query);
        mListener = listener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_trek, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.trek_item_image)
        ImageView imageView;

        @BindView(R.id.trek_item_name)
        TextView nameView;

        @BindView(R.id.trek_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.trek_item_num_ratings)
        TextView numRatingsView;

        @BindView(R.id.trek_item_date)
        TextView dateView;

        @BindView(R.id.trek_item_category)
        TextView categoryView;

        @BindView(R.id.trek_item_region)
        TextView regionView;
        private StorageReference storageReference;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            storageReference = FirebaseStorage.getInstance().getReference();
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnTrekSelectedListener listener) {

            TrekApplication trekApplication = (TrekApplication)(itemView.getContext().getApplicationContext());

            Trek trek = snapshot.toObject(Trek.class);
            Resources resources = itemView.getResources();

            String region = trekApplication.getRegionsTranslationReversedMap().get(trek.getCity());
            String category = trekApplication.getCategoriesTranslationReversedMap().get(trek.getCategory());

            storageReference.child("images/" + trek.getPhoto())
                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Picasso.get().load(uri).into(imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
            // Load image
           /* Glide.with(imageView.getContext())

                    .using(new FirebaseImageLoader())
                    .load(storageReference.child("images/" + trek.getPhoto()))
                    .into(imageView);*/

            nameView.setText(trek.getName());
            ratingBar.setRating((float) trek.getAvgRating());
            regionView.setText(region);
            categoryView.setText(category);
            dateView.setText(new SimpleDateFormat("EEE, dd/MM/yyyy")
                    .format(new Date(trek.getTrekStartDate())));
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    trek.getNumRatings()));


            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onTrekSelected(snapshot);
                    }
                }
            });
        }

    }
}
