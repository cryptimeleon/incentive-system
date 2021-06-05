package org.cryptimeleon.incentivesystem.app

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.onNavDestinationSelected
import org.cryptimeleon.incentivesystem.app.repository.CryptoRepository
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var cryptoRepository: CryptoRepository

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        System.loadLibrary("mcljava")

        // For logging
        Timber.plant(Timber.DebugTree())
    }

}