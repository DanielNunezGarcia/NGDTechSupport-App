package com.example.ngdtechsupport.data.local

import android.content.Context

class LocalDatabaseManager(context: Context) {

    private val dao = Dao(context)

    companion object {
        @Volatile
        private var instance: LocalDatabaseManager? = null

        fun getInstance(context: Context): LocalDatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: LocalDatabaseManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // ============ USER OPERATIONS ============

    fun saveUser(user: UserEntity): Long = dao.insertUser(user)

    fun getUser(id: String): UserEntity? = dao.getUserById(id)

    fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)

    fun getAllUsers(): List<UserEntity> = dao.getAllUsers()

    fun getUsersByCompany(companyId: String): List<UserEntity> = dao.getUsersByCompany(companyId)

    fun updateUser(user: UserEntity): Int = dao.updateUser(user)

    fun deleteUser(id: String): Int = dao.deleteUser(id)

    // ============ BUSINESS OPERATIONS ============

    fun saveBusiness(business: BusinessEntity): Long = dao.insertBusiness(business)

    fun getBusiness(id: String): BusinessEntity? = dao.getBusinessById(id)

    fun getAllBusinesses(): List<BusinessEntity> = dao.getAllBusinesses()

    fun getBusinessesByCompany(companyId: String): List<BusinessEntity> = dao.getBusinessesByCompany(companyId)

    fun updateBusiness(business: BusinessEntity): Int = dao.updateBusiness(business)

    fun deleteBusiness(id: String): Int = dao.deleteBusiness(id)

    // ============ CHANNEL OPERATIONS ============

    fun saveChannel(channel: ChannelEntity): Long = dao.insertChannel(channel)

    fun getChannel(id: String): ChannelEntity? = dao.getChannelById(id)

    fun getAllChannels(): List<ChannelEntity> = dao.getAllChannels()

    fun getChannelsByCompany(companyId: String): List<ChannelEntity> = dao.getChannelsByCompany(companyId)

    fun getChannelsByBusiness(businessId: String): List<ChannelEntity> = dao.getChannelsByBusiness(businessId)

    fun getPinnedChannels(companyId: String): List<ChannelEntity> = dao.getPinnedChannels(companyId)

    fun getArchivedChannels(companyId: String): List<ChannelEntity> = dao.getArchivedChannels(companyId)

    fun updateChannel(channel: ChannelEntity): Int = dao.updateChannel(channel)

    fun updateChannelLastMessage(channelId: String, lastMessage: String, lastMessageAt: Long): Int =
        dao.updateChannelLastMessage(channelId, lastMessage, lastMessageAt)

    fun togglePinChannel(channelId: String, pinned: Boolean): Int = dao.togglePinChannel(channelId, pinned)

    fun toggleArchiveChannel(channelId: String, archived: Boolean): Int = dao.toggleArchiveChannel(channelId, archived)

    fun deleteChannel(id: String): Int = dao.deleteChannel(id)

    // ============ MESSAGE OPERATIONS ============

    fun saveMessage(message: MessageEntity): Long = dao.insertMessage(message)

    fun saveMessages(messages: List<MessageEntity>) = dao.insertMessages(messages)

    fun getMessage(id: String): MessageEntity? = dao.getMessageById(id)

    fun getMessagesByChannel(channelId: String, limit: Int = 100, offset: Int = 0): List<MessageEntity> =
        dao.getMessagesByChannel(channelId, limit, offset)

    fun getMessagesByCompany(companyId: String): List<MessageEntity> = dao.getMessagesByCompany(companyId)

    fun getLastMessage(channelId: String): MessageEntity? = dao.getLastMessage(channelId)

    fun updateMessageStatus(messageId: String, status: String): Int = dao.updateMessageStatus(messageId, status)

    fun markMessagesAsRead(channelId: String): Int = dao.markMessagesAsRead(channelId)

    fun deleteMessage(id: String): Int = dao.deleteMessage(id)

    fun deleteMessagesByChannel(channelId: String): Int = dao.deleteMessagesByChannel(channelId)

    fun getUnreadMessageCount(channelId: String): Int = dao.getUnreadCount(channelId)

    // ============ AI CONFIG OPERATIONS ============

    fun saveAiConfig(config: AiConfigEntity): Long = dao.insertAiConfig(config)

    fun getAiConfig(companyId: String): AiConfigEntity? = dao.getAiConfigByCompany(companyId)

    fun updateAiConfig(config: AiConfigEntity): Int = dao.updateAiConfig(config)

    fun deleteAiConfig(companyId: String): Int = dao.deleteAiConfig(companyId)

    // ============ UPDATE OPERATIONS ============

    fun saveUpdate(update: UpdateEntity): Long = dao.insertUpdate(update)

    fun saveUpdates(updates: List<UpdateEntity>) = dao.insertUpdates(updates)

    fun getUpdate(id: String): UpdateEntity? = dao.getUpdateById(id)

    fun getAllUpdates(): List<UpdateEntity> = dao.getAllUpdates()

    fun getUpdatesByCompany(companyId: String): List<UpdateEntity> = dao.getUpdatesByCompany(companyId)

    fun getUnreadUpdates(companyId: String): List<UpdateEntity> = dao.getUnreadUpdates(companyId)

    fun markUpdateAsRead(id: String): Int = dao.markUpdateAsRead(id)

    fun markAllUpdatesAsRead(companyId: String): Int = dao.markAllUpdatesAsRead(companyId)

    fun deleteUpdate(id: String): Int = dao.deleteUpdate(id)

    fun getUnreadUpdatesCount(companyId: String): Int = dao.getUnreadCountUpdates(companyId)

    // ============ UTILITY OPERATIONS ============

    fun clearAllData() = dao.clearAllTables()

    fun clearTable(tableName: String) = dao.clearTable(tableName)

    fun getTableCount(tableName: String): Int = dao.getTableCount(tableName)

    fun getDatabaseStats(): Map<String, Int> {
        return mapOf(
            "users" to dao.getTableCount(DatabaseHelper.TABLE_USERS),
            "businesses" to dao.getTableCount(DatabaseHelper.TABLE_BUSINESSES),
            "channels" to dao.getTableCount(DatabaseHelper.TABLE_CHANNELS),
            "messages" to dao.getTableCount(DatabaseHelper.TABLE_MESSAGES),
            "ai_config" to dao.getTableCount(DatabaseHelper.TABLE_AI_CONFIG),
            "updates" to dao.getTableCount(DatabaseHelper.TABLE_UPDATES)
        )
    }

    fun close() = dao.close()
}
