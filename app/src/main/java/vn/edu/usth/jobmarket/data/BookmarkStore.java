package vn.edu.usth.jobmarket.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.usth.jobmarket.model.Job;

/** Saved jobs, kept in SharedPreferences as a JSON array. */
public class BookmarkStore {
    public interface Listener {
        void onBookmarksChanged();
    }

    private static final String PREFS = "bookmarks";
    private static final String KEY = "jobs";

    private final SharedPreferences prefs;
    private final Map<Long, Job> saved = new LinkedHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    public BookmarkStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        try {
            JSONArray array = new JSONArray(prefs.getString(KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                Job job = Job.fromJson(array.getJSONObject(i));
                saved.put(job.id, job);
            }
        } catch (JSONException e) {
            saved.clear();
        }
    }

    public void addListener(Listener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public boolean isSaved(Job job) {
        return saved.containsKey(job.id);
    }

    /** Newest first. */
    public List<Job> getAll() {
        List<Job> list = new ArrayList<>(saved.values());
        java.util.Collections.reverse(list);
        return list;
    }

    /** @return true if the job is saved after the call. */
    public boolean toggle(Job job) {
        boolean nowSaved;
        if (saved.containsKey(job.id)) {
            saved.remove(job.id);
            nowSaved = false;
        } else {
            saved.put(job.id, job);
            nowSaved = true;
        }
        persist();
        for (Listener l : new ArrayList<>(listeners)) {
            l.onBookmarksChanged();
        }
        return nowSaved;
    }

    private void persist() {
        JSONArray array = new JSONArray();
        try {
            for (Job job : saved.values()) {
                array.put(job.toJson());
            }
        } catch (JSONException e) {
            return;
        }
        prefs.edit().putString(KEY, array.toString()).apply();
    }
}
