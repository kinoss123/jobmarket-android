package vn.edu.usth.jobmarket.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/** One job posting. Plain data holder (the "Model" in the course's MVC). */
public class Job implements Serializable {
    public final long id;
    public final String title;
    public final String company;
    public final String category;
    public final String type;
    public final String location;
    public final String salary;
    public final String date;
    public final String url;
    public final String logoUrl;
    public final String tags;
    public final String description; // raw HTML, shortened

    public Job(long id, String title, String company, String category, String type,
               String location, String salary, String date, String url, String logoUrl,
               String tags, String description) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.category = category;
        this.type = type;
        this.location = location;
        this.salary = salary;
        this.date = date;
        this.url = url;
        this.logoUrl = logoUrl;
        this.tags = tags;
        this.description = description;
    }

    /** "full_time" -> "Full time" */
    public String typeLabel() {
        return humanize(type);
    }

    public static String humanize(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String s = raw.replace('_', ' ').replace('-', ' ').trim();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** "2026-10-12T09:00:00" -> "2026-10-12" */
    public String shortDate() {
        return date != null && date.length() >= 10 ? date.substring(0, 10) : "";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("title", title);
        o.put("company", company);
        o.put("category", category);
        o.put("type", type);
        o.put("location", location);
        o.put("salary", salary);
        o.put("date", date);
        o.put("url", url);
        o.put("logo", logoUrl);
        o.put("tags", tags);
        o.put("description", description);
        return o;
    }

    public static Job fromJson(JSONObject o) {
        return new Job(o.optLong("id"), o.optString("title"), o.optString("company"),
                o.optString("category"), o.optString("type"), o.optString("location"),
                o.optString("salary"), o.optString("date"), o.optString("url"),
                o.optString("logo"), o.optString("tags"), o.optString("description"));
    }
}
