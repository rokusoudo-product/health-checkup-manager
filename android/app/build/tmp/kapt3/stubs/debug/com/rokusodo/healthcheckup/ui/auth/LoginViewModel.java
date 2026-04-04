package com.rokusodo.healthcheckup.ui.auth;

/**
 * ログイン画面の ViewModel。
 * Firebase Auth を使った Google SSO の状態を管理する。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0001\u0011B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u0010R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0012"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "_loginState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "loginState", "Lkotlinx/coroutines/flow/StateFlow;", "getLoginState", "()Lkotlinx/coroutines/flow/StateFlow;", "resetState", "", "signIn", "idToken", "", "LoginState", "app_debug"})
public final class LoginViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState> _loginState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState> loginState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.auth.FirebaseAuth auth = null;
    
    public LoginViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState> getLoginState() {
        return null;
    }
    
    /**
     * Google ID トークンを使って Firebase Auth でサインインする。
     */
    public final void signIn(@org.jetbrains.annotations.NotNull()
    java.lang.String idToken) {
    }
    
    public final void resetState() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u0003\u0004\u0005\u0006B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0004\u0007\b\t\n\u00a8\u0006\u000b"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState;", "", "()V", "Error", "Idle", "Loading", "Success", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState$Error;", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState$Idle;", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState$Loading;", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState$Success;", "app_debug"})
    public static abstract class LoginState {
        
        private LoginState() {
            super();
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState$Error;", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState;", "message", "", "(Ljava/lang/String;)V", "getMessage", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
        public static final class Error extends com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState {
            @org.jetbrains.annotations.NotNull()
            private final java.lang.String message = null;
            
            public Error(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String getMessage() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.lang.String component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState.Error copy(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
                return null;
            }
            
            @java.lang.Override()
            public boolean equals(@org.jetbrains.annotations.Nullable()
            java.lang.Object other) {
                return false;
            }
            
            @java.lang.Override()
            public int hashCode() {
                return 0;
            }
            
            @java.lang.Override()
            @org.jetbrains.annotations.NotNull()
            public java.lang.String toString() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState$Idle;", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState;", "()V", "app_debug"})
        public static final class Idle extends com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState {
            @org.jetbrains.annotations.NotNull()
            public static final com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState.Idle INSTANCE = null;
            
            private Idle() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState$Loading;", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState;", "()V", "app_debug"})
        public static final class Loading extends com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState {
            @org.jetbrains.annotations.NotNull()
            public static final com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState.Loading INSTANCE = null;
            
            private Loading() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState$Success;", "Lcom/rokusodo/healthcheckup/ui/auth/LoginViewModel$LoginState;", "()V", "app_debug"})
        public static final class Success extends com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState {
            @org.jetbrains.annotations.NotNull()
            public static final com.rokusodo.healthcheckup.ui.auth.LoginViewModel.LoginState.Success INSTANCE = null;
            
            private Success() {
            }
        }
    }
}