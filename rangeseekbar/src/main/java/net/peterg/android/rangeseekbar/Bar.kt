package net.peterg.android.rangeseekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue

/**
 * Class for painting a bar presenting a seek range or the base path to move the thumbs on.
 * With the draw() methods you can draw this bar either between to given values presenting the thumbs or as the background bar between to boarders.
 * <p/>
 * This class is used by the {@link RangeSeekBar} to draw their lines.
 *
 * @author peterg
 */

class Bar(context: Context, val yCoordinate: Float, val leftXCoordinate: Float, val rightXCoordinate: Float, height: Float, color: Int, segmentCount: Int = 1) {

    private var segmentDistance = (rightXCoordinate - leftXCoordinate) / segmentCount

    private val paint = Paint()

    init {
        paint.color = color
        paint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                height,
                context.resources.displayMetrics)
        paint.isAntiAlias = true
    }

    /**
     * Draws the bar in the given canvas. The range is the one provided by the left and right values set in the constructor. Use this method to draw the background line.
     *
     * @param canvas the canvas in which the bar has to be drawn
     */
    fun draw(canvas: Canvas) {
        draw(canvas, leftXCoordinate, rightXCoordinate)
    }

    /**
     * Draws the bar into the given canvas between the range provided by leftX and rightX. Use this method to draw the connecting line between the two thumbs.
     *
     * @param canvas the canvas in which the bar has to be drawn
     * @param leftX  left x coordinate as pixel value
     * @param rightX right x coordinate as pixel value
     */
    fun draw(canvas: Canvas, leftX: Float, rightX: Float) {
        canvas.drawLine(leftX, yCoordinate, rightX, yCoordinate, paint)
    }

    /**
     * Returns the x coordinate for the given segment index.
     *
     * @param segmentIndex the segment index for which the x coordinate has to be returned
     * @return the x coordinate
     */
    fun getXCoordinate(segmentIndex: Int) = leftXCoordinate + segmentIndex * segmentDistance

    /**
     * Returns the nearest segment index to the given x coordinate.
     *
     * @param xCoordinate the xo coordinate which has to be assigned to an index
     * @return the nearest segment index
     */
    fun getNearestSegmentIndex(xCoordinate: Float) = ((xCoordinate - leftXCoordinate + segmentDistance / 2.0f) / segmentDistance).toInt()

    /**
     * Returns whether on the left side of the given x coordinate is more available space for the thumb to move than on the right
     * side of the x coordinate.
     *
     * @param xCoordinate the x coordinate for which the spaces has to be determined
     * @return true if on the left side of the x coordinate is more available space, false otherwise
     */
    fun leftMoreSpace(xCoordinate: Float) = (xCoordinate - leftXCoordinate) / (rightXCoordinate - leftXCoordinate) > 0.5f

    /**
     * Sets a new color for the bar.
     *
     * @param color the new color of the bar
     */
    fun setBarColor(color: Int) {
        paint.color = color
    }

    /**
     * Sets a new alpha value of the current color. The hex values of red, green and blue will be the same.
     *
     * @param transFactor the factor to multiply with the current alpha value
     */
    @Suppress("unused")
    fun setNewAlpha(transFactor: Float) {
        val alpha = Math.round(Color.alpha(paint.color) * transFactor)
        val red = Color.red(paint.color)
        val green = Color.green(paint.color)
        val blue = Color.blue(paint.color)
        paint.color = Color.argb(alpha, red, green, blue)
    }

    /**
     * Recalculates the segment distance with the given segment count used to provide x coordinates and the nearest segment index.
     */
    fun setSegmentDistanceWithSegmentCount(segmentCount: Int) {
        segmentDistance = (rightXCoordinate - leftXCoordinate) / segmentCount
    }
}