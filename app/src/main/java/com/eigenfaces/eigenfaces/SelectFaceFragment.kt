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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.eigenfaces.eigenfaces.databinding.FragmentSelectFaceBinding


const val IMAGE_WIDTH = 92
const val IMAGE_HEIGHT = 112
class SelectFaceFragment : Fragment() {
    private lateinit var binding : FragmentSelectFaceBinding

    //val for ivPortrait, the MyImageView that we will use to select the face
    private val previewImage by lazy { view?.findViewById<MyImageView>(R.id.ivPortrait) }

    //code to set ivPortrait to a bitmap of the image the user selects from the gallery
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
    ): View {
        binding = FragmentSelectFaceBinding.inflate(inflater, container, false)

        //button to pick image and allow saving
        binding.btnPick.setOnClickListener {
            selectImageFromGallery()
            binding.btnCalculate.visibility = View.VISIBLE
        }

        //enter the image into view model for later access, move to analysis fragment
        binding.btnCalculate.setOnClickListener {
            val preppedBitmap = captureBitmap()
            mainActivityViewModel.faceBitmap = preppedBitmap
            it.findNavController().navigate(R.id.action_selectFaceFragment_to_displayResultsFragment)
        }

        return binding.root
    }

    //create a bitmap to get the image in the MyImageView in the format we want
    private fun captureBitmap() : Bitmap {
        //create bitmap and draw the MyImageView onto it
        var bitmap = Bitmap.createBitmap(
            binding.ivPortrait.width, binding.ivPortrait.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        binding.ivPortrait.draw(canvas)
        //scale it to 112 x 92 pixel image and convert it to grayscale so it can be analyzed
        bitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, false)
        bitmap = bitmapToGrayscale(bitmap)

        return bitmap
    }

    //convert bitmap to grayscale
    private fun bitmapToGrayscale(bm : Bitmap) : Bitmap {
        val bmGrayscale = Bitmap.createBitmap(
            bm.width, bm.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmGrayscale)
        //use color matrix to create filter that will make a grayscale bitmap
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val filter = ColorMatrixColorFilter(cm)
        paint.colorFilter = filter
        canvas.drawBitmap(bm, 0f, 0f, paint)
        return bmGrayscale
    }

    //launch gallery to pick image
    private fun selectImageFromGallery() {
        selectImageFromGalleryResult.launch("image/*")
    }

}