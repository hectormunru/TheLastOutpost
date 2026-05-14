package com.example.thelastoutpost.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.thelastoutpost.R
import com.example.thelastoutpost.databinding.FragmentNameEntryBinding
import com.example.thelastoutpost.repository.GameRepository

class NameEntryFragment : Fragment() {

    private var _binding: FragmentNameEntryBinding? = null
    private val binding get() = _binding!!
    private lateinit var gameRepository: GameRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNameEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val application = requireActivity().application as com.example.thelastoutpost.TheLastOutpostApplication
        gameRepository = GameRepository(application, application.database.gameDao())

        // Si ya hay un nombre, saltamos directamente al menú de partidas
        if (gameRepository.getPlayerName().isNotEmpty()) {
            goToWelcome()
            return
        }

        binding.btnConfirmName.setOnClickListener {
            val name = binding.etPlayerName.text.toString().trim()
            if (name.isNotEmpty()) {
                gameRepository.setPlayerName(name)
                goToWelcome()
            } else {
                binding.etPlayerName.error = "Introduce un nombre válido"
            }
        }
    }

    private fun goToWelcome() {
        findNavController().navigate(NameEntryFragmentDirections.actionNameEntryToWelcome())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
