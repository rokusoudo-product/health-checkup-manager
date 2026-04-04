package com.rokusodo.healthcheckup.ui.main;

/**
 * 診断記録一覧の RecyclerView アダプター。
 * DiffUtil.ItemCallback を使用してリスト差分更新を行う。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u0012\u0012\u0004\u0012\u00020\u0002\u0012\b\u0012\u00060\u0003R\u00020\u00000\u0001:\u0003\u0011\u0012\u0013B\u0019\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u00a2\u0006\u0002\u0010\bJ\u001c\u0010\t\u001a\u00020\u00072\n\u0010\n\u001a\u00060\u0003R\u00020\u00002\u0006\u0010\u000b\u001a\u00020\fH\u0016J\u001c\u0010\r\u001a\u00060\u0003R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\fH\u0016R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter;", "Landroidx/recyclerview/widget/ListAdapter;", "Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter$RecordWithAbnormalCount;", "Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter$ViewHolder;", "onItemClick", "Lkotlin/Function1;", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationRecord;", "", "(Lkotlin/jvm/functions/Function1;)V", "onBindViewHolder", "holder", "position", "", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "DiffCallback", "RecordWithAbnormalCount", "ViewHolder", "app_debug"})
public final class RecordListAdapter extends androidx.recyclerview.widget.ListAdapter<com.rokusodo.healthcheckup.ui.main.RecordListAdapter.RecordWithAbnormalCount, com.rokusodo.healthcheckup.ui.main.RecordListAdapter.ViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord, kotlin.Unit> onItemClick = null;
    
    public RecordListAdapter(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord, kotlin.Unit> onItemClick) {
        super(null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.rokusodo.healthcheckup.ui.main.RecordListAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.rokusodo.healthcheckup.ui.main.RecordListAdapter.ViewHolder holder, int position) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016J\u0018\u0010\b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016\u00a8\u0006\t"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter$DiffCallback;", "Landroidx/recyclerview/widget/DiffUtil$ItemCallback;", "Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter$RecordWithAbnormalCount;", "()V", "areContentsTheSame", "", "oldItem", "newItem", "areItemsTheSame", "app_debug"})
    public static final class DiffCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<com.rokusodo.healthcheckup.ui.main.RecordListAdapter.RecordWithAbnormalCount> {
        
        public DiffCallback() {
            super();
        }
        
        @java.lang.Override()
        public boolean areItemsTheSame(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.ui.main.RecordListAdapter.RecordWithAbnormalCount oldItem, @org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.ui.main.RecordListAdapter.RecordWithAbnormalCount newItem) {
            return false;
        }
        
        @java.lang.Override()
        public boolean areContentsTheSame(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.ui.main.RecordListAdapter.RecordWithAbnormalCount oldItem, @org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.ui.main.RecordListAdapter.RecordWithAbnormalCount newItem) {
            return false;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\f\u001a\u00020\u0005H\u00c6\u0003J\u001d\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0011\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u0012\u001a\u00020\u0013H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0014"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter$RecordWithAbnormalCount;", "", "record", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationRecord;", "abnormalCount", "", "(Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationRecord;I)V", "getAbnormalCount", "()I", "getRecord", "()Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationRecord;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
    public static final class RecordWithAbnormalCount {
        @org.jetbrains.annotations.NotNull()
        private final com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord record = null;
        private final int abnormalCount = 0;
        
        public RecordWithAbnormalCount(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord record, int abnormalCount) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord getRecord() {
            return null;
        }
        
        public final int getAbnormalCount() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rokusodo.healthcheckup.ui.main.RecordListAdapter.RecordWithAbnormalCount copy(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord record, int abnormalCount) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/rokusodo/healthcheckup/databinding/ItemRecordBinding;", "(Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter;Lcom/rokusodo/healthcheckup/databinding/ItemRecordBinding;)V", "bind", "", "item", "Lcom/rokusodo/healthcheckup/ui/main/RecordListAdapter$RecordWithAbnormalCount;", "app_debug"})
    public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.rokusodo.healthcheckup.databinding.ItemRecordBinding binding = null;
        
        public ViewHolder(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.databinding.ItemRecordBinding binding) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.ui.main.RecordListAdapter.RecordWithAbnormalCount item) {
        }
    }
}