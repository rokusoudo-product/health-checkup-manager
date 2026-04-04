package com.rokusodo.healthcheckup.ui.graph;

/**
 * 経年グラフ画面。
 * 指定した検査項目の経年変化を折れ線グラフで表示する。
 * 薬事法対応: グラフタイトルは項目名と単位のみ。「正常」「異常」などの判定テキストなし。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0014\u001a\u00020\u0015H\u0002J$\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0016J\b\u0010\u001e\u001a\u00020\u0015H\u0016J\u001a\u0010\u001f\u001a\u00020\u00152\u0006\u0010 \u001a\u00020\u00172\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0016J\u0016\u0010!\u001a\u00020\u00152\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020$0#H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u0014\u0010\u000b\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\rR\u001b\u0010\u000e\u001a\u00020\u000f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\u0013\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006%"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/graph/TrendGraphFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/rokusodo/healthcheckup/databinding/FragmentTrendGraphBinding;", "args", "Lcom/rokusodo/healthcheckup/ui/graph/TrendGraphFragmentArgs;", "getArgs", "()Lcom/rokusodo/healthcheckup/ui/graph/TrendGraphFragmentArgs;", "args$delegate", "Landroidx/navigation/NavArgsLazy;", "binding", "getBinding", "()Lcom/rokusodo/healthcheckup/databinding/FragmentTrendGraphBinding;", "viewModel", "Lcom/rokusodo/healthcheckup/ui/graph/TrendGraphViewModel;", "getViewModel", "()Lcom/rokusodo/healthcheckup/ui/graph/TrendGraphViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "observeTrends", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "renderGraph", "trends", "", "Lcom/rokusodo/healthcheckup/data/db/dao/ItemTrend;", "app_debug"})
public final class TrendGraphFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.rokusodo.healthcheckup.databinding.FragmentTrendGraphBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final androidx.navigation.NavArgsLazy args$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    
    public TrendGraphFragment() {
        super();
    }
    
    private final com.rokusodo.healthcheckup.databinding.FragmentTrendGraphBinding getBinding() {
        return null;
    }
    
    private final com.rokusodo.healthcheckup.ui.graph.TrendGraphFragmentArgs getArgs() {
        return null;
    }
    
    private final com.rokusodo.healthcheckup.ui.graph.TrendGraphViewModel getViewModel() {
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
    
    private final void observeTrends() {
    }
    
    private final void renderGraph(java.util.List<com.rokusodo.healthcheckup.data.db.dao.ItemTrend> trends) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
}