package vn.edu.usth.jobmarket.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/** Downloads a company logo: URL + HttpURLConnection + AsyncTask -> Bitmap, with a memory cache. */
@SuppressWarnings("deprecation")
public final class ImageLoader {
    private static final int MAX_SIZE = 128;
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(60);

    private ImageLoader() {
    }

    public static void load(String url, ImageView view, int placeholder) {
        view.setTag(url);
        if (url == null || url.isEmpty()) {
            view.setImageResource(placeholder);
            return;
        }
        Bitmap cached = CACHE.get(url);
        if (cached != null) {
            view.setImageBitmap(cached);
            return;
        }
        view.setImageResource(placeholder);
        new Task(url, view).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class Task extends AsyncTask<Void, Void, Bitmap> {
        private final String url;
        private final WeakReference<ImageView> viewRef;

        Task(String url, ImageView view) {
            this.url = url;
            this.viewRef = new WeakReference<>(view);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                InputStream in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                if (bitmap != null && Math.max(bitmap.getWidth(), bitmap.getHeight()) > MAX_SIZE) {
                    float scale = MAX_SIZE / (float) Math.max(bitmap.getWidth(), bitmap.getHeight());
                    bitmap = Bitmap.createScaledBitmap(bitmap,
                            Math.max(1, Math.round(bitmap.getWidth() * scale)),
                            Math.max(1, Math.round(bitmap.getHeight() * scale)), true);
                }
                return bitmap;
            } catch (Exception e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            CACHE.put(url, bitmap);
            ImageView view = viewRef.get();
            // RecyclerView reuses views: only set if this view still wants this URL
            if (view != null && url.equals(view.getTag())) {
                view.setImageBitmap(bitmap);
            }
        }
    }
}
