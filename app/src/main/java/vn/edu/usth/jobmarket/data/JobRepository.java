package vn.edu.usth.jobmarket.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.jobmarket.model.Job;

/**
 * Single source of jobs for all screens.
 *
 * Order of attempts: fresh cache file -> Remotive over Volley -> older cache -> bundled sample.
 * Callbacks always arrive on the main thread.
 */
public class JobRepository {
    public enum Source { LIVE, CACHE, SAMPLE }

    public interface Listener {
        void onLoading();

        void onLoaded(List<Job> jobs, Source source, long timeMillis);
    }

    private static final String BASE_URL = "https://remotive.com/api/remote-jobs";
    private static final int LIMIT = 100;
    // The API asks clients not to poll often, so keep results for a while.
    private static final long CACHE_TTL = 30 * 60 * 1000L;

    private final Context context;
    private final RequestQueue queue;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Listener> listeners = new ArrayList<>();

    private List<Job> jobs = new ArrayList<>();
    private Source source = Source.SAMPLE;
    private long loadedAt;
    private boolean loading;
    private boolean everLoaded;
    private String category = "";

    public JobRepository(Context context) {
        this.context = context.getApplicationContext();
        this.queue = Volley.newRequestQueue(this.context);
    }

    // ---- state for screens ----

    public void addListener(Listener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Source getSource() {
        return source;
    }

    public long getLoadedAt() {
        return loadedAt;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean hasData() {
        return everLoaded;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String slug) {
        if (!slug.equals(category)) {
            category = slug;
            load(false);
        }
    }

    // ---- loading ----

    /** @param force skip the "cache is still fresh" shortcut (toolbar refresh button) */
    public void load(final boolean force) {
        if (loading) {
            return;
        }
        loading = true;
        for (Listener l : new ArrayList<>(listeners)) {
            l.onLoading();
        }
        final String cat = category;
        // Disk access goes to a worker thread, result comes back through the Handler.
        new Thread(() -> {
            final CacheEntry cache = readCache(cat);
            final boolean fresh = cache != null
                    && System.currentTimeMillis() - cache.time < CACHE_TTL;
            main.post(() -> {
                if (fresh && !force) {
                    deliver(cat, cache.jobs, Source.CACHE, cache.time);
                } else {
                    fetch(cat, cache);
                }
            });
        }).start();
    }

    private void fetch(final String cat, final CacheEntry cache) {
        String url = BASE_URL + "?limit=" + LIMIT + (cat.isEmpty() ? "" : "&category=" + cat);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<Job> parsed = JobParser.parse(response);
                        long now = System.currentTimeMillis();
                        writeCacheAsync(cat, parsed, now);
                        deliver(cat, parsed, Source.LIVE, now);
                    } catch (JSONException e) {
                        fallback(cat, cache);
                    }
                },
                error -> fallback(cat, cache));
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1f));
        queue.add(request);
    }

    private void fallback(String cat, CacheEntry cache) {
        if (cache != null) {
            deliver(cat, cache.jobs, Source.CACHE, cache.time);
        } else {
            deliver(cat, readSample(), Source.SAMPLE, System.currentTimeMillis());
        }
    }

    private void deliver(String cat, List<Job> result, Source src, long time) {
        loading = false;
        if (!cat.equals(category)) {
            // user changed category while we were loading: discard and start again
            load(false);
            return;
        }
        jobs = result;
        source = src;
        loadedAt = time;
        everLoaded = true;
        for (Listener l : new ArrayList<>(listeners)) {
            l.onLoaded(result, src, time);
        }
    }

    // ---- cache (private files) and sample (assets) ----

    private static class CacheEntry {
        final long time;
        final List<Job> jobs;

        CacheEntry(long time, List<Job> jobs) {
            this.time = time;
            this.jobs = jobs;
        }
    }

    private File cacheFile(String cat) {
        return new File(context.getFilesDir(), "jobs_" + (cat.isEmpty() ? "all" : cat) + ".json");
    }

    private CacheEntry readCache(String cat) {
        File file = cacheFile(cat);
        if (!file.exists()) {
            return null;
        }
        try (InputStream in = new FileInputStream(file)) {
            JSONObject root = new JSONObject(readAll(in));
            JSONArray array = root.getJSONArray("jobs");
            List<Job> list = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                list.add(Job.fromJson(array.getJSONObject(i)));
            }
            return new CacheEntry(root.getLong("time"), list);
        } catch (IOException | JSONException e) {
            return null;
        }
    }

    private void writeCacheAsync(final String cat, final List<Job> list, final long time) {
        new Thread(() -> {
            try {
                JSONArray array = new JSONArray();
                for (Job job : list) {
                    array.put(job.toJson());
                }
                JSONObject root = new JSONObject();
                root.put("time", time);
                root.put("jobs", array);
                try (FileOutputStream out = new FileOutputStream(cacheFile(cat))) {
                    out.write(root.toString().getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException | JSONException e) {
                // cache is best effort
            }
        }).start();
    }

    public void clearCache() {
        File[] files = context.getFilesDir().listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.getName().startsWith("jobs_")) {
                f.delete();
            }
        }
    }

    private List<Job> readSample() {
        try (InputStream in = context.getAssets().open("sample_jobs.json")) {
            return JobParser.parse(readAll(in));
        } catch (IOException | JSONException e) {
            return new ArrayList<>();
        }
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
