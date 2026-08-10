package com.ysompo.englishwords.ui.common

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.random.Random

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val emojis = listOf("⭐", "🎉", "✨", "🏆")
    private val activeAnimators = mutableListOf<AnimatorSet>()

    fun burst(particleCount: Int = 16) {
        val random = Random(System.currentTimeMillis())
        repeat(particleCount) {
            val particle = TextView(context).apply {
                text = emojis.random(random)
                textSize = 20f
            }
            val params = LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.CENTER
            addView(particle, params)

            val angle = random.nextDouble(0.0, 2 * Math.PI)
            val distance = 300f + random.nextFloat() * 200f
            val endX = (Math.cos(angle) * distance).toFloat()
            val endY = (Math.sin(angle) * distance).toFloat()

            val moveX = ObjectAnimator.ofFloat(particle, "translationX", 0f, endX)
            val moveY = ObjectAnimator.ofFloat(particle, "translationY", 0f, endY)
            val fade = ObjectAnimator.ofFloat(particle, "alpha", 1f, 0f)

            val animatorSet = AnimatorSet()
            animatorSet.apply {
                playTogether(moveX, moveY, fade)
                duration = 900
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        removeView(particle)
                        activeAnimators.remove(animatorSet)
                    }
                })
            }
            activeAnimators.add(animatorSet)
            animatorSet.start()
        }
    }

    override fun onDetachedFromWindow() {
        activeAnimators.toList().forEach { it.cancel() }
        activeAnimators.clear()
        super.onDetachedFromWindow()
    }
}
