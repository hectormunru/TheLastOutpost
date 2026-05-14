package com.example.thelastoutpost.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thelastoutpost.databinding.FragmentAchievementsBinding
import com.example.thelastoutpost.viewmodel.AchievementsViewModel
import com.example.thelastoutpost.repository.GameRepository

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AchievementsViewModel by viewModels {
        val application = requireActivity().application as com.example.thelastoutpost.TheLastOutpostApplication
        AchievementsViewModel.Factory(application.repository)
    }

    private lateinit var adapter: AchievementsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AchievementsAdapter()
        binding.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAchievements.adapter = adapter

        viewModel.achievements.observe(viewLifecycleOwner) { achievements ->
            adapter.submitList(achievements)
        }

        binding.btnBackFromAchievements.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
