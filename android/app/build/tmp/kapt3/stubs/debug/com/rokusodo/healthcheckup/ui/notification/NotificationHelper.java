package com.rokusodo.healthcheckup.ui.notification;

/**
 * 基準値外アラート通知ヘルパー。
 * 薬事法対応: 「基準値外の項目があります」という事実のみを通知。改善提案なし。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ$\u0010\u000b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/rokusodo/healthcheckup/ui/notification/NotificationHelper;", "", "()V", "CHANNEL_ID", "", "NOTIFICATION_ID", "", "createNotificationChannel", "", "context", "Landroid/content/Context;", "sendAbnormalAlert", "recordId", "", "abnormalItems", "", "Lcom/rokusodo/healthcheckup/data/db/entity/ExaminationItem;", "app_debug"})
public final class NotificationHelper {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID = "abnormal_alert";
    private static final int NOTIFICATION_ID = 1001;
    @org.jetbrains.annotations.NotNull()
    public static final com.rokusodo.healthcheckup.ui.notification.NotificationHelper INSTANCE = null;
    
    private NotificationHelper() {
        super();
    }
    
    /**
     * 通知チャンネルを作成する。Application の onCreate で呼ぶこと。
     */
    public final void createNotificationChannel(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * 基準値外アラートを通知する。
     * @param recordId 保存された ExaminationRecord の id
     * @param abnormalItems isAbnormal = true の検査項目リスト
     */
    public final void sendAbnormalAlert(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long recordId, @org.jetbrains.annotations.NotNull()
    java.util.List<com.rokusodo.healthcheckup.data.db.entity.ExaminationItem> abnormalItems) {
    }
}