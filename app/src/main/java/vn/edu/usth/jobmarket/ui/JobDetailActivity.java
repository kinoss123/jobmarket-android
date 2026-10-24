package vn.edu.usth.jobmarket.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import vn.edu.usth.jobmarket.JobMarketApp;
import vn.edu.usth.jobmarket.R;
import vn.edu.usth.jobmarket.data.BookmarkStore;
import vn.edu.usth.jobmarket.model.Job;
import vn.edu.usth.jobmarket.util.ImageLoader;

/** Full posting. Receives the Job through an Intent extra. */
public class JobDetailActivity extends AppCompatActivity {
    public static final String EXTRA_JOB = "job";

    private Job job;
    private BookmarkStore bookmarks;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        job = (Job) getIntent().getSerializableExtra(EXTRA_JOB);
        if (job == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_detail);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle(job.company);
        }
        bookmarks = JobMarketApp.get(this).getBookmarks();

        ImageLoader.load(job.logoUrl, (ImageView) findViewById(R.id.logo), R.drawable.ic_work);
        ((TextView) findViewById(R.id.title)).setText(job.title);
        ((TextView) findViewById(R.id.company)).setText(job.company);

        String where = job.location.isEmpty() ? getString(R.string.location_anywhere) : job.location;
        String meta = job.typeLabel().isEmpty() ? where : job.typeLabel() + " · " + where;
        if (!job.shortDate().isEmpty()) {
            meta += " · " + job.shortDate();
        }
        ((TextView) findViewById(R.id.meta)).setText(meta);
        ((TextView) findViewById(R.id.salary)).setText(
                job.salary.isEmpty() ? getString(R.string.salary_none) : job.salary);

        TextView tags = findViewById(R.id.tags);
        tags.setText(job.tags);
        tags.setVisibility(job.tags.isEmpty() ? TextView.GONE : TextView.VISIBLE);

        ((TextView) findViewById(R.id.description))
                .setText(HtmlCompat.fromHtml(job.description, HtmlCompat.FROM_HTML_MODE_LEGACY));

        findViewById(R.id.apply).setOnClickListener(v -> openApplyPage());
        findViewById(R.id.share).setOnClickListener(v -> shareJob());
        saveButton = findViewById(R.id.save);
        saveButton.setOnClickListener(v -> {
            boolean nowSaved = bookmarks.toggle(job);
            Toast.makeText(this, nowSaved ? R.string.toast_saved : R.string.toast_removed,
                    Toast.LENGTH_SHORT).show();
            updateSaveButton();
        });
        updateSaveButton();
    }

    private void updateSaveButton() {
        saveButton.setText(bookmarks.isSaved(job) ? R.string.detail_unsave : R.string.detail_save);
    }

    private void openApplyPage() {
        if (job.url.isEmpty()) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(job.url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareJob() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_text, job.title, job.company, job.url));
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
