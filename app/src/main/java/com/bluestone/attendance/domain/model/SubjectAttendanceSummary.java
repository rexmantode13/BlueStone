package com.bluestone.attendance.domain.model;

import com.bluestone.attendance.data.entities.Subject;

public class SubjectAttendanceSummary {
    private final Subject subject;
    private final int presentCount;
    private final int absentCount;
    private final int totalEligible;
    private final double percentage;
    private final int sessionsNeeded;
    private final int classesCanMiss;
    private final boolean belowTarget;

    public SubjectAttendanceSummary(Subject subject, int presentCount, int absentCount,
                                   int totalEligible, double percentage,
                                   int sessionsNeeded, int classesCanMiss, boolean belowTarget) {
        this.subject = subject;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.totalEligible = totalEligible;
        this.percentage = percentage;
        this.sessionsNeeded = sessionsNeeded;
        this.classesCanMiss = classesCanMiss;
        this.belowTarget = belowTarget;
    }

    public SubjectAttendanceSummary(Subject subject, int presentCount, int absentCount,
                                   int totalEligible, double percentage,
                                   int sessionsNeeded, boolean belowTarget) {
        this(subject, presentCount, absentCount, totalEligible, percentage, sessionsNeeded, 0, belowTarget);
    }

    public Subject getSubject() {
        return subject;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public int getTotalEligible() {
        return totalEligible;
    }

    public double getPercentage() {
        return percentage;
    }

    public int getSessionsNeeded() {
        return sessionsNeeded;
    }

    public int getClassesCanMiss() {
        return classesCanMiss;
    }

    public boolean isBelowTarget() {
        return belowTarget;
    }
}
