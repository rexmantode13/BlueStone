package com.bluestone.attendance.domain.model;

public class OverallAttendanceSummary {
    private final int totalPresent;
    private final int totalAbsent;
    private final int totalEligible;
    private final double overallPercentage;
    private final int subjectsAtRiskCount;

    public OverallAttendanceSummary(int totalPresent, int totalAbsent, int totalEligible,
                                  double overallPercentage, int subjectsAtRiskCount) {
        this.totalPresent = totalPresent;
        this.totalAbsent = totalAbsent;
        this.totalEligible = totalEligible;
        this.overallPercentage = overallPercentage;
        this.subjectsAtRiskCount = subjectsAtRiskCount;
    }

    public int getTotalPresent() {
        return totalPresent;
    }

    public int getTotalAbsent() {
        return totalAbsent;
    }

    public int getTotalEligible() {
        return totalEligible;
    }

    public double getOverallPercentage() {
        return overallPercentage;
    }

    public int getSubjectsAtRiskCount() {
        return subjectsAtRiskCount;
    }
}
