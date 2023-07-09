package com.eigenfaces.eigenfaces

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.eigenfaces.eigenfaces.databinding.FragmentDisplayResultsBinding
import org.jetbrains.kotlinx.multik.api.toNDArray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import java.util.Scanner

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DisplayResultsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DisplayResultsFragment : Fragment() {
    private lateinit var binding : FragmentDisplayResultsBinding
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDisplayResultsBinding.inflate(inflater, container, false)

        mainActivityViewModel.inputBitmap.observe(viewLifecycleOwner, Observer {
            binding.ivInput.setImageBitmap(it)
        })

        return binding.root
    }

    fun loadVfaces() : D2Array<Float> {
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
        Log.i("STREAM_TAG", (System.currentTimeMillis() - start).toString())
        istream.close()
        scanner.close()
        return array.toNDArray()
    }

}