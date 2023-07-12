package com.eigenfaces.eigenfaces.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array

@Entity(tableName = "portrait_data_table")
data class Portrait(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "portrait_id")
    var id : Int,
    @ColumnInfo(name = "portrait_name")
    var name : String,
    @ColumnInfo(name = "portrait_file_path")
    var filePath : String,
    @ColumnInfo(name = "portrait_coordinates")
    var coordinates : String
)