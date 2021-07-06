package com.shtormad.eventsclient

import java.time.LocalDate
import java.time.LocalTime

data class Event(val Id: Int, val Name: String, val Description: String,
                 val DateStart: LocalDate, val DateEnd: LocalDate,
                 val TimeStart: LocalTime, val TimeEnd: LocalTime, val Author: String)  //Храним ID, имя, описание, дату начала и конца, время начала и конца, автора