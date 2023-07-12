package com.eigenfaces.eigenfaces

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.eigenfaces.eigenfaces.databinding.FragmentDisplayResultsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.kotlinx.multik.api.Multik
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.toNDArray
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import java.util.Scanner

/**
 * A simple [Fragment] subclass.
 * Use the [DisplayResultsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
const val PORTRAIT_HEIGHT = 112
const val PORTRAIT_WIDTH = 92
const val NUM_PIXELS = 10304
const val NUM_COORDS = 400
class DisplayResultsFragment : Fragment() {
    private lateinit var binding : FragmentDisplayResultsBinding
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val portraitViewModel by activityViewModels<PortraitViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDisplayResultsBinding.inflate(inflater, container, false)
        binding.ivInput.setImageBitmap(mainActivityViewModel.faceBitmap)
        val start = System.currentTimeMillis()
        CoroutineScope(Dispatchers.IO).launch {
            val faceMatrix = loadFaceToAnalyze()
            val avgFace = loadAvgFaces()
            val faceNoAvg = faceMatrix - avgFace
            val coordinates = getCoordinates(faceNoAvg)
            Log.i("STREAM_TAG", (System.currentTimeMillis() - start).toString())
            doneLoading()
        }
        return binding.root
    }

    private fun getCoordinates(faceNoAvg : D1Array<Float>) : D1Array<Float> {
        val toReturn = FloatArray(NUM_COORDS)
        val vFaces = loadVFaces()
        for (i in 0 until NUM_COORDS) {
            for (j in 0 until NUM_PIXELS) {
                toReturn[i] += faceNoAvg[j] * vFaces[j][i]
            }
        }
        return Multik.ndarray(toReturn)
    }

    private fun loadVFaces() : D2Array<Float> {
        val array = Array(NUM_PIXELS) {FloatArray(NUM_COORDS)}
        val istream = resources.openRawResource(R.raw.vfaces)
        val scanner = Scanner(istream)
        var line: String
       // val start = System.currentTimeMillis()
        var row = 0
        while (scanner.hasNextLine()) {
            line = scanner.nextLine()
            val list = line.split(',')
            for (i in list.indices) {
                array[row][i] = list[i].toFloat()
            }
            row++
        }
//        Log.i("STREAM_TAG", (System.currentTimeMillis() - start).toString())
        istream.close()
        scanner.close()
        return Multik.ndarray(array)
    }

    private suspend fun doneLoading() {
        withContext(Dispatchers.Main) {
            binding.btnNo.visibility = View.VISIBLE
            binding.btnYes.visibility = View.VISIBLE
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvOutput.text = "Closest Match"
        }
    }
    private fun loadAvgFaces() : D1Array<Float> {
        val array = FloatArray(NUM_PIXELS)
        val istream = resources.openRawResource(R.raw.face_avg)
        val scanner = Scanner(istream)
        var line: String
        //val start = System.currentTimeMillis()
        var row = 0
        while (scanner.hasNextLine()) {
            line = scanner.nextLine()
            array[row] = line.toFloat()
            row++
        }
//        Log.i("STREAM_TAG", (System.currentTimeMillis() - start).toString())
        istream.close()
        scanner.close()
        return Multik.ndarray(array)
    }

    private fun loadFaceToAnalyze() : D1Array<Float> {
        val faceArrayInt = IntArray(NUM_PIXELS)
        val faceArrayFloat = FloatArray(NUM_PIXELS)
        val preppedBitmap = mainActivityViewModel.faceBitmap
        preppedBitmap.getPixels(faceArrayInt, 0, preppedBitmap.width, 0, 0, preppedBitmap.width, preppedBitmap.height)
        for (i in faceArrayInt.indices) {
            faceArrayFloat[i] = Color.red(faceArrayInt[i]).toFloat()
        }
        return Multik.ndarray(faceArrayFloat)
    }

}