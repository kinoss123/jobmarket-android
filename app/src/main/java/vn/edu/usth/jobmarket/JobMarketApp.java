package vn.edu.usth.jobmarket;

import android.app.Application;
import android.content.Context;

import vn.edu.usth.jobmarket.data.BookmarkStore;
import vn.edu.usth.jobmarket.data.JobRepository;

/** Application subclass: holds the objects every screen shares. */
public class JobMarketApp extends Application {
    private JobRepository repository;
    private BookmarkStore bookmarks;

    @Override
    public void onCreate() {
        super.onCreate();
        bookmarks = new BookmarkStore(this);
        repository = new JobRepository(this);
    }

    public static JobMarketApp get(Context context) {
        return (JobMarketApp) context.getApplicationContext();
    }

    public JobRepository getRepository() {
        return repository;
    }

    public BookmarkStore getBookmarks() {
        return bookmarks;
    }
}
