package com.example.chessmovecalculator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import androidx.core.graphics.toColorInt

class ChessboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var boardSize = 0
    private var squareSize = 0f
    private val lightPaint = Paint().apply { color = "#F0D9B5".toColorInt() }
    private val darkPaint = Paint().apply { color = "#565857".toColorInt() }
    private val startPaint = Paint().apply { color = "#32DE8A".toColorInt() }
    private val endPaint = Paint().apply { color = "#CA1551".toColorInt() }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private var startX = -1
    private var startY = -1
    //    private var endY = 0
    private var endY = -1
    private var endX = -1
    
 
    private var listener: OnBoardInteractionListener? = null
    private val padding = 40f

    interface OnBoardInteractionListener {
        fun onSquareSelected(x: Int, y: Int)
    }

    fun setBoardInteractionListener(listener: OnBoardInteractionListener) {
        this.listener = listener
    }

    fun setBoardSize(size: Int) {
        boardSize = size
        requestLayout()
        reset()
    }

    fun setStart(x: Int, y: Int) {
        startX = x
        startY = y
        invalidate()
    }

    fun setEnd(x: Int, y: Int) {
        endX = x
        endY = y
        invalidate()
    }

    fun reset() {
        startX = -1
        startY = -1
        endX = -1
        endY = -1
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val smallerDim = min(measuredWidth, measuredHeight)
        squareSize = (smallerDim - padding) / boardSize
        textPaint.textSize = squareSize / 3
        setMeasuredDimension(smallerDim, smallerDim)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLabels(canvas)
        canvas.save()
        canvas.translate(padding, padding)
        drawBoard(canvas)
        drawSelections(canvas)
        canvas.restore()
    }

    private fun drawLabels(canvas: Canvas) {
        for (i in 0 until boardSize) {
            canvas.drawText(
                (boardSize - i).toString(),
                padding / 2,
                padding + (i * squareSize) + (squareSize / 2) + (textPaint.textSize / 3),
                textPaint
            )
            canvas.drawText(
                ('a' + i).toString(),
                padding + (i * squareSize) + (squareSize / 2),
                padding / 2 + (textPaint.textSize / 2),
                textPaint
            )
        }
    }

    private fun drawBoard(canvas: Canvas) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val paint = if ((row + col) % 2 == 0) lightPaint else darkPaint
                canvas.drawRect(
                    col * squareSize,
                    row * squareSize,
                    (col + 1) * squareSize,
                    (row + 1) * squareSize,
                    paint
                )
            }
        }
    }

    private fun drawSelections(canvas: Canvas) {
        if (startX != -1 && startY != -1) {
            canvas.drawRect(
                startX * squareSize,
                startY * squareSize,
                (startX + 1) * squareSize,
                (startY + 1) * squareSize,
                startPaint
            )
        }

        if (endX != -1 && endY != -1) {
            canvas.drawRect(
                endX * squareSize,
                endY * squareSize,
                (endX + 1) * squareSize,
                (endY + 1) * squareSize,
                endPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = ((event.x - padding) / squareSize).toInt()
            val y = ((event.y - padding) / squareSize).toInt()
            if (x in 0 until boardSize && y in 0 until boardSize) {
                listener?.onSquareSelected(x, y)
            }
            return true
        }
        return super.onTouchEvent(event)
    }
}