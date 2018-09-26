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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.findtreks.hiking.R;
import com.findtreks.hiking.TrekApplication;
import com.findtreks.hiking.model.Trek;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

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
        return new ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.restaurant_item_image)
        ImageView imageView;

        @BindView(R.id.restaurant_item_name)
        TextView nameView;

        @BindView(R.id.restaurant_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.restaurant_item_num_ratings)
        TextView numRatingsView;

        @BindView(R.id.restaurant_item_category)
        TextView categoryView;

        @BindView(R.id.restaurant_item_region)
        TextView regionView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnTrekSelectedListener listener) {

            TrekApplication trekApplication = (TrekApplication)(itemView.getContext().getApplicationContext());

            Trek trek = snapshot.toObject(Trek.class);
            Resources resources = itemView.getResources();

            String region = trekApplication.getRegionsTranslationReversedMap().get(trek.getCity());
            String category = trekApplication.getCategoriesTranslationReversedMap().get(trek.getCategory());

            // Load image
            Glide.with(imageView.getContext())
                    .load(trek.getPhoto())
                    .into(imageView);

            nameView.setText(trek.getName());
            ratingBar.setRating((float) trek.getAvgRating());
            regionView.setText(region);
            categoryView.setText(category);
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
