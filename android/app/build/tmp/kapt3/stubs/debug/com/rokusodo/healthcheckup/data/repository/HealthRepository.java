package com.rokusodo.healthcheckup.data.repository;

/**
 * 健康診断データの Repository。
 * Room DB への読み書きを一元管理する。
 * 薬事法対応: isAbnormal は表示のみに使用し、医療診断を目的としない。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0012\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006J\u0012\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u00070\u0006J\u0012\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00070\u0006J\u001c\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00072\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011J\u001a\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u00062\u0006\u0010\u0013\u001a\u00020\u0014J\u001c\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\u0006\u0010\u0013\u001a\u00020\u0014H\u0086@\u00a2\u0006\u0002\u0010\u0016J,\u0010\u0017\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u00102\u0006\u0010\u0019\u001a\u00020\u00102\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0007H\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u0016\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010 R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/rokusodo/healthcheckup/data/repository/HealthRepository;", "", "db", "Lcom/rokusodo/healthcheckup/data/db/HealthCheckupDatabase;", "(Lcom/rokusodo/healthcheckup/data/db/HealthCheckupDatabase;)V", "getAllAbnormalItems", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationItem;", "getAllMasters", "Lcom/rokusodo/healthcheckup/data/db/entity/ItemMaster;", "getAllRecords", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationRecord;", "getItemTrend", "Lcom/rokusodo/healthcheckup/data/db/dao/ItemTrend;", "itemName", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getItemsForRecord", "recordId", "", "getItemsForRecordSnapshot", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveRecord", "date", "facility", "items", "Lcom/rokusodo/healthcheckup/OcrItem;", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsertMaster", "", "master", "(Lcom/rokusodo/healthcheckup/data/db/entity/ItemMaster;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class HealthRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.rokusodo.healthcheckup.data.db.HealthCheckupDatabase db = null;
    
    public HealthRepository(@org.jetbrains.annotations.NotNull()
    com.rokusodo.healthcheckup.data.db.HealthCheckupDatabase db) {
        super();
    }
    
    /**
     * OCR結果を診断記録として保存する。
     * @return 生成された ExaminationRecord の id
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveRecord(@org.jetbrains.annotations.NotNull()
    java.lang.String date, @org.jetbrains.annotations.NotNull()
    java.lang.String facility, @org.jetbrains.annotations.NotNull()
    java.util.List<com.rokusodo.healthcheckup.OcrItem> items, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationRecord>> getAllRecords() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem>> getItemsForRecord(long recordId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.rokusodo.healthcheckup.data.db.entity.ItemMaster>> getAllMasters() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object upsertMaster(@org.jetbrains.annotations.NotNull()
    com.rokusodo.healthcheckup.data.db.entity.ItemMaster master, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * 指定した recordId の検査項目を一度だけ取得する（通知判定用）。
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getItemsForRecordSnapshot(long recordId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem>> $completion) {
        return null;
    }
    
    /**
     * 指定した項目名の経年データを日付昇順で取得する（グラフ表示用）。
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getItemTrend(@org.jetbrains.annotations.NotNull()
    java.lang.String itemName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rokusodo.healthcheckup.data.db.dao.ItemTrend>> $completion) {
        return null;
    }
    
    /**
     * 基準値外の全検査項目を新しい順で取得する（Flow）。
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem>> getAllAbnormalItems() {
        return null;
    }
}