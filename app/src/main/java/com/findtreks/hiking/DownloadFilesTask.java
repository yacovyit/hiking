package com.findtreks.hiking;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.afinal.simplecache.ACache;

import java.io.IOException;

public class DownloadFilesTask extends AsyncTask<Uri, Integer, Bitmap> {

    private ImageView imageView;
    private String imageId;
    public DownloadFilesTask(ImageView imageView, String imageId){
        this.imageView = imageView;
        this.imageId = imageId;
    }
    @Override
    protected Bitmap doInBackground(Uri... uris) {
        for (Uri uri : uris){
            try {
                return Picasso.get().load(uri).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageView == null)
            return;
        this.imageView.setImageBitmap(bitmap);
        final TrekApplication trekApplication = (TrekApplication)(imageView.getContext().getApplicationContext());
        trekApplication.getmCache().put(imageId,bitmap,ACache.TIME_DAY);
        imageView.setImageBitmap(bitmap);
    }
}