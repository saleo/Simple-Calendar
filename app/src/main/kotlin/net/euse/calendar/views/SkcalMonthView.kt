package net.euse.calendar.views

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView


class SkcalMonthView(context: Context) : MonthView(context) {

    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mRadius: Int = 0

    /**
     * 自定义魅族标记的圆形背景
     *
     */
    private val mSchemeBasicPaint = Paint()


    init {
        mRectPaint.style = Paint.Style.STROKE
        mRectPaint.strokeWidth = dipToPx(context, 0.5f).toFloat()
        mRectPaint.color = -0x77101011

        mSchemeBasicPaint.isAntiAlias = true
        mSchemeBasicPaint.style = Paint.Style.FILL
        mSchemeBasicPaint.textAlign = Paint.Align.CENTER
        mSchemeBasicPaint.isFakeBoldText = true

        //兼容硬件加速无效的代码
        setLayerType(View.LAYER_TYPE_SOFTWARE, mSchemeBasicPaint)
        //4.0以上硬件加速会导致无效
        mSelectedPaint.maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.SOLID)
    }

    /**
     * 绘制选中的日子
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        canvas.drawRect(x.toFloat(), y.toFloat(), (x + mItemWidth).toFloat(), (y + mItemHeight).toFloat(), mSelectedPaint)
        return true
    }

    /**
     * 绘制标记的事件日子
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        mSchemeBasicPaint.color = calendar.schemeColor
        val schemes = calendar.schemes
        if (schemes == null || schemes.size == 0) {
            return
        }
        var colors=ArrayList<Int>(schemes.size)
        for (scheme in schemes) {
            colors.add(scheme.shcemeColor)
        }

        if (colors.contains(Color.RED)) mSchemePaint.color=Color.RED
        else if (colors.contains(Color.BLUE)) mSchemePaint.color=Color.BLUE
        else mSchemePaint.color=Color.GRAY

        val mPadding = dipToPx(context, 4f)
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight -2* mPadding

        mRadius = Math.min(mItemWidth, mItemHeight) / 16;

        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSchemePaint)
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
    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        canvas.drawRect(x.toFloat(), y.toFloat(), (x + mItemWidth).toFloat(), (y + mItemHeight).toFloat(), mRectPaint)
        val cx = x + mItemWidth / 2
        val top = y - mItemHeight / 6

        val isInRange = isInRange(calendar)

        if (isSelected) {
            canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                    mSelectTextPaint)
            canvas.drawText(calendar.lunar, cx.toFloat(), mTextBaseLine + y.toFloat() + (mItemHeight / 10).toFloat(), mSelectedLunarTextPaint)
        } else if (hasScheme) {
            canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                    if (calendar.isCurrentMonth && isInRange) mSchemeTextPaint else mOtherMonthTextPaint)

            canvas.drawText(calendar.lunar, cx.toFloat(), mTextBaseLine + y.toFloat() + (mItemHeight / 10).toFloat(),
                    if (calendar.isCurrentDay && isInRange)
                        mCurDayLunarTextPaint
                    else if (calendar.isCurrentMonth) mCurMonthLunarTextPaint else mOtherMonthLunarTextPaint)
        } else {
            canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                    if (calendar.isCurrentDay)
                        mCurDayTextPaint
                    else if (calendar.isCurrentMonth && isInRange) mCurMonthTextPaint else mOtherMonthTextPaint)
            canvas.drawText(calendar.lunar, cx.toFloat(), mTextBaseLine + y.toFloat() + (mItemHeight / 10).toFloat(),
                    if (calendar.isCurrentDay && isInRange)
                        mCurDayLunarTextPaint
                    else if (calendar.isCurrentMonth) mCurMonthLunarTextPaint else mOtherMonthLunarTextPaint)
        }
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    private fun dipToPx(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}