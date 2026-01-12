package com.callfilter.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.callfilter.domain.repository.ContactsRepository
import com.callfilter.util.PhoneNumberHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val phoneNumberHelper: PhoneNumberHelper
) : ContactsRepository {

    override suspend fun isNumberInContacts(number: String): Boolean {
        return withContext(Dispatchers.IO) {
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
        }
    }

    override suspend fun getContactName(number: String): String? {
        return withContext(Dispatchers.IO) {
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
        }
    }
}
