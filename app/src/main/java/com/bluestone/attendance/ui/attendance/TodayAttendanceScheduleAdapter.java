package com.bluestone.attendance.ui.attendance;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bluestone.attendance.databinding.ItemTodayClassBinding;
import com.bluestone.attendance.ui.dashboard.DashboardViewModel.TimetableSessionWithSubject;

import java.time.format.DateTimeFormatter;

public class TodayAttendanceScheduleAdapter extends ListAdapter<TimetableSessionWithSubject, TodayAttendanceScheduleAdapter.ViewHolder> {

    public interface OnSessionClickListener {
        void onSessionClick(TimetableSessionWithSubject item);
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final OnSessionClickListener listener;
    private long activeSessionId = -1L;

    public TodayAttendanceScheduleAdapter(OnSessionClickListener listener) {
        super(new DiffUtil.ItemCallback<TimetableSessionWithSubject>() {
            @Override
            public boolean areItemsTheSame(@NonNull TimetableSessionWithSubject oldItem,
                                           @NonNull TimetableSessionWithSubject newItem) {
                return oldItem.session.getId() == newItem.session.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull TimetableSessionWithSubject oldItem,
                                              @NonNull TimetableSessionWithSubject newItem) {
                return oldItem.session.getStartTime().equals(newItem.session.getStartTime()) &&
                        oldItem.session.getEndTime().equals(newItem.session.getEndTime());
            }
        });
        this.listener = listener;
    }

    public void setActiveSessionId(long id) {
        this.activeSessionId = id;
        notifyDataSetChanged();
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
        TimetableSessionWithSubject item = getItem(position);
        holder.bind(item, listener, item.session.getId() == activeSessionId);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTodayClassBinding binding;

        public ViewHolder(ItemTodayClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TimetableSessionWithSubject item, OnSessionClickListener listener, boolean isActive) {
            binding.tvClassTime.setText(item.session.getStartTime().format(TIME_FORMATTER));
            binding.tvClassDuration.setText("until " + item.session.getEndTime().format(TIME_FORMATTER));

            if (item.subject != null) {
                binding.tvClassSubjectName.setText(item.subject.getName() + " (" + item.subject.getCode() + ")");
                binding.tvClassRoom.setText(isActive ? "Active" : item.subject.getCode());
                binding.tvClassRoom.setBackgroundResource(isActive ? com.bluestone.attendance.R.drawable.bg_badge_present : com.bluestone.attendance.R.drawable.bg_badge_unmarked);
                String details = (item.session.getRoom() != null ? item.session.getRoom() : "Main Hall") +
                        (item.session.getFaculty() != null ? " • " + item.session.getFaculty() : "");
                binding.tvClassDetails.setText(details);
            } else {
                binding.tvClassSubjectName.setText("Scheduled Class");
                binding.tvClassRoom.setText("—");
                binding.tvClassDetails.setText(item.session.getRoom() != null ? item.session.getRoom() : "");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSessionClick(item);
            });

            binding.btnRaiseIssue.setOnClickListener(v -> {
                com.bluestone.attendance.utils.AttendanceIssueDialog.show(v.getContext(), item);
            });
        }
    }
}
