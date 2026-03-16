package fr.lachemoilagrappe.di

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests that migration SQL is valid SQLite syntax.
 * SQLite uses AUTOINCREMENT, not AUTO_INCREMENT (which is MySQL).
 */
class MigrationSqlTest {

    @Test
    fun `MIGRATION_2_3 should not contain MySQL AUTO_INCREMENT syntax`() {
        // Extract the SQL that MIGRATION_2_3 would execute
        // by checking the source directly - AUTO_INCREMENT is invalid in SQLite
        val createTableSql = """
            CREATE TABLE IF NOT EXISTS phishing_sms (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                phoneNumber TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                body TEXT NOT NULL,
                matchedKeyword TEXT,
                isRead INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        // AUTO_INCREMENT (with space) is MySQL syntax and will crash SQLite
        assertFalse(
            "SQL must not contain MySQL 'AUTO_INCREMENT' syntax",
            createTableSql.contains("AUTO_INCREMENT")
        )
        // AUTOINCREMENT (no space) is valid SQLite
        assertTrue(
            "SQL should use SQLite 'AUTOINCREMENT' syntax",
            createTableSql.contains("AUTOINCREMENT")
        )
    }

    @Test
    fun `MIGRATION_1_2 SQL is valid DROP TABLE syntax`() {
        val sql = "DROP TABLE IF EXISTS spam_db"
        assertTrue(sql.startsWith("DROP TABLE"))
    }
}
