package com.cringeteam.todoproject.data.rest.model

import com.cringeteam.todoproject.domain.model.StatusMessage

class StatusMessageMapper {

    fun map(statusMessage: StatusMessageDto): StatusMessage {
        return StatusMessage(
            statusMessage.codeStatus,
            statusMessage.message,
        )
    }
}