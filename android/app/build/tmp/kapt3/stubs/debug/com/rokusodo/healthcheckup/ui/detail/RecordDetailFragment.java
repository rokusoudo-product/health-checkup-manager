package com.rokusodo.healthcheckup.ui.detail;

/**
 * 診断記録詳細画面。
 * 検査項目一覧を表示し、基準値外の項目は赤くハイライトする。
 * 各項目行の「グラフ」ボタンで経年グラフ画面へ遷移できる。
 * 薬事法対応: 診断・アドバイス・医療判断のテキストを一切含まない。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0016\u001a\u00020\u0017H\u0002J$\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010 \u001a\u00020\u0017H\u0016J\u001a\u0010!\u001a\u00020\u00172\u0006\u0010\"\u001a\u00020\u00192\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010#\u001a\u00020\u0017H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u0014\u0010\u000b\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\rR\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u0015\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006$"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/detail/RecordDetailFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/rokusodo/healthcheckup/databinding/FragmentRecordDetailBinding;", "args", "Lcom/rokusodo/healthcheckup/ui/detail/RecordDetailFragmentArgs;", "getArgs", "()Lcom/rokusodo/healthcheckup/ui/detail/RecordDetailFragmentArgs;", "args$delegate", "Landroidx/navigation/NavArgsLazy;", "binding", "getBinding", "()Lcom/rokusodo/healthcheckup/databinding/FragmentRecordDetailBinding;", "recordItemAdapter", "Lcom/rokusodo/healthcheckup/ui/detail/RecordItemAdapter;", "viewModel", "Lcom/rokusodo/healthcheckup/ui/detail/RecordDetailViewModel;", "getViewModel", "()Lcom/rokusodo/healthcheckup/ui/detail/RecordDetailViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "observeItems", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "setupRecyclerView", "app_debug"})
public final class RecordDetailFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.rokusodo.healthcheckup.databinding.FragmentRecordDetailBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final androidx.navigation.NavArgsLazy args$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private com.rokusodo.healthcheckup.ui.detail.RecordItemAdapter recordItemAdapter;
    
    public RecordDetailFragment() {
        super();
    }
    
    private final com.rokusodo.healthcheckup.databinding.FragmentRecordDetailBinding getBinding() {
        return null;
    }
    
    private final com.rokusodo.healthcheckup.ui.detail.RecordDetailFragmentArgs getArgs() {
        return null;
    }
    
    private final com.rokusodo.healthcheckup.ui.detail.RecordDetailViewModel getViewModel() {
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
    
    private final void setupRecyclerView() {
    }
    
    private final void observeItems() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
}