package com.example.thelastoutpost.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.thelastoutpost.database.dao.GameDao
import com.example.thelastoutpost.model.Achievement
import com.example.thelastoutpost.model.SaveSlot
import com.example.thelastoutpost.model.Skill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SaveSlot::class, Achievement::class, Skill::class], version = 2, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "last_outpost_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDatabase(database.gameDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDatabase(gameDao: GameDao) {
            val initialAchievements = listOf(
                Achievement("first_kill", "Primer Sangre", "Derrota a tu primer enemigo", true),
                Achievement("wave_10", "Superviviente I", "Llega a la oleada 10", false),
                Achievement("wave_50", "Veterano", "Llega a la oleada 50", false),
                Achievement("gold_1000", "Banquero", "Acumula 1000 de oro", false)
            )
            for (achievement in initialAchievements) {
                gameDao.insertAchievement(achievement)
            }
        }
    }
}
