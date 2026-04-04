package com.rokusodo.healthcheckup;

/**
 * OCR結果一覧の RecyclerView アダプター。
 * 各行は「項目名・値・単位」の編集可能フォーム（EditText）で表示する。
 * TextWatcher で編集内容を items に即時反映するため、getItems() は常に最新の値を返す。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0013B\u0013\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0007\u001a\u00020\bH\u0016J\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00050\nJ\u001c\u0010\u000b\u001a\u00020\f2\n\u0010\r\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\bH\u0016J\u001c\u0010\u000f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\bH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/rokusodo/healthcheckup/OcrItemAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/rokusodo/healthcheckup/OcrItemAdapter$OcrItemViewHolder;", "items", "", "Lcom/rokusodo/healthcheckup/OcrItem;", "(Ljava/util/List;)V", "getItemCount", "", "getItems", "", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "OcrItemViewHolder", "app_debug"})
public final class OcrItemAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.rokusodo.healthcheckup.OcrItemAdapter.OcrItemViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.rokusodo.healthcheckup.OcrItem> items = null;
    
    public OcrItemAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.rokusodo.healthcheckup.OcrItem> items) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.rokusodo.healthcheckup.OcrItemAdapter.OcrItemViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.rokusodo.healthcheckup.OcrItemAdapter.OcrItemViewHolder holder, int position) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    /**
     * 編集済み内容を含む全アイテムを返す。TextWatcher により items は常に最新状態。
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.rokusodo.healthcheckup.OcrItem> getItems() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u001c\u0010\u000f\u001a\u00020\u00062\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\n0\u0011H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/rokusodo/healthcheckup/OcrItemAdapter$OcrItemViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/rokusodo/healthcheckup/databinding/ItemOcrResultBinding;", "(Lcom/rokusodo/healthcheckup/OcrItemAdapter;Lcom/rokusodo/healthcheckup/databinding/ItemOcrResultBinding;)V", "nameWatcher", "Landroid/text/TextWatcher;", "unitWatcher", "valueWatcher", "bind", "", "item", "Lcom/rokusodo/healthcheckup/OcrItem;", "position", "", "simpleWatcher", "onChange", "Lkotlin/Function1;", "", "app_debug"})
    public final class OcrItemViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.rokusodo.healthcheckup.databinding.ItemOcrResultBinding binding = null;
        @org.jetbrains.annotations.Nullable()
        private android.text.TextWatcher nameWatcher;
        @org.jetbrains.annotations.Nullable()
        private android.text.TextWatcher valueWatcher;
        @org.jetbrains.annotations.Nullable()
        private android.text.TextWatcher unitWatcher;
        
        public OcrItemViewHolder(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.databinding.ItemOcrResultBinding binding) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.OcrItem item, int position) {
        }
        
        private final android.text.TextWatcher simpleWatcher(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onChange) {
            return null;
        }
    }
}