package vn.edu.usth.jobmarket.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.edu.usth.jobmarket.JobMarketApp;
import vn.edu.usth.jobmarket.R;
import vn.edu.usth.jobmarket.data.BookmarkStore;
import vn.edu.usth.jobmarket.data.JobRepository;
import vn.edu.usth.jobmarket.model.Job;

/** Tab 1: browse, filter by category (server side) and search (local). */
public class JobListFragment extends Fragment
        implements JobRepository.Listener, BookmarkStore.Listener, JobAdapter.Actions {

    private JobRepository repo;
    private BookmarkStore bookmarks;
    private JobAdapter adapter;

    private TextView status, count, empty;
    private ProgressBar progress;
    private EditText search;
    private String[] slugs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jobs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repo = JobMarketApp.get(requireContext()).getRepository();
        bookmarks = JobMarketApp.get(requireContext()).getBookmarks();

        status = view.findViewById(R.id.status);
        count = view.findViewById(R.id.count);
        empty = view.findViewById(R.id.empty);
        progress = view.findViewById(R.id.progress);
        search = view.findViewById(R.id.search);

        RecyclerView list = view.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new JobAdapter(bookmarks, this);
        list.setAdapter(adapter);
        adapter.attachSwipe(list);

        slugs = getResources().getStringArray(R.array.category_slugs);
        Spinner category = view.findViewById(R.id.category);
        category.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.category_names)));
        category.setSelection(indexOfSlug(repo.getCategory()));
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                repo.setCategory(slugs[position]); // no-op if unchanged
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        repo.addListener(this);
        bookmarks.addListener(this);
        if (repo.isLoading()) {
            onLoading();
        } else {
            showSourceBanner(repo.getSource(), repo.getLoadedAt());
            applyFilter();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        repo.removeListener(this);
        bookmarks.removeListener(this);
    }

    private int indexOfSlug(String slug) {
        for (int i = 0; i < slugs.length; i++) {
            if (slugs[i].equals(slug)) {
                return i;
            }
        }
        return 0;
    }

    // ---- repository callbacks ----

    @Override
    public void onLoading() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
    }

    @Override
    public void onLoaded(List<Job> jobs, JobRepository.Source source, long time) {
        progress.setVisibility(View.GONE);
        showSourceBanner(source, time);
        applyFilter();
    }

    private void showSourceBanner(JobRepository.Source source, long time) {
        switch (source) {
            case LIVE:
                status.setVisibility(View.GONE);
                break;
            case CACHE:
                status.setVisibility(View.VISIBLE);
                status.setText(getString(R.string.status_cache,
                        DateUtils.getRelativeTimeSpanString(time)));
                break;
            default:
                status.setVisibility(View.VISIBLE);
                status.setText(R.string.status_sample);
        }
    }

    @Override
    public void onBookmarksChanged() {
        adapter.notifyDataSetChanged(); // refresh the bookmark icons
    }

    // ---- local search ----

    private void applyFilter() {
        String q = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<Job> result = new ArrayList<>();
        for (Job job : repo.getJobs()) {
            if (q.isEmpty() || job.title.toLowerCase(Locale.ROOT).contains(q)
                    || job.company.toLowerCase(Locale.ROOT).contains(q)
                    || job.tags.toLowerCase(Locale.ROOT).contains(q)) {
                result.add(job);
            }
        }
        adapter.setJobs(result);
        count.setText(getString(R.string.jobs_count, result.size()));
        empty.setVisibility(result.isEmpty() && !repo.isLoading() ? View.VISIBLE : View.GONE);
    }

    // ---- card actions ----

    @Override
    public void onOpen(Job job) {
        Intent intent = new Intent(requireContext(), JobDetailActivity.class);
        intent.putExtra(JobDetailActivity.EXTRA_JOB, job);
        startActivity(intent);
    }

    @Override
    public void onToggleSave(Job job) {
        boolean nowSaved = bookmarks.toggle(job);
        Toast.makeText(requireContext(),
                nowSaved ? R.string.toast_saved : R.string.toast_removed, Toast.LENGTH_SHORT).show();
    }
}
