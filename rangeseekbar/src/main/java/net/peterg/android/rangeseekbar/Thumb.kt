package net.peterg.android.rangeseekbar

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * Thumbs is a subclass of [View] and handles the thumb drawing with (shadow) pins. Thumbs are only small circles which the user can drag around to set a value in the range seek bar.
 *
 * @author peterg
 */

/**
 * Default thumb radius.
 */
const val DEFAULT_THUMB_RADIUS_DP = 6f

/**
 * Default scale factor in pressed state.
 */
const val THUMB_PRESSED_RADIUS_SCALE_INNER = 1.5f

/**
 * Default scale factor of the transparent shade in pressed state.
 */
const val THUMB_PRESSED_RADIUS_SCALE_OUTER = 3f

const val DEFAULT_DIFFERENCE_PADDING = 4f
const val DEFAULT_FINAL_PIN_PADDING = 8f

/**
 * Default scale factor of the transparent shade in pressed state.
 */
private const val DEFAULT_THUMB_COLOR_DISABLED = Color.DKGRAY

private const val DEFAULT_PIN_RADIUS = 0f
private const val DEFAULT_PIN_PADDING = 15f
private const val DEFAULT_TEXT_Y_PADDING = 3.5f
private const val DEFAULT_TEXT_SIZE = 14f

