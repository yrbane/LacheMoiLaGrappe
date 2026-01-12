package com.callfilter.domain.repository

interface ContactsRepository {
    suspend fun isNumberInContacts(number: String): Boolean
    suspend fun getContactName(number: String): String?
}
