package com.example.notepassapp.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notepassapp.persistence.dao.NoteDao
import com.example.notepassapp.persistence.entity.NoteEntity

//Provee objecte database Singleton
object DatabaseProvider {
    private lateinit var database: NoteDatabase
    fun getDatabase(context: Context):NoteDatabase{
        synchronized(NoteDatabase::class){
            if (!::database.isInitialized){
                database = Room.databaseBuilder(context.applicationContext, NoteDatabase::class.java, "Notes_Database").build()
            }
        }
        return database
    }
}
@Database(entities = [NoteEntity::class], version = 1)
abstract class NoteDatabase:RoomDatabase(){
    abstract fun noteDao():NoteDao
}