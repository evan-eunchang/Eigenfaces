package com.eigenfaces.eigenfaces

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.eigenfaces.eigenfaces.databinding.FragmentDisplayResultsBinding
import com.eigenfaces.eigenfaces.db.Portrait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.kotlinx.multik.api.Multik
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import java.io.File
import java.io.FileOutputStream
import java.util.Scanner
import kotlin.math.sqrt


//const val PORTRAIT_HEIGHT = 112
//const val PORTRAIT_WIDTH = 92
const val NUM_PIXELS = 10304
const val NUM_COORDS = 400
class DisplayResultsFragment : Fragment() {
    private lateinit var binding : FragmentDisplayResultsBinding
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val portraitViewModel by activityViewModels<PortraitViewModel> {
        PortraitViewModelFactory(MainActivity.dao!!)
    }
    private lateinit var closestMatch : Portrait
    private lateinit var coordinates : D1Array<Float>
    private lateinit var job : Job

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDisplayResultsBinding.inflate(inflater, container, false)
        binding.ivInput.setImageBitmap(mainActivityViewModel.faceBitmap)

        job = CoroutineScope(Dispatchers.IO).launch {
//            Log.i("ROOM_TAG", portraitViewModel.count.toString())
            val faceNoAvg = loadFaceToAnalyze() - loadAvgFaces()
            coordinates = getCoordinates(faceNoAvg)
            withContext(Dispatchers.Main){
                portraitViewModel.portraits.observeOnce(viewLifecycleOwner) { portraits ->
                    if (portraits.isEmpty()) {
                        mainActivityViewModel.coordinatesToSave = ndArrayToString(coordinates)
                        mainActivityViewModel.fileNameToSave = savePortraitImage()
                        view?.findNavController()
                            ?.navigate(R.id.action_displayResultsFragment_to_namePortraitFragment)
                        job.cancel()
                    } else {
                        Log.i("ROOM_TAG", portraits.size.toString())
                        findClosestMatch(coordinates, portraits)
                        doneLoading()
                    }
                }
            }
        }

        binding.btnYes.setOnClickListener {
            portraitViewModel.insertPortrait(
                Portrait(0, closestMatch.name, savePortraitImage(), ndArrayToString(coordinates))
            )
            view?.findNavController()
                ?.navigate(R.id.action_displayResultsFragment_to_welcomeFragment)
            job.cancel()
        }

        binding.btnNo.setOnClickListener {
            mainActivityViewModel.coordinatesToSave = ndArrayToString(coordinates)
            mainActivityViewModel.fileNameToSave = savePortraitImage()
            view?.findNavController()
                ?.navigate(R.id.action_displayResultsFragment_to_namePortraitFragment)
            job.cancel()
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
        var row = 0
        while (scanner.hasNextLine()) {
            line = scanner.nextLine()
            val list = line.split(',')
            for (i in list.indices) {
                array[row][i] = list[i].toFloat()
            }
            row++
        }
        istream.close()
        scanner.close()
        return Multik.ndarray(array)
    }

    private fun doneLoading() {
        binding.btnNo.visibility = View.VISIBLE
        binding.btnYes.visibility = View.VISIBLE
        binding.tvTitle.visibility = View.VISIBLE
        binding.ivOutput.visibility = View.VISIBLE
        binding.tvOutput.text = closestMatch.name
        val file = File(context?.filesDir?.path!!, closestMatch.fileName)
        val bitmap = BitmapFactory.decodeFile(file.path)
        binding.ivOutput.setImageBitmap(bitmap)
    }
    private fun loadAvgFaces() : D1Array<Float> {
        val array = FloatArray(NUM_PIXELS)
        val istream = resources.openRawResource(R.raw.face_avg)
        val scanner = Scanner(istream)
        var line: String
        var row = 0
        while (scanner.hasNextLine()) {
            line = scanner.nextLine()
            array[row] = line.toFloat()
            row++
        }
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

    private fun ndArrayToString(ndArray : D1Array<Float>) : String {
        var toReturn = ""
        toReturn += ndArray[0].toFloat()
        for (i in 1 until ndArray.size) {
            toReturn += "," + ndArray[i].toString()
        }
        return toReturn
    }

    private fun stringToNDarray(arrayInString : String) : D1Array<Float> {
        val arrayAsList = arrayInString.split(',')
        val toReturn = FloatArray(arrayAsList.size)
        for (i in 1 until arrayAsList.size) {
            toReturn[i] = arrayAsList[i].toFloat()
        }
        return Multik.ndarray(toReturn)
    }

    private fun norm(ndArray : D1Array<Float>) : Double {
        var total = 0f
        for (num in ndArray) {
            total += num * num
        }
        return sqrt(total.toDouble())
    }

    private fun findClosestMatch(coordinates : D1Array<Float>, portraits : List<Portrait>) {
        closestMatch = portraits[0]
        var min = norm(stringToNDarray(closestMatch.coordinates) - coordinates)
        for (i in portraits.indices) {
            val temp = norm(stringToNDarray(portraits[i].coordinates) - coordinates)
            if (temp < min) {
                min = temp
                closestMatch = portraits[i]
            }
        }
    }

    private fun savePortraitImage() : String {
        val bitmap = mainActivityViewModel.faceBitmap

        val outStream: FileOutputStream?
        val dir = File(context?.filesDir?.path!!)
        val fileName = String.format("%d.png", System.currentTimeMillis())
        val outFile = File(dir, fileName)
        outStream = FileOutputStream(outFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
        outStream.flush()
        outStream.close()

        return fileName
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {

            override fun onChanged(value: T) {
                observer.onChanged(value)
                removeObserver(this)
            }

        })
    }

}