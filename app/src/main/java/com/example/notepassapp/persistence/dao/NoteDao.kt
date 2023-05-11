package com.example.notepassapp.persistence.dao

import androidx.room.*
import com.example.notepassapp.persistence.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun getAllNotes(): Flow<List<NoteEntity>>
    @Insert
    suspend fun insertNote(noteEntity: NoteEntity):Long
    @Delete
    suspend fun deleteNote(noteEntity: NoteEntity):Int
    @Update
    suspend fun updateNote(noteEntity: NoteEntity):Int
}