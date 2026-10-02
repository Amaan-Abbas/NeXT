package com.example.next.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.next.data.local.dao.HomeDao
import com.example.next.data.local.dao.PomodoroSessionDao
import com.example.next.data.local.dao.TaskDao
import com.example.next.data.local.entity.HomeItemEntity
import com.example.next.data.local.entity.PomodoroSessionEntity
import com.example.next.data.local.entity.TaskEntity
import com.example.next.features.tasks.domain.model.TaskPriority

@Database(
    entities = [TaskEntity::class, HomeItemEntity::class, PomodoroSessionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun homeDao(): HomeDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "next_app.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            populateInitialSeedData(db)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun populateInitialSeedData(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            val highPriority = TaskPriority.HIGH.name
            val mediumPriority = TaskPriority.MEDIUM.name
            val lowPriority = TaskPriority.LOW.name

            val sql1 = """
                INSERT INTO tasks (id, title, description, isCompleted, priority, createdAt, updatedAt, completedAt, estimatedPomodoroSessions, completedPomodoroSessions)
                VALUES ('task-1', 'Design System Architecture', 'Define core color tokens, typography, and reusable components.', 1, '$highPriority', ${now - 86400000L * 2}, ${now - 86400000L}, ${now - 86400000L}, 4, 4);
            """.trimIndent()

            val sql2 = """
                INSERT INTO tasks (id, title, description, isCompleted, priority, createdAt, updatedAt, completedAt, estimatedPomodoroSessions, completedPomodoroSessions)
                VALUES ('task-2', 'Implement Persistent Room Data Layer', 'Build Task entity, DAO, Database integration, and Mappers.', 0, '$highPriority', ${now - 3600000L * 5}, ${now - 3600000L * 5}, NULL, 3, 1);
            """.trimIndent()

            val sql3 = """
                INSERT INTO tasks (id, title, description, isCompleted, priority, createdAt, updatedAt, completedAt, estimatedPomodoroSessions, completedPomodoroSessions)
                VALUES ('task-3', 'Setup Pomodoro Timer Core', 'Create countdown timer state machine and session recorder.', 0, '$mediumPriority', ${now - 3600000L * 2}, ${now - 3600000L * 2}, NULL, 2, 0);
            """.trimIndent()

            val sql4 = """
                INSERT INTO tasks (id, title, description, isCompleted, priority, createdAt, updatedAt, completedAt, estimatedPomodoroSessions, completedPomodoroSessions)
                VALUES ('task-4', 'Write Unit Tests for Persistence', 'Ensure task DAO operations and Mappers are thoroughly tested.', 0, '$lowPriority', ${now - 1800000L}, ${now - 1800000L}, NULL, 2, 0);
            """.trimIndent()

            db.execSQL(sql1)
            db.execSQL(sql2)
            db.execSQL(sql3)
            db.execSQL(sql4)
        }
    }
}
