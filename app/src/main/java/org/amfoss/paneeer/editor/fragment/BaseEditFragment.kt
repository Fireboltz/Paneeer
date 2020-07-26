package org.amfoss.paneeer.editor.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.amfoss.paneeer.Paneeer
import org.amfoss.paneeer.editor.EditImageActivity

abstract class BaseEditFragment : Fragment() {
    @JvmField
    protected var activity: EditImageActivity? = null
    protected fun ensureEditActivity(): EditImageActivity? {
        if (activity == null) {
            activity = getActivity() as EditImageActivity?
        }
        return activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ensureEditActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        Paneeer.getRefWatcher(getActivity()).watch(this)
    }

    abstract fun onShow()
}