package com.example.chessmovecalculator

class KnightPathfinder(
    private val boardSize: Int,
    private val maxMoves: Int
) {

    private val knightJumps = arrayOf(
        intArrayOf(1, 2), intArrayOf(2, 1),
        intArrayOf(2, -1), intArrayOf(1, -2),
        intArrayOf(-1, -2), intArrayOf(-2, -1),
        intArrayOf(-2, 1), intArrayOf(-1, 2)
    )

    fun findAllPaths(startX: Int, startY: Int, endX: Int, endY: Int): List<List<Pair<Int, Int>>> {
        val allFound = mutableListOf<List<Pair<Int, Int>>>()
        val pathNow = mutableListOf<Pair<Int, Int>>()
        pathNow.add(Pair(startX, startY))
        searchRecursively(startX, startY, endX, endY, 0, pathNow, allFound)
        return allFound
    }

    private fun searchRecursively(
        cx: Int,
        cy: Int,
        ex: Int,
        ey: Int,
        moveCount: Int,
        path: MutableList<Pair<Int, Int>>,
        results: MutableList<List<Pair<Int, Int>>>
    ) {
        if (cx == ex && cy == ey) {
            results.add(path.toList())
            return
        }

        if (moveCount >= maxMoves) return

        for (mv in knightJumps) {
            val nx = cx + mv[0]
            val ny = cy + mv[1]
            if (checkBounds(nx, ny)) {
                path.add(Pair(nx, ny))
                searchRecursively(nx, ny, ex, ey, moveCount + 1, path, results)
                path.removeAt(path.size - 1)
            }
        }
    }

    private fun checkBounds(x: Int, y: Int): Boolean {
        return (x >= 0 && x < boardSize && y >= 0 && y < boardSize)
    }

}