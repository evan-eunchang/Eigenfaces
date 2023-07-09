package com.eigenfaces.eigenfaces

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var inputBitmap = MutableLiveData<Bitmap>()


    fun setInputBitmap(input : Bitmap) {
        inputBitmap.value = input
    }
}