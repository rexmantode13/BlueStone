package com.bluestone.attendance.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bluestone.attendance.R;
import com.bluestone.attendance.databinding.ItemDashboardSubjectBinding;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;
import com.bluestone.attendance.utils.AttendanceCalculator;

public class DashboardSubjectsAdapter extends ListAdapter<SubjectAttendanceSummary, DashboardSubjectsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SubjectAttendanceSummary item);
    }

    private final OnItemClickListener listener;

    public DashboardSubjectsAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<SubjectAttendanceSummary>() {
            @Override
            public boolean areItemsTheSame(@NonNull SubjectAttendanceSummary oldItem,
                                           @NonNull SubjectAttendanceSummary newItem) {
                return oldItem.getSubject().getId() == newItem.getSubject().getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull SubjectAttendanceSummary oldItem,
                                              @NonNull SubjectAttendanceSummary newItem) {
                return oldItem.getPercentage() == newItem.getPercentage() &&
                        oldItem.getPresentCount() == newItem.getPresentCount() &&
                        oldItem.getAbsentCount() == newItem.getAbsentCount();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDashboardSubjectBinding binding = ItemDashboardSubjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubjectAttendanceSummary item = getItem(position);
        holder.bind(item, listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDashboardSubjectBinding binding;

        public ViewHolder(ItemDashboardSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SubjectAttendanceSummary item, OnItemClickListener listener) {
            binding.tvDashSubCode.setText(item.getSubject().getCode());
            binding.tvDashSubCredits.setText(item.getSubject().getCredits() + " Credits");
            binding.tvDashSubName.setText(item.getSubject().getName());
            double pct = item.getPercentage();
            String attendanceInfo = item.getPresentCount() + " / " + item.getTotalEligible() + " classes";
            if (pct >= 0) {
                if (item.isBelowTarget()) {
                    int needed = item.getSessionsNeeded();
                    attendanceInfo += " • Need " + needed + " class" + (needed > 1 ? "es" : "");
                } else if (item.getClassesCanMiss() > 0) {
                    int canMiss = item.getClassesCanMiss();
                    attendanceInfo += " • Can skip " + canMiss + " class" + (canMiss > 1 ? "es" : "");
                }
            }
            binding.tvDashSubFraction.setText(attendanceInfo);

            if (pct < 0) {
                binding.tvDashSubPercentage.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.unmarked)
                );
            } else if (item.isBelowTarget()) {
                binding.tvDashSubPercentage.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.absent)
                );
            } else {
                binding.tvDashSubPercentage.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.primary)
                );
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}
