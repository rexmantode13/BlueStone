package com.bluestone.attendance.ui.attendance;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bluestone.attendance.R;
import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.databinding.ItemAttendanceRecordBinding;
import com.bluestone.attendance.domain.model.AttendanceStatus;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AttendanceAdapter extends ListAdapter<AttendanceRecord, AttendanceAdapter.ViewHolder> {

    public interface OnRecordActionListener {
        void onDeleteClick(AttendanceRecord record);
        void onEditClick(AttendanceRecord record);
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");
    private final OnRecordActionListener listener;
    private final Map<Long, Subject> subjectMap = new HashMap<>();

    public AttendanceAdapter(OnRecordActionListener listener) {
        super(new DiffUtil.ItemCallback<AttendanceRecord>() {
            @Override
            public boolean areItemsTheSame(@NonNull AttendanceRecord oldItem, @NonNull AttendanceRecord newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull AttendanceRecord oldItem, @NonNull AttendanceRecord newItem) {
                return oldItem.getStatus() == newItem.getStatus() &&
                        oldItem.getDate().equals(newItem.getDate()) &&
                        oldItem.getSessionNumber() == newItem.getSessionNumber() &&
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
        ItemAttendanceRecordBinding binding = ItemAttendanceRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = getItem(position);
        Subject subject = subjectMap.get(record.getSubjectId());
        holder.bind(record, subject, listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceRecordBinding binding;

        public ViewHolder(ItemAttendanceRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AttendanceRecord record, Subject subject, OnRecordActionListener listener) {
            binding.tvRecordDate.setText(record.getDate().format(DATE_FORMATTER));
            binding.tvRecordSession.setText("Session " + record.getSessionNumber());

            if (subject != null) {
                binding.tvRecordSubject.setText(subject.getName() + " (" + subject.getCode() + ")");
            } else {
                binding.tvRecordSubject.setText("Subject #" + record.getSubjectId());
            }

            String notes = (record.getNotes() != null && !record.getNotes().isEmpty())
                    ? record.getNotes() : (record.isCancelled() ? "Cancelled" : "Standard session");
            binding.tvRecordNotes.setText(notes);

            // Status styling
            if (record.getStatus() == AttendanceStatus.PRESENT) {
                binding.tvRecordStatusBadge.setText("Present");
                binding.tvRecordStatusBadge.setBackgroundResource(R.drawable.bg_badge_present);
                binding.tvRecordStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.present));
            } else if (record.getStatus() == AttendanceStatus.ABSENT) {
                binding.tvRecordStatusBadge.setText("Absent");
                binding.tvRecordStatusBadge.setBackgroundResource(R.drawable.bg_badge_absent);
                binding.tvRecordStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.absent));
            } else {
                binding.tvRecordStatusBadge.setText("Not Marked");
                binding.tvRecordStatusBadge.setBackgroundResource(R.drawable.bg_badge_unmarked);
                binding.tvRecordStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.unmarked));
            }

            binding.btnDeleteRecord.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(record);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(record);
            });
        }
    }
}
