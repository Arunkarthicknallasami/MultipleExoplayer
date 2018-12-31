package com.app.ubiquifit.utils.session

import android.content.Context
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import java.util.*


/**
 * A [FrameLayout] that allows the user to drag and reposition child views.
 */
class DragFrameLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private var mTapDetector: GestureDetector? = null


    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : this(context, attrs)

    init {
        mTapDetector = GestureDetector(context, GestureTap())
    }

    /**
     * The list of [View]s that will be draggable.
     */
    val mDragViews: MutableList<View>


    internal var shouldClick = false
    /**
     * The [DragFrameLayoutController] that will be notify on drag.
     */
    var mDragFrameLayoutController: DragFrameLayoutController? = null

    val mDragHelper: ViewDragHelper

    init {
        mDragViews = ArrayList()

        /**
         * Create the [ViewDragHelper] and set its callback.
         */
        mDragHelper = ViewDragHelper.create(this, 1.0f, object : ViewDragHelper.Callback() {


            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return mDragViews.contains(child)
            }

            override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
                super.onViewPositionChanged(changedView, left, top, dx, dy)

            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                val leftBound = paddingLeft
                val rightBound = width - child.width - paddingRight
                return Math.min(Math.max(left, leftBound), rightBound)
                // return left;
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                val topBound = paddingTop
                val bottomBound = height - child.height - paddingBottom
                return Math.min(Math.max(top, topBound), bottomBound)

                //return top;
            }

            override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
                super.onViewCaptured(capturedChild, activePointerId)
                if (mDragFrameLayoutController != null) {
                    mDragFrameLayoutController!!.onDragDrop(true, capturedChild)
                }
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                super.onViewReleased(releasedChild, xvel, yvel)
                if (mDragFrameLayoutController != null) {
                    mDragFrameLayoutController!!.onDragDrop(false, releasedChild)
                }
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel()
            return false
        }
        return mDragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        mTapDetector?.onTouchEvent(motionEvent)

        mDragHelper.processTouchEvent(motionEvent)
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    /**
     * Adds a new [View] to the list of views that are draggable within the container.
     *
     * @param dragView the [View] to make draggable
     */
    fun addDragView(dragView: View) {
        mDragViews.add(dragView)
    }

    fun clearDragView() {
        mDragViews.clear()
    }

    fun removeDragview(view: View) {
        mDragViews.remove(view)
    }

    /**
     * Sets the [DragFrameLayoutController] that will receive the drag events.
     *
     * @param dragFrameLayoutController a [DragFrameLayoutController]
     */
    fun setDragFrameController(dragFrameLayoutController: DragFrameLayoutController) {
        mDragFrameLayoutController = dragFrameLayoutController
    }


    /**
     * A controller that will receive the drag events.
     */
    interface DragFrameLayoutController {

        fun onDragDrop(captured: Boolean, view: View)

        fun onclick(x: Float, y: Float)
    }

    internal inner class GestureTap : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
            mDragFrameLayoutController?.onclick(motionEvent.rawX, motionEvent.rawY)
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            super.onLongPress(e)
        }
    }
}
