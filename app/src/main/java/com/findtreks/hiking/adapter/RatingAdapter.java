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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.findtreks.hiking.R;
import com.findtreks.hiking.model.Rating;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a bunch of Ratings.
 */
public class RatingAdapter extends FirestoreAdapter<RatingAdapter.ViewHolder> {

    public RatingAdapter(Query query) {
        super(query);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false), parent.getContext());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position).toObject(Rating.class));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private Context ctx;
        @BindView(R.id.rating_item_name)
        TextView nameView;

        @BindView(R.id.rating_item_date)
        TextView dateView;

        @BindView(R.id.rating_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.rating_item_text)
        TextView textView;

        @BindView(R.id.toggleButtonIsComing)
        ToggleButton isComingToggleButton;

        @BindView(R.id.imageButtonPhone)
        Button buttonPhone;

        public ViewHolder(View itemView, Context ctx) {
            super(itemView);
            this.ctx = ctx;
            ButterKnife.bind(this, itemView);
        }

        public void bind(Rating rating) {
            nameView.setText(rating.getUserName());
            ratingBar.setRating((float) rating.getRating());
            textView.setText(rating.getText());
            dateView.setText(new SimpleDateFormat("EEE, dd/MM/yyyy HH:mm")
                    .format(new Date(rating.getTimestamp().getTime())));
            isComingToggleButton.setChecked(rating.getComing());
            final String number = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            buttonPhone.setText(number);
            buttonPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openWhatsApp(number);
                }
                private void openWhatsApp(String number) {
                    //String smsNumber = "7****"; //without '+'
                    number = number.replace("+","");
                    try {
                        Intent sendIntent = new Intent("android.intent.action.MAIN");
                        //sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                        sendIntent.putExtra("jid", number + "@s.whatsapp.net"); //phone number without "+" prefix
                        sendIntent.setPackage("com.whatsapp");
                        ctx.startActivity(sendIntent);
                    } catch(Exception e) {
                        //Toast.makeText(this, "Error/n" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

}
