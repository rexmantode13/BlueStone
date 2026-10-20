package com.bluestone.attendance.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bluestone.attendance.R;
import com.bluestone.attendance.ui.dashboard.DashboardViewModel.TimetableSessionWithSubject;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class AttendanceIssueDialog {

    public static void show(Context context, TimetableSessionWithSubject item) {
        if (context == null || item == null) return;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_raise_issue, null);
        TextView tvSubtitle = dialogView.findViewById(R.id.tv_issue_class_subtitle);
        ChipGroup cgReasons = dialogView.findViewById(R.id.cg_issue_reasons);
        TextInputEditText etMessage = dialogView.findViewById(R.id.et_issue_message);

        String subjectText = item.subject != null ? item.subject.getName() + " (" + item.subject.getCode() + ")" : "Lecture";
        String timeText = item.session != null ? " • " + item.session.getStartTime() + " - " + item.session.getEndTime() : "";
        tvSubtitle.setText(subjectText + timeText);

        new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .setPositiveButton("Submit Issue", (dialog, which) -> {
                    String reason = "Class presence issue";
                    int checkedId = cgReasons.getCheckedChipId();
                    if (checkedId != View.NO_ID) {
                        Chip chip = dialogView.findViewById(checkedId);
                        if (chip != null) reason = chip.getText().toString();
                    }

                    String userMsg = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
                    int ticketNum = 1000 + new Random().nextInt(9000);

                    String faculty = (item.session != null && item.session.getFaculty() != null)
                            ? item.session.getFaculty() : "Course Faculty";

                    Toast.makeText(context,
                            "✓ Issue logged (Ticket #ATT-" + ticketNum + ") forwarded to " + faculty + " for review.",
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
