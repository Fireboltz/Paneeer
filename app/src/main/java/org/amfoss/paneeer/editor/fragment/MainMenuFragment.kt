package org.amfoss.paneeer.editor.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import org.amfoss.paneeer.Paneeer
import org.amfoss.paneeer.R
import org.amfoss.paneeer.editor.EditImageActivity

class MainMenuFragment : BaseEditFragment(), View.OnClickListener {
    private var menu_filter: View? = null
    private var menu_enhance: View? = null
    private var menu_adjust: View? = null
    private var menu_stickers: View? = null
    private var menu_write: View? = null
    private var menu_frame: View? = null
    var context1: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_editor_main, container, false)
        context1 = getActivity()
        menu_filter = view.findViewById(R.id.menu_filter)
        menu_enhance = view.findViewById(R.id.menu_enhance)
        menu_adjust = view.findViewById(R.id.menu_adjust)
        menu_stickers = view.findViewById(R.id.menu_sticker)
        menu_write = view.findViewById(R.id.menu_write)
        menu_frame = view.findViewById(R.id.menu_frame)
        menu_filter?.setOnClickListener(this)
        menu_enhance?.setOnClickListener(this)
        menu_adjust?.setOnClickListener(this)
        menu_stickers?.setOnClickListener(this)
        menu_write?.setOnClickListener(this)
        menu_frame?.setOnClickListener(this)
        highLightSelectedOption(EditImageActivity.getMode())
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        Paneeer.getRefWatcher(getActivity()).watch(this)
    }

    override fun onShow() {}
    override fun onClick(v: View) {
        when (v.id) {
            R.id.menu_filter -> {
                activity!!.changeMode(EditImageActivity.MODE_FILTERS)
                activity!!.sliderFragment.resetBitmaps()
                activity!!.changeMiddleFragment(EditImageActivity.MODE_FILTERS)
            }
            R.id.menu_enhance -> {
                activity!!.changeMode(EditImageActivity.MODE_ENHANCE)
                activity!!.sliderFragment.resetBitmaps()
                activity!!.changeMiddleFragment(EditImageActivity.MODE_ENHANCE)
            }
            R.id.menu_adjust -> {
                activity!!.changeMode(EditImageActivity.MODE_ADJUST)
                activity!!.changeMiddleFragment(EditImageActivity.MODE_ADJUST)
            }
            R.id.menu_sticker -> {
                activity!!.changeMode(EditImageActivity.MODE_STICKER_TYPES)
                activity!!.changeMiddleFragment(EditImageActivity.MODE_STICKER_TYPES)
            }
            R.id.menu_write -> {
                activity!!.changeMode(EditImageActivity.MODE_WRITE)
                activity!!.changeMiddleFragment(EditImageActivity.MODE_WRITE)
            }
            R.id.menu_frame -> {
                activity!!.changeMode(EditImageActivity.MODE_FRAME)
                activity!!.changeMiddleFragment(EditImageActivity.MODE_FRAME)
            }
        }
    }

    fun highLightSelectedOption(mode: Int) {
        menu_filter!!.setBackgroundColor(Color.TRANSPARENT)
        menu_enhance!!.setBackgroundColor(Color.TRANSPARENT)
        menu_adjust!!.setBackgroundColor(Color.TRANSPARENT)
        menu_stickers!!.setBackgroundColor(Color.TRANSPARENT)
        menu_write!!.setBackgroundColor(Color.TRANSPARENT)
        menu_frame!!.setBackgroundColor(Color.TRANSPARENT)
        val color = ContextCompat.getColor(context1!!, R.color.md_grey_200)
        when (mode) {
            2 -> menu_filter!!.setBackgroundColor(color)
            3 -> menu_enhance!!.setBackgroundColor(color)
            4 -> menu_adjust!!.setBackgroundColor(color)
            5 -> menu_stickers!!.setBackgroundColor(color)
            6 -> menu_write!!.setBackgroundColor(color)
            12 -> menu_frame!!.setBackgroundColor(color)
            else -> {
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): MainMenuFragment {
            return MainMenuFragment()
        }
    }
}