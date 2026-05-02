package com.example.barnyhealth.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.barnyhealth.data.local.db.entity.NoteEntity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NoteEntity): Long

    @Query("""
        SELECT * FROM notes
        WHERE pet_id = :petId
        ORDER BY created_at DESC
    """)
    fun observeByPet(petId: Long): Flow<List<NoteEntity>>
}