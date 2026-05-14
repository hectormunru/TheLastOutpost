package com.example.thelastoutpost

import android.app.Application
import com.example.thelastoutpost.database.GameDatabase
import com.example.thelastoutpost.repository.GameRepository
import timber.log.Timber
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TheLastOutpostApplication : Application() {
    
    // Base de datos y Repositorio como Singletons
    val database: GameDatabase by lazy { GameDatabase.getInstance(this) }
    val repository: GameRepository by lazy { GameRepository(this, database.gameDao()) }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        
        // Inicializar logros al arrancar usando el repositorio único
        GlobalScope.launch {
            repository.initializeAchievements()
        }
    }
}
