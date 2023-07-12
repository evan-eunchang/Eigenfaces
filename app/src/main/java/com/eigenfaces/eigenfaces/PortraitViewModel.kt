package com.eigenfaces.eigenfaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eigenfaces.eigenfaces.db.Portrait
import com.eigenfaces.eigenfaces.db.PortraitDao
import kotlinx.coroutines.launch

class PortraitViewModel(private val dao : PortraitDao) : ViewModel() {
    val students = dao.getAllStudents()
    val count = dao.getCount()

    fun insertStudent(portrait : Portrait) = viewModelScope.launch {
        dao.insertPortrait(portrait)
    }

    fun updateStudent(portrait : Portrait) = viewModelScope.launch {
        dao.updatePortrait(portrait)
    }

    fun deleteStudent(portrait : Portrait) = viewModelScope.launch {
        dao.deletePortrait(portrait)
    }
}