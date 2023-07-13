package com.eigenfaces.eigenfaces

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.eigenfaces.eigenfaces.databinding.FragmentNamePortraitBinding
import com.eigenfaces.eigenfaces.db.Portrait
import com.eigenfaces.eigenfaces.db.PortraitDatabase


class NamePortraitFragment : Fragment() {
    private lateinit var binding : FragmentNamePortraitBinding
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val portraitViewModel by activityViewModels<PortraitViewModel> {
        PortraitViewModelFactory(MainActivity.dao!!)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNamePortraitBinding.inflate(inflater, container, false)

        binding.ivPortrait.setImageBitmap(mainActivityViewModel.faceBitmap)
        binding.btnSave.setOnClickListener {
            if (binding.etName.text.toString() == "") {
                Toast.makeText(context, "Please enter a name", Toast.LENGTH_LONG).show()
            } else {
                portraitViewModel.insertPortrait(
                    Portrait(0, binding.etName.text.toString(),
                        mainActivityViewModel.fileNameToSave, mainActivityViewModel.coordinatesToSave)
                )
                view?.findNavController()?.navigate(R.id.action_namePortraitFragment_to_welcomeFragment)
            }
        }
        return binding.root
    }

}