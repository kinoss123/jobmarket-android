package vn.edu.usth.jobmarket.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.LocaleListCompat;

import vn.edu.usth.jobmarket.JobMarketApp;
import vn.edu.usth.jobmarket.R;

/** Language switch (English / Vietnamese) and offline cache control. */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle(R.string.settings_title);
        }

        RadioGroup group = findViewById(R.id.language);
        String current = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
                .toLanguageTags();
        if (current.startsWith("vi")) {
            group.check(R.id.lang_vi);
        } else if (current.startsWith("en")) {
            group.check(R.id.lang_en);
        } else {
            group.check(R.id.lang_system);
        }
        group.setOnCheckedChangeListener((g, checkedId) -> {
            String tag = checkedId == R.id.lang_vi ? "vi" : checkedId == R.id.lang_en ? "en" : "";
            LocaleListCompat locales = tag.isEmpty()
                    ? LocaleListCompat.getEmptyLocaleList()
                    : LocaleListCompat.forLanguageTags(tag);
            // recreates the running activities with the new language
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locales);
        });

        findViewById(R.id.clear_cache).setOnClickListener(v -> {
            JobMarketApp.get(this).getRepository().clearCache();
            Toast.makeText(this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
        });
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
