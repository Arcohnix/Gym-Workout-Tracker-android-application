# ForgeGym Database Schema

ForgeGym utilizes Room (SQLite) for high-performance, offline training data management.

## 📊 Tables

### 1. `workouts`
Defines routine templates created by the user.
*   `id` (String, PK): UUID.
*   `name` (String): Routine title.
*   `category` (Enum): Strength, Hypertrophy, etc.
*   `difficulty` (Enum): Beginner, Intermediate, Advanced.

### 2. `exercises`
The encyclopedia of physical movements.
*   `id` (String, PK): UUID.
*   `name` (String): Exercise title.
*   `primaryMuscleGroup` (Enum): Chest, Back, etc.
*   `equipment` (Enum): Barbell, Dumbbell, etc.
*   `instructions` (JSON): Step-by-step guide.

### 3. `workout_exercise_cross_ref`
Many-to-many relationship mapping Exercises into Workouts.
*   `workoutId` (String): Reference to routine.
*   `exerciseId` (String): Reference to exercise template.
*   `order` (Int): Sequence in the workout.
*   `restTimeSeconds` (Int): Default recovery time.

### 4. `workout_sessions`
Historical records of completed training sessions.
*   `id` (String, PK): UUID.
*   `workoutId` (String): Routine template used.
*   `startTime` (Long): Epoch timestamp.
*   `endTime` (Long, Optional): Null if session is active.
*   `totalVolume` (Double): Aggregated weight lifted.

### 5. `completed_sets`
Detailed performance data per exercise within a session.
*   `id` (String, PK).
*   `sessionId` (String): Link to parent session.
*   `exerciseId` (String): Link to exercise template.
*   `weight` (Double).
*   `reps` (Int).

### 6. `personal_records`
Automated tracking of training milestones.
*   `exerciseId` (String).
*   `type` (Enum): MAX_WEIGHT, MAX_REPS, ESTIMATED_1RM.
*   `weight`, `reps`, `volume`.

## 🚀 Performance
*   **Indices**: Added on `startTime`, `workoutId`, and `exerciseName` to ensure buttery-smooth history browsing for power users.
*   **Transactions**: All routine saves and session completions are performed atomically.
