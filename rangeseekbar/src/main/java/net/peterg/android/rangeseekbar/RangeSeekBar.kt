package net.peterg.android.rangeseekbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

/**
 * Range seek bar to let the user select a range from a given data list. You can use this view inside your xml layout or create one from code. The data list can contain any data object which implements the [Parcelable] interface. The [toString]
 * method is used to show the current value inside of a pin. If you have to get notified about the updated values just set your own method for the [RangeSeekBar.callbackAction]. There you get the range bar which values where updated, the new
 * values and if the action was user intended.
 *
 * @author peterg
 */

/**
 * Returns the left index or 0 if the given item was not found from a list.
 */
fun List<Any>.getLeftIndexFromValue(value: Any) = if (contains(value)) indexOf(value) else 0

/**
 * Returns the right index or the last items index if the given item was not found from a list.
 */
fun List<Any>.getRightIndexFromValue(value: Any) = if (contains(value)) indexOf(value) else size - 1

const val DEFAULT_THUMB_SEEK_BAR_COLOR = "#ff33B5E5"

private const val SUPER = "super"
private const val COLOR = "color"
private const val THUMB_RADIUS_DP = "thumbRadiusDP"
private const val DATA_LIST = "dataList"
private const val LEFT_SEGMENT_INDEX = "leftSegmentIndex"
private const val RIGHT_SEGMENT_INDEX = "rightSegmentIndex"
private const val SHOW_PIN = "showPin"
private const val THUMB_TO_DRAW = "thumbToDraw"
private const val DRAW_PIN_SHADOW = "drawPinShadow"

private const val DEFAULT_BACKGROUND_BAR_HEIGHT_DP = 1f
private const val DEFAULT_BACKGROUND_BAR_COLOR = Color.LTGRAY

private const val DEFAULT_SCREEN_HEIGHT_ADAPTION = 4

private const val COLOR_TRANSPARENT = 0x00000000

private const val LEFT_THUMB = 0
private const val RIGHT_THUMB = 1
private const val BOTH = 2

private const val DEFAULT_WIDTH = 600

private inline fun <T : View> T.onLayoutDone(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            f()
        }
    })
}

class RangeSeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {

    private lateinit var leftThumb: Thumb
    private lateinit var rightThumb: Thumb
    private lateinit var backGroundBar: Bar
    private lateinit var seekBar: Bar

    private var dataList: List<Parcelable> = listOf()

    private var thumbSeekBarColor = Color.parseColor(DEFAULT_THUMB_SEEK_BAR_COLOR)
    private var thumbRadiusDP = DEFAULT_THUMB_RADIUS_DP

    private var showPin = false

    private var thumbToDraw = LEFT_THUMB
    private var drawPinShadow = false

    private var leftIndex = 0
    private var rightIndex = 1

