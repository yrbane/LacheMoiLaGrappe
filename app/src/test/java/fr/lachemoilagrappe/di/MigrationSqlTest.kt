package fr.lachemoilagrappe.di

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests that actual MIGRATION_1_2 and MIGRATION_2_3 in DatabaseModule execute valid SQLite syntax.
 */
class MigrationSqlTest {

    @Test
    fun `MIGRATION_2_3 executes valid SQLite AUTOINCREMENT and creates indices`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val executedStatements = mutableListOf<String>()
        every { db.execSQL(capture(executedStatements)) } returns Unit

        MIGRATION_2_3.migrate(db)

        assertTrue("Migration 2->3 should execute 3 SQL statements", executedStatements.size == 3)
        val createTableSql = executedStatements.first()

        assertFalse(
            "SQL must not contain MySQL 'AUTO_INCREMENT' syntax",
            createTableSql.contains("AUTO_INCREMENT")
        )
        assertTrue(
            "SQL should use SQLite 'AUTOINCREMENT' syntax",
            createTableSql.contains("AUTOINCREMENT")
        )
        assertTrue(
            "SQL should create phishing_sms table",
            createTableSql.contains("CREATE TABLE IF NOT EXISTS phishing_sms")
        )
    }

    @Test
    fun `MIGRATION_1_2 drops spam_db table`() {
        val db = mockk<SupportSQLiteDatabase>(relaxed = true)
        val sqlSlot = slot<String>()
        every { db.execSQL(capture(sqlSlot)) } returns Unit

        MIGRATION_1_2.migrate(db)

        verify(exactly = 1) { db.execSQL(any()) }
        assertTrue(sqlSlot.captured.startsWith("DROP TABLE IF EXISTS spam_db"))
    }
}
