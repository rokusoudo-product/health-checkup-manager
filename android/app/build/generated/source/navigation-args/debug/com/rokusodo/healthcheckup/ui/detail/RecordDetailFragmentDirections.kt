package com.rokusodo.healthcheckup.ui.detail

import android.os.Bundle
import androidx.navigation.NavDirections
import com.rokusodo.healthcheckup.R
import kotlin.Int
import kotlin.String

public class RecordDetailFragmentDirections private constructor() {
  private data class ActionRecordDetailToTrendGraph(
    public val itemName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_record_detail_to_trend_graph

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("itemName", this.itemName)
        return result
      }
  }

  public companion object {
    public fun actionRecordDetailToTrendGraph(itemName: String): NavDirections =
        ActionRecordDetailToTrendGraph(itemName)
  }
}
