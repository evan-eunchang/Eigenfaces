package com.eigenfaces.eigenfaces

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
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

        //use a coroutine to avoid overloading main thread
        job = CoroutineScope(Dispatchers.IO).launch {
            //get the face we are analyzing and subtract the average from it, then get its coordinates
            val faceNoAvg = loadFaceToAnalyze() - loadAvgFaces()
            coordinates = getCoordinates(faceNoAvg)
            //observe the list of portraits from our room database, and if there are none, proceed
            //directly to the naming fragment, otherwise find the closest match
            withContext(Dispatchers.Main){
                portraitViewModel.portraits.observeOnce(viewLifecycleOwner) { portraits ->
                    if (portraits.isEmpty()) {
                        mainActivityViewModel.coordinatesToSave = ndArrayToString(coordinates)
                        mainActivityViewModel.fileNameToSave = savePortraitImage()
                        view?.findNavController()
                            ?.navigate(R.id.action_displayResultsFragment_to_namePortraitFragment)
                        job.cancel()
                    } else {
                        findClosestMatch(coordinates, portraits)
                        doneLoading()
                    }
                }
            }
        }

        //if we are correct, insert the image to the Room database with the name of the closest match
        //and go back to the beginning
        binding.btnYes.setOnClickListener {
            portraitViewModel.insertPortrait(
                Portrait(0, closestMatch.name, savePortraitImage(), ndArrayToString(coordinates))
            )
            view?.findNavController()
                ?.navigate(R.id.action_displayResultsFragment_to_welcomeFragment)
            job.cancel()
        }

        //if not, go the naming fragment to get the correct name
        binding.btnNo.setOnClickListener {
            //pass the coordinates and file name so they can be saved to the Room database
            //in the naming fragment
            mainActivityViewModel.coordinatesToSave = ndArrayToString(coordinates)
            mainActivityViewModel.fileNameToSave = savePortraitImage()
            view?.findNavController()
                ?.navigate(R.id.action_displayResultsFragment_to_namePortraitFragment)
            job.cancel()
        }

        return binding.root
    }

    //implement matrix multiplication to multiply the face we are analyzing (minus the average)
    //by v_faces to get our 400 coordinate values
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

    //load v_faces, a coordinate transformation matrix that will allow us to approximate a face
    //with 400 coordinates instead of 10k then populate a 2D float array with those values
    private fun loadVFaces() : D2Array<Float> {
        val array = Array(NUM_PIXELS) {FloatArray(NUM_COORDS)}
        val istream = resources.openRawResource(R.raw.vfaces)
        //use a scanner to avoid loading too much at once
        val scanner = Scanner(istream)
        var line: String
        var row = 0
        //each row in the file will correspond to a row of our v_faces array
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

    //make buttons, text, and results visible now that a result has been found
    private fun doneLoading() {
        binding.btnNo.visibility = View.VISIBLE
        binding.btnYes.visibility = View.VISIBLE
        binding.tvTitle.visibility = View.VISIBLE
        binding.ivOutput.visibility = View.VISIBLE
        binding.tvOutput.text = closestMatch.name
        //load image of closestMatch from data files
        val file = File(context?.filesDir?.path!!, closestMatch.fileName)
        val bitmap = BitmapFactory.decodeFile(file.path)
        binding.ivOutput.setImageBitmap(bitmap)
    }

    //load the "average face" that our model was trained with from resource file, then populate a
    //1D float array with those values
    private fun loadAvgFaces() : D1Array<Float> {
        val array = FloatArray(NUM_PIXELS)
        val istream = resources.openRawResource(R.raw.face_avg)
        //use a scanner to avoid loading too much at once
        val scanner = Scanner(istream)
        var line: String
        var row = 0
        //file is formatted as a column vector so there is one entry per row
        while (scanner.hasNextLine()) {
            line = scanner.nextLine()
            array[row] = line.toFloat()
            row++
        }
        istream.close()
        scanner.close()
        return Multik.ndarray(array)
    }

    //take the bitmap of the image chosen by the user in SelectFaceFragment and use it to populate a
    //1D float array for matrix operations
    private fun loadFaceToAnalyze() : D1Array<Float> {
        //initialize IntArray to receive pixel values and FloatArray to return
        val faceArrayInt = IntArray(NUM_PIXELS)
        val faceArrayFloat = FloatArray(NUM_PIXELS)
        val preppedBitmap = mainActivityViewModel.faceBitmap
        //populate IntArray with color values
        preppedBitmap.getPixels(faceArrayInt, 0, preppedBitmap.width, 0, 0, preppedBitmap.width, preppedBitmap.height)
        //populate FloatArray with the greyscale color value of the pixel from 0..255
        for (i in faceArrayInt.indices) {
            faceArrayFloat[i] = Color.red(faceArrayInt[i]).toFloat()
        }
        return Multik.ndarray(faceArrayFloat)
    }

    //convert a 1D float array into a CSV string for storage in the Room Database
    private fun ndArrayToString(ndArray : D1Array<Float>) : String {
        var toReturn = ""
        //add first item
        toReturn += ndArray[0].toFloat()
        //add the rest of the items with a comma before them so there are no extra or missing commas
        for (i in 1 until ndArray.size) {
            toReturn += "," + ndArray[i].toString()
        }
        return toReturn
    }

    //Convert a CSV string into a 1D float array for matrix operations
    private fun stringToNDarray(arrayInString : String) : D1Array<Float> {
        //get all elements in string
        val arrayAsList = arrayInString.split(',')
        val toReturn = FloatArray(arrayAsList.size)
        //convert elements to floats and populate toReturn array
        for (i in 1 until arrayAsList.size) {
            toReturn[i] = arrayAsList[i].toFloat()
        }
        return Multik.ndarray(toReturn)
    }

    //find the Frobenius norm (square root of the sum of squares) of a 1D float array
    private fun norm(ndArray : D1Array<Float>) : Double {
        var total = 0f
        //add the square of entry to the total sum container
        for (num in ndArray) {
            total += num * num
        }
        //return square root
        return sqrt(total.toDouble())
    }

    //use the eigenface coordinate system and the "distance" between faces in this coordinate system
    // to find the face closest to the one we are analyzing
    private fun findClosestMatch(coordinates : D1Array<Float>, portraits : List<Portrait>) {
        //initialize closest match and the minimum distance using the first entries
        closestMatch = portraits[0]
        //the distance is the norm of the "distance" between the two faces
        var min = norm(stringToNDarray(closestMatch.coordinates) - coordinates)
        //check the distance between the face being analyzed and every other face we analyzed
        //if the distance is less, update minimum distance and closest match
        for (i in portraits.indices) {
            val temp = norm(stringToNDarray(portraits[i].coordinates) - coordinates)
            if (temp < min) {
                min = temp
                closestMatch = portraits[i]
            }
        }
    }

    //save the image to the /data file in the device for later retrieval and return the file name
    //for storage in Room Database
    private fun savePortraitImage() : String {
        val bitmap = mainActivityViewModel.faceBitmap

        val outStream: FileOutputStream?
        val dir = File(context?.filesDir?.path!!)
        //use the current time so every image has a unique name
        val fileName = String.format("%d.png", System.currentTimeMillis())
        val outFile = File(dir, fileName)
        outStream = FileOutputStream(outFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
        outStream.flush()
        outStream.close()

        return fileName
    }

    //function to observe data once and remove observer so observation does not continue
    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {

            override fun onChanged(value: T) {
                observer.onChanged(value)
                removeObserver(this)
            }

        })
    }

}