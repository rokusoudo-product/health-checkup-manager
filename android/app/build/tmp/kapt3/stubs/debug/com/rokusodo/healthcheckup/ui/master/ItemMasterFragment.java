package com.rokusodo.healthcheckup.ui.master;

/**
 * 項目マスター管理画面。
 * 検査項目ごとの基準値（参考値）を編集・追加できる。
 * 薬事法対応: この基準値は医療診断に使用しない。表示・ハイライトのみ。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0010\u001a\u00020\u0011H\u0002J$\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u0011H\u0016J\u001a\u0010\u001b\u001a\u00020\u00112\u0006\u0010\u001c\u001a\u00020\u00132\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001d\u001a\u00020\u0011H\u0002J\b\u0010\u001e\u001a\u00020\u0011H\u0002J\u0012\u0010\u001f\u001a\u00020\u00112\b\u0010 \u001a\u0004\u0018\u00010!H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\r\u00a8\u0006\""}, d2 = {"Lcom/rokusodo/healthcheckup/ui/master/ItemMasterFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/rokusodo/healthcheckup/databinding/FragmentItemMasterBinding;", "binding", "getBinding", "()Lcom/rokusodo/healthcheckup/databinding/FragmentItemMasterBinding;", "itemMasterAdapter", "Lcom/rokusodo/healthcheckup/ui/master/ItemMasterAdapter;", "viewModel", "Lcom/rokusodo/healthcheckup/ui/master/ItemMasterViewModel;", "getViewModel", "()Lcom/rokusodo/healthcheckup/ui/master/ItemMasterViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "observeMasters", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "setupFab", "setupRecyclerView", "showEditDialog", "existing", "Lcom/rokusodo/healthcheckup/data/db/entity/ItemMaster;", "app_debug"})
public final class ItemMasterFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.rokusodo.healthcheckup.databinding.FragmentItemMasterBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private com.rokusodo.healthcheckup.ui.master.ItemMasterAdapter itemMasterAdapter;
    
    public ItemMasterFragment() {
        super();
    }
    
    private final com.rokusodo.healthcheckup.databinding.FragmentItemMasterBinding getBinding() {
        return null;
    }
    
    private final com.rokusodo.healthcheckup.ui.master.ItemMasterViewModel getViewModel() {
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
    
    private final void setupFab() {
    }
    
    private final void observeMasters() {
    }
    
    private final void showEditDialog(com.rokusodo.healthcheckup.data.db.entity.ItemMaster existing) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
}