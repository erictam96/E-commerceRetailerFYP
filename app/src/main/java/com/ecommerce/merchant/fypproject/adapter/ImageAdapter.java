package com.ecommerce.merchant.fypproject.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;

public class ImageAdapter {
    private static ImageAdapter imageAdapter;

    private Network networkOBJ ;

    private RequestQueue requestQueue1;

    private final ImageLoader Imageloader1;

    private Cache cache1 ;

    private static Context context1;

    private final LruCache<String, Bitmap> LRUCACHE = new LruCache<>(30);

    private ImageAdapter(Context context) {

        context1 = context;

        this.requestQueue1 = RequestQueueFunction();

        Imageloader1 = new ImageLoader(requestQueue1, new ImageLoader.ImageCache() {

            @Override
            public Bitmap getBitmap(String URL) {

                return LRUCACHE.get(URL);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {

                LRUCACHE.put(url, bitmap);
            }
        });
    }

    public ImageLoader getImageLoader() {

        return Imageloader1;
    }

    public static ImageAdapter getInstance(Context SynchronizedContext) {

        if (imageAdapter == null) {

            imageAdapter = new ImageAdapter(SynchronizedContext);
        }
        return imageAdapter;
    }

    private RequestQueue RequestQueueFunction() {

        if (requestQueue1 == null) {

            cache1 = new DiskBasedCache(context1.getCacheDir());

            networkOBJ = new BasicNetwork(new HurlStack());

            requestQueue1 = new RequestQueue(cache1, networkOBJ);

            requestQueue1.start();
        }
        return requestQueue1;
    }
}
