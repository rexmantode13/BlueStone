package com.bluestone.attendance.ui.syllabus;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bluestone.attendance.R;
import com.bluestone.attendance.data.entities.SyllabusUnit;
import com.bluestone.attendance.databinding.ItemSyllabusUnitBinding;

public class SyllabusAdapter extends ListAdapter<SyllabusUnit, SyllabusAdapter.ViewHolder> {

    public interface OnUnitActionListener {
        void onToggleCompleted(SyllabusUnit unit, boolean isCompleted);
        void onEditUnit(SyllabusUnit unit);
        void onDeleteUnit(SyllabusUnit unit);
    }

    private final OnUnitActionListener listener;

    public SyllabusAdapter(OnUnitActionListener listener) {
        super(new DiffUtil.ItemCallback<SyllabusUnit>() {
            @Override
            public boolean areItemsTheSame(@NonNull SyllabusUnit oldItem, @NonNull SyllabusUnit newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull SyllabusUnit oldItem, @NonNull SyllabusUnit newItem) {
                return oldItem.isCompleted() == newItem.isCompleted() &&
                        oldItem.getTitle().equals(newItem.getTitle()) &&
                        oldItem.getUnitNumber() == newItem.getUnitNumber();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSyllabusUnitBinding binding = ItemSyllabusUnitBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSyllabusUnitBinding binding;

        public ViewHolder(ItemSyllabusUnitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SyllabusUnit unit, OnUnitActionListener listener) {
            binding.tvUnitNumber.setText("Unit " + unit.getUnitNumber());
            binding.tvUnitTitle.setText(unit.getTitle());

            String topics = (unit.getTopics() != null && !unit.getTopics().isEmpty())
                    ? "Topics: " + unit.getTopics() : "No topics listed";
            binding.tvUnitTopics.setText(topics);

            String objectives = (unit.getObjectives() != null && !unit.getObjectives().isEmpty())
                    ? "Objectives: " + unit.getObjectives() : "";
            binding.tvUnitObjectives.setText(objectives);

            // Completed state
            binding.cbUnitCompleted.setOnCheckedChangeListener(null);
            binding.cbUnitCompleted.setChecked(unit.isCompleted());
            binding.cbUnitCompleted.setOnCheckedChangeListener((btn, isChecked) -> {
                if (listener != null) listener.onToggleCompleted(unit, isChecked);
            });

            if (unit.isCompleted()) {
                binding.tvUnitStatus.setText("Completed");
                binding.tvUnitStatus.setBackgroundResource(R.drawable.bg_badge_present);
                binding.tvUnitStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.present));
            } else {
                binding.tvUnitStatus.setText("In Progress");
                binding.tvUnitStatus.setBackgroundResource(R.drawable.bg_badge_unmarked);
                binding.tvUnitStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.secondary));
            }

            binding.btnEditUnit.setOnClickListener(v -> {
                if (listener != null) listener.onEditUnit(unit);
            });

            binding.btnDeleteUnit.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteUnit(unit);
            });
        }
    }
}
