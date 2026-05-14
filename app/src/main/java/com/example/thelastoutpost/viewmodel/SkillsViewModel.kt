package com.example.thelastoutpost.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thelastoutpost.model.Skill
import com.example.thelastoutpost.repository.GameRepository

class SkillsViewModel(private val repository: GameRepository) : ViewModel() {

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SkillsViewModel::class.java)) {
                return SkillsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    private val _currentGold = MutableLiveData<Int>()
    val currentGold: LiveData<Int> = _currentGold

    // Ramas de la constelación
    private val _healthSkills = MutableLiveData<List<Skill>>()
    val healthSkills: LiveData<List<Skill>> = _healthSkills

    private val _damageSkills = MutableLiveData<List<Skill>>()
    val damageSkills: LiveData<List<Skill>> = _damageSkills

    private val _speedSkills = MutableLiveData<List<Skill>>()
    val speedSkills: LiveData<List<Skill>> = _speedSkills

    private val _goldenSkinSkill = MutableLiveData<Skill>()
    val goldenSkinSkill: LiveData<Skill> = _goldenSkinSkill

    init {
        loadData()
    }

    private fun loadData() {
        _currentGold.value = repository.getCurrentGold()

        // Vida (Corazones extra)
        val hp1 = Skill("hp_1", "Vida", "Corazón Extra I", "+20 HP Max", 20, repository.isSkillUnlocked("hp_1"))
        val hp2 = Skill("hp_2", "Vida", "Corazón Extra II", "+20 HP Max", 40, repository.isSkillUnlocked("hp_2"), "hp_1")
        val hp3 = Skill("hp_3", "Vida", "Corazón Férreo", "+40 HP Max", 60, repository.isSkillUnlocked("hp_3"), "hp_2")
        _healthSkills.value = listOf(hp1, hp2, hp3)

        // Daño
        val dmg1 = Skill("dmg_1", "Daño", "Afilado base", "x1.40 Daño", 30, repository.isSkillUnlocked("dmg_1"))
        val dmg2 = Skill("dmg_2", "Daño", "Arqueros aliados", "Desbloquea apoyo", 50, repository.isSkillUnlocked("dmg_2"), "dmg_1")
        val dmg3 = Skill("dmg_3", "Daño", "Afilado perfecto", "x2.00 Daño", 70, repository.isSkillUnlocked("dmg_3"), "dmg_2")
        _damageSkills.value = listOf(dmg1, dmg2, dmg3)

        // Velocidad
        val spd1 = Skill("spd_1", "Velocidad", "Botas ligeras", "+2 Vel. Mov.", 15, repository.isSkillUnlocked("spd_1"))
        val spd2 = Skill("spd_2", "Velocidad", "Zancada", "+3 Vel. Mov.", 30, repository.isSkillUnlocked("spd_2"), "spd_1")
        val spd3 = Skill("spd_3", "Velocidad", "Dash mejorado", "Dash más largo", 45, repository.isSkillUnlocked("spd_3"), "spd_2")
        _speedSkills.value = listOf(spd1, spd2, spd3)

        // Golden Skin (requiere que las top skills estén desbloqueadas)
        _goldenSkinSkill.value = Skill(
            id = "golden_skin",
            category = "Especial",
            name = "Skin Dorada",
            description = "Estatuto de Leyenda",
            cost = 200,
            isUnlocked = repository.isSkillUnlocked("golden_skin"),
            // Dependencia múltiple evaluada en lógica manual, aquí no asignamos solo uno
            requiredSkillId = "hp_3,dmg_3,spd_3" 
        )
    }

    fun canUnlock(skill: Skill): Boolean {
        if (skill.isUnlocked) return false
        val gold = _currentGold.value ?: 0
        if (gold < skill.cost) return false

        // Check required
        if (skill.id == "golden_skin") {
            return repository.isSkillUnlocked("hp_3") && 
                   repository.isSkillUnlocked("dmg_3") && 
                   repository.isSkillUnlocked("spd_3")
        }

        skill.requiredSkillId?.let { reqId ->
            if (!repository.isSkillUnlocked(reqId)) return false
        }

        return true
    }

    fun unlockSkill(skill: Skill): Boolean {
        if (canUnlock(skill)) {
            if (repository.spendGold(skill.cost)) {
                repository.unlockSkill(skill.id)
                loadData() // Recargar listas
                return true
            }
        }
        return false
    }
}
