package com.rokusodo.healthcheckup;

/**
 * OCR結果を表示し、各行（項目名・値・単位）を編集可能なフォームで提示するフラグメント。
 * 薬事法対応: 医療アドバイス・改善提案のテキストを一切含まない。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\n\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u001a\u001a\u00020\u001bH\u0002J$\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010!2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0016J\b\u0010$\u001a\u00020\u001bH\u0016J\u001a\u0010%\u001a\u00020\u001b2\u0006\u0010&\u001a\u00020\u001d2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0016J\u001c\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020)0(2\u0006\u0010*\u001a\u00020\u0012H\u0002J\u0010\u0010+\u001a\u00020\u001b2\u0006\u0010,\u001a\u00020)H\u0002J\u0010\u0010-\u001a\u00020\u001b2\u0006\u0010.\u001a\u00020\u0012H\u0002J\b\u0010/\u001a\u00020\u001bH\u0002J\b\u00100\u001a\u00020\u001bH\u0002J\u0010\u00101\u001a\u00020\u001b2\u0006\u00102\u001a\u00020\u0012H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u0014\u0010\u000b\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\rR\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0010\u001a\u0010\u0012\f\u0012\n \u0013*\u0004\u0018\u00010\u00120\u00120\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0014\u001a\u00020\u00158BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0018\u0010\u0019\u001a\u0004\b\u0016\u0010\u0017\u00a8\u00063"}, d2 = {"Lcom/rokusodo/healthcheckup/OcrResultFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/rokusodo/healthcheckup/databinding/FragmentOcrResultBinding;", "args", "Lcom/rokusodo/healthcheckup/OcrResultFragmentArgs;", "getArgs", "()Lcom/rokusodo/healthcheckup/OcrResultFragmentArgs;", "args$delegate", "Landroidx/navigation/NavArgsLazy;", "binding", "getBinding", "()Lcom/rokusodo/healthcheckup/databinding/FragmentOcrResultBinding;", "ocrItemAdapter", "Lcom/rokusodo/healthcheckup/OcrItemAdapter;", "requestNotificationPermission", "Landroidx/activity/result/ActivityResultLauncher;", "", "kotlin.jvm.PlatformType", "viewModel", "Lcom/rokusodo/healthcheckup/ui/ocrresult/OcrResultViewModel;", "getViewModel", "()Lcom/rokusodo/healthcheckup/ui/ocrresult/OcrResultViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "observeSaveState", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "parsePayload", "Lkotlin/Pair;", "Lcom/rokusodo/healthcheckup/OcrAnalyzer$OcrError;", "payload", "setupErrorMessage", "errorType", "setupRecyclerView", "ocrText", "setupSaveButton", "showDatePickerDialog", "showFacilityInputDialog", "date", "app_debug"})
public final class OcrResultFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.rokusodo.healthcheckup.databinding.FragmentOcrResultBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final androidx.navigation.NavArgsLazy args$delegate = null;
    private com.rokusodo.healthcheckup.OcrItemAdapter ocrItemAdapter;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> requestNotificationPermission = null;
    
    public OcrResultFragment() {
        super();
    }
    
    private final com.rokusodo.healthcheckup.databinding.FragmentOcrResultBinding getBinding() {
        return null;
    }
    
    private final com.rokusodo.healthcheckup.OcrResultFragmentArgs getArgs() {
        return null;
    }
    
    private final com.rokusodo.healthcheckup.ui.ocrresult.OcrResultViewModel getViewModel() {
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
    
    /**
     * CameraFragment からのペイロードを解析し、OCRテキストとエラー種別に分離する。
     */
    private final kotlin.Pair<java.lang.String, com.rokusodo.healthcheckup.OcrAnalyzer.OcrError> parsePayload(java.lang.String payload) {
        return null;
    }
    
    private final void setupErrorMessage(com.rokusodo.healthcheckup.OcrAnalyzer.OcrError errorType) {
    }
    
    private final void setupRecyclerView(java.lang.String ocrText) {
    }
    
    private final void setupSaveButton() {
    }
    
    private final void showDatePickerDialog() {
    }
    
    private final void showFacilityInputDialog(java.lang.String date) {
    }
    
    private final void observeSaveState() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
}