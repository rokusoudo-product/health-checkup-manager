package com.rokusodo.healthcheckup.ui.graph;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0001\u0013B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0011\u001a\u00020\u0012H\u0002R\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006\u0014"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/graph/TrendGraphViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/rokusodo/healthcheckup/data/repository/HealthRepository;", "itemName", "", "(Lcom/rokusodo/healthcheckup/data/repository/HealthRepository;Ljava/lang/String;)V", "_trends", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rokusodo/healthcheckup/data/db/dao/ItemTrend;", "getItemName", "()Ljava/lang/String;", "trends", "Lkotlinx/coroutines/flow/StateFlow;", "getTrends", "()Lkotlinx/coroutines/flow/StateFlow;", "loadTrends", "", "Factory", "app_debug"})
public final class TrendGraphViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.rokusodo.healthcheckup.data.repository.HealthRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String itemName = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rokusodo.healthcheckup.data.db.dao.ItemTrend>> _trends = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rokusodo.healthcheckup.data.db.dao.ItemTrend>> trends = null;
    
    public TrendGraphViewModel(@org.jetbrains.annotations.NotNull()
    com.rokusodo.healthcheckup.data.repository.HealthRepository repository, @org.jetbrains.annotations.NotNull()
    java.lang.String itemName) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getItemName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rokusodo.healthcheckup.data.db.dao.ItemTrend>> getTrends() {
        return null;
    }
    
    private final void loadTrends() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J%\u0010\u0007\u001a\u0002H\b\"\b\b\u0000\u0010\b*\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u0002H\b0\u000bH\u0016\u00a2\u0006\u0002\u0010\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/graph/TrendGraphViewModel$Factory;", "Landroidx/lifecycle/ViewModelProvider$Factory;", "repository", "Lcom/rokusodo/healthcheckup/data/repository/HealthRepository;", "itemName", "", "(Lcom/rokusodo/healthcheckup/data/repository/HealthRepository;Ljava/lang/String;)V", "create", "T", "Landroidx/lifecycle/ViewModel;", "modelClass", "Ljava/lang/Class;", "(Ljava/lang/Class;)Landroidx/lifecycle/ViewModel;", "app_debug"})
    public static final class Factory implements androidx.lifecycle.ViewModelProvider.Factory {
        @org.jetbrains.annotations.NotNull()
        private final com.rokusodo.healthcheckup.data.repository.HealthRepository repository = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String itemName = null;
        
        public Factory(@org.jetbrains.annotations.NotNull()
        com.rokusodo.healthcheckup.data.repository.HealthRepository repository, @org.jetbrains.annotations.NotNull()
        java.lang.String itemName) {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public <T extends androidx.lifecycle.ViewModel>T create(@org.jetbrains.annotations.NotNull()
        java.lang.Class<T> modelClass) {
            return null;
        }
    }
}