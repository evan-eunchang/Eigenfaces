package com.eigenfaces.eigenfaces

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.eigenfaces.eigenfaces.databinding.ActivityMainBinding
import com.eigenfaces.eigenfaces.db.PortraitDao
import com.eigenfaces.eigenfaces.db.PortraitDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize data access object
        dao = PortraitDatabase.getInstance(application).portraitDao()
    }
    companion object {
        //this data access object will be used for the rest of this lifecycle by every fragment
        var dao : PortraitDao? = null
    }

}