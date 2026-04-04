package com.rokusodo.healthcheckup

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class OcrResultFragmentArgs(
  public val ocrText: String = "",
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("ocrText", this.ocrText)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("ocrText", this.ocrText)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): OcrResultFragmentArgs {
      bundle.setClassLoader(OcrResultFragmentArgs::class.java.classLoader)
      val __ocrText : String?
      if (bundle.containsKey("ocrText")) {
        __ocrText = bundle.getString("ocrText")
        if (__ocrText == null) {
          throw IllegalArgumentException("Argument \"ocrText\" is marked as non-null but was passed a null value.")
        }
      } else {
        __ocrText = ""
      }
      return OcrResultFragmentArgs(__ocrText)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): OcrResultFragmentArgs {
      val __ocrText : String?
      if (savedStateHandle.contains("ocrText")) {
        __ocrText = savedStateHandle["ocrText"]
        if (__ocrText == null) {
          throw IllegalArgumentException("Argument \"ocrText\" is marked as non-null but was passed a null value")
        }
      } else {
        __ocrText = ""
      }
      return OcrResultFragmentArgs(__ocrText)
    }
  }
}
