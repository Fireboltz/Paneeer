package org.amfoss.paneeer.gallery.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

class PagerRecyclerView : RecyclerView {
    private var currPosition = -1

    constructor(context: Context?) : super(context!!) {
        if (!isInEditMode) init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        if (!isInEditMode) init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context!!, attrs, defStyle) {
        if (!isInEditMode) init()
    }

    private fun init() {
        PagerSnapHelper().attachToRecyclerView(this)
    }

    fun setOnPageChangeListener(onPageChangeListener: OnPageChangeListener?) {
        if (onPageChangeListener == null) return
        if (currPosition == -1) {
            currPosition =
                (layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()
            if (currPosition == -1) currPosition = 0
            addOnScrollListener(
                object : OnScrollListener() {
                    override fun onScrolled(
                        recyclerView: RecyclerView,
                        dx: Int,
                        dy: Int
                    ) {
                        super.onScrolled(recyclerView, dx, dy)
                        val oldPosition = currPosition
                        val newPosition = (layoutManager as LinearLayoutManager?)
                            ?.findFirstCompletelyVisibleItemPosition()
                        if (newPosition != -1) currPosition = newPosition!!
                        if (currPosition != oldPosition) onPageChangeListener.onPageChanged(
                            oldPosition,
                            currPosition
                        )
                    }
                })
        }
    }

    interface OnPageChangeListener {
        fun onPageChanged(oldPosition: Int, newPosition: Int)
    }
}