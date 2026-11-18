package com.example.campushub.util

import android.graphics.Bitmap
import android.graphics.Color

object QrCodeGenerator {

    fun gerarBitmap(payload: String, size: Int = 512): Bitmap {
        val matrixSize = 25
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }

        fun drawFinderPattern(rowStart: Int, colStart: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isOuter = (r == 0 || r == 6 || c == 0 || c == 6)
                    val isInner = (r in 2..4 && c in 2..4)
                    matrix[rowStart + r][colStart + c] = (isOuter || isInner)
                }
            }
        }

        drawFinderPattern(0, 0)
        drawFinderPattern(0, matrixSize - 7)
        drawFinderPattern(matrixSize - 7, 0)

        for (i in 7 until matrixSize - 7) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        val bytes = payload.toByteArray(Charsets.UTF_8)
        var bitIndex = 0

        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                val inFinder1 = (r in 0..6 && c in 0..6)
                val inFinder2 = (r in 0..6 && c in (matrixSize - 7) until matrixSize)
                val inFinder3 = (r in (matrixSize - 7) until matrixSize && c in 0..6)
                val inTiming = (r == 6 || c == 6)

                if (!inFinder1 && !inFinder2 && !inFinder3 && !inTiming) {
                    val byteVal = if (bytes.isNotEmpty()) bytes[bitIndex % bytes.size].toInt() else 0
                    val shift = (bitIndex % 8)
                    val bit = ((byteVal shr shift) and 1) == 1
                    matrix[r][c] = bit
                    bitIndex++
                }
            }
        }

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val scale = size / matrixSize

        for (r in 0 until size) {
            for (c in 0 until size) {
                val mr = (r / scale).coerceAtMost(matrixSize - 1)
                val mc = (c / scale).coerceAtMost(matrixSize - 1)
                val color = if (matrix[mr][mc]) Color.parseColor("#0F172A") else Color.WHITE
                bitmap.setPixel(c, r, color)
            }
        }

        return bitmap
    }
}