    private val differencePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_DIFFERENCE_PADDING,
            resources.displayMetrics)
    private val expandedPinRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_THUMB_RADIUS_DP * THUMB_PRESSED_RADIUS_SCALE_OUTER,
            resources.displayMetrics)
    private val pinPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_FINAL_PIN_PADDING,
            resources.displayMetrics)

    private val minViewHeight: Int

    /**
     * Set your custom method to get notified about value changes.
     * bar        the RangeSeekBar in which the change happened
     * leftThumb  the new left thumbs value
     * rightThumb the new right thumbs value
     * fromUser   boolean value set to true if this call was indicated by the user, false otherwise
     */
    var callbackAction: (bar: RangeSeekBar, leftThumb: Any, rightThumb: Any, fromUser: Boolean) -> Unit = { _, _, _, _ -> }

    init {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar, 0, 0)
        try {
            thumbSeekBarColor = attributes.getColor(R.styleable.RangeSeekBar_thumbAndSeekBarColor, thumbSeekBarColor)
            thumbRadiusDP = attributes.getDimension(R.styleable.RangeSeekBar_thumbRadius, DEFAULT_THUMB_RADIUS_DP)

            showPin = attributes.getBoolean(R.styleable.RangeSeekBar_showPin, false)
            drawPinShadow = attributes.getBoolean(R.styleable.RangeSeekBar_drawPinShadow, false)

            thumbToDraw = attributes.getInt(R.styleable.RangeSeekBar_thumbToDraw, LEFT_THUMB)
        } finally {
            attributes.recycle()
        }

        minViewHeight = if (showPin) {
            val screenHeightAdaption = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    DEFAULT_SCREEN_HEIGHT_ADAPTION.toFloat(),
                    resources.displayMetrics).toInt()
            val viewHeightShowPin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    DEFAULT_THUMB_RADIUS_DP,
                    resources.displayMetrics).toInt()
            viewHeightShowPin + (2 * expandedPinRadius + pinPadding + differencePadding).toInt() + screenHeightAdaption
        } else {
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    DEFAULT_THUMB_RADIUS_DP * THUMB_PRESSED_RADIUS_SCALE_OUTER,
                    resources.displayMetrics).toInt() * 2
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val width = when (View.MeasureSpec.getMode(widthMeasureSpec)) {
            View.MeasureSpec.AT_MOST -> measureWidth + paddingLeft + paddingRight
            View.MeasureSpec.EXACTLY -> measureWidth
            View.MeasureSpec.UNSPECIFIED -> DEFAULT_WIDTH
            else -> DEFAULT_WIDTH
        }

        val measureHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val height = when (View.MeasureSpec.getMode(heightMeasureSpec)) {
            View.MeasureSpec.AT_MOST -> Math.min(minViewHeight, measureHeight)
            View.MeasureSpec.EXACTLY -> measureHeight
            View.MeasureSpec.UNSPECIFIED -> minViewHeight
            else -> minViewHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val yCoordinate = if (showPin) {
            h - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    thumbRadiusDP + 2,
                    resources.displayMetrics)
        } else {
            h / 2f
        }

        backGroundBar = Bar(context = context,
                yCoordinate = yCoordinate,
                leftXCoordinate = getThumbRadiusPx(thumbRadiusDP),
                rightXCoordinate = w - getThumbRadiusPx(thumbRadiusDP),
                height = DEFAULT_BACKGROUND_BAR_HEIGHT_DP,
                color = DEFAULT_BACKGROUND_BAR_COLOR)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        backGroundBar.draw(canvas)
        if (this::seekBar.isInitialized) seekBar.draw(canvas, leftThumb.xCoordinate, rightThumb.xCoordinate)

        if (!this::leftThumb.isInitialized || !this::rightThumb.isInitialized) return

        when (thumbToDraw) {
            LEFT_THUMB -> leftThumb.draw(canvas)
            RIGHT_THUMB -> rightThumb.draw(canvas)
            BOTH -> {
                leftThumb.draw(canvas)
                rightThumb.draw(canvas)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(SUPER, super.onSaveInstanceState())

        bundle.putInt(LEFT_SEGMENT_INDEX, leftIndex)
        bundle.putInt(RIGHT_SEGMENT_INDEX, rightIndex)
        bundle.putParcelableArrayList(DATA_LIST, ArrayList(dataList))

        bundle.putInt(COLOR, thumbSeekBarColor)
        bundle.putFloat(THUMB_RADIUS_DP, thumbRadiusDP)

        bundle.putBoolean(SHOW_PIN, showPin)
        bundle.putBoolean(DRAW_PIN_SHADOW, drawPinShadow)
        bundle.putInt(THUMB_TO_DRAW, thumbToDraw)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        state?.let {
            val bundle = state as Bundle
            super.onRestoreInstanceState(bundle.getParcelable(SUPER))

            leftIndex = bundle.getInt(LEFT_SEGMENT_INDEX)
            rightIndex = bundle.getInt(RIGHT_SEGMENT_INDEX)
            dataList = bundle.getParcelableArrayList(DATA_LIST)

            thumbSeekBarColor = bundle.getInt(COLOR)
            thumbRadiusDP = bundle.getFloat(THUMB_RADIUS_DP)

            showPin = bundle.getBoolean(SHOW_PIN)
            drawPinShadow = bundle.getBoolean(DRAW_PIN_SHADOW)
            thumbToDraw = bundle.getInt(THUMB_TO_DRAW)
        }

        setThumbs(leftIndex, rightIndex)
        if (this::backGroundBar.isInitialized) {
            backGroundBar.setSegmentDistanceWithSegmentCount(this.dataList.size - 1)
            createThumbs()
        } else {
            onLayoutDone {
                backGroundBar.setSegmentDistanceWithSegmentCount(this.dataList.size - 1)
                createThumbs()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchActionDown(event.x, event.y)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                touchActionMove(event.x)
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchActionUp(event.x)
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        leftThumb.isEnabled = enabled
        rightThumb.isEnabled = enabled

        seekBar.setBarColor(if (enabled) thumbSeekBarColor else COLOR_TRANSPARENT)
        backGroundBar.setBarColor(if (enabled) DEFAULT_BACKGROUND_BAR_COLOR else Color.DKGRAY)

        invalidate()
    }

    /**
     * Sets the data the user can choose from. The thumbs will be set to the default positions.
     */
    fun setData(dataList: List<Parcelable>) {
        this.dataList = dataList
        if (this::backGroundBar.isInitialized) {
            backGroundBar.setSegmentDistanceWithSegmentCount(this.dataList.size - 1)
            rightIndex = this.dataList.size - 1
            createThumbs()

            invalidate()
        } else {
            onLayoutDone {
                backGroundBar.setSegmentDistanceWithSegmentCount(this.dataList.size - 1)
                rightIndex = this.dataList.size - 1
                createThumbs()

                invalidate()
            }
        }
    }

    /**
     * Resets the whole range bar and the thumbs to their default positions.
     */
    @Suppress("unused")
    fun reset() {
        leftIndex = 0
        rightIndex = dataList.size - 1

        backGroundBar = Bar(context = context,
                yCoordinate = backGroundBar.yCoordinate,
                leftXCoordinate = leftThumb.thumbPressedShadeRadiusPx,
                rightXCoordinate = width - rightThumb.thumbPressedShadeRadiusPx,
                height = DEFAULT_BACKGROUND_BAR_HEIGHT_DP,
                color = DEFAULT_BACKGROUND_BAR_COLOR,
                segmentCount = dataList.size - 1)

        leftThumb.xCoordinate = backGroundBar.getXCoordinate(leftIndex)
        rightThumb.xCoordinate = backGroundBar.getXCoordinate(rightIndex)
        updateIndices(false)

        leftThumb.pinText = dataList[leftIndex].toString()
        rightThumb.pinText = dataList[rightIndex].toString()
        invalidate()
    }

    /**
     * Sets the thumbs to the given positions. If the data list doesn't contain the given values a [IllegalArgumentException] is thrown. If the rightThumb value index is bigger than the left one no thumb will be set.
     * The [callbackAction] will be called if the values are valid with the fromUser parameter set to false.
     * @param leftThumbValue the new leftThumb value to set
     * @param rightThumbValue the new rightThumb value to set
     * @throws IllegalArgumentException if the datalist doesn't contains the given values
     */
    @Suppress("unused")
    fun setThumbs(leftThumbValue: Parcelable, rightThumbValue: Parcelable) {
        if (!dataList.contains(leftThumbValue) || !dataList.contains(rightThumbValue)) throw IllegalArgumentException("Values not in provided data list")

        val newLeftIndex = dataList.getLeftIndexFromValue(leftThumbValue)
        val newRightIndex = dataList.getRightIndexFromValue(rightThumbValue)

        setThumbs(newLeftIndex, newRightIndex)
    }

    private fun setThumbs(newLeftIndex: Int, newRightIndex: Int) {
        if (newLeftIndex > newRightIndex) return
        this.leftIndex = newLeftIndex
        this.rightIndex = newRightIndex

        updateListener(false)
        invalidate()
    }

    private fun createThumbs() {
        leftThumb = Thumb(context, backGroundBar.yCoordinate, thumbSeekBarColor, thumbRadiusDP, showPin, drawPinShadow, expandedPinRadius)
        rightThumb = Thumb(context, backGroundBar.yCoordinate, thumbSeekBarColor, thumbRadiusDP, showPin, drawPinShadow, expandedPinRadius)

        leftThumb.xCoordinate = backGroundBar.getXCoordinate(leftIndex)
        rightThumb.xCoordinate = backGroundBar.getXCoordinate(rightIndex)

        leftThumb.pinText = dataList[leftIndex].toString()
        rightThumb.pinText = dataList[rightIndex].toString()

        seekBar = Bar(context = context,
                yCoordinate = backGroundBar.yCoordinate,
                leftXCoordinate = backGroundBar.getXCoordinate(leftIndex),
                rightXCoordinate = backGroundBar.getXCoordinate(rightIndex),
                height = DEFAULT_BACKGROUND_BAR_HEIGHT_DP + 3,
                color = thumbSeekBarColor)

        checkEnabled()
    }

    private fun touchActionDown(x: Float, y: Float) {
        val thumb = if (thumbToDraw != BOTH) testSingleThumb(x, y) else testMultipleThumb(x, y)

        thumb?.let {
            parent?.requestDisallowInterceptTouchEvent(true)
            updateIndices(true)
            animationDown(thumb)
            invalidate()
        }
    }

    private fun touchActionMove(x: Float) {
        if (leftThumb.isPressed && x in backGroundBar.leftXCoordinate..backGroundBar.getXCoordinate(rightIndex)) {
            leftThumb.xCoordinate = x
            leftThumb.pinText = dataList[backGroundBar.getNearestSegmentIndex(x)].toString()
        } else if (rightThumb.isPressed && x in backGroundBar.getXCoordinate(leftIndex)..backGroundBar.rightXCoordinate) {
            rightThumb.xCoordinate = x
            rightThumb.pinText = dataList[backGroundBar.getNearestSegmentIndex(x)].toString()
        }
        invalidate()
        updateIndices(true)
    }

    private fun touchActionUp(x: Float) {
        val thumb = when {
            leftThumb.isPressed -> {
                leftThumb.isPressed = false

                leftThumb.xCoordinate = when {
                    x < backGroundBar.leftXCoordinate -> backGroundBar.leftXCoordinate
                    x > backGroundBar.getXCoordinate(rightIndex) -> backGroundBar.getXCoordinate(rightIndex)
                    else -> backGroundBar.getXCoordinate(backGroundBar.getNearestSegmentIndex(x))
                }
                leftThumb
            }
            rightThumb.isPressed -> {
                rightThumb.isPressed = false
                rightThumb.xCoordinate = when {
                    x < backGroundBar.getXCoordinate(leftIndex) -> backGroundBar.getXCoordinate(leftIndex)
                    x > backGroundBar.rightXCoordinate -> backGroundBar.rightXCoordinate
                    else -> backGroundBar.getXCoordinate(backGroundBar.getNearestSegmentIndex(x))
                }
                rightThumb
            }
            else -> null
        }

        thumb?.let {
            parent?.requestDisallowInterceptTouchEvent(false)
            updateIndices(true)
            thumb.pinText = dataList[backGroundBar.getNearestSegmentIndex(thumb.xCoordinate)].toString()
            animationUp(thumb)
            invalidate()
        }
    }

    private fun animationDown(thumb: Thumb) {
        if (!showPin) return

        val animator = ValueAnimator.ofFloat(0f, expandedPinRadius)
        animator.addUpdateListener {
            val pinRadius = it.animatedValue as Float
            thumb.setAnimationSize(press = true,
                    size = pinRadius,
                    padding = pinPadding * it.animatedFraction + differencePadding,
                    radiusAnimationRatio = pinRadius / expandedPinRadius,
                    animationFraction = it.animatedFraction)
            invalidate()
        }
        animator.start()
    }

    private fun animationUp(thumb: Thumb) {
        if (!showPin) return

        val animator = ValueAnimator.ofFloat(expandedPinRadius, 0f)
        animator.addUpdateListener {
            val pinRadius = it.animatedValue as Float
            thumb.setAnimationSize(press = false,
                    size = pinRadius,
                    padding = pinPadding - pinPadding * it.animatedFraction + differencePadding,
                    radiusAnimationRatio = pinRadius / expandedPinRadius,
                    animationFraction = it.animatedFraction)
            invalidate()
        }
        animator.start()
    }

    private fun testSingleThumb(x: Float, y: Float) = if (leftThumb.isInTouchArea(x, y) && thumbToDraw == LEFT_THUMB) {
        leftThumb.isPressed = true
        leftThumb
    } else if (rightThumb.isInTouchArea(x, y) && thumbToDraw == RIGHT_THUMB) {
        rightThumb.isPressed = true
        rightThumb
    } else {
        null
    }

    private fun testMultipleThumb(x: Float, y: Float) = if (leftThumb.isInTouchArea(x, y) && rightThumb.isInTouchArea(x, y)) {
        if (backGroundBar.leftMoreSpace(x)) {
            leftThumb.isPressed = true
            leftThumb
        } else {
            rightThumb.isPressed = true
            rightThumb
        }
    } else if (leftThumb.isInTouchArea(x, y)) {
        leftThumb.isPressed = true
        leftThumb
    } else if (rightThumb.isInTouchArea(x, y)) {
        rightThumb.isPressed = true
        rightThumb
    } else {
        null
    }

    private fun updateIndices(fromUser: Boolean) {
        val newLeftIndex = backGroundBar.getNearestSegmentIndex(leftThumb.xCoordinate)
        val newRightIndex = backGroundBar.getNearestSegmentIndex(rightThumb.xCoordinate)

        if (newLeftIndex != leftIndex) {
            leftIndex = newLeftIndex
            updateListener(fromUser)
        }
        if (newRightIndex != rightIndex) {
            rightIndex = newRightIndex
            updateListener(fromUser)
        }
    }

    private fun checkEnabled() {
        leftThumb.isEnabled = isEnabled
        rightThumb.isEnabled = isEnabled

        if (!isEnabled) {
            seekBar.setBarColor(COLOR_TRANSPARENT)
            backGroundBar.setBarColor(Color.DKGRAY)
        }
    }

    private fun updateListener(fromUser: Boolean) {
        callbackAction(this, dataList[leftIndex], dataList[rightIndex], fromUser)
    }

    private fun getThumbRadiusPx(thumbRadiusDP: Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    thumbRadiusDP * THUMB_PRESSED_RADIUS_SCALE_OUTER,
                    resources.displayMetrics)
}