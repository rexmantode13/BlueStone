package com.bluestone.attendance.ui.subjects;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bluestone.attendance.R;
import com.bluestone.attendance.databinding.ItemSubjectBinding;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;
import com.bluestone.attendance.utils.AttendanceCalculator;

public class SubjectAdapter extends ListAdapter<SubjectAttendanceSummary, SubjectAdapter.ViewHolder> {

    public interface OnSubjectClickListener {
        void onSubjectClick(SubjectAttendanceSummary item);
    }

    private final OnSubjectClickListener listener;

    public SubjectAdapter(OnSubjectClickListener listener) {
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
                        oldItem.getSubject().getName().equals(newItem.getSubject().getName()) &&
                        oldItem.getSubject().getCode().equals(newItem.getSubject().getCode()) &&
                        oldItem.getSubject().getSemester() == newItem.getSubject().getSemester();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubjectBinding binding = ItemSubjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubjectBinding binding;

        public ViewHolder(ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SubjectAttendanceSummary item, OnSubjectClickListener listener) {
            binding.tvSubjectCode.setText(item.getSubject().getCode());
            binding.tvSubjectMeta.setText(
                    "Sem " + item.getSubject().getSemester() + " • " + item.getSubject().getCredits() + " Credits"
            );
            binding.tvSubjectName.setText(item.getSubject().getName());
            binding.tvSubjectFaculty.setText(
                    item.getSubject().getFaculty() != null ? item.getSubject().getFaculty() : "Faculty not specified"
            );

            double pct = item.getPercentage();
            binding.tvSubjectPercentage.setText(AttendanceCalculator.formatPercentage(pct));

            if (pct < 0) {
                binding.tvSubjectPercentage.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.unmarked));
                binding.tvSubjectStatusBadge.setText("No Data");
                binding.tvSubjectStatusBadge.setBackgroundResource(R.drawable.bg_badge_unmarked);
                binding.tvSubjectStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.unmarked));
                binding.tvSubjectFaculty.setText("No classes marked yet");
                binding.tvSubjectFaculty.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.on_surface_variant));
            } else if (item.isBelowTarget()) {
                binding.tvSubjectPercentage.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.absent));
                binding.tvSubjectStatusBadge.setText("Attention");
                binding.tvSubjectStatusBadge.setBackgroundResource(R.drawable.bg_badge_absent);
                binding.tvSubjectStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.absent));
                int needed = item.getSessionsNeeded();
                binding.tvSubjectFaculty.setText("⚡ Attend " + needed + " class" + (needed > 1 ? "es" : "") + " to reach target");
                binding.tvSubjectFaculty.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.absent));
            } else {
                binding.tvSubjectPercentage.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                binding.tvSubjectStatusBadge.setText("Safe");
                binding.tvSubjectStatusBadge.setBackgroundResource(R.drawable.bg_badge_present);
                binding.tvSubjectStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.present));
                int canMiss = item.getClassesCanMiss();
                if (canMiss > 0) {
                    binding.tvSubjectFaculty.setText("✓ " + canMiss + " class" + (canMiss > 1 ? "es" : "") + " can be safely skipped");
                    binding.tvSubjectFaculty.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
                } else {
                    binding.tvSubjectFaculty.setText("✓ On track with target requirement");
                    binding.tvSubjectFaculty.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.on_surface_variant));
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSubjectClick(item);
            });
        }
    }
}
