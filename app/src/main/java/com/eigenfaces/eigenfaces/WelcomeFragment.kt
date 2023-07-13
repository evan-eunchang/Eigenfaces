package com.eigenfaces.eigenfaces

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.eigenfaces.eigenfaces.databinding.FragmentWelcomeBinding
import com.eigenfaces.eigenfaces.db.PortraitDatabase


class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    val portraitViewModel by activityViewModels<PortraitViewModel> {
        PortraitViewModelFactory(MainActivity.dao!!)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        MainActivity.dao = PortraitDatabase.getInstance((activity as MainActivity).application).portraitDao()
        binding.btnStart.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcomeFragment_to_selectFaceFragment)
        }

        return binding.root
    }

}