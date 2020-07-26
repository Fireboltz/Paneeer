package org.amfoss.paneeer.gallery.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView

class CustomScrollBarRecyclerView : RecyclerView {
    private var scrollBarColor = Color.RED

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context!!, attrs, defStyle) {
    }

    fun setScrollBarColor(@ColorInt scrollBarColor: Int) {
        this.scrollBarColor = scrollBarColor
    }

    /** Called by Android [android.view.View.onDrawScrollBars]  */
    protected fun onDrawHorizontalScrollBar(
        canvas: Canvas?,
        scrollBar: Drawable,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP)
        scrollBar.setBounds(l, t, r, b)
        scrollBar.draw(canvas!!)
    }

    /** Called by Android [android.view.View.onDrawScrollBars]  */
    protected fun onDrawVerticalScrollBar(
        canvas: Canvas?,
        scrollBar: Drawable,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP)
        scrollBar.setBounds(l, t, r, b)
        scrollBar.draw(canvas!!)
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        val returnValue: Boolean
        returnValue = super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
        if (offsetInWindow!![1] != 0) {
            offsetInWindow[1] = 0
        }
        return returnValue
    }
}