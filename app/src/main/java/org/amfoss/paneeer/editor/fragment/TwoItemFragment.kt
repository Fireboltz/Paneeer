package org.amfoss.paneeer.editor.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.amfoss.paneeer.Paneeer
import org.amfoss.paneeer.R
import org.amfoss.paneeer.editor.EditImageActivity

class TwoItemFragment : BaseEditFragment(), View.OnClickListener {
    var ll_item1: LinearLayout? = null
    var ll_item2: LinearLayout? = null
    var icon1: ImageView? = null
    var icon2: ImageView? = null
    var text1: TextView? = null
    var text2: TextView? = null
    var mode = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_editor_twoitem, container, false)
        ll_item1 =
            view.findViewById(R.id.menu_item1)
        ll_item2 =
            view.findViewById(R.id.menu_item2)
        icon1 = view.findViewById(R.id.item1iconimage)
        icon2 = view.findViewById(R.id.item2iconimage)
        text1 = view.findViewById(R.id.item1text)
        text2 = view.findViewById(R.id.item2text)
        ll_item1?.setOnClickListener(this)
        ll_item2?.setOnClickListener(this)
        if (mode == EditImageActivity.MODE_WRITE) {
            icon1?.setImageResource(R.drawable.ic_text)
            icon2?.setImageResource(R.drawable.ic_paint)
            text1?.setText(getString(R.string.text))
            text2?.setText(getString(R.string.paint))
        } else {
            icon1?.setImageResource(R.drawable.ic_crop)
            icon2?.setImageResource(R.drawable.ic_rotate)
            text1?.setText(getString(R.string.crop))
            text2?.setText(getString(R.string.rotate))
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        Paneeer.getRefWatcher(getActivity()).watch(this)
    }

    override fun onShow() {}
    override fun onClick(v: View) {
        when (v.id) {
            R.id.menu_item1 -> {
                clearSelection()
                ll_item1!!.setBackgroundColor(
                    ContextCompat.getColor(
                        v.context,
                        R.color.md_grey_200
                    )
                )
                firstItemClicked()
            }
            R.id.menu_item2 -> {
                clearSelection()
                ll_item2!!.setBackgroundColor(
                    ContextCompat.getColor(
                        v.context,
                        R.color.md_grey_200
                    )
                )
                secondItemClicked()
            }
        }
    }

    fun clearSelection() {
        ll_item1!!.setBackgroundColor(Color.TRANSPARENT)
        ll_item2!!.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun firstItemClicked() {
        if (mode == EditImageActivity.MODE_ADJUST) {
            activity!!.changeMode(EditImageActivity.MODE_CROP)
            activity!!.changeBottomFragment(EditImageActivity.MODE_CROP)
        } else if (mode == EditImageActivity.MODE_WRITE) {
            activity!!.changeMode(EditImageActivity.MODE_TEXT)
            activity!!.changeBottomFragment(EditImageActivity.MODE_TEXT)
        }
    }

    private fun secondItemClicked() {
        if (mode == EditImageActivity.MODE_ADJUST) {
            activity!!.changeMode(EditImageActivity.MODE_ROTATE)
            activity!!.changeBottomFragment(EditImageActivity.MODE_ROTATE)
        } else if (mode == EditImageActivity.MODE_WRITE) {
            activity!!.changeMode(EditImageActivity.MODE_PAINT)
            activity!!.changeBottomFragment(EditImageActivity.MODE_PAINT)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(mode: Int): TwoItemFragment {
            val fragment = TwoItemFragment()
            fragment.mode = mode
            return fragment
        }
    }
}