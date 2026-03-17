package com.example.ngdtechsupport.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ngdtechsupport.db"
        const val DATABASE_VERSION = 1

        const val TABLE_USERS = "users"
        const val TABLE_BUSINESSES = "businesses"
        const val TABLE_CHANNELS = "channels"
        const val TABLE_MESSAGES = "messages"
        const val TABLE_AI_CONFIG = "ai_config"
        const val TABLE_UPDATES = "updates"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id TEXT PRIMARY KEY,
                email TEXT UNIQUE NOT NULL,
                name TEXT,
                role TEXT DEFAULT 'client',
                companyId TEXT,
                createdAt INTEGER,
                updatedAt INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_BUSINESSES (
                id TEXT PRIMARY KEY,
                companyId TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                status TEXT DEFAULT 'development',
                version TEXT,
                iconUrl TEXT,
                createdAt INTEGER,
                updatedAt INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_CHANNELS (
                id TEXT PRIMARY KEY,
                companyId TEXT NOT NULL,
                businessId TEXT,
                name TEXT NOT NULL,
                type TEXT DEFAULT 'hybrid',
                pinned INTEGER DEFAULT 0,
                archived INTEGER DEFAULT 0,
                lastMessage TEXT,
                lastMessageAt INTEGER,
                createdAt INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_MESSAGES (
                id TEXT PRIMARY KEY,
                channelId TEXT NOT NULL,
                companyId TEXT NOT NULL,
                message TEXT NOT NULL,
                senderId TEXT NOT NULL,
                senderName TEXT,
                senderType TEXT DEFAULT 'user',
                timestamp INTEGER NOT NULL,
                status TEXT DEFAULT 'sent',
                requiresHumanSupport INTEGER DEFAULT 0,
                FOREIGN KEY (channelId) REFERENCES $TABLE_CHANNELS(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_AI_CONFIG (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                companyId TEXT NOT NULL,
                aiEnabled INTEGER DEFAULT 1,
                autoGreeting INTEGER DEFAULT 1,
                greetingMessages TEXT,
                quickReplies TEXT,
                escalationKeywords TEXT,
                fallbackMessage TEXT,
                updatedAt INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_UPDATES (
                id TEXT PRIMARY KEY,
                companyId TEXT NOT NULL,
                title TEXT NOT NULL,
                content TEXT,
                version TEXT,
                publishedAt INTEGER,
                isRead INTEGER DEFAULT 0
            )
        """)

        db.execSQL("CREATE INDEX idx_messages_channel ON $TABLE_MESSAGES(channelId)")
        db.execSQL("CREATE INDEX idx_channels_company ON $TABLE_CHANNELS(companyId)")
        db.execSQL("CREATE INDEX idx_businesses_company ON $TABLE_BUSINESSES(companyId)")
        db.execSQL("CREATE INDEX idx_updates_company ON $TABLE_UPDATES(companyId)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUSINESSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHANNELS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AI_CONFIG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_UPDATES")
        onCreate(db)
    }
}
