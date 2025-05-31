package com.joincoded.bankapi.repository

import com.joincoded.bankapi.model.Card
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CardRepository : JpaRepository<Card, Long> {
    fun findByIsActiveTrue(): List<Card>
} 