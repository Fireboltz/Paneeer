package org.amfoss.paneeer.editor.view.imagezoom.easing

class Cubic : Easing {
    override fun easeOut(
        time: Double,
        start: Double,
        end: Double,
        duration: Double
    ): Double {
        var time = time
        return end * ((time / duration - 1.0.also { time = it }) * time * time + 1.0) + start
    }

    override fun easeIn(
        time: Double,
        start: Double,
        end: Double,
        duration: Double
    ): Double {
        var time = time
        return end * duration.let { time /= it; time } * time * time + start
    }

    override fun easeInOut(
        time: Double,
        start: Double,
        end: Double,
        duration: Double
    ): Double {
        var time = time
        return if (duration / 2.0.let { time /= it; time } < 1.0) end / 2.0 * time * time * time + start else end / 2.0 * (2.0.let { time -= it; time } * time * time + 2.0) + start
    }
}