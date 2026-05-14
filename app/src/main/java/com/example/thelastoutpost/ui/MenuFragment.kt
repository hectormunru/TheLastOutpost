package com.example.thelastoutpost.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.thelastoutpost.R
import com.example.thelastoutpost.databinding.FragmentMenuBinding
import com.example.thelastoutpost.viewmodel.MenuViewModel

/**
 * Fragment del menú principal.
 * Usa DataBinding para vincular el layout con el ViewModel,
 * y Navigation Component para navegar a otros fragmentos.
 */
class MenuFragment : Fragment() {

    // ViewModel con LiveData (sobrevive a rotaciones de pantalla)
    private val viewModel: MenuViewModel by viewModels()

    // Binding nulable para evitar memory leaks (se limpia en onDestroyView)
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos con DataBinding
        _binding = FragmentMenuBinding.inflate(inflater, container, false)

        // Vinculamos el ViewModel al layout para que las expresiones @{} funcionen
        binding.viewModel = viewModel

        // Necesario para que LiveData actualice la UI automáticamente
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtenemos el nombre recibido de WelcomeFragment
        val args = MenuFragmentArgs.fromBundle(requireArguments())
        val playerName = args.playerName ?: "Héroe"
        viewModel.playerName.value = playerName

        // Botón "Jugar" → navega al GameFragment enviando playerName
        binding.btnPlay.setOnClickListener {
            val action = MenuFragmentDirections.actionMenuToGame(playerName)
            findNavController().navigate(action)
        }

        // Botón "Logros"
        binding.btnAchievements.setOnClickListener {
            findNavController().navigate(MenuFragmentDirections.actionMenuToAchievements())
        }

        // Botón "Estadísticas"
        binding.btnStatistics.setOnClickListener {
            findNavController().navigate(MenuFragmentDirections.actionMenuToStatistics())
        }

        // Botón "Ajustes" → navega al SettingsFragment
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(MenuFragmentDirections.actionMenuToSettings())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para evitar memory leaks
        _binding = null
    }
}
