package com.example.newdiary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;

/**
 * 绘制文字时基准点为文字底部中心
 */
public class MeiZuMonthView extends MonthView {
    private Context context;

    /**
     * 自定义魅族标记的文本画笔
     */
    private Paint mTextPaint = new Paint();
    private Paint mSchemeXPaint = new Paint();

    /**
     * 自定义魅族标记的圆形背景
     */
    private Paint mSchemeBasicPaint = new Paint();
    private float mRadio;
    private int mPadding;
    private float mSchemeBaseLine;

    public MeiZuMonthView(Context context) {
        super(context);
        this.context = context;

        // 自己创建的画笔在这里初始化，自带的画笔在init方法中初始化
        mTextPaint.setTextSize(dipToPx(context, 8));
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setFakeBoldText(true);
        mSchemeBasicPaint.setColor(context.getColor(R.color.black));

        mSchemeXPaint.setColor(context.getColor(R.color.red));
        mSchemeXPaint.setTextSize(25);
        mSchemeXPaint.setAntiAlias(true);
        mSchemeXPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeXPaint.setFakeBoldText(true);
        mSchemeXPaint.setStrokeWidth(3);

        mRadio = dipToPx(getContext(), 7);
        mPadding = dipToPx(getContext(), 4);
        Paint.FontMetrics metrics = mSchemeBasicPaint.getFontMetrics();
        mSchemeBaseLine = mRadio - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(getContext(), 1);


    }

    @Override
    protected void initPaint() {
        super.initPaint();
        mSelectedPaint.setStyle(Paint.Style.STROKE);
        mSelectedPaint.setStrokeWidth(dipToPx(context, 2));
        mSelectedPaint.setColor(context.getColor(R.color.selected_theme_color));
        PathEffect pathEffect = new CornerPathEffect(10);
        mSelectedPaint.setPathEffect(pathEffect);
        mSelectedPaint.setAntiAlias(true);

//        mSchemeTextPaint.setTextSize(6);
//        mSchemeTextPaint.setColor(context.getColor(R.color.red));
//        mSchemeTextPaint.setTextSize(25);
    }

    /**
     * 绘制选中的日子
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return 返回true 则会继续绘制onDrawScheme，因为这里背景色不是是互斥的，所以返回true，返回false，则点击scheme标记的日子，则不继续绘制onDrawScheme，自行选择即可
     */
    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
//        mSelectedPaint.setStyle(Paint.Style.FILL);

        canvas.drawRect(x + mPadding, y + mPadding, x + mItemWidth - mPadding, y + mItemHeight - mPadding, mSelectedPaint);
//        canvas.drawCircle(x + (float) (mItemWidth / 2), y + (float) mItemHeight / 2, (float) mItemHeight / 2 - mPadding, mSelectedPaint);
        return true;
    }

    /**
     * 绘制标记的事件日子
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        // 圆和内部文字
//        canvas.drawCircle(x + mItemWidth - mPadding - mRadio / 2, y + mPadding + mRadio, mRadio, mSchemeBasicPaint);
//        canvas.drawText(calendar.getScheme(), x + mItemWidth - mPadding - mRadio, y + mPadding + mSchemeBaseLine, mTextPaint);

        // 下划线和标记
        canvas.drawLine(x + mItemWidth / 3, y + mItemHeight - mItemHeight / 7, x + (mItemWidth / 3) * 2, y + mItemHeight - mItemHeight / 7, mSchemeXPaint);
//        canvas.drawText(calendar.getScheme(), x + mItemWidth - mPadding - mRadio, y + mPadding + mSchemeBaseLine, mSchemeXPaint);
    }

    /**
     * 绘制文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        int top = y - mItemHeight / 6;

        if (isSelected) {//优先绘制选择的
            if (calendar.isCurrentDay()) {
                mSelectTextPaint.setColor(context.getColor(R.color.red));
                mSelectedLunarTextPaint.setColor(context.getColor(R.color.red));
            } else {
                mSelectTextPaint.setColor(context.getColor(R.color.calendar_text));
                mSelectedLunarTextPaint.setColor(context.getColor(R.color.calendar_text));
            }
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top, mSelectTextPaint);
            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + y + mItemHeight / 10, mSelectedLunarTextPaint);
        } else if (hasScheme) {//否则绘制具有标记的
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                    calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);

            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + y + mItemHeight / 10, mCurMonthLunarTextPaint);
        } else {//最好绘制普通文本
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                    calendar.isCurrentDay() ? mCurDayTextPaint :
                            calendar.isCurrentMonth() ? mCurMonthTextPaint : mOtherMonthTextPaint);
            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + y + mItemHeight / 10,
                    calendar.isCurrentDay() ? mCurDayLunarTextPaint :
                            calendar.isCurrentMonth() ? mCurMonthLunarTextPaint : mOtherMonthLunarTextPaint);
        }
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    private static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
