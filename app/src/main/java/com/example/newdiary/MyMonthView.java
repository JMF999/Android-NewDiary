package com.example.newdiary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;

public class MyMonthView extends MonthView {
    private Paint paint,textPaint;

    public MyMonthView(Context context) {
        super(context);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5f);
        paint.setColor(context.getColor(R.color.black));
        paint.setTextSize(30);

        textPaint = new Paint();
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(50f);
        textPaint.setColor(context.getColor(R.color.black));
    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        canvas.drawLine(x,y,x+10f,y+10f,paint);
        return false;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {

    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        canvas.drawText("J",x+(mItemWidth/2),y+(mItemHeight/2),textPaint);
    }
}
