package com.rokusodo.healthcheckup.data.db.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bH\'J\u001c\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u001c\u0010\f\u001a\b\u0012\u0004\u0012\u00020\n0\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\t2\u0006\u0010\u000f\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u001c\u0010\u0012\u001a\u00020\u00032\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u00a7@\u00a2\u0006\u0002\u0010\u0014\u00a8\u0006\u0015"}, d2 = {"Lcom/rokusodo/healthcheckup/data/db/dao/ExaminationItemDao;", "", "deleteByRecordId", "", "recordId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllAbnormalItems", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationItem;", "getByRecordId", "getByRecordIdOnce", "getItemTrendByName", "Lcom/rokusodo/healthcheckup/data/db/dao/ItemTrend;", "itemName", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertAll", "items", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface ExaminationItemDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertAll(@org.jetbrains.annotations.NotNull()
    java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem> items, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM examination_items WHERE recordId = :recordId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteByRecordId(long recordId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM examination_items WHERE recordId = :recordId ORDER BY id ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem>> getByRecordId(long recordId);
    
    /**
     * 特定の項目名を持つ検査項目を日付昇順で取得（グラフ用）
     */
    @androidx.room.Query(value = "\n        SELECT ei.id, ei.recordId, ei.itemName, ei.value, ei.unit,\n               ei.referenceMin, ei.referenceMax, ei.isAbnormal,\n               er.date as recordDate\n        FROM examination_items ei\n        INNER JOIN examination_records er ON ei.recordId = er.id\n        WHERE ei.itemName = :itemName\n        ORDER BY er.date ASC\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getItemTrendByName(@org.jetbrains.annotations.NotNull()
    java.lang.String itemName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rokusodo.healthcheckup.data.db.dao.ItemTrend>> $completion);
    
    /**
     * isAbnormal = true の全検査項目を新しい順で取得
     */
    @androidx.room.Query(value = "SELECT * FROM examination_items WHERE isAbnormal = 1 ORDER BY id DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem>> getAllAbnormalItems();
    
    /**
     * 指定した recordId の検査項目を一度だけ取得（通知判定用）
     */
    @androidx.room.Query(value = "SELECT * FROM examination_items WHERE recordId = :recordId ORDER BY id ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getByRecordIdOnce(long recordId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem>> $completion);
}