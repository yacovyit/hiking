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
import android.widget.Spinner;

import com.findtreks.hiking.model.Trek;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing filter form.
 */
public class AddTrekDialogFragment extends DialogFragment {

    public static final String TAG = "AddTrekDialog";

    interface CreateTrekListener {

        void onCreateTrek(Trek trek);

    }

    private View mRootView;

    @BindView(R.id.spinner_category)
    Spinner mCategorySpinner;

    @BindView(R.id.spinner_region)
    Spinner mRegionSpinner;

    @BindView(R.id.spinner_price)
    Spinner mPriceSpinner;

    @BindView(R.id.edit_trek_details)
    EditText mTrekDetails;

    private CreateTrekListener mCreateTrekListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_create_trek, container, false);
        ButterKnife.bind(this, mRootView);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CreateTrekListener) {
            mCreateTrekListener = (CreateTrekListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.button_search)
    public void onSearchClicked() {
        if (mCreateTrekListener != null) {
            mCreateTrekListener.onCreateTrek(getTrek());
        }

        dismiss();
    }

    @OnClick(R.id.button_cancel)
    public void onCancelClicked() {
        dismiss();
    }

    @Nullable
    private String getSelectedCategory() {
        String selected = (String) mCategorySpinner.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedRegion() {
        String selected = (String) mRegionSpinner.getSelectedItem();
        if (getString(R.string.value_any_region).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    private int getSelectedTrekDate() {
        String selected = (String) mPriceSpinner.getSelectedItem();
        if (selected.equals(getString(R.string.time_today))) {
            return 1;
        } else if (selected.equals(getString(R.string.time_tomorrow))) {
            return 2;
        } else if (selected.equals(getString(R.string.time_this_week))) {
            return 3;
        } else {
            return -1;
        }
    }

    private String getTreDetails() {
        String trekDetails = mTrekDetails.getText().toString();
        return trekDetails;
    }


    public void resetTrek() {
        if (mRootView != null) {
            mCategorySpinner.setSelection(0);
            mRegionSpinner.setSelection(0);
            mPriceSpinner.setSelection(0);
        }
    }

    public Trek getTrek() {
        Trek trek = new Trek();

        if (mRootView != null) {
            //normalize category and region values to english keys
            TrekApplication trekApplication = (TrekApplication)(this.getContext().getApplicationContext());
            String category = trekApplication.getCategoriesTranslationMap().get(getSelectedCategory());
            String region = trekApplication.getRegionsTranslationMap().get(getSelectedRegion());

            trek.setCategory(category);
            trek.setCity(region);
            trek.setPrice(getSelectedTrekDate());
            trek.setName(getTreDetails());

        }

        return trek;
    }
}
