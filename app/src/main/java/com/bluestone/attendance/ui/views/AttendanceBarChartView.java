package com.bluestone.attendance.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bluestone.attendance.R;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Modern, high-performance canvas bar chart designed specifically for students.
 * Features capsule pill bars with LinearGradients, background track capsules,
 * dashed goal threshold line with pill badge, fraction attendance counters, and zero-allocation rendering.
 */
public class AttendanceBarChartView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textFractionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thresholdLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thresholdBadgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thresholdTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint chipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePillBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF trackRect = new RectF();
    private final RectF barRect = new RectF();
    private final RectF badgeRect = new RectF();
    private final RectF chipRect = new RectF();
    private final RectF valuePillRect = new RectF();
    private final Path thresholdPath = new Path();

    private final List<SubjectAttendanceSummary> data = new ArrayList<>();
    private double threshold = 75.0;

    // Gradient cache — avoids per-frame allocation in onDraw
    private LinearGradient[] cachedGradients;
    private boolean gradientsDirty = true;

    // 2-Color Palette based colors
    private int colorPrimary;
    private int colorPrimaryLight;
    private int colorPrimaryDark;
    private int colorTrack;
    private int colorOutline;
    private int colorTextDark;
    private int colorTextMuted;
    private int colorRiskStart;
    private int colorRiskEnd;
    private int colorThreshold;
    private int colorThresholdBg;

    public AttendanceBarChartView(Context context) {
        super(context);
        init();
    }

    public AttendanceBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AttendanceBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        colorPrimary = ContextCompat.getColor(getContext(), R.color.primary);
        colorPrimaryLight = ContextCompat.getColor(getContext(), R.color.primary_light);
        colorPrimaryDark = ContextCompat.getColor(getContext(), R.color.primary_dark);
        colorTrack = ContextCompat.getColor(getContext(), R.color.surface_variant);
        colorOutline = ContextCompat.getColor(getContext(), R.color.outline);
        colorTextDark = ContextCompat.getColor(getContext(), R.color.on_background);
        colorTextMuted = ContextCompat.getColor(getContext(), R.color.on_surface_variant);
        colorRiskStart = 0xFFF43F5E;
        colorRiskEnd = 0xFF9333EA;
        colorThreshold = 0xFF7C3AED;
        colorThresholdBg = ContextCompat.getColor(getContext(), R.color.primary_container);

        // Track paint
        trackPaint.setStyle(Paint.Style.FILL);
        trackPaint.setColor(colorTrack);

        trackStrokePaint.setStyle(Paint.Style.STROKE);
        trackStrokePaint.setStrokeWidth(1.5f);
        trackStrokePaint.setColor(colorOutline);

        // Grid lines
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setColor(colorOutline);
        gridLinePaint.setStrokeWidth(1.2f);

        // Threshold dashed line
        thresholdLinePaint.setStyle(Paint.Style.STROKE);
        thresholdLinePaint.setStrokeWidth(2.5f);
        thresholdLinePaint.setColor(colorThreshold);
        thresholdLinePaint.setPathEffect(new DashPathEffect(new float[]{12f, 10f}, 0));

        // Threshold badge
        thresholdBadgePaint.setStyle(Paint.Style.FILL);
        thresholdBadgePaint.setColor(colorThresholdBg);

        thresholdTextPaint.setColor(colorThreshold);
        thresholdTextPaint.setTextSize(getResources().getDimension(R.dimen.text_label_medium));
        thresholdTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        thresholdTextPaint.setTextAlign(Paint.Align.CENTER);

        // Text value above bar
        textValuePaint.setTextSize(30f);
        textValuePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textValuePaint.setTextAlign(Paint.Align.CENTER);

        // Text label below bar (Subject code)
        textLabelPaint.setTextSize(30f);
        textLabelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textLabelPaint.setColor(colorTextDark);
        textLabelPaint.setTextAlign(Paint.Align.CENTER);

        // Text fraction (e.g. 18/20 attended)
        textFractionPaint.setTextSize(24f);
        textFractionPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        textFractionPaint.setColor(colorTextMuted);
        textFractionPaint.setTextAlign(Paint.Align.CENTER);

        // Bottom chip bg
        chipBgPaint.setStyle(Paint.Style.FILL);
        chipBgPaint.setColor(colorTrack);

        valuePillBgPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(List<SubjectAttendanceSummary> summaries, double threshold) {
        this.data.clear();
        if (summaries != null) {
            this.data.addAll(summaries);
        }
        this.threshold = threshold;
        gradientsDirty = true;
        cachedGradients = null;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        float density = getResources().getDisplayMetrics().density;
        float barWidth = 44f * density;
        float spacing = 28f * density;
        int count = Math.max(1, data.size());
        int calculatedWidth = (int) (count * (barWidth + spacing) + spacing + getPaddingLeft() + getPaddingRight() + 30f);

        int minScreenWidth = getResources().getDisplayMetrics().widthPixels - (int) (40f * density);
        int finalWidth = Math.max(calculatedWidth, minScreenWidth);

        if (widthMode == MeasureSpec.EXACTLY) {
            finalWidth = Math.max(widthSize, finalWidth);
        }

        int height = (int) (230f * density);
        setMeasuredDimension(finalWidth, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty()) {
            textValuePaint.setColor(colorTextMuted);
            textValuePaint.setTextSize(36f);
            canvas.drawText("No subject attendance data available", getWidth() / 2f, getHeight() / 2f, textValuePaint);
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        float paddingLeft = getPaddingLeft() + 20f * density;
        float paddingRight = getPaddingRight() + 20f * density;
        float paddingTop = 44f * density;
        float paddingBottom = 60f * density;

        float chartHeight = getHeight() - paddingTop - paddingBottom;
        float availableWidth = getWidth() - paddingLeft - paddingRight;

        int count = data.size();
        float barWidth = 44f * density;
        float spacing = (count > 1) ? (availableWidth - (count * barWidth)) / (count + 1) : availableWidth / 2f;
        if (spacing < 16f * density) spacing = 16f * density;

        float baselineY = paddingTop + chartHeight;

        // Subtle background grid lines at 25%, 50%, 75%, 100%
        float[] gridLevels = {0.25f, 0.50f, 0.75f, 1.0f};
        for (float level : gridLevels) {
            float y = baselineY - (chartHeight * level);
            canvas.drawLine(paddingLeft, y, getWidth() - paddingRight, y, gridLinePaint);
        }

        // Draw Target Threshold dashed line
        float thresholdY = baselineY - (float) (chartHeight * (threshold / 100.0));
        thresholdPath.reset();
        thresholdPath.moveTo(paddingLeft, thresholdY);
        thresholdPath.lineTo(getWidth() - paddingRight, thresholdY);
        canvas.drawPath(thresholdPath, thresholdLinePaint);

        // Draw Target Badge pill on right side of threshold line
        String badgeText = String.format(Locale.getDefault(), "Goal %.0f%%", threshold);
        float textWidth = thresholdTextPaint.measureText(badgeText);
        float badgeWidth = textWidth + 24f * density;
        float badgeHeight = 22f * density;
        float badgeX = getWidth() - paddingRight - badgeWidth;
        badgeRect.set(badgeX, thresholdY - badgeHeight / 2f, badgeX + badgeWidth, thresholdY + badgeHeight / 2f);
        canvas.drawRoundRect(badgeRect, badgeHeight / 2f, badgeHeight / 2f, thresholdBadgePaint);
        canvas.drawText(badgeText, badgeRect.centerX(), badgeRect.centerY() + 5f * density, thresholdTextPaint);

        // Draw Each Bar
        float currentX = paddingLeft + spacing;
        float pillRadius = barWidth / 2f;

        for (SubjectAttendanceSummary item : data) {
            double pct = item.getPercentage();

            // 1. Full height capsule track (representing 100% capacity)
            trackRect.set(currentX, paddingTop, currentX + barWidth, baselineY);
            canvas.drawRoundRect(trackRect, pillRadius, pillRadius, trackPaint);
            canvas.drawRoundRect(trackRect, pillRadius, pillRadius, trackStrokePaint);

            // 2. Bar Fill Capsule
            if (pct > 0) {
                float displayPct = (float) Math.min(100.0, pct);
                float barHeight = chartHeight * (displayPct / 100.0f);
                float topY = baselineY - barHeight;

                barRect.set(currentX, topY, currentX + barWidth, baselineY);

                // Use cached gradients to avoid per-frame allocation
                if (cachedGradients == null || gradientsDirty) {
                    cachedGradients = new LinearGradient[count];
                    gradientsDirty = false;
                }
                int idx = data.indexOf(item);
                if (idx >= 0 && idx < cachedGradients.length && cachedGradients[idx] == null) {
                    if (pct < threshold) {
                        cachedGradients[idx] = new LinearGradient(
                                currentX, topY, currentX, baselineY,
                                colorRiskStart, colorRiskEnd,
                                Shader.TileMode.CLAMP
                        );
                    } else {
                        cachedGradients[idx] = new LinearGradient(
                                currentX, topY, currentX, baselineY,
                                colorPrimaryLight, colorPrimaryDark,
                                Shader.TileMode.CLAMP
                        );
                    }
                }
                barPaint.setShader(idx >= 0 && idx < cachedGradients.length ? cachedGradients[idx] : null);
                canvas.drawRoundRect(barRect, pillRadius, pillRadius, barPaint);
                barPaint.setShader(null);
            }

            // 3. Percentage Badge Pill above bar
            String valueText = (pct < 0) ? "—" : String.format(Locale.getDefault(), "%.0f%%", pct);
            float valueTextW = textValuePaint.measureText(valueText);
            float valPillW = Math.max(barWidth, valueTextW + 12f * density);
            float valPillH = 20f * density;
            float valPillX = currentX + (barWidth - valPillW) / 2f;
            float valPillY = paddingTop - valPillH - 4f * density;
            valuePillRect.set(valPillX, valPillY, valPillX + valPillW, valPillY + valPillH);

            if (pct < threshold && pct >= 0) {
                valuePillBgPaint.setColor(0xFFFFE4E6);
                textValuePaint.setColor(colorRiskStart);
            } else {
                valuePillBgPaint.setColor(colorThresholdBg);
                textValuePaint.setColor(colorPrimary);
            }
            canvas.drawRoundRect(valuePillRect, valPillH / 2f, valPillH / 2f, valuePillBgPaint);
            canvas.drawText(valueText, valuePillRect.centerX(), valuePillRect.centerY() + 4f * density, textValuePaint);

            // 4. Bottom Subject Code Chip
            String code = item.getSubject().getCode();
            float codeTextWidth = textLabelPaint.measureText(code);
            float chipWidth = Math.max(barWidth + 8f * density, codeTextWidth + 14f * density);
            float chipHeight = 22f * density;
            float chipLeft = currentX + (barWidth - chipWidth) / 2f;
            float chipTop = baselineY + 8f * density;

            chipRect.set(chipLeft, chipTop, chipLeft + chipWidth, chipTop + chipHeight);
            canvas.drawRoundRect(chipRect, chipHeight / 2f, chipHeight / 2f, chipBgPaint);
            canvas.drawText(code, chipRect.centerX(), chipRect.centerY() + 4.5f * density, textLabelPaint);

            // 5. Fraction Text (e.g. 18/20) under chip
            String fractionText = item.getPresentCount() + "/" + item.getTotalEligible();
            canvas.drawText(fractionText, currentX + barWidth / 2f, chipTop + chipHeight + 14f * density, textFractionPaint);

            currentX += barWidth + spacing;
        }
    }
}
