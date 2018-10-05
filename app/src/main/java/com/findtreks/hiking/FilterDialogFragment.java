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
import android.widget.Spinner;

import com.findtreks.hiking.util.TrekUtil;
import com.google.firebase.firestore.Query;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing filter form.
 */
public class FilterDialogFragment extends DialogFragment {

    public static final String TAG = "FilterDialog";

    interface FilterListener {

        void onFilter(Filters filters);

    }

    private View mRootView;

    @BindView(R.id.spinner_category)
    Spinner mCategorySpinner;

    @BindView(R.id.spinner_region)
    Spinner mCitySpinner;


    @BindView(R.id.spinner_date)
    Spinner mDateSpinner;

    private FilterListener mFilterListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);
        ButterKnife.bind(this, mRootView);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FilterListener) {
            mFilterListener = (FilterListener) context;
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
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
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
        String selected = (String) mCitySpinner.getSelectedItem();
        if (getString(R.string.value_any_region).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }
    private String getSelectedTrekDatePeriod(){
        String selected = (String) mDateSpinner.getSelectedItem();
        return selected;
    }
    private long[] getSelectedTrekDate() {
        String selected = (String) mDateSpinner.getSelectedItem();
        Calendar c = Calendar.getInstance();
        TrekUtil.setStartOfDateCalender(c);
        long[] startEndTime = new long[2];


        //now for end time
        if (selected.equals(getString(R.string.time_today))) {
            //start time and end time is the same
            startEndTime[0] = c.getTimeInMillis();
            startEndTime[1] = c.getTimeInMillis();
        } else if (selected.equals(getString(R.string.time_tomorrow))) {
            //start time and end time with diff of 1 day
            startEndTime[0] = c.getTimeInMillis();
            c.add(Calendar.DATE, 1);
            startEndTime[1] = c.getTimeInMillis();
        } else if (selected.equals(getString(R.string.time_this_week))) {
            startEndTime[0] = c.getTimeInMillis();
            //add 7 days
            c.add(Calendar.DATE, 7);
            //got back to the first day of the next week
            c.set(Calendar.DAY_OF_WEEK,c.getFirstDayOfWeek());
            //from the first day subtract 1 day
            c.add(Calendar.DATE, -1);
            startEndTime[1] = c.getTimeInMillis();
        }else if (selected.equals(getString(R.string.time_next_week))) {
            //add 7 days
            c.add(Calendar.DATE, 7);
            //got back to the first day of the next week
            c.set(Calendar.DAY_OF_WEEK,c.getFirstDayOfWeek());
            startEndTime[0] = c.getTimeInMillis();
            //add 7 days
            c.add(Calendar.DATE, 7);
            //got back to the first day of the next next week
            c.set(Calendar.DAY_OF_WEEK,c.getFirstDayOfWeek());
            //from the first day subtract 1 day
            c.add(Calendar.DATE, -1);
            startEndTime[1] = c.getTimeInMillis();
        }else if (selected.equals(getString(R.string.time_this_month))) {
            //today
            startEndTime[0] = c.getTimeInMillis();
            c.set(Calendar.DATE, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            //last day of this month
            startEndTime[1] = c.getTimeInMillis();
        }else if (selected.equals(getString(R.string.time_next_month))) {
            c.add(Calendar.MONTH, 1);
            //first day of the next month
            c.set(Calendar.DATE, c.getActualMinimum(Calendar.DAY_OF_MONTH));
            startEndTime[0] = c.getTimeInMillis();
            //last day of the next month
            c.set(Calendar.DATE, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            startEndTime[1] = c.getTimeInMillis();
        } else {
            //on selecting any time then just show treks from today
            startEndTime[0] = c.getTimeInMillis();
            //zero indicate ignore filtering end date
            startEndTime[1] = 0;
        }
        return startEndTime;
    }

    public void resetFilters() {
        if (mRootView != null) {
            mCategorySpinner.setSelection(0);
            mCitySpinner.setSelection(0);
            mDateSpinner.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();


        if (mRootView != null) {
            //normalize category and region values to english keys
            TrekApplication trekApplication = (TrekApplication)(this.getContext().getApplicationContext());
            String category = trekApplication.getCategoriesTranslationMap().get(getSelectedCategory());
            String region = trekApplication.getRegionsTranslationMap().get(getSelectedRegion());

            String trekTimePeriod= trekApplication.getTrekDateTranslationMap().get(getSelectedTrekDatePeriod());

            filters.setCategory(category);
            filters.setRegion(region);
            filters.setTrekTimePeriod(trekTimePeriod);
            filters.setTrekTime(getSelectedTrekDate());
        }

        return filters;
    }
}
