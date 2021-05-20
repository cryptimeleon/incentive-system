package org.cryptimeleon.incentivesystem.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import org.cryptimeleon.incentivesystem.app.crypto.CryptoRepository
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem
import org.cryptimeleon.incentivesystem.cryptoprotocol.Setup
import timber.log.Timber

const val SECURITY_PARAMETER = 128

class MainActivity : AppCompatActivity() {
    private lateinit var cryptoRepository: CryptoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // For logging
        Timber.plant(Timber.DebugTree());

        /*
        cryptoRepository = CryptoRepository(applicationContext)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment);
        val navController = navHost!!.findNavController()

        val navInflater = navController.navInflater;
        val graph = navInflater.inflate(R.navigation.main_navigation);

        if (cryptoRepository.getSetupFinished()) {
            graph.startDestination = R.id.info2;
        } else {
            graph.startDestination = R.id.setup;
        }

        navController.graph = graph;
        */
    }

}