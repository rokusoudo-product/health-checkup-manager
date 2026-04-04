package com.rokusodo.healthcheckup.ui.auth

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.rokusodo.healthcheckup.R

public class LoginFragmentDirections private constructor() {
  public companion object {
    public fun actionLoginToMain(): NavDirections =
        ActionOnlyNavDirections(R.id.action_login_to_main)
  }
}
