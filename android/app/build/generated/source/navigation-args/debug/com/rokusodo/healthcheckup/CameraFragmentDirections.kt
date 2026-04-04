package com.rokusodo.healthcheckup

import android.os.Bundle
import androidx.navigation.NavDirections
import kotlin.Int
import kotlin.String

public class CameraFragmentDirections private constructor() {
  private data class ActionCameraToOcrResult(
    public val ocrText: String = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_camera_to_ocr_result

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("ocrText", this.ocrText)
        return result
      }
  }

  public companion object {
    public fun actionCameraToOcrResult(ocrText: String = ""): NavDirections =
        ActionCameraToOcrResult(ocrText)
  }
}
