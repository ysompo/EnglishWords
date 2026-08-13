package com.ysompo.englishwords.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.ysompo.englishwords.R
import com.ysompo.englishwords.logic.DecorationType
import com.ysompo.englishwords.logic.LevelMapGeometry
import com.ysompo.englishwords.logic.LevelMapSceneryGenerator
import com.ysompo.englishwords.logic.LevelNodePosition

/**
 * Draws the winding "game level map": scenery (hills/trees/clouds) and numbered level nodes,
 * via Canvas primitives only (no image assets/animation library available in this project).
 * Non-trivial geometry lives in [LevelMapGeometry] / [LevelMapSceneryGenerator] so it stays
 * unit-testable outside onDraw/onTouchEvent.
 *
 * Tappability constraint: onTouchEvent only ever fires [listener] when the tapped node's level
 * equals currentLevel. Completed/locked nodes render but are structurally inert - there's no
 * way for LearnWordsActivity to start a specific past or future level, so we never pretend
 * otherwise.
 */
class LevelMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    fun interface Listener {
        fun onCurrentLevelTapped()
    }

    var listener: Listener? = null

    private var currentLevel = 1
    private var levelsCompleted = 0
    private var totalLevels = 1
    private var positions: List<LevelNodePosition> = emptyList()

    private val density = resources.displayMetrics.density
    private val nodeSpacingPx = 170f * density
    private val topPaddingPx = 90f * density
    private val bottomPaddingPx = 90f * density
    private val nodeRadiusPx = 30f * density
    private val currentNodeRadiusPx = 40f * density
    private val hitRadiusPx = 44f * density

    private val rubik = ResourcesCompat.getFont(context, R.font.rubik)

    // Note: DashPathEffect (the dashed road center-line below) silently degrades to a solid
    // stroke on hardware-accelerated Canvases below API 28 - it does not fail to draw. A
    // whole-view LAYER_TYPE_SOFTWARE was tried to force it everywhere, but that backs this
    // (tall, scrollable) view with an offscreen bitmap sized to its full content height, which
    // exceeds Android's software-layer size cap and made the entire view silently not draw -
    // worse than the cosmetic issue it was meant to fix. Left as hardware-accelerated.

    private val pathOutlinePaint = strokePaint(R.color.locked_gray, 24f)
    private val pathPaint = strokePaint(android.R.color.white, 18f).apply { color = Color.WHITE }
    private val pathDashPaint = strokePaint(R.color.gold_reward, 4f).apply {
        pathEffect = DashPathEffect(floatArrayOf(10f * density, 14f * density), 0f)
    }

    private val lockedFillPaint = solidPaint(R.color.locked_gray)
    private val completedFillPaint = solidPaint(R.color.success_green)
    private val currentFillPaint = solidPaint(R.color.coral_primary)
    private val currentGlowPaint = solidPaint(R.color.gold_reward).apply { alpha = 110 }

    private val hillPaintFar = solidPaint(R.color.teal_secondary_light)
    private val hillPaintNear = solidPaint(R.color.teal_secondary)
    private val treeCanopyPaint = solidPaint(R.color.success_green)
    private val treeTrunkPaint = solidPaint(R.color.text_charcoal_soft)
    private val cloudPaint = solidPaint(R.color.bg_card).apply { alpha = 200 }

    private val nodeTextPaintLight = textPaint(R.color.text_on_color, 20f)
    private val nodeTextPaintDark = textPaint(R.color.text_charcoal_soft, 18f)

    private fun solidPaint(colorRes: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, colorRes)
        style = Paint.Style.FILL
    }

    private fun strokePaint(colorRes: Int, widthDp: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, colorRes)
        style = Paint.Style.STROKE
        strokeWidth = widthDp * density
        strokeCap = Paint.Cap.ROUND
    }

    private fun textPaint(colorRes: Int, sizeSp: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, colorRes)
        textSize = sizeSp * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
        typeface = rubik
    }

    fun setState(currentLevel: Int, levelsCompleted: Int, totalLevels: Int) {
        this.currentLevel = currentLevel
        this.levelsCompleted = levelsCompleted
        this.totalLevels = totalLevels
        requestLayout()
        invalidate()
    }

    /** Local-coordinate Y of the current level's node, for HomeActivity's auto-scroll. */
    fun currentLevelNodeY(): Float? = positions.firstOrNull { it.level == currentLevel }?.centerYPx

    fun contentHeightPx(): Float =
        LevelMapGeometry.contentHeightPx(positions.size, nodeSpacingPx, topPaddingPx, bottomPaddingPx)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val window = LevelMapGeometry.windowRange(currentLevel, totalLevels)
        positions = LevelMapGeometry.nodePositions(window, nodeSpacingPx, topPaddingPx)
        val height = LevelMapGeometry.contentHeightPx(positions.size, nodeSpacingPx, topPaddingPx, bottomPaddingPx)
        setMeasuredDimension(width, height.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        if (positions.isEmpty()) return
        drawWindingPath(canvas)
        positions.forEach { drawScenery(canvas, it) }
        positions.forEach { drawNode(canvas, it) }
    }

    private fun drawWindingPath(canvas: Canvas) {
        val path = Path()
        val first = positions.first()
        path.moveTo(first.centerXFraction * width, first.centerYPx)
        for (i in 1 until positions.size) {
            val prev = positions[i - 1]
            val curr = positions[i]
            val prevX = prev.centerXFraction * width
            val currX = curr.centerXFraction * width
            val midY = (prev.centerYPx + curr.centerYPx) / 2f
            path.cubicTo(prevX, midY, currX, midY, currX, curr.centerYPx)
        }
        canvas.drawPath(path, pathOutlinePaint)
        canvas.drawPath(path, pathPaint)
        canvas.drawPath(path, pathDashPaint)
    }

    private fun drawScenery(canvas: Canvas, pos: LevelNodePosition) {
        LevelMapSceneryGenerator.decorationsFor(pos.level).forEach { decoration ->
            val cx = decoration.xFraction * width
            val cy = pos.centerYPx + decoration.yOffsetPx
            when (decoration.type) {
                DecorationType.HILL -> drawHill(canvas, cx, cy, decoration.scale)
                DecorationType.TREE -> drawTree(canvas, cx, cy, decoration.scale)
                DecorationType.CLOUD -> drawCloud(canvas, cx, cy, decoration.scale)
            }
        }
    }

    private fun drawHill(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val r = 46f * density * scale
        canvas.drawCircle(cx, cy + r * 0.4f, r, hillPaintFar)
        canvas.drawCircle(cx - r * 0.5f, cy + r * 0.6f, r * 0.7f, hillPaintNear)
    }

    private fun drawTree(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val trunkW = 6f * density * scale
        val trunkH = 22f * density * scale
        canvas.drawRect(cx - trunkW / 2f, cy, cx + trunkW / 2f, cy + trunkH, treeTrunkPaint)
        canvas.drawCircle(cx, cy - 6f * density * scale, 20f * density * scale, treeCanopyPaint)
    }

    private fun drawCloud(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val r = 16f * density * scale
        canvas.drawCircle(cx, cy, r, cloudPaint)
        canvas.drawCircle(cx + r * 0.9f, cy + r * 0.2f, r * 0.7f, cloudPaint)
        canvas.drawCircle(cx - r * 0.9f, cy + r * 0.2f, r * 0.7f, cloudPaint)
    }

    private fun drawNode(canvas: Canvas, pos: LevelNodePosition) {
        val cx = pos.centerXFraction * width
        val cy = pos.centerYPx
        val completed = pos.level <= levelsCompleted
        val isCurrent = pos.level == currentLevel

        when {
            isCurrent -> {
                canvas.drawCircle(cx, cy, currentNodeRadiusPx * 1.25f, currentGlowPaint)
                canvas.drawCircle(cx, cy, currentNodeRadiusPx, currentFillPaint)
                canvas.drawText(pos.level.toString(), cx, cy + nodeTextPaintLight.textSize / 3f, nodeTextPaintLight)
            }
            completed -> {
                canvas.drawCircle(cx, cy, nodeRadiusPx, completedFillPaint)
                canvas.drawText("✓", cx, cy + nodeTextPaintLight.textSize / 3f, nodeTextPaintLight)
            }
            else -> {
                canvas.drawCircle(cx, cy, nodeRadiusPx, lockedFillPaint)
                canvas.drawText(pos.level.toString(), cx, cy + nodeTextPaintDark.textSize / 3f, nodeTextPaintDark)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val tapped = LevelMapGeometry.levelAt(event.x, event.y, positions, width.toFloat(), hitRadiusPx)
            if (tapped != null && tapped == currentLevel) {
                performClick()
                listener?.onCurrentLevelTapped()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
