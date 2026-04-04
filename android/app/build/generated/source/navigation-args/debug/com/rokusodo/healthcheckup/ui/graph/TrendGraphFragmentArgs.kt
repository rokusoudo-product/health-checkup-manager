package com.rokusodo.healthcheckup.ui.graph

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class TrendGraphFragmentArgs(
  public val itemName: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("itemName", this.itemName)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("itemName", this.itemName)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): TrendGraphFragmentArgs {
      bundle.setClassLoader(TrendGraphFragmentArgs::class.java.classLoader)
      val __itemName : String?
      if (bundle.containsKey("itemName")) {
        __itemName = bundle.getString("itemName")
        if (__itemName == null) {
          throw IllegalArgumentException("Argument \"itemName\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"itemName\" is missing and does not have an android:defaultValue")
      }
      return TrendGraphFragmentArgs(__itemName)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): TrendGraphFragmentArgs {
      val __itemName : String?
      if (savedStateHandle.contains("itemName")) {
        __itemName = savedStateHandle["itemName"]
        if (__itemName == null) {
          throw IllegalArgumentException("Argument \"itemName\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"itemName\" is missing and does not have an android:defaultValue")
      }
      return TrendGraphFragmentArgs(__itemName)
    }
  }
}
