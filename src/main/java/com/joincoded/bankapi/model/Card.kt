package com.joincoded.bankapi.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "cards")
data class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val cardNumber: String,

    @Column(nullable = false)
    val cardHolderName: String,

    @Column(nullable = false)
    val expiryDate: String,

    @Column(nullable = false)
    val cvv: String,

    @Column(nullable = false)
    val balance: BigDecimal,

    @Column(nullable = false)
    val currency: String,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val isActive: Boolean = true
) 