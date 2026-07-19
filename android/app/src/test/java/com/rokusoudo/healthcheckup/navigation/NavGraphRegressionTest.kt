package com.rokusoudo.healthcheckup.navigation

import android.app.Application
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavDestination
import androidx.navigation.Navigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.rokusoudo.healthcheckup.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * nav_graph の遷移テスト（画面遷移刷新001・Phase3）。
 * 仕様書 v0.2 の遷移図（S-01〜S-07）+ 既存4画面の導線維持（決定Q7）を検証する。
 * Fragment は起動せず、グラフ構造と action による遷移先のみを検証する。
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
    fun `開始画面はホーム(S-02)である`() {
        assertEquals(R.id.homeFragment, currentId())
    }

    @Test
    fun `ホームから登録方法選択を経てカメラOCRフローに入り登録完了でホームへ戻る`() {
        navController.navigate(R.id.action_home_to_register_method)
        assertEquals(R.id.registerMethodFragment, currentId())
        navController.navigate(R.id.action_register_method_to_camera)
        assertEquals(R.id.cameraFragment, currentId())
        navController.navigate(R.id.action_camera_to_ocr_result)
        assertEquals(R.id.ocrResultFragment, currentId())
        // 「この内容で登録」→ 保存成功 → ホームへ戻る（S-06a→S-02）
        assertTrue(navController.popBackStack(R.id.homeFragment, false))
        assertEquals(R.id.homeFragment, currentId())
    }

    @Test
    fun `ホームから登録方法選択を経て手入力に入り登録完了でホームへ戻る`() {
        navController.navigate(R.id.action_home_to_register_method)
        navController.navigate(R.id.action_register_method_to_manual_entry)
        assertEquals(R.id.manualEntryFragment, currentId())
        // 「登録する」→ 保存成功 → ホームへ戻る（S-06b→S-02）
        assertTrue(navController.popBackStack(R.id.homeFragment, false))
        assertEquals(R.id.homeFragment, currentId())
    }

    @Test
    fun `ホームから項目一覧を経て項目グラフへ遷移できる`() {
        navController.navigate(R.id.action_home_to_item_list)
        assertEquals(R.id.itemListFragment, currentId())
        navController.navigate(R.id.action_item_list_to_trend_graph, bundleOf("itemName" to "BMI"))
        assertEquals(R.id.trendGraphFragment, currentId())
    }

    @Test
    fun `ホームからお問い合わせへ遷移できる`() {
        navController.navigate(R.id.action_home_to_contact)
        assertEquals(R.id.contactFragment, currentId())
    }

    @Test
    fun `メニューから記録一覧と記録詳細とグラフの既存動線が維持される`() {
        navController.navigate(R.id.action_home_to_record_list)
        assertEquals(R.id.mainFragment, currentId())
        navController.navigate(R.id.action_main_to_record_detail, bundleOf("recordId" to 1L))
        assertEquals(R.id.recordDetailFragment, currentId())
        navController.navigate(R.id.action_record_detail_to_trend_graph, bundleOf("itemName" to "BMI"))
        assertEquals(R.id.trendGraphFragment, currentId())
    }

    @Test
    fun `メニューから項目マスターと基準値外一覧へ遷移できる`() {
        navController.navigate(R.id.action_home_to_item_master)
        assertEquals(R.id.itemMasterFragment, currentId())

        moveTo(R.id.homeFragment)
        navController.navigate(R.id.action_home_to_abnormal_list)
        assertEquals(R.id.abnormalListFragment, currentId())
    }

    @Test
    fun `記録一覧のFABから登録方法選択へ遷移できる`() {
        navController.navigate(R.id.action_home_to_record_list)
        navController.navigate(R.id.action_main_to_register_method)
        assertEquals(R.id.registerMethodFragment, currentId())
    }

    @Test
    fun `ログアウトでホームが除去されログイン成功でホームのみのスタックになる`() {
        navController.navigate(R.id.action_home_to_login)
        assertEquals(R.id.loginFragment, currentId())
        assertFalse(navController.popBackStack())

        moveTo(R.id.loginFragment)
        navController.navigate(R.id.action_login_to_home)
        assertEquals(R.id.homeFragment, currentId())
    }
}
