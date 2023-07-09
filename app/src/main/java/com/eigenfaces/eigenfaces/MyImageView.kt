package com.eigenfaces.eigenfaces

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView


class MyImageView : AppCompatImageView {
    private val INVALID_POINTER_ID = -1

    private var mImage : Drawable? = null

    private var mPosX : Float = 0f
    private var mPosY : Float = 0f

    private var mLastTouchX : Float = 0f
    private var mLastTouchY : Float = 0f
    private var mActivePointerID = INVALID_POINTER_ID

    private var mScaleGestureDetector: ScaleGestureDetector
    private var mScaleFactor : Float = 1f
    private var minScaleFactor: Float = 1f


    constructor(context : Context, attrs : AttributeSet) : this(context, attrs, 0) {
        //Log.i("CONSTRUCTORTAG", "constructed!")
        mImage = resources.getDrawable(R.drawable.ic_launcher_background, context.theme)
        mImage!!.setBounds(0, 0, mImage!!.intrinsicWidth, mImage!!.intrinsicHeight)
    }

    constructor(context : Context, attrs: AttributeSet?, defStyle : Int) : super(context, attrs, defStyle) {
        mScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                mLastTouchX = x
                mLastTouchY = y
                mActivePointerID = event.getPointerId(0)
                //Log.i("TOUCHTAG", "down")
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(mActivePointerID)
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)
                if (!mScaleGestureDetector.isInProgress()) {
                    val dx = x - mLastTouchX
                    val dy = y - mLastTouchY
                    if ((mPosX + mImage!!.intrinsicWidth * (1f + mScaleFactor) / 2f + dx) >= super.getWidth()
                        && (mPosX + mImage!!.intrinsicWidth * (1f - mScaleFactor) / 2f + dx) <= 0f) {
                        mPosX += dx
                    }
                    if ((mPosY + mImage!!.intrinsicHeight * (1f + mScaleFactor) / 2f + dy) >= super.getHeight()
                        && (mPosY + mImage!!.intrinsicHeight * (1f - mScaleFactor) / 2f + dy) <= 0f ) {
                        mPosY += dy
                    }

                    invalidate()
                }

                mLastTouchX = x
                mLastTouchY = y
                //Log.i("TOUCHTAG", "move")
            }
            MotionEvent.ACTION_UP -> {
                mActivePointerID = INVALID_POINTER_ID
                //Log.i("TOUCHTAG", "up")
            }
            MotionEvent.ACTION_CANCEL -> {
                mActivePointerID = INVALID_POINTER_ID
                //Log.i("TOUCHTAG", "cancel")
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex: Int = (event.getAction() and MotionEvent.ACTION_POINTER_INDEX_MASK
                        shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == mActivePointerID) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = event.getX(newPointerIndex)
                    mLastTouchY = event.getY(newPointerIndex)
                    mActivePointerID = event.getPointerId(newPointerIndex)
                }
                //Log.i("TOUCHTAG", "pointer up")
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mImage == null) {
            return
        }

        val pivotX = mImage!!.intrinsicWidth / 2f
        val pivotY = mImage!!.intrinsicHeight / 2f

        //Log.i("DRAWTAG", "mImage Width: " + mImage!!.intrinsicWidth.toString())
        //Log.i("DRAWTAG", "view Width: " + super.getWidth().toString())

        if ((mPosX + mImage!!.intrinsicWidth * (1f + mScaleFactor) / 2f) < super.getWidth()) {
            mPosX = super.getWidth() - mImage!!.intrinsicWidth * (1f + mScaleFactor) / 2f
        } else if ((mPosX + mImage!!.intrinsicWidth * (1f - mScaleFactor) / 2f) > 0f) {
            mPosX = - mImage!!.intrinsicWidth * (1f - mScaleFactor) / 2f
        }
        if ((mPosY + mImage!!.intrinsicHeight * (1f + mScaleFactor) / 2f) < super.getHeight()) {
            mPosY = super.getHeight() - mImage!!.intrinsicHeight * (1f + mScaleFactor) / 2f
        } else if ((mPosY + mImage!!.intrinsicHeight * (1f - mScaleFactor) / 2f) > 0f ) {
            mPosY = - mImage!!.intrinsicHeight * (1f - mScaleFactor) / 2f
        }

        canvas.save()
        //Log.i("DRAWTAG", "X: "+mPosX+" Y: "+mPosY)
        canvas.translate(mPosX, mPosY)
        canvas.scale(mScaleFactor, mScaleFactor, pivotX, pivotY)
        mImage!!.draw(canvas)
        canvas.restore()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        minScaleFactor = (super.getWidth().toFloat() / bm!!.width.toFloat()).coerceAtLeast(
            super.getHeight().toFloat() / bm.height)
        mScaleFactor = minScaleFactor
        //Log.i("BITMAP_TAG", "scale: " + mScaleFactor.toString())
        mPosX = (super.getWidth() - bm.width) / 2f
        mPosY = (super.getHeight() - bm.height) / 2f
        //Log.i("BITMAP_TAG", "X: " + mPosX.toString())
        //Log.i("BITMAP_TAG", "Y: " + mPosY.toString())
        mImage = BitmapDrawable(resources, bm)
        mImage!!.setBounds(0, 0, mImage!!.intrinsicWidth, mImage!!.intrinsicHeight)
    }



    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor

            mScaleFactor = (mScaleFactor.coerceAtMost(10.0f)).coerceAtLeast(minScaleFactor)

            return true
        }
    }

}


