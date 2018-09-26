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
 package com.findtreks.hiking.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

/**
 * Trek POJO.
 */
@IgnoreExtraProperties
public class Trek {

    public static final String FIELD_CITY = "city";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_TREK_DATE = "trekStartDate";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";

    private String name;
    private String city;
    private String category;
    private String photo;
    private int price;
    private int numRatings;
    private double avgRating;
    private long trekStartDate;

    public Trek() {}

    public Trek(String name, String city, String category, String photo,
                int price, int numRatings, double avgRating,long trekStartDate) {
        this.name = name;
        this.city = city;
        this.category = category;
        this.price = price;
        this.numRatings = numRatings;
        this.avgRating = avgRating;
        this.trekStartDate = trekStartDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public long getTrekStartDate() {
        return trekStartDate;
    }

    public void setTrekStartDate(long trekStartDate) {
        this.trekStartDate = trekStartDate;
    }
}
