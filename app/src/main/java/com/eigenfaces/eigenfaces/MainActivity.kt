package com.eigenfaces.eigenfaces

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.kotlinx.multik.api.toNDArray
import java.util.Scanner


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var array = Array(10304) {FloatArray(400)}
        val istream = resources.openRawResource(R.raw.vfaces)
        val scanner = Scanner(istream)
        var line: String
        val start = System.currentTimeMillis()
        var row = 0
        while (scanner.hasNextLine()) {
            line = scanner.nextLine()
            val list = line.split(',')
            for (i in list.indices) {
                array[row][i] = list[i].toFloat()
            }
            row++
        }
        array.toNDArray()
        Log.i("STREAM_TAG", (System.currentTimeMillis() - start).toString())
        istream.close()
        scanner.close()
    }
}