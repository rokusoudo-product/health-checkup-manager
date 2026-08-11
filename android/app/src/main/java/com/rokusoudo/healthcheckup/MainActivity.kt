package com.rokusoudo.healthcheckup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.rokusoudo.healthcheckup.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

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

        // 未ログイン（currentUser == null）なら S-01 ログイン画面、ログイン済みなら S-02 ホームを開始画面にする
        val startDestinationId = if (FirebaseAuth.getInstance().currentUser == null) {
            R.id.loginFragment
        } else {
            R.id.homeFragment
        }
        graph.setStartDestination(startDestinationId)

        navController = navHostFragment.navController
        navController.graph = graph

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.loginFragment)
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        resyncWithFirestoreOnStartup()
    }

    /**
     * Issue #26: ログイン済み状態でアプリを起動した際、Firestoreとの再同期をバックグラウンドで行う。
     * 未ログインの場合や、多重実行の抑止は HealthRepository.resyncOnStartupIfNeeded() 側で判定する。
     * UIをブロックしないよう lifecycleScope から呼び出す。
     */
    private fun resyncWithFirestoreOnStartup() {
        val app = application as HealthCheckupApp
        lifecycleScope.launch {
            app.repository.resyncOnStartupIfNeeded()
        }
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
