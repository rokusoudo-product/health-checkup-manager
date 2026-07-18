package com.rokusodo.healthcheckup.navigation

import android.app.Application
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDestination
import androidx.navigation.Navigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.rokusodo.healthcheckup.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * nav_graph の遷移リグレッションテスト（画面遷移刷新001・Phase1）。
 * Fragment は起動せず、グラフ構造と action による遷移先のみを検証する。
 * Phase3 で新遷移図（S-01〜S-07）に合わせて拡張する。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class NavGraphRegressionTest {

    /** <fragment> デスティネーションを Fragment 起動なしで扱うためのスタブ Navigator */
    @Navigator.Name("fragment")
    class FragmentStubNavigator : Navigator<NavDestination>() {
        override fun createDestination(): NavDestination = NavDestination(this)
    }

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(FragmentStubNavigator())
        navController.setGraph(R.navigation.nav_graph)
    }

    private fun currentId() = navController.currentDestination?.id

    private fun moveTo(destinationId: Int, args: Bundle? = null) {
        navController.setCurrentDestination(destinationId, args ?: Bundle())
    }

    @Test
    fun `開始画面はホーム(記録一覧)である`() {
        assertEquals(R.id.mainFragment, currentId())
    }

    @Test
    fun `ホームから撮影しOCR確認へ遷移できる`() {
        navController.navigate(R.id.action_main_to_camera)
        assertEquals(R.id.cameraFragment, currentId())
        navController.navigate(R.id.action_camera_to_ocr_result)
        assertEquals(R.id.ocrResultFragment, currentId())
    }

    @Test
    fun `ホームから記録詳細を経て項目グラフへ遷移できる`() {
        navController.navigate(R.id.action_main_to_record_detail, bundleOf("recordId" to 1L))
        assertEquals(R.id.recordDetailFragment, currentId())
        navController.navigate(R.id.action_record_detail_to_trend_graph, bundleOf("itemName" to "BMI"))
        assertEquals(R.id.trendGraphFragment, currentId())
    }

    @Test
    fun `ホームから項目マスターと基準値外一覧へ遷移できる`() {
        navController.navigate(R.id.action_main_to_item_master)
        assertEquals(R.id.itemMasterFragment, currentId())

        moveTo(R.id.mainFragment)
        navController.navigate(R.id.action_main_to_abnormal_list)
        assertEquals(R.id.abnormalListFragment, currentId())
    }

    @Test
    fun `ログアウト遷移でホームがバックスタックから除去される`() {
        navController.navigate(R.id.action_main_to_login)
        assertEquals(R.id.loginFragment, currentId())
        // popUpTo=mainFragment inclusive のため、戻り先は存在しない（グラフ直下に login のみ）
        assertEquals(false, navController.popBackStack())
    }

    @Test
    fun `ログイン成功でホームへ遷移しログインがバックスタックから除去される`() {
        // 実フロー: 未認証でホーム→ログイン（popUpToでホーム除去）→ 認証成功でホーム（popUpToでログイン除去）
        navController.navigate(R.id.action_main_to_login)
        navController.navigate(R.id.action_login_to_main)
        assertEquals(R.id.mainFragment, currentId())
        assertEquals(false, navController.popBackStack())
    }
}
