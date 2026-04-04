package com.rokusodo.healthcheckup

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import kotlin.Int
import kotlin.Long

public class MainFragmentDirections private constructor() {
  private data class ActionMainToRecordDetail(
    public val recordId: Long,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_main_to_record_detail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putLong("recordId", this.recordId)
        return result
      }
  }

  public companion object {
    public fun actionMainToCamera(): NavDirections =
        ActionOnlyNavDirections(R.id.action_main_to_camera)

    public fun actionMainToRecordDetail(recordId: Long): NavDirections =
        ActionMainToRecordDetail(recordId)

    public fun actionMainToItemMaster(): NavDirections =
        ActionOnlyNavDirections(R.id.action_main_to_item_master)

    public fun actionMainToAbnormalList(): NavDirections =
        ActionOnlyNavDirections(R.id.action_main_to_abnormal_list)

    public fun actionMainToLogin(): NavDirections =
        ActionOnlyNavDirections(R.id.action_main_to_login)
  }
}