class Thumb(context: Context,
            private val yCoordinate: Float = 0f,
            color: Int = Color.parseColor("#33B5E5"),
            thumbRadiusDP: Float = DEFAULT_THUMB_RADIUS_DP,
            private val showPin: Boolean = false,
            drawShadowPin: Boolean = false,
            private val expandedPinRadius: Float = 0f,
            attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(context, color = Color.parseColor("#33B5E5"), attrs = attrs, defStyleAttr = defStyleAttr)

    private val alpha = Math.round(Color.alpha(color) * 0.3f)
    private val red = Color.red(color)
    private val green = Color.green(color)
    private val blue = Color.blue(color)
    private val thumbColorPressed = Color.argb(alpha, red, green, blue)

    private var thumbRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            thumbRadiusDP,
            resources.displayMetrics)
    private val thumbPressedRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            thumbRadiusDP * THUMB_PRESSED_RADIUS_SCALE_INNER,
            resources.displayMetrics)
    val thumbPressedShadeRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            thumbRadiusDP * THUMB_PRESSED_RADIUS_SCALE_OUTER,
            resources.displayMetrics)

    private var pinRadiusPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_PIN_RADIUS,
            resources.displayMetrics)
    private var pinPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_PIN_PADDING,
            resources.displayMetrics)

    private val textYPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_TEXT_Y_PADDING,
            resources.displayMetrics)
    private val expandedTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            DEFAULT_TEXT_SIZE,
            resources.displayMetrics)

    private var thumbHalfWidth = thumbRadiusPx
    var xCoordinate = thumbRadiusPx

    private val paintPressed = Paint()
    private val paintDisabled = Paint()
    private val paintNormal = Paint()
    private val paintText = Paint()

    private val pin = context.resources.getDrawable(R.drawable.pin, null)
    private val shadowPin: Drawable?
    private val finalPinPadding: Float

    private var relativeTextSize = 0f
    var pinText = ""

    private val bounds = Rect()

    init {
        paintPressed.color = thumbColorPressed
        paintPressed.isAntiAlias = true

        paintDisabled.color = DEFAULT_THUMB_COLOR_DISABLED
        paintDisabled.isAntiAlias = true

        paintNormal.color = color
        paintNormal.isAntiAlias = true

        paintText.color = Color.WHITE
        paintText.isAntiAlias = true
        paintText.textSize = expandedTextSize

        if (drawShadowPin) {
            shadowPin = context.resources.getDrawable(R.drawable.pin, null)
            finalPinPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    DEFAULT_DIFFERENCE_PADDING,
                    resources.displayMetrics) + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    DEFAULT_FINAL_PIN_PADDING,
                    resources.displayMetrics)
        } else {
            shadowPin = null
            finalPinPadding = 0f
        }
    }

    /**
     * Sets the pin and the thumb sizes during popup animation on press.
     *
     * @param press                true if animation is on press, false otherwise
     * @param size                 the size of the pin radius
     * @param padding              the size of the padding
     * @param radiusAnimationRatio the ratio to set the thumbRadius
     * @param animationFraction    the animationFraction
     */
    fun setAnimationSize(press: Boolean, size: Float, padding: Float, radiusAnimationRatio: Float, animationFraction: Float) {
        thumbRadiusPx = thumbHalfWidth - radiusAnimationRatio * thumbHalfWidth

        relativeTextSize = if (press) {
            expandedTextSize * animationFraction
        } else {
            expandedTextSize - expandedTextSize * animationFraction
        }
        pinPadding = padding
        pinRadiusPx = size
        invalidate()
    }

    /**
     * Returns whether the x coordinates lies in a defined touch area of a thumb. The touch areas height is restricted by the views height. The touch areas width is 2 times the thumb half width.
     *
     * @param xCoordinate the x coordinate to check
     * @param yCoordinate the y coordinate to check
     * @return true if the x coordinate is in the touch area, false otherwise
     */
    fun isInTouchArea(xCoordinate: Float, @Suppress("UNUSED_PARAMETER") yCoordinate: Float) = Math.abs(xCoordinate - this.xCoordinate) <= (2 * thumbHalfWidth) //&& Math.abs(yCoord - yCoordinate) <= (useBitmap ? thumbHalfWidth : 2 * thumbHalfWidth)

    /**
     * Draws the bar in the given canvas.
     *
     * @param canvas the canvas where to draw the thumb
     */
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (!isEnabled) {
            canvas.drawCircle(xCoordinate, yCoordinate, thumbRadiusPx, paintDisabled)
        } else {
            if (isPressed) {
                if (!showPin) {
                    canvas.drawCircle(
                            xCoordinate,
                            yCoordinate,
                            thumbPressedRadiusPx,
                            paintNormal)

                    canvas.drawCircle(
                            xCoordinate,
                            yCoordinate,
                            thumbPressedShadeRadiusPx,
                            paintPressed)
                } else {
                    canvas.drawCircle(
                            xCoordinate,
                            yCoordinate,
                            thumbRadiusPx,
                            paintNormal)
                }
            } else {
                canvas.drawCircle(
                        xCoordinate,
                        yCoordinate,
                        thumbRadiusPx,
                        paintNormal)
            }
        }

        if (showPin) {

            var text = pinText
            if (pinText.length > 4) { // Currently the pin only has space for 4 characters
                text = pinText.substring(0, 4)
            }

            if (pinRadiusPx > 0) {
                bounds.set(Math.round(xCoordinate - pinRadiusPx),
                        Math.round(yCoordinate - pinRadiusPx * 2 - pinPadding),
                        Math.round(xCoordinate + pinRadiusPx),
                        Math.round(yCoordinate - pinPadding))

                pin.bounds = bounds
                pin.setColorFilter(paintNormal.color, PorterDuff.Mode.SRC_ATOP)
                pin.draw(canvas)

                paintText.textSize = relativeTextSize
                paintText.getTextBounds(text, 0, text.length, bounds)
                paintText.textAlign = Paint.Align.CENTER

                canvas.drawText(text,
                        xCoordinate, yCoordinate - pinRadiusPx - pinPadding + textYPadding,
                        paintText)
            }

            shadowPin?.let {
                val shadowBound = Rect(Math.round(xCoordinate - expandedPinRadius),
                        Math.round(yCoordinate - expandedPinRadius * 2 - finalPinPadding),
                        Math.round(xCoordinate + expandedPinRadius),
                        Math.round(yCoordinate - finalPinPadding))

                shadowPin.bounds = shadowBound
                shadowPin.mutate().setColorFilter(paintNormal.color, PorterDuff.Mode.SRC_ATOP)
                shadowPin.alpha = 50
                shadowPin.draw(canvas)

                paintText.textSize = expandedTextSize
                paintText.getTextBounds(text, 0, text.length, shadowBound)
                paintText.textAlign = Paint.Align.CENTER
                canvas.drawText(text,
                        xCoordinate, yCoordinate - expandedPinRadius - finalPinPadding + textYPadding,
                        paintText)
            }
        }
    }
}