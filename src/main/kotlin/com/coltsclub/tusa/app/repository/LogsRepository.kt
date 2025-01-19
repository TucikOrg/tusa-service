package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.LogsEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LogsRepository: CrudRepository<LogsEntity, Long> {
}