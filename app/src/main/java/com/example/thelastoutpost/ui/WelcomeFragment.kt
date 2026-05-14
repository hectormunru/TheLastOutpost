package com.example.thelastoutpost.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.thelastoutpost.R
import com.example.thelastoutpost.databinding.FragmentWelcomeBinding
import com.example.thelastoutpost.viewmodel.WelcomeViewModel
import com.example.thelastoutpost.repository.GameRepository

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WelcomeViewModel by viewModels {
        val application = requireActivity().application as com.example.thelastoutpost.TheLastOutpostApplication
        val repository = GameRepository(application, application.database.gameDao())
        WelcomeViewModel.Factory(repository)
    }

    private lateinit var adapter: SaveSlotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Nueva Partida con Diálogo de Nombre
        binding.btnNewGame.setOnClickListener {
            showNewGameDialog()
        }

        // Ajustes
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_settings)
        }

        // Créditos
        binding.btnCredits.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_credits)
        }

        // Salir
        binding.btnExit.setOnClickListener {
            activity?.finish()
        }
    }

    private fun showNewGameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_game, null)
        val etSlotName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSlotName)
        
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("NUEVA AVENTURA")
            .setView(dialogView)
            .setPositiveButton("COMENZAR") { _, _ ->
                val slotName = etSlotName.text?.toString()?.trim() ?: ""
                if (slotName.isNotEmpty()) {
                    viewModel.createNewGame(slotName) { newSlot ->
                        // Esto se ejecuta tras crear la partida en la DB
                        requireActivity().runOnUiThread {
                            launchGame(newSlot.id, newSlot.playerName)
                        }
                    }
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = SaveSlotAdapter(
            onSlotClick = { slot ->
                // Activar slot
                requireContext().getSharedPreferences("game_stats", android.content.Context.MODE_PRIVATE)
                    .edit().putString("active_slot", slot.id).apply()
                launchGame(slot.id, slot.playerName)
            },
            onDeleteClick = { slotToDelete ->
                viewModel.deleteSaveSlot(slotToDelete)
            }
        )
        binding.rvSaveSlots.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        binding.rvSaveSlots.adapter = adapter

        // Observar los datos desde Room
        viewModel.saveSlots.observe(viewLifecycleOwner) { slots ->
            adapter.submitList(slots)
        }
    }

    private fun launchGame(slotId: String, playerName: String) {
        val bundle = bundleOf("playerName" to playerName)
        findNavController().navigate(R.id.action_welcome_to_game, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
