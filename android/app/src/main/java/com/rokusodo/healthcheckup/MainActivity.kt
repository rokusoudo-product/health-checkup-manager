package com.rokusodo.healthcheckup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.rokusodo.healthcheckup.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()

        // NoActionBar テーマのため Toolbar を ActionBar として設定
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // ログイン状態に応じて startDestination を動的に変更
        val navInflater = navHostFragment.navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)

        // TODO: Firebase Console で Web Client ID を設定後に認証を有効化する
        // 現在は google-services.json に Web Client ID 未登録のため認証をスキップ
        graph.setStartDestination(R.id.mainFragment)

        navController = navHostFragment.navController
        navController.graph = graph

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mainFragment, R.id.loginFragment)
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * targetSdk 35 の edge-to-edge 強制により AppBarLayout がステータスバーの下に
     * 潜り込んでしまうため、ステータスバー分の高さを明示的に top padding として適用する。
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBarInsets.top)
            insets
        }
    }
}
