package com.eigenfaces.eigenfaces

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    lateinit var faceBitmap: Bitmap
    lateinit var fileNameToSave: String
    lateinit var coordinatesToSave: String
}