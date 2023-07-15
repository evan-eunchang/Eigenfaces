package com.eigenfaces.eigenfaces.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PortraitDao {
    @Insert
    suspend fun insertPortrait(portrait : Portrait)

    @Update
    suspend fun updatePortrait(portrait : Portrait)

    @Delete
    suspend fun deletePortrait(portrait : Portrait)

    @Query("SELECT * FROM portrait_data_table")
    fun getAllPortraits(): LiveData<List<Portrait>>

    @Query("SELECT COUNT(*) FROM portrait_data_table")
    fun getCount() : LiveData<Int>
}