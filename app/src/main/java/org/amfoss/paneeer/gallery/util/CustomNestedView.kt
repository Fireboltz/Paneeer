package org.amfoss.paneeer.gallery.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

class CustomNestedView : NestedScrollView {
    private var enableScrolling = true

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context!!, attrs, defStyleAttr) {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (scrollingEnabled()) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (scrollingEnabled()) {
            super.onTouchEvent(ev)
        } else {
            false
        }
    }

    private fun scrollingEnabled(): Boolean {
        return enableScrolling
    }

    fun setScrolling(enableScrolling: Boolean) {
        this.enableScrolling = enableScrolling
    }
}