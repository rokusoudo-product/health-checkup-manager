package com.rokusoudo.healthcheckup.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.rokusoudo.healthcheckup.MainActivity
import com.rokusoudo.healthcheckup.R
import com.rokusoudo.healthcheckup.data.db.entity.ExaminationItem

/**
 * 基準値外アラート通知ヘルパー。
 * 薬事法対応: 「基準値外の項目があります」という事実のみを通知。改善提案なし。
 */
object NotificationHelper {

    const val CHANNEL_ID = "abnormal_alert"
    private const val NOTIFICATION_ID = 1001

    /**
     * 通知チャンネルを作成する。Application の onCreate で呼ぶこと。
     */
    fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.notification_channel_name)
        val channel = NotificationChannel(
            CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 基準値外アラートを通知する。
     * @param recordId 保存された ExaminationRecord の id
     * @param abnormalItems isAbnormal = true の検査項目リスト
     */
    fun sendAbnormalAlert(
        context: Context,
        recordId: Long,
        abnormalItems: List<ExaminationItem>
    ) {
        if (abnormalItems.isEmpty()) return

        // 異常項目の itemName を最大3件列挙
        val itemNames = abnormalItems
            .take(3)
            .joinToString("、") { it.itemName }
        val moreCount = abnormalItems.size - 3
        val summaryText = if (moreCount > 0) {
            context.getString(R.string.notification_text_abnormal, "$itemNames 他${moreCount}件")
        } else {
            context.getString(R.string.notification_text_abnormal, itemNames)
        }

        // タップで MainActivity を開く PendingIntent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(summaryText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
