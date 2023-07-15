package com.eigenfaces.eigenfaces

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eigenfaces.eigenfaces.databinding.FragmentWelcomeBinding


class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var portraitRecyclerView: RecyclerView
    private lateinit var adapter: PortraitRecyclerViewAdapter
    private val portraitViewModel by activityViewModels<PortraitViewModel> {
        PortraitViewModelFactory(MainActivity.dao!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        binding.btnStart.setOnClickListener {
            it.findNavController().navigate(R.id.action_welcomeFragment_to_selectFaceFragment)
        }
        portraitRecyclerView = binding.rvPortraits

        initRecyclerView()

        return binding.root
    }
    private fun initRecyclerView() {
        portraitRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PortraitRecyclerViewAdapter()
        portraitRecyclerView.adapter = adapter
        displayPortraitsList()
    }

    private fun displayPortraitsList() {
        portraitViewModel.portraits.observe(viewLifecycleOwner) {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        }
    }
}