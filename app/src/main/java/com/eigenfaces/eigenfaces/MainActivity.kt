package com.eigenfaces.eigenfaces

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.eigenfaces.eigenfaces.databinding.ActivityMainBinding
import com.eigenfaces.eigenfaces.db.PortraitDao
import com.eigenfaces.eigenfaces.db.PortraitDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    //private lateinit var portraitViewModel: PortraitViewModel
    //var dao : PortraitDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        dao = PortraitDatabase.getInstance(application).portraitDao()
        //val factory = PortraitViewModelFactory(dao!!)
        //portraitViewModel = ViewModelProvider(this, factory)[PortraitViewModel::class.java]
    }
    companion object {
        var dao : PortraitDao? = null
    }

}