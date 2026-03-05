package fr.lachemoilagrappe.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import fr.lachemoilagrappe.domain.repository.ContactsRepository
import fr.lachemoilagrappe.util.PhoneNumberHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val phoneNumberHelper: PhoneNumberHelper
) : ContactsRepository {

    override suspend fun isNumberInContacts(number: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val normalizedNumber = phoneNumberHelper.normalize(number)
                if (normalizedNumber.isNullOrBlank()) return@withContext false

                val uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(normalizedNumber)
                )

                val projection = arrayOf(ContactsContract.PhoneLookup._ID)

                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    cursor.count > 0
                } ?: false
            } catch (e: SecurityException) {
                Timber.w(e, "Contacts permission not granted")
                false
            } catch (e: Exception) {
                Timber.e(e, "Failed to query contacts for number")
                false
            }
        }
    }

    override suspend fun getContactName(number: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val normalizedNumber = phoneNumberHelper.normalize(number)
                if (normalizedNumber.isNullOrBlank()) return@withContext null

                val uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(normalizedNumber)
                )

                val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(0)
                    } else {
                        null
                    }
                }
            } catch (e: SecurityException) {
                Timber.w(e, "Contacts permission not granted")
                null
            } catch (e: Exception) {
                Timber.e(e, "Failed to get contact name for number")
                null
            }
        }
    }
}
