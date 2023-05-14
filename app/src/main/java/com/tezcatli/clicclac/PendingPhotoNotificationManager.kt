package com.tezcatli.clicclac

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime


class NotificationWorkerFactory(val pendingPhotoNotificationManager: PendingPhotoNotificationManager) :
    WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            NotificationWorker::class.java.name ->
                NotificationWorker(
                    appContext,
                    workerParameters,
                    pendingPhotoNotificationManager
                )

            else ->
                // Return null, so that the base class can delegate to the default WorkerFactory.
                null
        }

    }
}

class NotificationWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    val pendingPhotoNotificationManager: PendingPhotoNotificationManager
) : Worker(appContext, workerParameters) {

    override fun doWork(): Result {

        pendingPhotoNotificationManager.scheduleNextNotification()

        return Result.success()
    }


}

class PendingPhotoNotificationManager(val appContext: Context, val escrowManager: EscrowManager) {
    var notification: NotificationCompat.Builder


    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object Keys {

        @Volatile
        private var Instance: PendingPhotoNotificationManager? = null


        fun getInstance(
            context: Context,
            escrowManager: EscrowManager
        ): PendingPhotoNotificationManager {
            return Instance ?: synchronized(this) {
                return PendingPhotoNotificationManager(context, escrowManager)
            }
        }

    }


    fun scheduleNextNotification(scheduleOnly : Boolean = false) {

        scope.launch() {

            val listAll = escrowManager.listAllF().filterNotNull().first()

            if (!listAll.isEmpty()) {
                val now = ZonedDateTime.now()
                val expiredCount = listAll.count { it.deadline < now }
                val pendingList = listAll.filter {
                    it.deadline >= now
                }
                Log.e("CLICCLAC", "expiredCount = $expiredCount")


                if (expiredCount > 0  && ! scheduleOnly) {
                    if (expiredCount == 1)
                        notification.setContentText("$expiredCount photo ready to be developed.")
                    else
                        notification.setContentText("$expiredCount photos ready to be developed.")



                    with(NotificationManagerCompat.from(appContext)) {
                        if (ActivityCompat.checkSelfPermission(
                                appContext,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.e("CLICCLAC",  "Unable to send notification, no permission")
                            return@with
                        }
                        Log.e("CLICCLAC",  "Sent notification")
                        notify(0, notification.build())
                    }
                }

                if (!pendingList.isEmpty()) {
                    val delay =  Duration.between(now, pendingList[0].deadline)
                    val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay( delay )
                        .build()
                    WorkManager.getInstance(appContext).enqueue(request)
                }
            }

        }
    }

    init {

        Instance = this
        val name = appContext.getString(R.string.channel_name)
        val descriptionText = appContext.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW

        val mChannel = NotificationChannel("ClicClac", name, importance)
        mChannel.description = descriptionText


        val notificationManager =
            appContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)


        val taskDetailIntent = Intent(
            ACTION_VIEW,
            "clicclac://home".toUri())

        val pending: PendingIntent = TaskStackBuilder.create(appContext).run {
            addNextIntentWithParentStack(taskDetailIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE )
        }

        //    escrowManager.listAllF().filterNotNull() {
        notification = NotificationCompat.Builder(appContext, "ClicClac")
            .setSmallIcon(R.drawable.clicclac_logo_v2)
            .setContentTitle("Clic Clac")
            //    .setContentText("Clic Clac Content")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
      //      .setAutoCancel(true)


        scheduleNextNotification()

    }
}

