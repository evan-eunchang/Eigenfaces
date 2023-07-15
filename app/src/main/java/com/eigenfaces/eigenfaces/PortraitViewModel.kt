package com.eigenfaces.eigenfaces

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eigenfaces.eigenfaces.db.Portrait
import com.eigenfaces.eigenfaces.db.PortraitDao
import kotlinx.coroutines.launch

class PortraitViewModel(private val dao : PortraitDao) : ViewModel() {
    val portraits = dao.getAllPortraits()
    val count = dao.getCount()

    fun insertPortrait(portrait : Portrait) = viewModelScope.launch {
        Log.i("ROOM_TAG", "Portrait was inserted at viewModel level")
        dao.insertPortrait(portrait)
    }

    fun updatePortrait(portrait : Portrait) = viewModelScope.launch {
        dao.updatePortrait(portrait)
    }

    fun deletePortrait(portrait : Portrait) = viewModelScope.launch {
        dao.deletePortrait(portrait)
    }
}