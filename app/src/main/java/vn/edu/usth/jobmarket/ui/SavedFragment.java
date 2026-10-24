package vn.edu.usth.jobmarket.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.usth.jobmarket.JobMarketApp;
import vn.edu.usth.jobmarket.R;
import vn.edu.usth.jobmarket.data.BookmarkStore;
import vn.edu.usth.jobmarket.model.Job;

/** Tab 3: bookmarked jobs. Works offline, swipe a card to remove it. */
public class SavedFragment extends Fragment
        implements BookmarkStore.Listener, JobAdapter.Actions {

    private BookmarkStore bookmarks;
    private JobAdapter adapter;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bookmarks = JobMarketApp.get(requireContext()).getBookmarks();
        empty = view.findViewById(R.id.empty);
        RecyclerView list = view.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new JobAdapter(bookmarks, this);
        list.setAdapter(adapter);
        adapter.attachSwipe(list);
    }

    @Override
    public void onStart() {
        super.onStart();
        bookmarks.addListener(this);
        refresh();
    }

    @Override
    public void onStop() {
        super.onStop();
        bookmarks.removeListener(this);
    }

    private void refresh() {
        adapter.setJobs(bookmarks.getAll());
        empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBookmarksChanged() {
        refresh();
    }

    @Override
    public void onOpen(Job job) {
        Intent intent = new Intent(requireContext(), JobDetailActivity.class);
        intent.putExtra(JobDetailActivity.EXTRA_JOB, job);
        startActivity(intent);
    }

    @Override
    public void onToggleSave(Job job) {
        bookmarks.toggle(job);
    }
}
