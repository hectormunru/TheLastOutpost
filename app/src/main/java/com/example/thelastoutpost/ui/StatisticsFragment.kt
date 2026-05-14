package com.example.thelastoutpost.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.thelastoutpost.databinding.FragmentStatisticsBinding

/**
 * Fragment que muestra las estadísticas de juego.
 * Usa DataBinding para formatear los textos.
 */
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val application = requireActivity().application as com.example.thelastoutpost.TheLastOutpostApplication
        val repository = application.repository
        
        // Asignamos los valores reales del repositorio único al binding
        binding.apply {
            tvEnemiesKilled.text = getString(com.example.thelastoutpost.R.string.stats_enemies_killed, repository.getKills())
            tvGoldEarned.text = getString(com.example.thelastoutpost.R.string.stats_gold_earned, repository.getTotalGold())
            tvMaxWave.text = getString(com.example.thelastoutpost.R.string.stats_max_wave, repository.getMaxWave())
            
            btnBackFromStats.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
