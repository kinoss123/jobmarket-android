package vn.edu.usth.jobmarket.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vn.edu.usth.jobmarket.JobMarketApp;
import vn.edu.usth.jobmarket.R;
import vn.edu.usth.jobmarket.data.JobRepository;
import vn.edu.usth.jobmarket.model.Job;

/**
 * Tab 2: what the current set of postings says about the market.
 * Bars are plain Views sized with layout_weight, no chart library.
 */
public class InsightsFragment extends Fragment implements JobRepository.Listener {
    private static final int TOP_N = 6;

    private JobRepository repo;
    private LinearLayout content;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_insights, container, false);
        content = v.findViewById(R.id.content);
        empty = v.findViewById(R.id.empty);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        repo = JobMarketApp.get(requireContext()).getRepository();
        repo.addListener(this);
        render(repo.getJobs());
    }

    @Override
    public void onStop() {
        super.onStop();
        repo.removeListener(this);
    }

    @Override
    public void onLoading() {
    }

    @Override
    public void onLoaded(List<Job> jobs, JobRepository.Source source, long time) {
        render(jobs);
    }

    private void render(List<Job> jobs) {
        content.removeAllViews();
        empty.setVisibility(jobs.isEmpty() ? View.VISIBLE : View.GONE);
        if (jobs.isEmpty()) {
            return;
        }

        Map<String, Integer> byCategory = new HashMap<>();
        Map<String, Integer> byType = new HashMap<>();
        Map<String, Integer> byCompany = new HashMap<>();
        Set<String> companies = new HashSet<>();
        int withSalary = 0;
        for (Job job : jobs) {
            bump(byCategory, job.category);
            bump(byType, job.typeLabel());
            bump(byCompany, job.company);
            companies.add(job.company);
            if (!job.salary.isEmpty()) {
                withSalary++;
            }
        }

        // headline numbers
        LinearLayout tiles = new LinearLayout(requireContext());
        tiles.setOrientation(LinearLayout.HORIZONTAL);
        addTile(tiles, String.valueOf(jobs.size()), getString(R.string.insights_total));
        addTile(tiles, String.valueOf(companies.size()), getString(R.string.insights_companies));
        addTile(tiles, Math.round(100f * withSalary / jobs.size()) + "%",
                getString(R.string.insights_with_salary));
        content.addView(tiles);

        addSection(R.string.insights_by_category, byCategory, true);
        addSection(R.string.insights_by_type, byType, false);
        addSection(R.string.insights_top_companies, byCompany, false);
    }

    private static void bump(Map<String, Integer> map, String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        Integer old = map.get(key);
        map.put(key, old == null ? 1 : old + 1);
    }

    private void addTile(LinearLayout parent, String value, String label) {
        View tile = getLayoutInflater().inflate(R.layout.item_stat, parent, false);
        ((TextView) tile.findViewById(R.id.value)).setText(value);
        ((TextView) tile.findViewById(R.id.label)).setText(label);
        parent.addView(tile);
    }

    private void addSection(int titleRes, Map<String, Integer> data, boolean humanizeKeys) {
        if (data.isEmpty()) {
            return;
        }
        TextView title = new TextView(requireContext());
        title.setText(titleRes);
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary));
        int pad = getResources().getDimensionPixelSize(R.dimen.margin_small);
        title.setPadding(pad, pad * 3, pad, pad);
        content.addView(title);

        List<Map.Entry<String, Integer>> rows = new ArrayList<>(data.entrySet());
        Collections.sort(rows, (a, b) -> b.getValue() - a.getValue());
        int max = rows.get(0).getValue();
        for (int i = 0; i < Math.min(TOP_N, rows.size()); i++) {
            Map.Entry<String, Integer> row = rows.get(i);
            View v = getLayoutInflater().inflate(R.layout.item_bar, content, false);
            ((TextView) v.findViewById(R.id.label))
                    .setText(humanizeKeys ? Job.humanize(row.getKey()) : row.getKey());
            ((TextView) v.findViewById(R.id.count)).setText(String.valueOf(row.getValue()));
            setWeight(v.findViewById(R.id.bar), row.getValue());
            setWeight(v.findViewById(R.id.rest), max - row.getValue());
            content.addView(v);
        }
    }

    private static void setWeight(View view, float weight) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.weight = weight;
        view.setLayoutParams(lp);
    }
}
