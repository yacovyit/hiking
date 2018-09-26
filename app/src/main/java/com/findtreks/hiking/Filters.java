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
import android.text.TextUtils;

import com.findtreks.hiking.model.Trek;
import com.google.firebase.firestore.Query;

/**
 * Object for passing filters around.
 */
public class Filters {

    private String category = null;
    private String region = null;
    private long trekTime;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Trek.FIELD_AVG_RATING);
        filters.setSortDirection(Query.Direction.DESCENDING);

        return filters;
    }

    public boolean hasCategory() {
        return !(TextUtils.isEmpty(category));
    }

    public boolean hasCity() {
        return !(TextUtils.isEmpty(region));
    }

    public boolean hasTrekDate() {
        return trekTime > 0;
    }

    public long getTrekTime() {
        return trekTime;
    }

    public void setTrekTime(long trekTime) {
        this.trekTime = trekTime;
    }

    public boolean hasSortBy() {
        return !(TextUtils.isEmpty(sortBy));
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }


    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (category == null && region == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_treks));
            desc.append("</b>");
        }

        if (category != null) {
            desc.append("<b>");
            desc.append(category);
            desc.append("</b>");
        }

        if (category != null && region != null) {
            desc.append(" * ");
        }

        if (region != null) {
            desc.append("<b>");
            desc.append(region);
            desc.append("</b>");
        }

        /*if (price > 0) {
            desc.append(" * ");
            desc.append("<b>");
            desc.append(TrekUtil.getPriceString(price));
            desc.append("</b>");
        }*/

        return desc.toString();
    }

    public String getOrderDescription(Context context) {
        if (Trek.FIELD_TREK_DATE.equals(sortBy)) {
            return context.getString(R.string.sorted_by_time);
        } else if (Trek.FIELD_POPULARITY.equals(sortBy)) {
            return context.getString(R.string.sorted_by_registration);
        } else {
            return context.getString(R.string.sorted_by_rating);
        }
    }
}
