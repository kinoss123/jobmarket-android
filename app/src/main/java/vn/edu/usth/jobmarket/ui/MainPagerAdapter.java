package vn.edu.usth.jobmarket.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import vn.edu.usth.jobmarket.R;

/** Which fragment goes in which tab. */
@SuppressWarnings("deprecation")
public class MainPagerAdapter extends FragmentPagerAdapter {
    private final Context context;

    public MainPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new JobListFragment();
            case 1:
                return new InsightsFragment();
            default:
                return new SavedFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.tab_jobs);
            case 1:
                return context.getString(R.string.tab_insights);
            default:
                return context.getString(R.string.tab_saved);
        }
    }
}
