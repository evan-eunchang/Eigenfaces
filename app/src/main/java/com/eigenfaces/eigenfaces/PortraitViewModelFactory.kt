package com.eigenfaces.eigenfaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eigenfaces.eigenfaces.db.PortraitDao

class PortraitViewModelFactory(
    private val dao: PortraitDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortraitViewModel::class.java)) {
            return PortraitViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}