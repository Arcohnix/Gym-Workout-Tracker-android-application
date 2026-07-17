package com.example.forgegym.data.sample

import com.example.forgegym.data.models.*

object ExerciseSeeder {
    fun getPredefinedExercises(): List<Exercise> {
        val exercises = mutableListOf<Exercise>()

        // --- CHEST ---
        exercises.add(Exercise(
            name = "Bench Press (Barbell)", 
            primaryMuscleGroup = MuscleGroup.CHEST, 
            equipment = Equipment.BARBELL, 
            difficulty = Difficulty.INTERMEDIATE,
            instructions = listOf(
                "Lie on a flat bench and grip the barbell slightly wider than shoulder-width.",
                "Lower the bar to your mid-chest while keeping your elbows at a 45-degree angle.",
                "Press the bar back up until your arms are fully extended."
            ),
            tips = listOf("Maintain a slight arch in your back.", "Drive your feet into the ground."),
            commonMistakes = listOf("Bouncing the bar off your chest.", "Flaring your elbows too wide."),
            recommendedRepRange = 5..8,
            recommendedSetRange = 3..5,
            restRecommendationSeconds = 120
        ))
        exercises.add(Exercise(
            name = "Incline Bench Press (Barbell)", 
            primaryMuscleGroup = MuscleGroup.CHEST, 
            equipment = Equipment.BARBELL, 
            difficulty = Difficulty.INTERMEDIATE,
            instructions = listOf(
                "Set the bench to a 30-45 degree incline.",
                "Grip the bar slightly wider than shoulder-width.",
                "Lower the bar to your upper chest.",
                "Press back up to full extension."
            ),
            recommendedRepRange = 8..12
        ))
        exercises.add(Exercise(
            name = "Dumbbell Fly", 
            primaryMuscleGroup = MuscleGroup.CHEST, 
            equipment = Equipment.DUMBBELL, 
            exerciseType = ExerciseType.ISOLATION,
            difficulty = Difficulty.BEGINNER,
            instructions = listOf(
                "Lie on a flat bench with dumbbells held above your chest.",
                "Lower the weights out to your sides in a wide arc with a slight bend in your elbows.",
                "Squeeze your chest to bring the weights back together."
            ),
            recommendedRepRange = 12..15
        ))
        exercises.add(Exercise(name = "Push Up", primaryMuscleGroup = MuscleGroup.CHEST, equipment = Equipment.BODYWEIGHT, exerciseType = ExerciseType.COMPOUND, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Chest Press Machine", primaryMuscleGroup = MuscleGroup.CHEST, equipment = Equipment.MACHINE, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Cable Crossover", primaryMuscleGroup = MuscleGroup.CHEST, equipment = Equipment.CABLE, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.INTERMEDIATE))
        exercises.add(Exercise(name = "Pec Deck Machine", primaryMuscleGroup = MuscleGroup.CHEST, equipment = Equipment.MACHINE, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.BEGINNER))

        // --- BACK ---
        exercises.add(Exercise(
            name = "Deadlift (Barbell)", 
            primaryMuscleGroup = MuscleGroup.BACK, 
            secondaryMuscleGroups = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CORE),
            equipment = Equipment.BARBELL, 
            difficulty = Difficulty.ADVANCED,
            instructions = listOf(
                "Stand with feet hip-width apart and the bar over your mid-foot.",
                "Bend at the hips and knees to grip the bar outside your legs.",
                "Keep your back flat and pull the bar up along your legs by extending your hips and knees.",
                "Stand tall at the top, then lower the bar under control."
            ),
            recommendedRepRange = 3..5,
            restRecommendationSeconds = 180
        ))
        exercises.add(Exercise(name = "Pull Up", primaryMuscleGroup = MuscleGroup.BACK, equipment = Equipment.BODYWEIGHT, exerciseType = ExerciseType.COMPOUND, difficulty = Difficulty.INTERMEDIATE))
        exercises.add(Exercise(name = "Bent Over Row (Barbell)", primaryMuscleGroup = MuscleGroup.BACK, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE))
        exercises.add(Exercise(name = "Lat Pulldown", primaryMuscleGroup = MuscleGroup.BACK, equipment = Equipment.CABLE, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Seated Cable Row", primaryMuscleGroup = MuscleGroup.BACK, equipment = Equipment.CABLE, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Back Extension", primaryMuscleGroup = MuscleGroup.BACK, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.BEGINNER))

