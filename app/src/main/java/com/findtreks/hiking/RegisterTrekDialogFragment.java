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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.findtreks.hiking.model.Register;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing rating form.
 */
public class RegisterTrekDialogFragment extends DialogFragment {

    public static final String TAG = "RegisterTrekDialog";


    @BindView(R.id.restaurant_form_text)
    EditText mRatingText;

    @BindView(R.id.toggleButtonIsComing)
    ToggleButton mToggleButtonIsComing;
    interface RegisterListener {
        void onRegister(Register register, String ratingId);
    }

    private RegisterListener mRatingListener;
    private String text ;
    private Boolean isComing;
    private String registerId;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        text = getArguments().getString("text");
        isComing = getArguments().getBoolean("coming",false);
        registerId = getArguments().getString("registerId");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_rating, container, false);
        ButterKnife.bind(this, v);
        mRatingText.setText(text);

        mToggleButtonIsComing.setChecked(isComing);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof RegisterListener) {
            mRatingListener = (RegisterListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @OnClick(R.id.restaurant_form_button)
    public void onSubmitClicked(View view) {
        Register register = new Register(
                FirebaseAuth.getInstance().getCurrentUser(),
                mRatingText.getText().toString(),
                mToggleButtonIsComing.isChecked());

        if (mRatingListener != null) {
            mRatingListener.onRegister(register, registerId);
        }

        dismiss();
    }

    @OnClick(R.id.restaurant_form_cancel)
    public void onCancelClicked(View view) {
        dismiss();
    }
}
