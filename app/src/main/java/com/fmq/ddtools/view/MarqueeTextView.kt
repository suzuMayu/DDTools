package com.fmq.ddtools.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import com.fmq.ddtools.R
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.ceil

/**
 * 文本横向滚动，跑马灯效果
 * - 设置的内容不要太长，建议不要超过 [String] 最大长度的 1/5
 */
open class MarqueeTextView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "MarqueeTextView"
        const val BLANK = " "
        const val REPEAT_SINGLE = 1 //一次结束
        const val REPEAT_SINGLE_LOOP = 0 //单个循序
        const val REPEAT_FILL_LOOP = -1 // 填充后循环
    }

    /**
     * 滚动速度
     */
    var speed = 1f
        set(value) {
            if (value <= 0) {
                field = 0f
                stop()
            } else {
                field = value
            }
        }

    /**
     * 文本内容
     */
    var text = ""
        set(value) {
            if (field.isEmpty() && value.isEmpty()) {
                return
            }
            field = value
            var targetContent = value
            if (isResetLocation) { //控制重新设置文本内容的时候，是否初始化xLocation。
                xLocation = width * startLocationDistance
            }
            val itemEndBlank = getItemEndBlank()
            if (!targetContent.endsWith(itemEndBlank)) {
                targetContent += itemEndBlank //避免没有后缀, 补充空格位
            }
            //计算宽度，根据模式来
            if (repeat == REPEAT_FILL_LOOP) {
                mFinalDrawText = ""
                //计算文本的宽度
                mSingleContentWidth = getTextWidth(targetContent)
                if (mSingleContentWidth > 0) {
                    // 最大可见内容项数
                    val maxVisibleCount = ceil(width / mSingleContentWidth.toDouble()).toInt() + 1
                    repeat(maxVisibleCount) {
                        mFinalDrawText += targetContent
                    }
                }
                contentWidth = getTextWidth(mFinalDrawText)
            } else {
                if (xLocation < 0 && repeat == REPEAT_SINGLE) {
                    if (abs(xLocation) > contentWidth) {
                        xLocation = width * startLocationDistance
                    }
                }
                mFinalDrawText = targetContent
                contentWidth = getTextWidth(mFinalDrawText)
                mSingleContentWidth = contentWidth
            }
            textHeight = getTextHeight()
            invalidate()
        }

    /**
     * 文本第一个填充颜色
     */
    @ColorInt
    var gradientFirst = 0
        set(value) {
            if (value != field) {
                field = value
            }
        }

    /**
     * 文本第二个填充颜色
     */
    @ColorInt
    var gradientSecond = 0
        set(value) {
            if (value != field) {
                field = value
            }
        }

    /**
     * 文本第三个填充颜色
     */
    @ColorInt
    var gradientThird = 0
        set(value) {
            if (value != field) {
                field = value
            }
        }

    /**
     * 文本第四个填充颜色
     */
    @ColorInt
    var gradientFourth = 0
        set(value) {
            if (value != field) {
                field = value
            }
        }

    /**
     * 最终绘制显示的文本
     */
    private var mFinalDrawText: String = ""

    /**
     * 文字颜色
     */
    @ColorInt
    var textColor = Color.BLACK
        set(value) {
            if (value != field) {
                field = value
                textPaint.color = textColor
                invalidate()
            }
        }

    /**
     * 字体大小
     */
    @Px
    var textSize = 12f
        set(value) {
            if (value > 0 && value != field) {
                field = value
                textPaint.textSize = value
                if (text.isNotEmpty()) {
                    text = text
                }
            }
        }

    /**
     * item间距
     */
    @Px
    var textItemDistance = 50f
        set(value) {
            if (field == value) {
                return
            }
            field = if (value < 0f) 0f else value
            if (text.isNotEmpty()) {
                text = text
            }
        }

    /**
     * 滚动模式
     */
    private var repeat = REPEAT_SINGLE_LOOP
        set(value) {
            if (value != field) {
                field = value
                resetInit = true
                text = text
            }
        }

    /**
     * 开始的位置距离左边，0~1，0:最左边，1:最右边，0.5:中间。
     */
    @FloatRange(from = 0.0, to = 1.0)
    var startLocationDistance = 0.0f
        set(value) {
            if (value == field) {
                return
            }
            field = when {
                value < 0f -> 0f
                value > 1f -> 1f
                else -> value
            }
        }

    /**
     * 渲染模式
     */
    var tileMode = 0
        set(value) {
            if (value != field) {
                field = value
            }
        }

    /**
     * 渐变色样式
     */
    var gradientStyle = 0
        set(value) {
            if (value != field) {
                field = value
            }
        }

    /**
     * 是否重置文本绘制的位置，默认为true
     */
    private var isResetLocation = true
    private var xLocation = 0f //文本的x坐标

    /**
     * 单个显示内容的宽度
     */
    private var mSingleContentWidth: Float = 0f

    /**内容的宽度*/
    private var contentWidth = 0f

    /**是否继续滚动*/
    var isRolling = false
        private set

    /**画笔*/
    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var textHeight = 0f
    private var resetInit = true

    /**渐变色数组*/
    private lateinit var textGradientColors: IntArray
    private val mHandler by lazy { MyHandler(this) }

    /**
     * 是否用户主动调用，默认 true
     */
    var isRollByUser = true

    init {
        initAttrs(attrs)
        initPaint()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (resetInit && text.isNotEmpty()) {
            textItemDistance = textItemDistance
            xLocation = width * startLocationDistance
            resetInit = false
        }
        val absLocation = abs(xLocation)
        when (repeat) {
            REPEAT_SINGLE -> if (contentWidth < absLocation) {
                stop()
            }

            REPEAT_SINGLE_LOOP -> if (contentWidth <= absLocation) {
                //一轮结束
                xLocation = width.toFloat()
            }

            REPEAT_FILL_LOOP -> if (xLocation < 0 && mSingleContentWidth <= absLocation) {
                xLocation = mSingleContentWidth - absLocation
            }

            else ->
                if (contentWidth < absLocation) {
                    //也就是说文字已经到头了
                    stop()
                }
        }
        //绘制文本
        if (mFinalDrawText.isNotBlank()) {
            canvas.drawText(mFinalDrawText, xLocation, height / 2 + textHeight / 2, textPaint)
        }
        if (textGradientColors.size > 1) {
            val mRectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
            when (gradientStyle) {
                0 -> {
                    textPaint.shader = LinearGradient(
                        0f, mRectF.bottom, mRectF.right, 0f,
                        textGradientColors,
                        null,
                        when (tileMode) {
                            0 -> Shader.TileMode.CLAMP
                            1 -> Shader.TileMode.REPEAT
                            2 -> Shader.TileMode.MIRROR
                            else -> {
                                Shader.TileMode.CLAMP
                            }
                        }
                    )
                }
                1 -> {
                    textPaint.shader = LinearGradient(
                        0f, 0f, mRectF.right, mRectF.bottom,
                        textGradientColors,
                        null,
                        when (tileMode) {
                            0 -> Shader.TileMode.CLAMP
                            1 -> Shader.TileMode.REPEAT
                            2 -> Shader.TileMode.MIRROR
                            else -> {
                                Shader.TileMode.CLAMP
                            }
                        }
                    )
                }
                2 -> {
                    textPaint.shader = LinearGradient(
                        mRectF.right, 0f, 0f, mRectF.bottom,
                        textGradientColors,
                        null,
                        when (tileMode) {
                            0 -> Shader.TileMode.CLAMP
                            1 -> Shader.TileMode.REPEAT
                            2 -> Shader.TileMode.MIRROR
                            else -> {
                                Shader.TileMode.CLAMP
                            }
                        }
                    )
                }
                3 -> {
                    textPaint.shader = LinearGradient(
                        mRectF.right, mRectF.bottom, 0f, 0f,
                        textGradientColors,
                        null,
                        when (tileMode) {
                            0 -> Shader.TileMode.CLAMP
                            1 -> Shader.TileMode.REPEAT
                            2 -> Shader.TileMode.MIRROR
                            else -> {
                                Shader.TileMode.CLAMP
                            }
                        }
                    )
                }
                4 -> {
                    textPaint.shader = LinearGradient(
                        0f, 0f, mRectF.right, 0f,
                        textGradientColors,
                        null,
                        when (tileMode) {
                            0 -> Shader.TileMode.CLAMP
                            1 -> Shader.TileMode.REPEAT
                            2 -> Shader.TileMode.MIRROR
                            else -> {
                                Shader.TileMode.CLAMP
                            }
                        }
                    )
                }
                5 -> {
                    textPaint.shader = LinearGradient(
                        mRectF.right,
                        0f,
                        0f,
                        0f,
                        textGradientColors,
                        null,
                        when (tileMode) {
                            0 -> Shader.TileMode.CLAMP
                            1 -> Shader.TileMode.REPEAT
                            2 -> Shader.TileMode.MIRROR
                            else -> {
                                Shader.TileMode.CLAMP
                            }
                        }
                    )
                }
                6 -> {
                    textPaint.shader = LinearGradient(
                        0f, 0f, 0f, mRectF.bottom,
                        textGradientColors,
                        null,
                        when (tileMode) {
                            0 -> Shader.TileMode.CLAMP
                            1 -> Shader.TileMode.REPEAT
                            2 -> Shader.TileMode.MIRROR
                            else -> {
                                Shader.TileMode.CLAMP
                            }
                        }
                    )
                }
                7 -> {
                    textPaint.shader = LinearGradient(
                        0f, mRectF.bottom, 0f, 0f,
                        textGradientColors,
                        null,
                        when (tileMode) {
                            0 -> Shader.TileMode.CLAMP
                            1 -> Shader.TileMode.REPEAT
                            2 -> Shader.TileMode.MIRROR
                            else -> {
                                Shader.TileMode.CLAMP
                            }
                        }
                    )
                }
            }
        }


    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isRollByUser) {
            startInternal(true)
        }
    }

    override fun onDetachedFromWindow() {
        if (isRolling) {
            stopInternal(false)
        }
        super.onDetachedFromWindow()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility != VISIBLE) {
            stopInternal(false)
        } else {
            if (!isRollByUser) {
                startInternal(false)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        text = text
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val colorList = ArrayList<Int>()
        val a = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView)
        textColor = a.getColor(R.styleable.MarqueeTextView_android_textColor, textColor)
        isResetLocation = a.getBoolean(R.styleable.MarqueeTextView_marqueeResetLocation, true)
        speed = a.getFloat(R.styleable.MarqueeTextView_marqueeSpeed, 1f)
        gradientFirst = a.getColor(R.styleable.MarqueeTextView_gradientFirst, 0)
        gradientSecond = a.getColor(R.styleable.MarqueeTextView_gradientSecond, 0)
        gradientThird = a.getColor(R.styleable.MarqueeTextView_gradientThird, 0)
        gradientFourth = a.getColor(R.styleable.MarqueeTextView_gradientFourth, 0)
        textSize = a.getDimension(R.styleable.MarqueeTextView_android_textSize, 12f)
        textItemDistance = a.getDimension(R.styleable.MarqueeTextView_marqueeItemDistance, 50f)
        startLocationDistance =
            a.getFloat(R.styleable.MarqueeTextView_marqueeStartLocationDistance, 0f)
        repeat = a.getInt(R.styleable.MarqueeTextView_marqueeRepeat, REPEAT_SINGLE_LOOP)
        text = a.getText(R.styleable.MarqueeTextView_android_text)?.toString() ?: ""
        tileMode = a.getInt(R.styleable.MarqueeTextView_tileMode, 0)
        gradientStyle = a.getInt(R.styleable.MarqueeTextView_gradientStyle, 0)
        if (gradientFirst != 0) colorList.add(gradientFirst)
        if (gradientSecond != 0) colorList.add(gradientSecond)
        if (gradientThird != 0) colorList.add(gradientThird)
        if (gradientFourth != 0) colorList.add(gradientFourth)
        textGradientColors = colorList.toIntArray()
        a.recycle()
        colorList.clear()

    }

    /**
     * 刻字机修改
     */
    private fun initPaint() {
        textPaint.apply {
            style = Paint.Style.FILL
            color = textColor
            textSize = textSize
            isAntiAlias = true
            density = 1 / resources.displayMetrics.density
        }
    }

    /**
     * 切换开始暂停
     */
    fun toggle() {
        if (isRolling) {
            stop()
        } else {
            start()
        }
    }

    /**
     * 继续滚动
     */
    fun start() {
        startInternal(true)
    }

    /**
     * [isRollByUser] 是否用户主动调用
     */
    private fun startInternal(isRollByUser: Boolean) {
        this.isRollByUser = isRollByUser
        stop()
        if (text.isNotBlank()) {
            mHandler.sendEmptyMessage(MyHandler.WHAT_RUN)
            isRolling = true
        }
    }

    /**
     * 停止滚动
     */
    private fun stop() {
        stopInternal(true)
    }

    /**
     * [isRollByUser] 是否用户主动调用
     */
    protected fun stopInternal(isRollByUser: Boolean) {
        this.isRollByUser = isRollByUser
        isRolling = false
        mHandler.removeMessages(MyHandler.WHAT_RUN)
    }

    /**
     * 计算出一个空格的宽度
     * @return
     */
    private fun getBlankWidth(): Float {
        return getTextWidth(BLANK)
    }

    private fun getTextWidth(text: String?): Float {
        if (text.isNullOrEmpty()) {
            return 0f
        }
        return textPaint.measureText(text)
    }

    /**
     * 文本高度
     */
    private fun getTextHeight(): Float {
        val fontMetrics = textPaint.fontMetrics
        return abs(fontMetrics.bottom - fontMetrics.top) / 2
    }

    private fun getItemEndBlank(): String {
        val oneBlankWidth = getBlankWidth() //空格的宽度
        var count = 1
        if (textItemDistance > 0 && oneBlankWidth != 0f) {
            count = (ceil(textItemDistance / oneBlankWidth).toInt()) //粗略计算空格数量
        }
        val builder = StringBuilder(count)
        for (i in 0..count) {
            builder.append(BLANK)//间隔字符串
        }
        return builder.toString()
    }

    private class MyHandler(view: MarqueeTextView) : Handler(Looper.getMainLooper()) {

        companion object {
            internal const val WHAT_RUN = 1001
        }

        private val mRef = WeakReference(view)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == WHAT_RUN) {
                mRef.get()?.apply {
                    if (speed > 0) {
                        xLocation -= speed
                        invalidate()
                        sendEmptyMessageDelayed(WHAT_RUN, 1)
                    }
                }
            }
        }
    }

}