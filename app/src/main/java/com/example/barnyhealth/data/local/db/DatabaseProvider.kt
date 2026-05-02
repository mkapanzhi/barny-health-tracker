package com.example.barnyhealth.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseProvider {

    @Volatile
    private var INSTANCE: BarnyHealthDatabase? = null

    fun get(context: Context): BarnyHealthDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                BarnyHealthDatabase::class.java,
                "barny_health.db"
            )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                DatabaseSeeder(database).seed()
                            }
                        }
                    }
                })
                .build()
                .also { INSTANCE = it }
        }
    }
}