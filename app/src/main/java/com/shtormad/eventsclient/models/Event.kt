package com.shtormad.eventsclient.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * DATA-Класс для структурированного хранения мероприятий
 *
 * Храним ID, название, описание, даты начала и конца, время начала и конца и автора
 */

@Serializable
data class Event(val id: Int, val name: String, val description: String,
                 val dateStart: LocalDateTime, val dateEnd: LocalDateTime,
                 val timeStart: LocalDateTime, val timeEnd: LocalDateTime, val author: String)