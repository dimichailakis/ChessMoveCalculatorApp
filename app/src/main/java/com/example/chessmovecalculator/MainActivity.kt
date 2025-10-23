package com.example.chessmovecalculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var usersizeInput: EditText
    private lateinit var maxMovesInput: EditText
    private lateinit var chessboardView: ChessboardView
    private lateinit var resetButton: Button


    private lateinit var pathsRecyclerView: RecyclerView
    private lateinit var statusTextView: TextView

    private lateinit var pathAdapter: PathAdapter

    private var endX = -1
    private var startX = -1
    private var startY = -1
    private var endY = -1
    private var boardSize = 0

    private val move_limit = 8

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usersizeInput = findViewById(R.id.usersizeInput)
        maxMovesInput = findViewById(R.id.maxMovesInput)
        chessboardView = findViewById(R.id.chessboardView)

        resetButton = findViewById(R.id.resetButton)
        pathsRecyclerView = findViewById(R.id.pathsRecyclerView)
        statusTextView = findViewById(R.id.statusTextView)

        setupRecyclerView()


        setupListeners()
        //        setupRecyclerView()
        restoreState()
    }

    private fun setupRecyclerView() { pathAdapter = PathAdapter(emptyList())
        pathsRecyclerView.adapter = pathAdapter
//        pathsRecyclerView.layoutManager = LinearLayoutCompat

        pathsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        usersizeInput.addTextChangedListener(object : TextWatcher { override fun afterTextChanged(s: Editable?) {
                val sizeStr = s.toString()
                if (sizeStr.isNotEmpty()) {
                    val size = sizeStr.toIntOrNull()


                    if (size != null && size in 6..16) {
                        boardSize = size
                        chessboardView.setBoardSize(boardSize)
                        reset()
                        usersizeInput.error = null
                    } else { usersizeInput.error = "Size must be 6-16"
                    }
                } else {
                    boardSize = 0
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(set: Set) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        resetButton.setOnClickListener{
            reset()
        }

        chessboardView.setBoardInteractionListener(object : ChessboardView.OnBoardInteractionListener {
            override fun onSquareSelected(x: Int, y: Int) {

                handleSquareSelection(x, y)
            }
        })
    }

    private fun handleSquareSelection(x: Int, y: Int) {
        if (boardSize == 0) {
            usersizeInput.error = "Please set a board size first! Pick a number between 6 and 16"
            return
        }

        if (startX == -1) {

            startY = y

            startX = x

            chessboardView.setStart(x, y)
            statusTextView.text = getString(R.string.status_select_end, toAlgebraic(x, y))
        } else if (endX == -1) {
            endY = y

            endX = x

            chessboardView.setEnd(x, y)

            val maxMovesStr = maxMovesInput.text.toString()
            if (maxMovesStr.isEmpty()) { maxMovesInput.error = "Set max moves"
                chessboardView.setEnd(-1,-1)
                endX = -1
                return
            }

            val maxMoves = maxMovesStr.toInt()
            if (maxMoves > move_limit) {
                showHighMoveCountWarning(maxMoves)
            } else { statusTextView.text = getString(R.string.status_calculating, toAlgebraic(startX, startY), toAlgebraic(x, y))
                calculatePaths(maxMoves)
            }
        }
    }

    private fun showHighMoveCountWarning(maxMoves: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.warning_title)
            .setMessage(getString(R.string.warning_message, move_limit))
            .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                statusTextView.text = getString(R.string.status_calculating, toAlgebraic(startX, startY), toAlgebraic(endX, endY))
                calculatePaths(maxMoves)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_no) { dialog, _ ->
                endX = -1
                endY = -1
                chessboardView.setEnd(-1, -1)
                statusTextView.text = getString(R.string.status_select_end, toAlgebraic(startX, startY))
                dialog.dismiss()
            }
            .show()
    }

    private fun toAlgebraic(x: Int, y: Int): String {
        return "${'a' + x}${boardSize - y}"
    }


    private fun calculatePaths(maxMoves: Int) {
        lifecycleScope.launch(Dispatchers.Default) {

            val finderThing = KnightPathfinder(boardSize, maxMoves)
            val allPaths = finderThing.findAllPaths(startX, startY, endX, endY)


            withContext(Dispatchers.Main) {
                displayPaths(allPaths, maxMoves)
                saveState(allPaths)
            }
        }
    }


    private fun reset() {
        startX = -1
        endY = -1
        startY = -1
        endX = -1

        chessboardView.reset()
        statusTextView.text = getString(R.string.initial_status_text)
        pathAdapter.updatePaths(emptyList())
    }

    private fun displayPaths(paths: List<List<Pair<Int, Int>>>, maxMoves: Int) {
        val organizedPaths = paths.sortedBy { it.size }
        val formattedPaths = organizedPaths.map { p ->
            p.joinToString(" -> ") { (x, y) -> toAlgebraic(x, y) }
        }


        if (formattedPaths.isEmpty()) {
            statusTextView.text = getString(
                R.string.status_no_solution,
                toAlgebraic(startX, startY),
                toAlgebraic(endX, endY),
                maxMoves.toString()
            )
            pathAdapter.updatePaths(emptyList())
        } else {

            statusTextView.text = resources.getQuantityString(
                R.plurals.status_solutions_found,
                formattedPaths.size,
                formattedPaths.size
            )
            pathAdapter.updatePaths(formattedPaths)
        }
    }

    private fun saveState(paths: List<List<Pair<Int, Int>>>) {
        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        val textifiedPaths = paths.map { path ->
            path.joinToString(" -> ") { (x, y) -> toAlgebraic(x, y) }
        }


        with(sharedPrefs.edit()) {
            putInt("boardSize", boardSize)

            val movesText = maxMovesInput.text.toString()
            val maxMoves = movesText.toIntOrNull()
            if (maxMoves != null) {
                putInt("maxMoves", maxMoves)
            } else {
                remove("maxMoves")
            }

            putInt("startX", startX)
            putInt("startY", startY)
            putInt("endX", endX)
            putInt("endY", endY)

            putStringSet("paths", textifiedPaths.toSet())
            putString("statusText", statusTextView.text.toString())

            apply() // commit instantly (commit() might be safer but meh)
        }
    }


    private fun restoreState() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        boardSize = sharedPreferences.getInt("boardSize", 0)
        if (boardSize != 0) {
            usersizeInput.setText(boardSize.toString())
            chessboardView.setBoardSize(boardSize)
        }

        val maxMoves = sharedPreferences.getInt("maxMoves", -1)
        if (maxMoves != -1) {
            maxMovesInput.setText(maxMoves.toString())
        }
        
        startX = sharedPreferences.getInt("startX", -1)
        startY = sharedPreferences.getInt("startY", -1)
        endX = sharedPreferences.getInt("endX", -1)
        endY = sharedPreferences.getInt("endY", -1)
        val savedPaths = sharedPreferences.getStringSet("paths", emptySet())?.toList()?.sortedBy { it.length } ?: emptyList()
        val statusText = sharedPreferences.getString("statusText", getString(R.string.initial_status_text))

        if (startX != -1) chessboardView.setStart(startX, startY)
        if (endX != -1) chessboardView.setEnd(endX, endY)
        pathAdapter.updatePaths(savedPaths)
        statusTextView.text = statusText
    }
}