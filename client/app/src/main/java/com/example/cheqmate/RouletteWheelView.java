package com.example.cheqmate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class RouletteWheelView extends View {

    private final Paint sectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<String> participants;
    private float wheelAngle = 0f;

    private static final int[] COLORS = {
        Color.parseColor("#E53935"),
        Color.parseColor("#1E88E5"),
        Color.parseColor("#43A047"),
        Color.parseColor("#FB8C00"),
        Color.parseColor("#8E24AA"),
        Color.parseColor("#00ACC1"),
        Color.parseColor("#F4511E"),
        Color.parseColor("#6D4C41"),
    };

    public RouletteWheelView(Context context) {
        super(context);
        init();
    }

    public RouletteWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);

        centerPaint.setAntiAlias(true);

        pointerPaint.setColor(Color.parseColor("#FF6F00"));
        pointerPaint.setStyle(Paint.Style.FILL);
        pointerPaint.setAntiAlias(true);
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
        invalidate();
    }

    public float getWheelAngle() {
        return wheelAngle;
    }

    public void setWheelAngle(float angle) {
        this.wheelAngle = angle;
        invalidate();
    }

    public int getWinnerIndex(float angle) {
        if (participants == null || participants.isEmpty()) return 0;
        int count = participants.size();
        float sectorAngle = 360f / count;
        float pointerOriginal = ((-90 - angle) % 360 + 360) % 360;
        float relativeAngle = ((pointerOriginal - 270) % 360 + 360) % 360;
        return (int) (relativeAngle / sectorAngle) % count;
    }

    public float calcFinalAngle(int winnerIndex, int extraRotations) {
        if (participants == null || participants.isEmpty()) return 0;
        int count = participants.size();
        float sectorAngle = 360f / count;
        float targetRelativeAngle = (winnerIndex + 0.5f) * sectorAngle;
        float targetPointerOriginal = (270 + targetRelativeAngle) % 360;
        float baseAngle = ((-90 - targetPointerOriginal) % 360 + 360) % 360;
        return baseAngle + 360 * extraRotations;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (participants == null || participants.isEmpty()) return;

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - 24f;

        RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
        int count = participants.size();
        float sectorAngle = 360f / count;

        float textSize = Math.max(22f, Math.min(44f, radius * sectorAngle / 300f));
        textPaint.setTextSize(textSize);

        canvas.save();
        canvas.rotate(wheelAngle, cx, cy);

        float startAngle = -90f;
        for (int i = 0; i < count; i++) {
            sectorPaint.setColor(COLORS[i % COLORS.length]);
            sectorPaint.setStyle(Paint.Style.FILL);
            canvas.drawArc(oval, startAngle, sectorAngle, true, sectorPaint);
            canvas.drawArc(oval, startAngle, sectorAngle, true, borderPaint);

            canvas.save();
            float midAngle = startAngle + sectorAngle / 2f;
            canvas.rotate(midAngle + 90f, cx, cy);
            float textR = radius * 0.62f;
            String name = participants.get(i);
            if (name.length() > 7) name = name.substring(0, 6) + "…";
            canvas.drawText(name, cx, cy - textR, textPaint);
            canvas.restore();

            startAngle += sectorAngle;
        }
        canvas.restore();

        centerPaint.setColor(Color.WHITE);
        centerPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, 18f, centerPaint);
        centerPaint.setColor(Color.DKGRAY);
        centerPaint.setStyle(Paint.Style.STROKE);
        centerPaint.setStrokeWidth(2f);
        canvas.drawCircle(cx, cy, 18f, centerPaint);

        float tipY = cy - radius - 4f;
        Path path = new Path();
        path.moveTo(cx, tipY + 44f);
        path.lineTo(cx - 22f, tipY);
        path.lineTo(cx + 22f, tipY);
        path.close();
        canvas.drawPath(path, pointerPaint);
    }
}
