package vn.edu.usth.jobmarket.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.jobmarket.R;
import vn.edu.usth.jobmarket.data.BookmarkStore;
import vn.edu.usth.jobmarket.model.Job;
import vn.edu.usth.jobmarket.util.ImageLoader;

/** Card list used by both the Jobs and Saved tabs. */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.Holder> {
    public interface Actions {
        void onOpen(Job job);

        void onToggleSave(Job job);
    }

    private final BookmarkStore bookmarks;
    private final Actions actions;
    private List<Job> shown = new ArrayList<>();

    public JobAdapter(BookmarkStore bookmarks, Actions actions) {
        this.bookmarks = bookmarks;
        this.actions = actions;
    }

    public void setJobs(List<Job> jobs) {
        shown = new ArrayList<>(jobs);
        notifyDataSetChanged();
    }

    /** Swipe a card left or right to toggle its bookmark (touch gesture). */
    public void attachSwipe(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || pos >= shown.size()) {
                    return;
                }
                actions.onToggleSave(shown.get(pos));
                // the store listener may already have shrunk the list (Saved tab)
                if (pos < getItemCount()) {
                    notifyItemChanged(pos); // snap the card back
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final Job job = shown.get(position);
        Context ctx = h.itemView.getContext();
        h.title.setText(job.title);
        h.company.setText(job.company);

        String where = job.location.isEmpty() ? ctx.getString(R.string.location_anywhere) : job.location;
        String meta = job.typeLabel().isEmpty() ? where : job.typeLabel() + " · " + where;
        if (!job.shortDate().isEmpty()) {
            meta += " · " + job.shortDate();
        }
        h.meta.setText(meta);

        if (job.salary.isEmpty()) {
            h.salary.setVisibility(View.GONE);
        } else {
            h.salary.setVisibility(View.VISIBLE);
            h.salary.setText(job.salary);
        }

        ImageLoader.load(job.logoUrl, h.logo, R.drawable.ic_work);
        h.save.setImageResource(bookmarks.isSaved(job)
                ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);

        h.itemView.setOnClickListener(v -> actions.onOpen(job));
        h.save.setOnClickListener(v -> {
            actions.onToggleSave(job);
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shown.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView logo;
        final TextView title, company, meta, salary;
        final ImageButton save;

        Holder(View v) {
            super(v);
            logo = v.findViewById(R.id.logo);
            title = v.findViewById(R.id.title);
            company = v.findViewById(R.id.company);
            meta = v.findViewById(R.id.meta);
            salary = v.findViewById(R.id.salary);
            save = v.findViewById(R.id.save);
        }
    }
}
