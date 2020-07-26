package org.amfoss.paneeer.editor.utils

class Matrix3() {
    private val data: FloatArray

    constructor(values: FloatArray) : this() {
        this.values = values
    }

    var values: FloatArray
        get() {
            val retValues = FloatArray(9)
            System.arraycopy(data, 0, retValues, 0, 9)
            return retValues
        }
        set(values) {
            var i = 0
            val len = values.size
            while (i < len) {
                data[i] = values[i]
                i++
            }
        }

    fun copy(): Matrix3 {
        return Matrix3(values)
    }

    /**
     * Two matrix multiplication
     *
     * @param m
     */
    fun multiply(m: Matrix3) {
        val ma = copy().values
        val mb = m.copy().values
        data[0] = ma[0] * mb[0] + ma[1] * mb[3] + ma[2] * mb[6]
        data[1] = ma[0] * mb[1] + ma[1] * mb[4] + ma[2] * mb[7]
        data[2] = ma[0] * mb[2] + ma[1] * mb[5] + ma[2] * mb[8]
        data[3] = ma[3] * mb[0] + ma[4] * mb[3] + ma[5] * mb[6]
        data[4] = ma[3] * mb[1] + ma[4] * mb[4] + ma[5] * mb[7]
        data[5] = ma[3] * mb[2] + ma[4] * mb[5] + ma[5] * mb[8]
        data[6] = ma[6] * mb[0] + ma[7] * mb[3] + ma[8] * mb[6]
        data[7] = ma[6] * mb[1] + ma[7] * mb[4] + ma[8] * mb[7]
        data[8] = ma[6] * mb[2] + ma[7] * mb[5] + ma[8] * mb[8]
    }

    /**
     * Seeking current Inverse Matrix
     *
     * @return
     */
    fun inverseMatrix(): Matrix3 {
        val m = copy().values
        val sx = m[0]
        val sy = m[4]
        m[0] = 1 / sx
        m[1] = 0F
        m[2] = -1 * (data[2] / sx)
        m[3] = 0F
        m[4] = 1 / sy
        m[5] = -1 * (data[5] / sy)
        m[6] = 0F
        m[7] = 0F
        m[8] = 1F
        return Matrix3(m)
    }

    init {
        data = FloatArray(9)
    }
}