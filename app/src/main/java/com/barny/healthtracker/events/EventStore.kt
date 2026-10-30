package com.barny.healthtracker.events

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "calendar_events", indices = [Index("dateKey")])
data class CalendarEvent(
    @PrimaryKey val id: String,
    val dateKey: Int,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Dao
interface EventDao {
    @Query("SELECT * FROM calendar_events WHERE dateKey BETWEEN :first AND :last ORDER BY dateKey, createdAt, id")
    fun observeMonth(first: Int, last: Int): Flow<List<CalendarEvent>>

    @Insert
    suspend fun insert(event: CalendarEvent)

    @Update
    suspend fun update(event: CalendarEvent): Int

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun delete(id: String)
}

@Database(entities = [CalendarEvent::class], version = 1, exportSchema = true)
abstract class EventDatabase : RoomDatabase() {
    abstract fun events(): EventDao

    companion object {
        @Volatile private var instance: EventDatabase? = null
        fun get(context: Context): EventDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, EventDatabase::class.java, "calendar_events.db"
            ).build().also { instance = it }
        }
    }
}
