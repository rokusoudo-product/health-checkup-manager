package com.rokusodo.healthcheckup.ui.notification;

/**
 * 基準値外項目一覧の RecyclerView アダプター。
 * 薬事法対応: 「要注意」「危険」などの医療判断テキストは含まない。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u0012\u0012\u0004\u0012\u00020\u0002\u0012\b\u0012\u00060\u0003R\u00020\u00000\u0001:\u0002\u000e\u000fB\u0005\u00a2\u0006\u0002\u0010\u0004J\u001c\u0010\u0005\u001a\u00020\u00062\n\u0010\u0007\u001a\u00060\u0003R\u00020\u00002\u0006\u0010\b\u001a\u00020\tH\u0016J\u001c\u0010\n\u001a\u00060\u0003R\u00020\u00002\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\tH\u0016\u00a8\u0006\u0010"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/notification/AbnormalListAdapter;", "Landroidx/recyclerview/widget/ListAdapter;", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationItem;", "Lcom/rokusodo/healthcheckup/ui/notification/AbnormalListAdapter$ViewHolder;", "()V", "onBindViewHolder", "", "holder", "position", "", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "DiffCallback", "ViewHolder", "app_debug"})
public final class AbnormalListAdapter extends androidx.recyclerview.widget.ListAdapter<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem, com.rokusodo.healthcheckup.ui.notification.AbnormalListAdapter.ViewHolder> {
    
    public AbnormalListAdapter() {
        super(null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.rokusodo.healthcheckup.ui.notification.AbnormalListAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.rokusodo.healthcheckup.ui.notification.AbnormalListAdapter.ViewHolder holder, int position) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016J\u0018\u0010\b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016\u00a8\u0006\t"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/notification/AbnormalListAdapter$DiffCallback;", "Landroidx/recyclerview/widget/DiffUtil$ItemCallback;", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationItem;", "()V", "areContentsTheSame", "", "oldItem", "newItem", "areItemsTheSame", "app_debug"})
    public static final class DiffCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem> {
        
        public DiffCallback() {
            super();
        }
        
        @java.lang.Override()
        public boolean areItemsTheSame(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.data.db.entity.ExaminationItem oldItem, @org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.data.db.entity.ExaminationItem newItem) {
            return false;
        }
        
        @java.lang.Override()
        public boolean areContentsTheSame(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.data.db.entity.ExaminationItem oldItem, @org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.data.db.entity.ExaminationItem newItem) {
            return false;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/notification/AbnormalListAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/rokusodo/healthcheckup/databinding/ItemAbnormalBinding;", "(Lcom/rokusodo/healthcheckup/ui/notification/AbnormalListAdapter;Lcom/rokusodo/healthcheckup/databinding/ItemAbnormalBinding;)V", "bind", "", "item", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationItem;", "app_debug"})
    public final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.rokusodo.healthcheckup.databinding.ItemAbnormalBinding binding = null;
        
        public ViewHolder(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.databinding.ItemAbnormalBinding binding) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.data.db.entity.ExaminationItem item) {
        }
    }
}