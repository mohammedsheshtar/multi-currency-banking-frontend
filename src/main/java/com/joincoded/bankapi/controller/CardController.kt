package com.joincoded.bankapi.controller

import com.joincoded.bankapi.model.Card
import com.joincoded.bankapi.repository.CardRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = ["*"])
class CardController(private val cardRepository: CardRepository) {

    @GetMapping
    fun getAllCards(): ResponseEntity<List<Card>> {
        return ResponseEntity.ok(cardRepository.findByIsActiveTrue())
    }

    @GetMapping("/{id}")
    fun getCardById(@PathVariable id: Long): ResponseEntity<Card> {
        return cardRepository.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    fun createCard(@RequestBody card: Card): ResponseEntity<Card> {
        return ResponseEntity.ok(cardRepository.save(card))
    }

    @DeleteMapping("/{id}")
    fun deleteCard(@PathVariable id: Long): ResponseEntity<Unit> {
        return cardRepository.findById(id)
            .map { card ->
                cardRepository.save(card.copy(isActive = false))
                ResponseEntity.ok().build()
            }
            .orElse(ResponseEntity.notFound().build())
    }
} 