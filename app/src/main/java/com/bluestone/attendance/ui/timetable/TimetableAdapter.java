package com.bluestone.attendance.ui.timetable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.TimetableSession;
import com.bluestone.attendance.databinding.ItemTimetableSessionBinding;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TimetableAdapter extends ListAdapter<TimetableSession, TimetableAdapter.ViewHolder> {

    public interface OnSessionActionListener {
        void onEdit(TimetableSession session);
        void onDelete(TimetableSession session);
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final OnSessionActionListener listener;
    private final Map<Long, Subject> subjectMap = new HashMap<>();

    public TimetableAdapter(OnSessionActionListener listener) {
        super(new DiffUtil.ItemCallback<TimetableSession>() {
            @Override
            public boolean areItemsTheSame(@NonNull TimetableSession oldItem, @NonNull TimetableSession newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull TimetableSession oldItem, @NonNull TimetableSession newItem) {
                return oldItem.getStartTime().equals(newItem.getStartTime()) &&
                        oldItem.getEndTime().equals(newItem.getEndTime()) &&
                        oldItem.getDayOfWeek() == newItem.getDayOfWeek() &&
                        oldItem.isCancelled() == newItem.isCancelled();
            }
        });
        this.listener = listener;
    }

    public void setSubjects(Map<Long, Subject> map) {
        this.subjectMap.clear();
        if (map != null) {
            this.subjectMap.putAll(map);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimetableSessionBinding binding = ItemTimetableSessionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimetableSession session = getItem(position);
        Subject subject = subjectMap.get(session.getSubjectId());
        holder.bind(session, subject, listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimetableSessionBinding binding;

        public ViewHolder(ItemTimetableSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TimetableSession session, Subject subject, OnSessionActionListener listener) {
            binding.tvTtStartTime.setText(session.getStartTime().format(TIME_FORMATTER));
            binding.tvTtEndTime.setText(session.getEndTime().format(TIME_FORMATTER));

            if (subject != null) {
                binding.tvTtSubjectCode.setText(subject.getCode());
                binding.tvTtSubjectName.setText(subject.getName());
            } else {
                binding.tvTtSubjectCode.setText("—");
                binding.tvTtSubjectName.setText("Lecture Session");
            }

            String details = (session.getRoom() != null ? session.getRoom() : "Classroom") +
                    (session.getFaculty() != null ? " • " + session.getFaculty() : "");
            binding.tvTtLocationFaculty.setText(details);

            binding.tvTtCancelledBadge.setVisibility(session.isCancelled() ? View.VISIBLE : View.GONE);

            binding.btnEditSession.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(session);
            });

            binding.btnDeleteSession.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(session);
            });
        }
    }
}
