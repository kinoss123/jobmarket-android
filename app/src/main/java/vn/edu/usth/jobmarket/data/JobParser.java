package vn.edu.usth.jobmarket.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.jobmarket.model.Job;

/** Turns the Remotive JSON ({"jobs": [...]}) into Job objects. */
public final class JobParser {
    private static final int MAX_DESCRIPTION = 4000;

    private JobParser() {
    }

    public static List<Job> parse(String json) throws JSONException {
        JSONArray array = new JSONObject(json).getJSONArray("jobs");
        List<Job> result = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            result.add(new Job(
                    o.optLong("id"),
                    str(o, "title"),
                    str(o, "company_name"),
                    str(o, "category"),
                    str(o, "job_type"),
                    str(o, "candidate_required_location"),
                    str(o, "salary"),
                    str(o, "publication_date"),
                    str(o, "url"),
                    str(o, "company_logo"),
                    tags(o.optJSONArray("tags")),
                    shorten(str(o, "description"))));
        }
        return result;
    }

    // JSONObject.optString() returns the text "null" for JSON null, so check first.
    private static String str(JSONObject o, String key) {
        return o.isNull(key) ? "" : o.optString(key, "");
    }

    private static String tags(JSONArray array) {
        if (array == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(array.optString(i));
        }
        return sb.toString();
    }

    private static String shorten(String s) {
        return s.length() > MAX_DESCRIPTION ? s.substring(0, MAX_DESCRIPTION) + "..." : s;
    }
}
