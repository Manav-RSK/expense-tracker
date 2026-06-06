package com.example.exp.data.source.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.exp.data.local.db.AppDatabase
import com.example.exp.data.local.entity.RawEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.example.exp.data.repository.RawEventRepository
import com.example.exp.data.source.parser.SimpleSmsParser
import com.example.exp.domain.classifier.TransactionClassifier
import com.example.exp.domain.contact.ContactMatcher
import com.example.exp.domain.history.HistoryMatcher
import com.example.exp.domain.processor.RawEventProcessor
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class NotificationCaptureService: NotificationListenerService() {

    // Use a stable scope for service coroutines to avoid creating ad-hoc scopes
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)
    private val processing = AtomicBoolean(false)

    private val allowedPackages =
        setOf(
            "com.google.android.apps.nbu.paisa.user",
            "com.phonepe.app",
            "net.one97.paytm",
            "com.dbs.in.digitalbank",
            "com.example.exp"
        )

    override fun onCreate() {
        super.onCreate()

        Log.d(
            "NOTIF_TEST",
            "SERVICE CREATED"
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.d(
            "NOTIF_TEST",
            "LISTENER CONNECTED"
        )
    }


    override fun onNotificationPosted(
        sbn: StatusBarNotification?
    )    {
        super.onNotificationPosted(sbn)
        sbn?.let {
            // Extract notification details
            val packageName = sbn.packageName

            val extras =
                sbn.notification?.extras

            val title =
                extras?.getString(Notification.EXTRA_TITLE)

            val text =
                extras?.getCharSequence(
                    Notification.EXTRA_TEXT
                )?.toString()

            // Process the notification (e.g., log it, send to server, etc.)
            val db = AppDatabase.getInstance(this)
            val repository = RawEventRepository(db.rawEventDao())
            val rawEvent = RawEventEntity(
                id = UUID.randomUUID().toString(),
                rawText = text ?: "",
                sender = title ?: packageName,
                source = "NOTIFICATION",
                eventTime = System.currentTimeMillis(),
                receivedAt = System.currentTimeMillis(),
                processed = false,
                sourceId = sbn.key
            )
            serviceScope.launch {
                try {
                    // Use repository to perform insert (reuses dedup logic)
                    val inserted = repository.insert(rawEvent)
                    if (inserted) {
                        Log.d("NOTIF_TEST", "Inserted RawEvent via repository")

                        // Build processor dependencies and run processing immediately
                        val parser = SimpleSmsParser()
                        val classifier = TransactionClassifier()
                        val contactMatcher = ContactMatcher()
                        val historyMatcher = HistoryMatcher(db.transactionDao())

                        val processor = RawEventProcessor(
                            repository = repository,
                            transactionDao = db.transactionDao(),
                            parser = parser,
                            classifier = classifier,
                            contactMatcher = contactMatcher,
                            historyMatcher = historyMatcher
                        )

                        if (processing.compareAndSet(false, true)) {
                            try {
                                processor.processBatch()
                                Log.d("NOTIF_TEST", "Processor.run completed")
                            } catch (procEx: Throwable) {
                                Log.w("NOTIF_TEST", "Processor failed", procEx)
                            } finally {
                                processing.set(false)
                            }
                        } else {
                            Log.d("NOTIF_TEST", "Processor already running, skipping")
                        }
                    } else {
                        Log.d("NOTIF_TEST", "Insert was deduped, not processing")
                    }
                } catch (t: Throwable) {
                    Log.w("NOTIF_TEST", "Failed to insert raw event", t)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}