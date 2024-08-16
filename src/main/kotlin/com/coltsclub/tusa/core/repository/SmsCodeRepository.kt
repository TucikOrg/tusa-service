package com.coltsclub.tusa.core.repository

import com.coltsclub.tusa.core.entity.SmsCodeEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SmsCodeRepository : JpaRepository<SmsCodeEntity, Long> {

    @Query(
        value = """
          select s from smsCode s 
          where s.phone = :phone and s.code = :code and s.activated = false and s.expiredAt > current_timestamp
        """
    )
    fun findLoginCode(phone: String, code: String): Optional<SmsCodeEntity>
}