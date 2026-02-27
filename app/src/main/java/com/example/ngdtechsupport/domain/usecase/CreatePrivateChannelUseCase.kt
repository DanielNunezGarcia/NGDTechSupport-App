package com.example.ngdtechsupport.domain.usecase

import com.example.ngdtechsupport.data.repository.ChannelRepository

class CreatePrivateChannelUseCase(
    private val repository: ChannelRepository
) {
    suspend operator fun invoke(
        companyId: String,
        channelId: String,
        adminUid: String,
        memberUid: String
    ) {
        repository.createPrivateChannel(
            companyId,
            channelId,
            adminUid,
            memberUid
        )
    }
}