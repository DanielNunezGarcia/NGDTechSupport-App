package com.example.ngdtechsupport.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class Dao(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val db get() = dbHelper.writableDatabase

    // ============ USERS ============

    fun insertUser(user: UserEntity): Long {
        val values = ContentValues().apply {
            put("id", user.id)
            put("email", user.email)
            put("name", user.name)
            put("role", user.role)
            put("companyId", user.companyId)
            put("createdAt", user.createdAt)
            put("updatedAt", user.updatedAt)
        }
        return db.insertWithOnConflict(DatabaseHelper.TABLE_USERS, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getUserById(id: String): UserEntity? {
        val cursor = db.query(DatabaseHelper.TABLE_USERS, null, "id = ?", arrayOf(id), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToUser(it) else null }
    }

    fun getUserByEmail(email: String): UserEntity? {
        val cursor = db.query(DatabaseHelper.TABLE_USERS, null, "email = ?", arrayOf(email), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToUser(it) else null }
    }

    fun getAllUsers(): List<UserEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, "createdAt DESC")
        return cursor.use { cursorToList(it) { cursorToUser(it) } }
    }

    fun getUsersByCompany(companyId: String): List<UserEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_USERS, null, "companyId = ?", arrayOf(companyId), null, null, null)
        return cursor.use { cursorToList(it) { cursorToUser(it) } }
    }

    fun updateUser(user: UserEntity): Int {
        val values = ContentValues().apply {
            put("email", user.email)
            put("name", user.name)
            put("role", user.role)
            put("companyId", user.companyId)
            put("updatedAt", user.updatedAt)
        }
        return db.update(DatabaseHelper.TABLE_USERS, values, "id = ?", arrayOf(user.id))
    }

    fun deleteUser(id: String): Int {
        return db.delete(DatabaseHelper.TABLE_USERS, "id = ?", arrayOf(id))
    }

    private fun cursorToUser(cursor: Cursor): UserEntity {
        return UserEntity(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            role = cursor.getString(cursor.getColumnIndexOrThrow("role")),
            companyId = cursor.getString(cursor.getColumnIndexOrThrow("companyId")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt"))
        )
    }

    // ============ BUSINESSES ============

    fun insertBusiness(business: BusinessEntity): Long {
        val values = ContentValues().apply {
            put("id", business.id)
            put("companyId", business.companyId)
            put("name", business.name)
            put("description", business.description)
            put("status", business.status)
            put("version", business.version)
            put("iconUrl", business.iconUrl)
            put("createdAt", business.createdAt)
            put("updatedAt", business.updatedAt)
        }
        return db.insertWithOnConflict(DatabaseHelper.TABLE_BUSINESSES, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getBusinessById(id: String): BusinessEntity? {
        val cursor = db.query(DatabaseHelper.TABLE_BUSINESSES, null, "id = ?", arrayOf(id), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToBusiness(it) else null }
    }

    fun getAllBusinesses(): List<BusinessEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_BUSINESSES, null, null, null, null, null, "createdAt DESC")
        return cursor.use { cursorToList(it) { cursorToBusiness(it) } }
    }

    fun getBusinessesByCompany(companyId: String): List<BusinessEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_BUSINESSES, null, "companyId = ?", arrayOf(companyId), null, null, "createdAt DESC")
        return cursor.use { cursorToList(it) { cursorToBusiness(it) } }
    }

    fun updateBusiness(business: BusinessEntity): Int {
        val values = ContentValues().apply {
            put("companyId", business.companyId)
            put("name", business.name)
            put("description", business.description)
            put("status", business.status)
            put("version", business.version)
            put("iconUrl", business.iconUrl)
            put("updatedAt", business.updatedAt)
        }
        return db.update(DatabaseHelper.TABLE_BUSINESSES, values, "id = ?", arrayOf(business.id))
    }

    fun deleteBusiness(id: String): Int {
        return db.delete(DatabaseHelper.TABLE_BUSINESSES, "id = ?", arrayOf(id))
    }

    private fun cursorToBusiness(cursor: Cursor): BusinessEntity {
        return BusinessEntity(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            companyId = cursor.getString(cursor.getColumnIndexOrThrow("companyId")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
            status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
            version = cursor.getString(cursor.getColumnIndexOrThrow("version")),
            iconUrl = cursor.getString(cursor.getColumnIndexOrThrow("iconUrl")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt"))
        )
    }

    // ============ CHANNELS ============

    fun insertChannel(channel: ChannelEntity): Long {
        val values = ContentValues().apply {
            put("id", channel.id)
            put("companyId", channel.companyId)
            put("businessId", channel.businessId)
            put("name", channel.name)
            put("type", channel.type)
            put("pinned", if (channel.pinned) 1 else 0)
            put("archived", if (channel.archived) 1 else 0)
            put("lastMessage", channel.lastMessage)
            put("lastMessageAt", channel.lastMessageAt)
            put("createdAt", channel.createdAt)
        }
        return db.insertWithOnConflict(DatabaseHelper.TABLE_CHANNELS, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getChannelById(id: String): ChannelEntity? {
        val cursor = db.query(DatabaseHelper.TABLE_CHANNELS, null, "id = ?", arrayOf(id), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToChannel(it) else null }
    }

    fun getAllChannels(): List<ChannelEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_CHANNELS, null, null, null, null, null, "lastMessageAt DESC")
        return cursor.use { cursorToList(it) { cursorToChannel(it) } }
    }

    fun getChannelsByCompany(companyId: String): List<ChannelEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_CHANNELS, null, "companyId = ?", arrayOf(companyId), null, null, "lastMessageAt DESC")
        return cursor.use { cursorToList(it) { cursorToChannel(it) } }
    }

    fun getChannelsByBusiness(businessId: String): List<ChannelEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_CHANNELS, null, "businessId = ?", arrayOf(businessId), null, null, "lastMessageAt DESC")
        return cursor.use { cursorToList(it) { cursorToChannel(it) } }
    }

    fun getPinnedChannels(companyId: String): List<ChannelEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_CHANNELS, null, "companyId = ? AND pinned = 1", arrayOf(companyId), null, null, "lastMessageAt DESC")
        return cursor.use { cursorToList(it) { cursorToChannel(it) } }
    }

    fun getArchivedChannels(companyId: String): List<ChannelEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_CHANNELS, null, "companyId = ? AND archived = 1", arrayOf(companyId), null, null, "lastMessageAt DESC")
        return cursor.use { cursorToList(it) { cursorToChannel(it) } }
    }

    fun updateChannel(channel: ChannelEntity): Int {
        val values = ContentValues().apply {
            put("companyId", channel.companyId)
            put("businessId", channel.businessId)
            put("name", channel.name)
            put("type", channel.type)
            put("pinned", if (channel.pinned) 1 else 0)
            put("archived", if (channel.archived) 1 else 0)
            put("lastMessage", channel.lastMessage)
            put("lastMessageAt", channel.lastMessageAt)
        }
        return db.update(DatabaseHelper.TABLE_CHANNELS, values, "id = ?", arrayOf(channel.id))
    }

    fun updateChannelLastMessage(channelId: String, lastMessage: String, lastMessageAt: Long): Int {
        val values = ContentValues().apply {
            put("lastMessage", lastMessage)
            put("lastMessageAt", lastMessageAt)
        }
        return db.update(DatabaseHelper.TABLE_CHANNELS, values, "id = ?", arrayOf(channelId))
    }

    fun togglePinChannel(channelId: String, pinned: Boolean): Int {
        val values = ContentValues().apply {
            put("pinned", if (pinned) 1 else 0)
        }
        return db.update(DatabaseHelper.TABLE_CHANNELS, values, "id = ?", arrayOf(channelId))
    }

    fun toggleArchiveChannel(channelId: String, archived: Boolean): Int {
        val values = ContentValues().apply {
            put("archived", if (archived) 1 else 0)
        }
        return db.update(DatabaseHelper.TABLE_CHANNELS, values, "id = ?", arrayOf(channelId))
    }

    fun deleteChannel(id: String): Int {
        return db.delete(DatabaseHelper.TABLE_CHANNELS, "id = ?", arrayOf(id))
    }

    private fun cursorToChannel(cursor: Cursor): ChannelEntity {
        return ChannelEntity(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            companyId = cursor.getString(cursor.getColumnIndexOrThrow("companyId")),
            businessId = cursor.getString(cursor.getColumnIndexOrThrow("businessId")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
            pinned = cursor.getInt(cursor.getColumnIndexOrThrow("pinned")) == 1,
            archived = cursor.getInt(cursor.getColumnIndexOrThrow("archived")) == 1,
            lastMessage = cursor.getString(cursor.getColumnIndexOrThrow("lastMessage")),
            lastMessageAt = cursor.getLong(cursor.getColumnIndexOrThrow("lastMessageAt")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt"))
        )
    }

    // ============ MESSAGES ============

    fun insertMessage(message: MessageEntity): Long {
        val values = ContentValues().apply {
            put("id", message.id)
            put("channelId", message.channelId)
            put("companyId", message.companyId)
            put("message", message.message)
            put("senderId", message.senderId)
            put("senderName", message.senderName)
            put("senderType", message.senderType)
            put("timestamp", message.timestamp)
            put("status", message.status)
            put("requiresHumanSupport", if (message.requiresHumanSupport) 1 else 0)
        }
        return db.insertWithOnConflict(DatabaseHelper.TABLE_MESSAGES, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertMessages(messages: List<MessageEntity>) {
        db.beginTransaction()
        try {
            messages.forEach { insertMessage(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getMessageById(id: String): MessageEntity? {
        val cursor = db.query(DatabaseHelper.TABLE_MESSAGES, null, "id = ?", arrayOf(id), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToMessage(it) else null }
    }

    fun getMessagesByChannel(channelId: String, limit: Int = 100, offset: Int = 0): List<MessageEntity> {
        val cursor = db.query(
            DatabaseHelper.TABLE_MESSAGES, null, "channelId = ?",
            arrayOf(channelId), null, null, "timestamp ASC",
            "$offset, $limit"
        )
        return cursor.use { cursorToList(it) { cursorToMessage(it) } }
    }

    fun getMessagesByCompany(companyId: String): List<MessageEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_MESSAGES, null, "companyId = ?", arrayOf(companyId), null, null, "timestamp DESC")
        return cursor.use { cursorToList(it) { cursorToMessage(it) } }
    }

    fun getLastMessage(channelId: String): MessageEntity? {
        val cursor = db.query(
            DatabaseHelper.TABLE_MESSAGES, null, "channelId = ?",
            arrayOf(channelId), null, null, "timestamp DESC", "1"
        )
        return cursor.use { if (it.moveToFirst()) cursorToMessage(it) else null }
    }

    fun updateMessageStatus(messageId: String, status: String): Int {
        val values = ContentValues().apply {
            put("status", status)
        }
        return db.update(DatabaseHelper.TABLE_MESSAGES, values, "id = ?", arrayOf(messageId))
    }

    fun markMessagesAsRead(channelId: String): Int {
        val values = ContentValues().apply {
            put("status", "read")
        }
        return db.update(DatabaseHelper.TABLE_MESSAGES, values, "channelId = ? AND status = ?", arrayOf(channelId, "sent"))
    }

    fun deleteMessage(id: String): Int {
        return db.delete(DatabaseHelper.TABLE_MESSAGES, "id = ?", arrayOf(id))
    }

    fun deleteMessagesByChannel(channelId: String): Int {
        return db.delete(DatabaseHelper.TABLE_MESSAGES, "channelId = ?", arrayOf(channelId))
    }

    fun getUnreadCount(channelId: String): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_MESSAGES} WHERE channelId = ? AND status = 'sent'", arrayOf(channelId))
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun cursorToMessage(cursor: Cursor): MessageEntity {
        return MessageEntity(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            channelId = cursor.getString(cursor.getColumnIndexOrThrow("channelId")),
            companyId = cursor.getString(cursor.getColumnIndexOrThrow("companyId")),
            message = cursor.getString(cursor.getColumnIndexOrThrow("message")),
            senderId = cursor.getString(cursor.getColumnIndexOrThrow("senderId")),
            senderName = cursor.getString(cursor.getColumnIndexOrThrow("senderName")),
            senderType = cursor.getString(cursor.getColumnIndexOrThrow("senderType")),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
            status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
            requiresHumanSupport = cursor.getInt(cursor.getColumnIndexOrThrow("requiresHumanSupport")) == 1
        )
    }

    // ============ AI CONFIG ============

    fun insertAiConfig(config: AiConfigEntity): Long {
        val values = ContentValues().apply {
            put("companyId", config.companyId)
            put("aiEnabled", if (config.aiEnabled) 1 else 0)
            put("autoGreeting", if (config.autoGreeting) 1 else 0)
            put("greetingMessages", config.greetingMessages)
            put("quickReplies", config.quickReplies)
            put("escalationKeywords", config.escalationKeywords)
            put("fallbackMessage", config.fallbackMessage)
            put("updatedAt", config.updatedAt)
        }
        return db.insertWithOnConflict(DatabaseHelper.TABLE_AI_CONFIG, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAiConfigByCompany(companyId: String): AiConfigEntity? {
        val cursor = db.query(DatabaseHelper.TABLE_AI_CONFIG, null, "companyId = ?", arrayOf(companyId), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToAiConfig(it) else null }
    }

    fun updateAiConfig(config: AiConfigEntity): Int {
        val values = ContentValues().apply {
            put("aiEnabled", if (config.aiEnabled) 1 else 0)
            put("autoGreeting", if (config.autoGreeting) 1 else 0)
            put("greetingMessages", config.greetingMessages)
            put("quickReplies", config.quickReplies)
            put("escalationKeywords", config.escalationKeywords)
            put("fallbackMessage", config.fallbackMessage)
            put("updatedAt", config.updatedAt)
        }
        return db.update(DatabaseHelper.TABLE_AI_CONFIG, values, "companyId = ?", arrayOf(config.companyId))
    }

    fun deleteAiConfig(companyId: String): Int {
        return db.delete(DatabaseHelper.TABLE_AI_CONFIG, "companyId = ?", arrayOf(companyId))
    }

    private fun cursorToAiConfig(cursor: Cursor): AiConfigEntity {
        return AiConfigEntity(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            companyId = cursor.getString(cursor.getColumnIndexOrThrow("companyId")),
            aiEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("aiEnabled")) == 1,
            autoGreeting = cursor.getInt(cursor.getColumnIndexOrThrow("autoGreeting")) == 1,
            greetingMessages = cursor.getString(cursor.getColumnIndexOrThrow("greetingMessages")),
            quickReplies = cursor.getString(cursor.getColumnIndexOrThrow("quickReplies")),
            escalationKeywords = cursor.getString(cursor.getColumnIndexOrThrow("escalationKeywords")),
            fallbackMessage = cursor.getString(cursor.getColumnIndexOrThrow("fallbackMessage")),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt"))
        )
    }

    // ============ UPDATES ============

    fun insertUpdate(update: UpdateEntity): Long {
        val values = ContentValues().apply {
            put("id", update.id)
            put("companyId", update.companyId)
            put("title", update.title)
            put("content", update.content)
            put("version", update.version)
            put("publishedAt", update.publishedAt)
            put("isRead", if (update.isRead) 1 else 0)
        }
        return db.insertWithOnConflict(DatabaseHelper.TABLE_UPDATES, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertUpdates(updates: List<UpdateEntity>) {
        db.beginTransaction()
        try {
            updates.forEach { insertUpdate(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getUpdateById(id: String): UpdateEntity? {
        val cursor = db.query(DatabaseHelper.TABLE_UPDATES, null, "id = ?", arrayOf(id), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToUpdate(it) else null }
    }

    fun getAllUpdates(): List<UpdateEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_UPDATES, null, null, null, null, null, "publishedAt DESC")
        return cursor.use { cursorToList(it) { cursorToUpdate(it) } }
    }

    fun getUpdatesByCompany(companyId: String): List<UpdateEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_UPDATES, null, "companyId = ?", arrayOf(companyId), null, null, "publishedAt DESC")
        return cursor.use { cursorToList(it) { cursorToUpdate(it) } }
    }

    fun getUnreadUpdates(companyId: String): List<UpdateEntity> {
        val cursor = db.query(DatabaseHelper.TABLE_UPDATES, null, "companyId = ? AND isRead = 0", arrayOf(companyId), null, null, "publishedAt DESC")
        return cursor.use { cursorToList(it) { cursorToUpdate(it) } }
    }

    fun markUpdateAsRead(id: String): Int {
        val values = ContentValues().apply {
            put("isRead", 1)
        }
        return db.update(DatabaseHelper.TABLE_UPDATES, values, "id = ?", arrayOf(id))
    }

    fun markAllUpdatesAsRead(companyId: String): Int {
        val values = ContentValues().apply {
            put("isRead", 1)
        }
        return db.update(DatabaseHelper.TABLE_UPDATES, values, "companyId = ?", arrayOf(companyId))
    }

    fun deleteUpdate(id: String): Int {
        return db.delete(DatabaseHelper.TABLE_UPDATES, "id = ?", arrayOf(id))
    }

    fun getUnreadCountUpdates(companyId: String): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_UPDATES} WHERE companyId = ? AND isRead = 0", arrayOf(companyId))
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun cursorToUpdate(cursor: Cursor): UpdateEntity {
        return UpdateEntity(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            companyId = cursor.getString(cursor.getColumnIndexOrThrow("companyId")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
            version = cursor.getString(cursor.getColumnIndexOrThrow("version")),
            publishedAt = cursor.getLong(cursor.getColumnIndexOrThrow("publishedAt")),
            isRead = cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 1
        )
    }

    // ============ UTILITIES ============

    fun clearAllTables() {
        db.execSQL("DELETE FROM ${DatabaseHelper.TABLE_MESSAGES}")
        db.execSQL("DELETE FROM ${DatabaseHelper.TABLE_CHANNELS}")
        db.execSQL("DELETE FROM ${DatabaseHelper.TABLE_USERS}")
        db.execSQL("DELETE FROM ${DatabaseHelper.TABLE_BUSINESSES}")
        db.execSQL("DELETE FROM ${DatabaseHelper.TABLE_AI_CONFIG}")
        db.execSQL("DELETE FROM ${DatabaseHelper.TABLE_UPDATES}")
    }

    fun clearTable(tableName: String) {
        db.execSQL("DELETE FROM $tableName")
    }

    fun getTableCount(tableName: String): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun <T> cursorToList(cursor: Cursor, mapper: (Cursor) -> T): List<T> {
        val list = mutableListOf<T>()
        while (cursor.moveToNext()) {
            list.add(mapper(cursor))
        }
        return list
    }

    fun close() {
        dbHelper.close()
    }
}

// ============ ENTITY CLASSES ============

data class UserEntity(
    val id: String,
    val email: String,
    val name: String?,
    val role: String = "client",
    val companyId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class BusinessEntity(
    val id: String,
    val companyId: String,
    val name: String,
    val description: String?,
    val status: String = "development",
    val version: String?,
    val iconUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class ChannelEntity(
    val id: String,
    val companyId: String,
    val businessId: String?,
    val name: String,
    val type: String = "hybrid",
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val lastMessage: String?,
    val lastMessageAt: Long?,
    val createdAt: Long
)

data class MessageEntity(
    val id: String,
    val channelId: String,
    val companyId: String,
    val message: String,
    val senderId: String,
    val senderName: String?,
    val senderType: String = "user",
    val timestamp: Long,
    val status: String = "sent",
    val requiresHumanSupport: Boolean = false
)

data class AiConfigEntity(
    val id: Long = 0,
    val companyId: String,
    val aiEnabled: Boolean = true,
    val autoGreeting: Boolean = true,
    val greetingMessages: String?,
    val quickReplies: String?,
    val escalationKeywords: String?,
    val fallbackMessage: String?,
    val updatedAt: Long
)

data class UpdateEntity(
    val id: String,
    val companyId: String,
    val title: String,
    val content: String?,
    val version: String?,
    val publishedAt: Long,
    val isRead: Boolean = false
)
