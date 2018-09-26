package com.findtreks.hiking.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;

import java.util.Locale;

public class TranslationUtil {
    public static String getDefaultString(Context context, @StringRes int stringId, Locale locale){
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        //Locale defaultLocale = new Locale("en"); // default locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            configuration.setLocales(localeList);
            return context.createConfigurationContext(configuration).getString(stringId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration).getString(stringId);
        }
        return context.getString(stringId);
    }
    public static String[] getLocaleStringArray(Context context, @ArrayRes int stringArrayId, Locale locale){
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        //Locale defaultLocale = new Locale("en"); // default locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            configuration.setLocales(localeList);
            return context.createConfigurationContext(configuration).getResources().getStringArray(stringArrayId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration).getResources().getStringArray(stringArrayId);
        }
        return context.getResources().getStringArray(stringArrayId);
    }
}
