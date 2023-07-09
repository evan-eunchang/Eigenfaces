package com.eigenfaces.eigenfaces

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.eigenfaces.eigenfaces.databinding.FragmentSelectFaceBinding


class SelectFaceFragment : Fragment() {
    private lateinit var binding : FragmentSelectFaceBinding

    private val previewImage by lazy { view?.findViewById<MyImageView>(R.id.ivPortrait) }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? -> uri?.let {
        val imageStream = activity?.contentResolver?.openInputStream(uri)
        val yourSelectedImage = BitmapFactory.decodeStream(imageStream)
        previewImage?.setImageBitmap(yourSelectedImage)
    }
    }
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectFaceBinding.inflate(inflater, container, false)

        binding.btnPick.setOnClickListener {
            selectImageFromGallery()
            binding.btnCalculate.visibility = View.VISIBLE
        }

        binding.btnCalculate.setOnClickListener {
            val preppedBitmap = captureBitmap()
            mainActivityViewModel.setInputBitmap(preppedBitmap)
            it.findNavController().navigate(R.id.action_selectFaceFragment_to_displayResultsFragment)
        }

        return binding.root
    }

    private fun captureBitmap() : Bitmap {
        var bitmap = Bitmap.createBitmap(
            binding.ivPortrait.width, binding.ivPortrait.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        binding.ivPortrait.draw(canvas)
        bitmap = Bitmap.createScaledBitmap(bitmap, 92, 112, false)
        bitmap = bitmapToGrayscale(bitmap)

        return bitmap
    }

    private fun bitmapToGrayscale(bm : Bitmap) : Bitmap {
        val bmGrayscale = Bitmap.createBitmap(
            bm.width, bm.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val filter = ColorMatrixColorFilter(cm)
        paint.colorFilter = filter
        canvas.drawBitmap(bm, 0f, 0f, paint)
        return bmGrayscale
    }

    private fun selectImageFromGallery() {
        selectImageFromGalleryResult.launch("image/*")
    }

}