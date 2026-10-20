package com.bluestone.attendance.ui.dashboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bluestone.attendance.databinding.ItemTodayClassBinding;

import java.time.format.DateTimeFormatter;

public class TodayClassesAdapter extends ListAdapter<DashboardViewModel.TimetableSessionWithSubject, TodayClassesAdapter.ViewHolder> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TodayClassesAdapter() {
        super(new DiffUtil.ItemCallback<DashboardViewModel.TimetableSessionWithSubject>() {
            @Override
            public boolean areItemsTheSame(@NonNull DashboardViewModel.TimetableSessionWithSubject oldItem,
                                           @NonNull DashboardViewModel.TimetableSessionWithSubject newItem) {
                return oldItem.session.getId() == newItem.session.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull DashboardViewModel.TimetableSessionWithSubject oldItem,
                                              @NonNull DashboardViewModel.TimetableSessionWithSubject newItem) {
                return oldItem.session.getStartTime().equals(newItem.session.getStartTime()) &&
                        oldItem.session.getEndTime().equals(newItem.session.getEndTime());
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTodayClassBinding binding = ItemTodayClassBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardViewModel.TimetableSessionWithSubject item = getItem(position);
        holder.bind(item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTodayClassBinding binding;

        public ViewHolder(ItemTodayClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DashboardViewModel.TimetableSessionWithSubject item) {
            binding.tvClassTime.setText(item.session.getStartTime().format(TIME_FORMATTER));
            binding.tvClassDuration.setText("until " + item.session.getEndTime().format(TIME_FORMATTER));

            if (item.subject != null) {
                binding.tvClassSubjectName.setText(item.subject.getName() + " (" + item.subject.getCode() + ")");
                binding.tvClassRoom.setText(item.subject.getCode());
                String details = (item.session.getRoom() != null ? item.session.getRoom() : "Main Hall") +
                        (item.session.getFaculty() != null ? " • " + item.session.getFaculty() : "");
                binding.tvClassDetails.setText(details);
            } else {
                binding.tvClassSubjectName.setText("Scheduled Class");
                binding.tvClassRoom.setText("—");
                binding.tvClassDetails.setText(item.session.getRoom() != null ? item.session.getRoom() : "");
            }

            binding.btnRaiseIssue.setOnClickListener(v -> {
                com.bluestone.attendance.utils.AttendanceIssueDialog.show(v.getContext(), item);
            });
        }
    }
}
