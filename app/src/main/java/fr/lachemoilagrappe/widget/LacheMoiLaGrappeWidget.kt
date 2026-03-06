package fr.lachemoilagrappe.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.room.Room
import fr.lachemoilagrappe.R
import fr.lachemoilagrappe.data.local.db.LacheMoiLaGrappeDatabase
import fr.lachemoilagrappe.domain.model.CallDecision
import fr.lachemoilagrappe.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class LacheMoiLaGrappeWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "fr.lachemoilagrappe.widget.ACTION_REFRESH"

        /**
         * Trigger an update of all widget instances from anywhere in the app.
         */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, LacheMoiLaGrappeWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val widgetManager = AppWidgetManager.getInstance(context)
            val ids = widgetManager.getAppWidgetIds(
                ComponentName(context, LacheMoiLaGrappeWidget::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, LacheMoiLaGrappeWidget::class.java)
            )
            onUpdate(context, appWidgetManager, ids)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Build the RemoteViews with a loading state first
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Set click intent to open the app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // Set initial text while loading
        views.setTextViewText(
            R.id.widget_blocked_count,
            context.getString(R.string.widget_loading)
        )
        views.setTextViewText(R.id.widget_status, context.getString(R.string.widget_status_active))

        // Push the initial views so the widget is not blank
        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Now fetch the actual count asynchronously
        scope.launch {
            try {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    LacheMoiLaGrappeDatabase::class.java,
                    LacheMoiLaGrappeDatabase.DATABASE_NAME
                ).build()

                val dao = db.callLogDao()

                // Start of today (midnight)
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // Count rejected calls today (all rejection types)
                val rejectedCount = dao.getCallCountByDecisionSince(
                    CallDecision.REJECTED, todayStart
                )
                val telemarketerCount = dao.getCallCountByDecisionSince(
                    CallDecision.REJECTED_TELEMARKETER, todayStart
                )
                val hiddenCount = dao.getCallCountByDecisionSince(
                    CallDecision.REJECTED_HIDDEN, todayStart
                )
                val blockedCount = dao.getCallCountByDecisionSince(
                    CallDecision.BLOCKED, todayStart
                )

                val totalBlocked = rejectedCount + telemarketerCount +
                        hiddenCount + blockedCount

                // Check if filtering is enabled via DataStore
                val prefs = context.applicationContext.getSharedPreferences(
                    "widget_cache", Context.MODE_PRIVATE
                )
                // We read the DataStore preference file directly is not practical,
                // so we check the call screening role as a proxy for "active".
                // For simplicity, we assume active if the app has been set up.
                val isActive = isFilterActive(context)

                // Update views with actual data
                val updatedViews = RemoteViews(context.packageName, R.layout.widget_layout)
                updatedViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                val countText = context.resources.getQuantityString(
                    R.plurals.widget_blocked_today,
                    totalBlocked,
                    totalBlocked
                )
                updatedViews.setTextViewText(R.id.widget_blocked_count, countText)

                if (isActive) {
                    updatedViews.setTextViewText(
                        R.id.widget_status,
                        context.getString(R.string.widget_status_active)
                    )
                    updatedViews.setInt(
                        R.id.widget_status,
                        "setBackgroundResource",
                        R.drawable.widget_status_badge
                    )
                } else {
                    updatedViews.setTextViewText(
                        R.id.widget_status,
                        context.getString(R.string.widget_status_inactive)
                    )
                    updatedViews.setInt(
                        R.id.widget_status,
                        "setBackgroundResource",
                        R.drawable.widget_status_badge_inactive
                    )
                }

                appWidgetManager.updateAppWidget(appWidgetId, updatedViews)

                db.close()
            } catch (e: Exception) {
                // On error, show zero count
                val errorViews = RemoteViews(context.packageName, R.layout.widget_layout)
                errorViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                val countText = context.resources.getQuantityString(
                    R.plurals.widget_blocked_today, 0, 0
                )
                errorViews.setTextViewText(R.id.widget_blocked_count, countText)
                errorViews.setTextViewText(
                    R.id.widget_status,
                    context.getString(R.string.widget_status_active)
                )
                appWidgetManager.updateAppWidget(appWidgetId, errorViews)
            }
        }
    }

    /**
     * Check if the app holds the call screening role (API 29+).
     * On older APIs, we assume active if the app is installed.
     */
    private fun isFilterActive(context: Context): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(
                    android.app.role.RoleManager::class.java
                )
                roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_CALL_SCREENING)
            } else {
                true
            }
        } catch (e: Exception) {
            true
        }
    }
}
