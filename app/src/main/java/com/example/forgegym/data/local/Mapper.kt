package com.example.forgegym.data.local

import com.example.forgegym.data.local.entity.*
import com.example.forgegym.data.models.*

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    primaryMuscleGroup = primaryMuscleGroup,
    secondaryMuscleGroups = secondaryMuscleGroups,
    equipment = equipment,
    difficulty = difficulty,
    category = category,
    exerciseType = exerciseType,
    forceType = forceType,
    mechanics = mechanics,
    instructions = instructions,
    commonMistakes = commonMistakes,
    tips = tips,
    restRecommendationSeconds = restRecommendationSeconds,
    estimatedCaloriesBurnedPerMin = estimatedCaloriesBurnedPerMin,
    recommendedRepRange = minRecommendedReps..maxRecommendedReps,
    recommendedSetRange = minRecommendedSets..maxRecommendedSets,
    isBeginnerFriendly = isBeginnerFriendly,
    videoUrl = videoUrl,
    isCustom = isCustom,
    isFavorite = isFavorite,
    notes = notes
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    primaryMuscleGroup = primaryMuscleGroup,
    secondaryMuscleGroups = secondaryMuscleGroups,
    equipment = equipment,
    difficulty = difficulty,
    category = category,
    exerciseType = exerciseType,
    forceType = forceType,
    mechanics = mechanics,
    instructions = instructions,
    commonMistakes = commonMistakes,
    tips = tips,
    restRecommendationSeconds = restRecommendationSeconds,
    estimatedCaloriesBurnedPerMin = estimatedCaloriesBurnedPerMin,
    minRecommendedReps = recommendedRepRange.first,
    maxRecommendedReps = recommendedRepRange.last,
    minRecommendedSets = recommendedSetRange.first,
    maxRecommendedSets = recommendedSetRange.last,
    isBeginnerFriendly = isBeginnerFriendly,
    videoUrl = videoUrl,
    isCustom = isCustom,
    isFavorite = isFavorite,
    notes = notes
)

fun WorkoutExerciseWithExercise.toDomain(): WorkoutExercise = WorkoutExercise(
    id = crossRef.id,
    exercise = exercise.toDomain(),
    order = crossRef.order,
    restTimeSeconds = crossRef.restTimeSeconds,
    note = crossRef.note,
    supersetId = crossRef.supersetId
)

fun WorkoutExercise.toEntity(workoutId: String): WorkoutExerciseCrossRef = WorkoutExerciseCrossRef(
    id = id,
    workoutId = workoutId,
    exerciseId = exercise.id,
    order = order,
    restTimeSeconds = restTimeSeconds,
    note = note,
    supersetId = supersetId
)

fun WorkoutWithExercises.toDomain(): Workout = Workout(
    id = workout.id,
    name = workout.name,
    description = workout.description,
    category = workout.category,
    difficulty = workout.difficulty,
    estimatedDurationMinutes = workout.estimatedDurationMinutes,
    lastPerformedAt = workout.lastPerformedAt,
    createdAt = workout.createdAt,
    workoutExercises = exercises.map { it.toDomain() }.sortedBy { it.order }
)

fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = id,
    name = name,
    description = description,
    category = category,
    difficulty = difficulty,
    estimatedDurationMinutes = estimatedDurationMinutes,
    lastPerformedAt = lastPerformedAt,
    createdAt = createdAt
)

fun CompletedSetEntity.toDomain(): CompletedSet = CompletedSet(
    id = id,
    weight = weight,
    reps = reps,
    note = note,
    isWarmup = isWarmup,
    isPersonalRecord = isPersonalRecord,
    timestamp = timestamp
)

fun CompletedSet.toEntity(sessionId: String, exerciseId: String): CompletedSetEntity = CompletedSetEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    weight = weight,
    reps = reps,
    note = note,
    isWarmup = isWarmup,
    isPersonalRecord = isPersonalRecord,
    timestamp = timestamp
)

fun SessionWithSets.toDomain(exerciseNames: Map<String, String>): WorkoutSession {
    val groupedSets = sets.groupBy { it.exerciseId }
    val exerciseSessions = groupedSets.map { (exerciseId, setEntities) ->
        ExerciseSession(
            exerciseId = exerciseId,
            exerciseName = exerciseNames[exerciseId] ?: "Unknown Exercise",
            sets = setEntities.map { it.toDomain() }
        )
    }

    return WorkoutSession(
        id = session.id,
        workoutId = session.workoutId,
        workoutName = session.workoutName,
        startTime = session.startTime,
        endTime = session.endTime,
        note = session.note,
        exercises = exerciseSessions,
        totalVolume = session.totalVolume,
        currentExerciseIndex = session.currentExerciseIndex,
        isPaused = session.isPaused,
        lastPausedAt = session.lastPausedAt,
        totalPausedDurationMillis = session.totalPausedDurationMillis
    )
}

fun WorkoutSession.toEntity(): WorkoutSessionEntity = WorkoutSessionEntity(
    id = id,
    workoutId = workoutId,
    workoutName = workoutName,
    startTime = startTime,
    endTime = endTime,
    note = note,
    totalVolume = totalVolume,
    currentExerciseIndex = currentExerciseIndex,
    isPaused = isPaused,
    lastPausedAt = lastPausedAt,
    totalPausedDurationMillis = totalPausedDurationMillis
)

fun PersonalRecordEntity.toDomain(): PersonalRecord = PersonalRecord(
    id = id,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    weight = weight,
    reps = reps,
    volume = volume,
    estimatedOneRepMax = estimatedOneRepMax,
    date = date,
    type = type
)

fun PersonalRecord.toEntity(): PersonalRecordEntity = PersonalRecordEntity(
    id = id,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    weight = weight,
    reps = reps,
    volume = volume,
    estimatedOneRepMax = estimatedOneRepMax,
    date = date,
    type = type
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    name = name,
    dateOfBirth = dateOfBirth,
    height = height,
    currentWeight = currentWeight,
    goalWeight = goalWeight,
    trainingGoal = TrainingGoal.valueOf(trainingGoal),
    profilePictureUrl = profilePictureUrl
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = 0,
    name = name,
    dateOfBirth = dateOfBirth,
    height = height,
    currentWeight = currentWeight,
    goalWeight = goalWeight,
    trainingGoal = trainingGoal.name,
    profilePictureUrl = profilePictureUrl
)

fun BodyMeasurementEntity.toDomain(): BodyMeasurement = BodyMeasurement(
    id = id,
    date = date,
    neck = neck,
    chest = chest,
    shoulders = shoulders,
    leftArm = leftArm,
    rightArm = rightArm,
    waist = waist,
    hips = hips,
    leftThigh = leftThigh,
    rightThigh = rightThigh,
    leftCalf = leftCalf,
    rightCalf = rightCalf
)

fun BodyMeasurement.toEntity(): BodyMeasurementEntity = BodyMeasurementEntity(
    id = id,
    date = date,
    neck = neck,
    chest = chest,
    shoulders = shoulders,
    leftArm = leftArm,
    rightArm = rightArm,
    waist = waist,
    hips = hips,
    leftThigh = leftThigh,
    rightThigh = rightThigh,
    leftCalf = leftCalf,
    rightCalf = rightCalf
)
