package com.rokusodo.healthcheckup;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\u0018\u0000 22\u00020\u0001:\u00012B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u001b\u001a\u00020\u001cH\u0002J\u0018\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u00132\u0006\u0010 \u001a\u00020!H\u0002J$\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020%2\b\u0010&\u001a\u0004\u0018\u00010\'2\b\u0010(\u001a\u0004\u0018\u00010)H\u0016J\b\u0010*\u001a\u00020\u001eH\u0016J\b\u0010+\u001a\u00020\u001eH\u0016J\u001a\u0010,\u001a\u00020\u001e2\u0006\u0010-\u001a\u00020#2\b\u0010(\u001a\u0004\u0018\u00010)H\u0016J\b\u0010.\u001a\u00020\u001eH\u0002J\b\u0010/\u001a\u00020\u001eH\u0002J\b\u00100\u001a\u00020\u001eH\u0002J\b\u00101\u001a\u00020\u001eH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0011\u001a\u0010\u0012\f\u0012\n \u0014*\u0004\u0018\u00010\u00130\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0015\u001a\u00020\u00168BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0019\u0010\u001a\u001a\u0004\b\u0017\u0010\u0018\u00a8\u00063"}, d2 = {"Lcom/rokusodo/healthcheckup/CameraFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/rokusodo/healthcheckup/databinding/FragmentCameraBinding;", "binding", "getBinding", "()Lcom/rokusodo/healthcheckup/databinding/FragmentCameraBinding;", "cameraExecutor", "Ljava/util/concurrent/ExecutorService;", "capturedImages", "", "Landroidx/camera/core/ImageProxy;", "imageCapture", "Landroidx/camera/core/ImageCapture;", "ocrScope", "Lkotlinx/coroutines/CoroutineScope;", "requestPermissionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "kotlin.jvm.PlatformType", "textRecognizer", "Lcom/google/mlkit/vision/text/TextRecognizer;", "getTextRecognizer", "()Lcom/google/mlkit/vision/text/TextRecognizer;", "textRecognizer$delegate", "Lkotlin/Lazy;", "hasCameraPermission", "", "navigateToOcrResult", "", "ocrText", "ocrError", "Lcom/rokusodo/healthcheckup/OcrAnalyzer$OcrError;", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onDestroyView", "onViewCreated", "view", "startCamera", "startOcrProcessing", "takePhoto", "updateCapturedCountUI", "Companion", "app_debug"})
public final class CameraFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.rokusodo.healthcheckup.databinding.FragmentCameraBinding _binding;
    private java.util.concurrent.ExecutorService cameraExecutor;
    @org.jetbrains.annotations.Nullable()
    private androidx.camera.core.ImageCapture imageCapture;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<androidx.camera.core.ImageProxy> capturedImages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope ocrScope = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy textRecognizer$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestPermissionLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "CameraFragment";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ERROR_PREFIX_INSUFFICIENT = "##ERROR_INSUFFICIENT##";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ERROR_PREFIX_LOW_CONFIDENCE = "##ERROR_LOW_CONFIDENCE##";
    @org.jetbrains.annotations.NotNull()
    public static final com.rokusodo.healthcheckup.CameraFragment.Companion Companion = null;
    
    public CameraFragment() {
        super();
    }
    
    private final com.rokusodo.healthcheckup.databinding.FragmentCameraBinding getBinding() {
        return null;
    }
    
    private final com.google.mlkit.vision.text.TextRecognizer getTextRecognizer() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final boolean hasCameraPermission() {
        return false;
    }
    
    private final void startCamera() {
    }
    
    private final void takePhoto() {
    }
    
    private final void updateCapturedCountUI() {
    }
    
    /**
     * 撮影リスト内の全画像をML KitでOCR処理し、テキストを結合してOCR結果画面に遷移する。
     * 処理はIOディスパッチャで非同期実行。
     */
    private final void startOcrProcessing() {
    }
    
    private final void navigateToOcrResult(java.lang.String ocrText, com.rokusodo.healthcheckup.OcrAnalyzer.OcrError ocrError) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/rokusodo/healthcheckup/CameraFragment$Companion;", "", "()V", "ERROR_PREFIX_INSUFFICIENT", "", "ERROR_PREFIX_LOW_CONFIDENCE", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}