        // --- SHOULDERS ---
        exercises.add(Exercise(name = "Overhead Press (Barbell)", primaryMuscleGroup = MuscleGroup.SHOULDERS, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE))
        exercises.add(Exercise(name = "Lateral Raise (Dumbbell)", primaryMuscleGroup = MuscleGroup.SHOULDERS, equipment = Equipment.DUMBBELL, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Arnold Press", primaryMuscleGroup = MuscleGroup.SHOULDERS, equipment = Equipment.DUMBBELL, difficulty = Difficulty.INTERMEDIATE))
        exercises.add(Exercise(name = "Face Pull", primaryMuscleGroup = MuscleGroup.SHOULDERS, secondaryMuscleGroups = listOf(MuscleGroup.BACK), equipment = Equipment.CABLE, difficulty = Difficulty.BEGINNER))

        // --- LEGS ---
        exercises.add(Exercise(name = "Squat (Barbell)", primaryMuscleGroup = MuscleGroup.QUADRICEPS, secondaryMuscleGroups = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS), equipment = Equipment.BARBELL, difficulty = Difficulty.ADVANCED))
        exercises.add(Exercise(name = "Leg Press", primaryMuscleGroup = MuscleGroup.QUADRICEPS, equipment = Equipment.MACHINE, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Leg Extension", primaryMuscleGroup = MuscleGroup.QUADRICEPS, equipment = Equipment.MACHINE, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Leg Curl", primaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipment = Equipment.MACHINE, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Romanian Deadlift", primaryMuscleGroup = MuscleGroup.HAMSTRINGS, equipment = Equipment.BARBELL, difficulty = Difficulty.INTERMEDIATE))
        exercises.add(Exercise(name = "Calf Raise", primaryMuscleGroup = MuscleGroup.CALVES, equipment = Equipment.MACHINE, difficulty = Difficulty.BEGINNER))

        // --- ARMS ---
        exercises.add(Exercise(name = "Bicep Curl (Dumbbell)", primaryMuscleGroup = MuscleGroup.BICEPS, equipment = Equipment.DUMBBELL, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Hammer Curl", primaryMuscleGroup = MuscleGroup.BICEPS, equipment = Equipment.DUMBBELL, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Tricep Pushdown", primaryMuscleGroup = MuscleGroup.TRICEPS, equipment = Equipment.CABLE, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Skull Crusher", primaryMuscleGroup = MuscleGroup.TRICEPS, equipment = Equipment.BARBELL, exerciseType = ExerciseType.ISOLATION, difficulty = Difficulty.INTERMEDIATE))

        // --- CORE ---
        exercises.add(Exercise(name = "Plank", primaryMuscleGroup = MuscleGroup.CORE, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Crunch", primaryMuscleGroup = MuscleGroup.CORE, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Hanging Leg Raise", primaryMuscleGroup = MuscleGroup.CORE, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.INTERMEDIATE))

        // --- MOBILITY & CARDIO ---
        exercises.add(Exercise(name = "Cat Cow", primaryMuscleGroup = MuscleGroup.MOBILITY, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "World's Greatest Stretch", primaryMuscleGroup = MuscleGroup.MOBILITY, equipment = Equipment.BODYWEIGHT, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Running", primaryMuscleGroup = MuscleGroup.CARDIO, equipment = Equipment.OTHER, difficulty = Difficulty.BEGINNER))
        exercises.add(Exercise(name = "Jump Rope", primaryMuscleGroup = MuscleGroup.CARDIO, equipment = Equipment.OTHER, difficulty = Difficulty.BEGINNER))

        // Generate more to reach higher count
        val variations = listOf("Single Arm ", "Incline ", "Decline ", "Smith Machine ", "Weighted ", "Seated ", "Standing ", "Machine ", "Cable ")
        val baseExercises = exercises.toList()
        
        for (variation in variations) {
            for (base in baseExercises) {
                if (exercises.size >= 500) break
                exercises.add(base.copy(id = java.util.UUID.randomUUID().toString(), name = variation + base.name))
            }
        }

        return exercises
    }
}
