package com.rokusodo.healthcheckup.ui.detail

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Long
import kotlin.jvm.JvmStatic

public data class RecordDetailFragmentArgs(
  public val recordId: Long,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putLong("recordId", this.recordId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("recordId", this.recordId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): RecordDetailFragmentArgs {
      bundle.setClassLoader(RecordDetailFragmentArgs::class.java.classLoader)
      val __recordId : Long
      if (bundle.containsKey("recordId")) {
        __recordId = bundle.getLong("recordId")
      } else {
        throw IllegalArgumentException("Required argument \"recordId\" is missing and does not have an android:defaultValue")
      }
      return RecordDetailFragmentArgs(__recordId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): RecordDetailFragmentArgs {
      val __recordId : Long?
      if (savedStateHandle.contains("recordId")) {
        __recordId = savedStateHandle["recordId"]
        if (__recordId == null) {
          throw IllegalArgumentException("Argument \"recordId\" of type long does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"recordId\" is missing and does not have an android:defaultValue")
      }
      return RecordDetailFragmentArgs(__recordId)
    }
  }
}
