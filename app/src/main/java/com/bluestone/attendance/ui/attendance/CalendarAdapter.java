package com.bluestone.attendance.ui.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bluestone.attendance.data.entities.AttendanceRecord;
import com.bluestone.attendance.databinding.ItemCalendarDayBinding;
import com.bluestone.attendance.domain.model.AttendanceStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(LocalDate date);
    }

    public static class DayItem {
        public final LocalDate date;
        public final boolean isCurrentMonth;

        public DayItem(LocalDate date, boolean isCurrentMonth) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
        }
    }

    private final List<DayItem> days = new ArrayList<>();
    private final Map<LocalDate, List<AttendanceRecord>> recordMap = new HashMap<>();
    private final OnDayClickListener listener;

    public CalendarAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setMonth(YearMonth yearMonth, List<AttendanceRecord> records) {
        days.clear();
        recordMap.clear();

        if (records != null) {
            for (AttendanceRecord r : records) {
                if (!recordMap.containsKey(r.getDate())) {
                    recordMap.put(r.getDate(), new ArrayList<>());
                }
                recordMap.get(r.getDate()).add(r);
            }
        }

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeekOffset = firstOfMonth.getDayOfWeek().getValue() - 1; // Mon = 0 ... Sun = 6

        // Padding before start of month
        for (int i = dayOfWeekOffset; i > 0; i--) {
            days.add(new DayItem(firstOfMonth.minusDays(i), false));
        }

        // Days in month
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(new DayItem(yearMonth.atDay(i), true));
        }

        // Trailing padding to make full weeks
        int remaining = (7 - (days.size() % 7)) % 7;
        LocalDate lastOfMonth = yearMonth.atDay(daysInMonth);
        for (int i = 1; i <= remaining; i++) {
            days.add(new DayItem(lastOfMonth.plusDays(i), false));
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DayItem item = days.get(position);
        List<AttendanceRecord> dayRecords = recordMap.get(item.date);
        holder.bind(item, dayRecords, listener);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarDayBinding binding;

        public ViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DayItem item, List<AttendanceRecord> records, OnDayClickListener listener) {
            binding.tvCalendarDay.setText(String.valueOf(item.date.getDayOfMonth()));
            binding.tvCalendarDay.setAlpha(item.isCurrentMonth ? 1.0f : 0.35f);

            boolean hasPresent = false;
            boolean hasAbsent = false;

            if (records != null && !records.isEmpty()) {
                for (AttendanceRecord r : records) {
                    if (r.getStatus() == AttendanceStatus.PRESENT) hasPresent = true;
                    if (r.getStatus() == AttendanceStatus.ABSENT) hasAbsent = true;
                }
            }

            binding.viewDot1.setVisibility(hasPresent ? View.VISIBLE : View.GONE);
            binding.viewDot2.setVisibility(hasAbsent ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onDayClick(item.date);
            });
        }
    }
}
