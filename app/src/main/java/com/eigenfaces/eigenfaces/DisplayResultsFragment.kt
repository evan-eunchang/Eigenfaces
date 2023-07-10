package com.eigenfaces.eigenfaces

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.eigenfaces.eigenfaces.databinding.FragmentDisplayResultsBinding
import org.jetbrains.kotlinx.multik.api.Multik
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.toNDArray
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import java.util.Scanner

/**
 * A simple [Fragment] subclass.
 * Use the [DisplayResultsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
const val PORTRAIT_HEIGHT = 112
const val PORTRAIT_WIDTH = 92
const val NUM_COORDS = 400
class DisplayResultsFragment : Fragment() {
    private lateinit var binding : FragmentDisplayResultsBinding
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDisplayResultsBinding.inflate(inflater, container, false)
        binding.ivInput.setImageBitmap(mainActivityViewModel.faceBitmap)
        val faceMatrix = loadFaceToAnalyze()
        val avgFace = loadAvgFaces()
        return binding.root
    }

    fun loadVFaces() : D2Array<Float> {
        val array = Array(PORTRAIT_HEIGHT * PORTRAIT_WIDTH) {FloatArray(NUM_COORDS)}
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
        Log.i("STREAM_TAG", (System.currentTimeMillis() - start).toString())
        istream.close()
        scanner.close()
        return array.toNDArray()
    }

    fun loadAvgFaces() : D1Array<Float> {
        val array = FloatArray(PORTRAIT_HEIGHT * PORTRAIT_WIDTH)
        val istream = resources.openRawResource(R.raw.face_avg)
        val scanner = Scanner(istream)
        var line: String
        val start = System.currentTimeMillis()
        var row = 0
        while (scanner.hasNextLine()) {
            line = scanner.nextLine()
            array[row] = line.toFloat()
            row++
        }
        Log.i("STREAM_TAG", (System.currentTimeMillis() - start).toString())
        istream.close()
        scanner.close()
        return Multik.ndarray(array)
    }

    fun loadFaceToAnalyze() : D1Array<Int> {
        val faceArray = IntArray(10304)
        val preppedBitmap = mainActivityViewModel.faceBitmap
        preppedBitmap.getPixels(faceArray, 0, preppedBitmap.width, 0, 0, preppedBitmap.width, preppedBitmap.height)
        for (i in faceArray.indices) {
            faceArray[i] = Color.red(faceArray[i])
        }
        return Multik.ndarray(faceArray)
    }

}