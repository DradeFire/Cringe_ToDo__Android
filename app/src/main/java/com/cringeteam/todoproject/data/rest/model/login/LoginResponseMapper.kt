package com.cringeteam.todoproject.data.rest.model.login

import com.cringeteam.todoproject.data.rest.model.token.Token
import com.cringeteam.todoproject.domain.model.LoginResponse

class LoginResponseMapper {

    fun map(dto: Token): LoginResponse {
        return LoginResponse(
            token = dto.token,
            userId = dto.userId,
            login = dto.login,
        )
    }
}