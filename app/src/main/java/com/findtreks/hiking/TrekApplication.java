package com.findtreks.hiking;

import android.app.Application;

import com.findtreks.hiking.util.TranslationUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class TrekApplication extends Application {
    private Map<String,String> categoriesTranslationMap, categoriesTranslationReversedMap;
    private Map<String,String> regionsTranslationMap,regionsTranslationReversedMap;
    private Map<String,String> trekDateTranslationMap, trekDateTranslationReversedMap;
    private Locale defaultLocale = new Locale("en");
    @Override
    public void onCreate() {
        super.onCreate();
        initTranslationsMappers();
    }
    private void initTranslationsMappers(){
        initCategoriesTranslationMap();
        initRegionsTranslationMap();
        initTrekDateTranslationMap();
    }
    private void initCategoriesTranslationMap(){
        categoriesTranslationMap = new HashMap<>();
        categoriesTranslationReversedMap = new HashMap<>();
        String[] defaultCategories = TranslationUtil.getLocaleStringArray(this, R.array.categories, defaultLocale);
        String[] localeCategories = TranslationUtil.getLocaleStringArray(this, R.array.categories, getResources().getConfiguration().locale);

        for (int i=0; i<defaultCategories.length; i++){
            categoriesTranslationMap.put(localeCategories[i], defaultCategories[i]);
            categoriesTranslationReversedMap.put(defaultCategories[i], localeCategories[i]);
        }
    }
    private void initRegionsTranslationMap(){
        regionsTranslationMap = new HashMap<>();
        regionsTranslationReversedMap = new HashMap<>();
        String[] defaultRegions = TranslationUtil.getLocaleStringArray(this, R.array.regions, defaultLocale);
        String[] localeRegions = TranslationUtil.getLocaleStringArray(this, R.array.regions, getResources().getConfiguration().locale);
        for (int i=0; i<defaultRegions.length; i++){
            regionsTranslationMap.put(localeRegions[i], defaultRegions[i]);
            regionsTranslationReversedMap.put(defaultRegions[i], localeRegions[i]);
        }

    }
    private void initTrekDateTranslationMap(){
        trekDateTranslationMap = new HashMap<>();
        trekDateTranslationReversedMap = new HashMap<>();
        String[] defaultTrekDate = TranslationUtil.getLocaleStringArray(this, R.array.trek_date, defaultLocale);
        String[] localeTrekDate = TranslationUtil.getLocaleStringArray(this, R.array.trek_date, getResources().getConfiguration().locale);

        for (int i=0; i<defaultTrekDate.length; i++){
            trekDateTranslationMap.put(localeTrekDate[i], defaultTrekDate[i]);
            trekDateTranslationReversedMap.put(defaultTrekDate[i], localeTrekDate[i]);
        }
    }
    public Map<String, String> getCategoriesTranslationMap() {
        return categoriesTranslationMap;
    }

    public Map<String, String> getRegionsTranslationMap() {
        return regionsTranslationMap;
    }

    public Map<String, String> getCategoriesTranslationReversedMap() {
        return categoriesTranslationReversedMap;
    }

    public Map<String, String> getRegionsTranslationReversedMap() {
        return regionsTranslationReversedMap;
    }

    public Map<String, String> getTrekDateTranslationMap() {
        return trekDateTranslationMap;
    }

    public Map<String, String> getTrekDateTranslationReversedMap() {
        return trekDateTranslationReversedMap;
    }
}
