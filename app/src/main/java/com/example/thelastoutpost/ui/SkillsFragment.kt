package com.example.thelastoutpost.ui

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.thelastoutpost.R
import com.example.thelastoutpost.databinding.FragmentSkillsBinding
import com.example.thelastoutpost.model.Skill
import com.example.thelastoutpost.viewmodel.SkillsViewModel

class SkillsFragment : Fragment() {

    private val viewModel: SkillsViewModel by viewModels {
        val application = requireActivity().application as com.example.thelastoutpost.TheLastOutpostApplication
        val repository = com.example.thelastoutpost.repository.GameRepository(application, application.database.gameDao())
        SkillsViewModel.Factory(repository)
    }
    private var _binding: FragmentSkillsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.speedSkills.observe(viewLifecycleOwner) { skills ->
            populateBranch(binding.llSpeedBranch, skills)
        }
        
        viewModel.healthSkills.observe(viewLifecycleOwner) { skills ->
            populateBranch(binding.llHealthBranch, skills)
        }

        viewModel.damageSkills.observe(viewLifecycleOwner) { skills ->
            populateBranch(binding.llDamageBranch, skills)
        }

        viewModel.goldenSkinSkill.observe(viewLifecycleOwner) { skill ->
            binding.llGoldenSkin.removeAllViews()
            val nodeView = createSkillNodeView(skill, isGolden = true, container = binding.llGoldenSkin)
            binding.llGoldenSkin.addView(nodeView)
        }
    }

    private fun populateBranch(container: LinearLayout, skills: List<Skill>) {
        container.removeAllViews()
        
        // Lo mostramos de arriba (mayor nivel) a abajo (nivel 1)
        val reversed = skills.reversed()
        for (i in reversed.indices) {
            val skill = reversed[i]
            
            // Añadir el nodo
            val nodeView = createSkillNodeView(skill, isGolden = false, container = container)
            container.addView(nodeView)

            // Añadir la línea conectora (excepto después del último de abajo)
            if (i < reversed.size - 1) {
                val line = View(requireContext()).apply {
                    val isNodeUnlocked = skill.isUnlocked
                    layoutParams = LinearLayout.LayoutParams(
                        (4 * resources.displayMetrics.density).toInt(), // 4dp width
                        (40 * resources.displayMetrics.density).toInt() // 40dp height
                    )
                    setBackgroundColor(if (isNodeUnlocked) Color.parseColor("#FFD700") else Color.parseColor("#555555"))
                }
                container.addView(line)
            }
        }
    }

    private fun createSkillNodeView(skill: Skill, isGolden: Boolean, container: ViewGroup): View {
        val view = layoutInflater.inflate(R.layout.item_skill_node, container, false)
        
        val flNodeCircle = view.findViewById<FrameLayout>(R.id.flNodeCircle)
        val tvCost = view.findViewById<TextView>(R.id.tvCost)
        val tvSkillName = view.findViewById<TextView>(R.id.tvSkillName)
        val tvSkillDesc = view.findViewById<TextView>(R.id.tvSkillDesc)
        val tvCta = view.findViewById<TextView>(R.id.tvCta)

        tvSkillName.text = skill.name
        tvSkillDesc.text = skill.description
        tvCost.text = "${skill.cost}"

        // Actualizar aspecto visual
        if (skill.isUnlocked) {
            flNodeCircle.setBackgroundResource(R.drawable.bg_skill_node_unlocked)
            tvCost.text = "OK"
            tvCost.setTextColor(Color.parseColor("#00FF00"))
            tvCta.visibility = View.GONE
        } else {
            flNodeCircle.setBackgroundResource(R.drawable.bg_skill_node_locked)
            if (viewModel.canUnlock(skill)) {
                tvCta.text = "COMPRAR"
                tvCta.setTextColor(Color.parseColor("#FFD700"))
            } else {
                tvCta.text = "BLOQUEADO"
                tvCta.setTextColor(Color.parseColor("#888888"))
            }
        }

        // Tinte especial para el nodo Golden Skin
        if (isGolden) {
            // Fondo dorado si está desbloqueado
            if (skill.isUnlocked) {
                flNodeCircle.background.setColorFilter(Color.parseColor("#FFD700"), PorterDuff.Mode.SRC_ATOP)
            } else {
                tvSkillName.setTextColor(Color.parseColor("#FFD700"))
                tvCost.setTextColor(Color.parseColor("#FFD700"))
            }
        }

        // Manejar el Click
        flNodeCircle.setOnClickListener {
            if (skill.isUnlocked) {
                // Ya la tiene
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(skill.name)
                    .setMessage("Ya posees esta mejora:\n${skill.description}")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            if (viewModel.canUnlock(skill)) {
                showPurchaseDialog(skill)
            } else {
                // No cumple requisitos (oro o habilidades previas)
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Bloqueado")
                    .setMessage("No puedes desbloquear esto aún. Revisa si tienes suficiente oro o si necesitas mejoras anteriores.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        return view
    }

    private fun showPurchaseDialog(skill: Skill) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Comprar Mejora")
            .setMessage("¿Quieres desbloquear '${skill.name}' por ${skill.cost} de Oro?")
            .setPositiveButton("Comprar") { _, _ ->
                if (viewModel.unlockSkill(skill)) {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root, 
                        "¡Has desbloqueado ${skill.name}!", 
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root, 
                        "Error al comprar ${skill.name}", 
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
