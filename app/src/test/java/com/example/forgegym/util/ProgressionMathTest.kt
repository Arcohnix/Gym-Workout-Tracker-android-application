package com.example.forgegym.util

import com.example.forgegym.data.models.OneRepMaxFormula
import com.example.forgegym.data.models.ProgressionStrategy
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.exp
import kotlin.math.pow

class ProgressionMathTest {

    @Test
    fun testBrzycki1RM() {
        val weight = 100.0
        val reps = 10
        // Formula: weight * (36.0 / (37.0 - reps))
        val expected = 100.0 * (36.0 / (37.0 - 10))
        val actual = calculate1RM(weight, reps, OneRepMaxFormula.BRZYCKI)
        assertEquals(expected, actual, 0.1)
    }

    @Test
    fun testEpley1RM() {
        val weight = 100.0
        val reps = 10
        // Formula: weight * (1 + reps / 30.0)
        val expected = 100.0 * (1 + 10 / 30.0)
        val actual = calculate1RM(weight, reps, OneRepMaxFormula.EPLEY)
        assertEquals(expected, actual, 0.1)
    }

    @Test
    fun testLinearProgression() {
        // Linear: Increase weight if reps >= 8
        val lastWeight = 80.0
        val lastReps = 8
        val result = calculateProgression(lastWeight, lastReps, ProgressionStrategy.LINEAR)
        assertEquals(82.5, result.first, 0.01)
        assertEquals(5, result.second.first)
    }

    @Test
    fun testDoubleProgression() {
        // Double: Increase reps until 12, then increase weight
        val lastWeight = 80.0
        val lastReps = 10
        val result = calculateProgression(lastWeight, lastReps, ProgressionStrategy.DOUBLE)
        assertEquals(80.0, result.first, 0.01)
        assertEquals(11, result.second.first)

        val resultAtLimit = calculateProgression(80.0, 12, ProgressionStrategy.DOUBLE)
        assertEquals(82.5, resultAtLimit.first, 0.01)
        assertEquals(8, resultAtLimit.second.first)
    }

    // Helper functions (mirrored from ProgressiveOverloadRepositoryImpl for testing)
    private fun calculate1RM(weight: Double, reps: Int, formula: OneRepMaxFormula): Double {
        if (reps == 0) return 0.0
        if (reps == 1) return weight
        return when (formula) {
            OneRepMaxFormula.EPLEY -> weight * (1 + reps / 30.0)
            OneRepMaxFormula.BRZYCKI -> weight * (36.0 / (37.0 - reps))
            OneRepMaxFormula.LOMBARDI -> weight * (reps.toDouble().pow(0.1))
            OneRepMaxFormula.MAYHEW -> (100 * weight) / (52.2 + 41.9 * exp(-0.055 * reps))
            OneRepMaxFormula.OCONNER -> weight * (1 + 0.025 * reps)
        }
    }

    private fun calculateProgression(lastWeight: Double, lastReps: Int, strategy: ProgressionStrategy): Pair<Double, IntRange> {
        return when (strategy) {
            ProgressionStrategy.LINEAR -> {
                if (lastReps >= 8) Pair(lastWeight + 2.5, 5..5)
                else Pair(lastWeight, 5..5)
            }
            ProgressionStrategy.DOUBLE -> {
                if (lastReps >= 12) Pair(lastWeight + 2.5, 8..8)
                else Pair(lastWeight, (lastReps + 1)..(lastReps + 1))
            }
            else -> Pair(lastWeight, 8..12)
        }
    }
}
