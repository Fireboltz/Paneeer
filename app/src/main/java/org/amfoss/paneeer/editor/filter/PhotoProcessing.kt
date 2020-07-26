package org.amfoss.paneeer.editor.filter

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat

object PhotoProcessing {
    private const val TAG = "PhotoProcessing"
    @JvmStatic
    fun processImage(
        bitmap: Bitmap,
        effectType: Int,
        `val`: Int
    ): Bitmap {
        val inputMat = Mat(bitmap.width, bitmap.height, CvType.CV_8UC3)
        val outputMat = Mat()
        Utils.bitmapToMat(bitmap, inputMat)
        if (!isEnhance(effectType)) nativeApplyFilter(
            effectType % 100, `val`, inputMat.nativeObjAddr, outputMat.nativeObjAddr
        ) else nativeEnhanceImage(
            effectType % 100, `val`, inputMat.nativeObjAddr, outputMat.nativeObjAddr
        )
        inputMat.release()
        if (outputMat != null) {
            val outbit = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                bitmap.config
            )
            Utils.matToBitmap(outputMat, outbit)
            outputMat.release()
            return outbit
        }
        return bitmap.copy(bitmap.config, true)
    }

    private external fun nativeApplyFilter(
        mode: Int,
        `val`: Int,
        inpAddr: Long,
        outAddr: Long
    )

    private external fun nativeEnhanceImage(
        mode: Int,
        `val`: Int,
        inpAddr: Long,
        outAddr: Long
    )

    private fun isEnhance(effectType: Int): Boolean {
        return effectType / 300 == 1
    }

    init {
        if (!OpenCVLoader.initDebug()) {
            Log.e("$TAG - Error", "Unable to load OpenCV")
        } else {
            System.loadLibrary("nativeimageprocessing")
        }
    }
}