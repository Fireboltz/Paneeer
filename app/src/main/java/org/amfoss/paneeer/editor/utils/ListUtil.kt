package org.amfoss.paneeer.editor.utils

object ListUtil {
    @JvmStatic
    fun isEmpty(list: List<*>?): Boolean {
        return if (list == null) true else list.size == 0
    }
}