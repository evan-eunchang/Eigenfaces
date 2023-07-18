package com.eigenfaces.eigenfaces

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView


const val INVALID_POINTER_ID = -1

//custom imageview class to allow for panning and zooming of the image without panning the image off the view
class MyImageView : AppCompatImageView {

    private var mImage : Drawable? = null

    private var mPosX : Float = 0f
    private var mPosY : Float = 0f

    private var mLastTouchX : Float = 0f
    private var mLastTouchY : Float = 0f
    private var mActivePointerID = INVALID_POINTER_ID

    private var mScaleGestureDetector: ScaleGestureDetector
    private var mScaleFactor : Float = 1f
    private var minScaleFactor: Float = 1f


    //initialize the view with an example face and scale it so it fills the view
    constructor(context : Context, attrs : AttributeSet) : this(context, attrs, 0) {
        mImage = resources.getDrawable(R.drawable.example_face, context.theme)
        mImage!!.setBounds(0, 0, mImage!!.intrinsicWidth, mImage!!.intrinsicHeight)
        //minimum scale factor ensure the image can never get small enough that blank space appears
        minScaleFactor = (super.getWidth().toFloat() / mImage!!.intrinsicWidth.toFloat()).coerceAtLeast(
            super.getHeight().toFloat() / mImage!!.intrinsicHeight.toFloat())
        mScaleFactor = minScaleFactor
    }

    //set up scale gesture detector to detect zooms
    constructor(context : Context, attrs: AttributeSet?, defStyle : Int) : super(context, attrs, defStyle) {
        mScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    //this function will translate touch gestures to numbers we can use to pan the image
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            //gesture begins, take note of where
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                mLastTouchX = x
                mLastTouchY = y
                mActivePointerID = event.getPointerId(0)
            }
            //as the finger moves, log these changes
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(mActivePointerID)
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)
                if (!mScaleGestureDetector.isInProgress) {
                    val dx = x - mLastTouchX
                    val dy = y - mLastTouchY
                    //only allow pans if the image will not pan off the screen
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
            }
            MotionEvent.ACTION_UP -> {
                mActivePointerID = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_CANCEL -> {
                mActivePointerID = INVALID_POINTER_ID
            }
            //
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex: Int = (event.action and MotionEvent.ACTION_POINTER_INDEX_MASK
                        shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == mActivePointerID) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = event.getX(newPointerIndex)
                    mLastTouchY = event.getY(newPointerIndex)
                    mActivePointerID = event.getPointerId(newPointerIndex)
                }
            }
        }

        return true
    }

    //take the changes into account and draw the image on the imageview, while again making sure
    //that a gesture will not move the image off the view
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mImage == null) return

        val pivotX = mImage!!.intrinsicWidth / 2f
        val pivotY = mImage!!.intrinsicHeight / 2f

        //check that gesture will not image off imageview
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

        //translate and scale image according to user inputs
        canvas.save()
        canvas.translate(mPosX, mPosY)
        canvas.scale(mScaleFactor, mScaleFactor, pivotX, pivotY)
        mImage!!.draw(canvas)
        canvas.restore()
    }

    //override setImageBitmap to take into account our unique requirements
    override fun setImageBitmap(bm: Bitmap?) {
        //set minimum scale factor
        minScaleFactor = (super.getWidth().toFloat() / bm!!.width.toFloat()).coerceAtLeast(
            super.getHeight().toFloat() / bm.height)
        mScaleFactor = minScaleFactor
        //initialize image
        mPosX = (super.getWidth() - bm.width) / 2f
        mPosY = (super.getHeight() - bm.height) / 2f
        mImage = BitmapDrawable(resources, bm)
        mImage!!.setBounds(0, 0, mImage!!.intrinsicWidth, mImage!!.intrinsicHeight)

    }



    //class and function to update scale factor based on user inputs
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            //make sure the scale factor is not too large or small
            mScaleFactor = (mScaleFactor.coerceAtMost(10.0f)).coerceAtLeast(minScaleFactor)
            invalidate()
            return true
        }
    }

}


