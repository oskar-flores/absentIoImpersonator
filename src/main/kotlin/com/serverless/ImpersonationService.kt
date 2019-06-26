package com.serverless

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.ObjectMapper
import com.mashape.unirest.http.Unirest
import com.wealdtech.hawk.HawkClient
import com.wealdtech.hawk.HawkCredentials
import org.json.JSONArray
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

class ImpersonationService {
    fun start(id: String, type: String): HttpResponse<JsonNode>? {


        val workTime =formatDate(Instant.now())
        val payload = mapOf(
                "userId" to id,
                "start" to workTime,
                "timezoneName" to "CEST",
                "timezone" to "+0200",
                "type" to type)


        val generateAuthorizationHeader = generateCredentials("https://app.absence.io/api/v2/timespans/create", "post")

        val response = Unirest.post("https://app.absence.io/api/v2/timespans/create")
                .header("Authorization", generateAuthorizationHeader)
                .header("Content-Type", "application/json")
                .body(payload)
                .asJson()

        System.out.println(response.body)
        System.out.println(response.status)

        return response
    }

    fun pause() {
        stop()
        start(id, "break")
    }


     fun stop() {
        val todayDate = LocalDateTime.now().atZone(ZoneId.of("UTC"))
        val timeSpanId = getNewestTimespanIdForDate(todayDate)

        val putCredentials = generateCredentials("https://app.absence.io/api/v2/timespans/$timeSpanId", "put")
        val endDate =formatDate(todayDate.toInstant())

        Unirest.put("https://app.absence.io/api/v2/timespans/$timeSpanId")
                .header("Authorization", putCredentials)
                .header("Content-Type", "application/json")
                .body(mapOf(
                        "end" to endDate
                ))
                .asJson()
    }

    private fun formatDate(instant:Instant):String{
        return  DateTimeFormatter.ISO_INSTANT.format(instant.with(ChronoField.NANO_OF_SECOND , 0))
    }

    private fun getNewestTimespanIdForDate(todayDate: ZonedDateTime): String? {
        val tomorrow = todayDate.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val today = todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val gte = "\$gte"
        val lt = "\$lt"

        val payload = """{
            "filter": {
                "userId": "$id",
                "start": {"$gte": "$today"},
                "end": {"$lt": "$tomorrow"}
            },
            "sortBy":{
                "start": -1
            },
            "limit": 10,
            "skip": 0
        }"""

        val generateAuthorizationHeader = generateCredentials("https://app.absence.io/api/v2/timespans", "post")

        val response = Unirest.post("https://app.absence.io/api/v2/timespans")
                .header("Authorization", generateAuthorizationHeader)
                .header("Content-Type", "application/json")
                .body(payload)
                .asJson()

        val timespanId:String?  = (response.body.`object`.get("data") as JSONArray).getJSONObject(0).get("_id") as String?
        return timespanId
    }

    companion object {
        val id = System.getenv("id")
        val key = System.getenv("key")

        fun init() {
            Unirest.setObjectMapper(object : ObjectMapper {
                var mapper = com.fasterxml.jackson.databind.ObjectMapper()

                override fun writeValue(value: Any): String {
                    return mapper.writeValueAsString(value)
                }

                override fun <T> readValue(value: String, valueType: Class<T>): T {
                    return mapper.readValue(value, valueType)
                }
            })
        }

        fun generateCredentials(url: String, method: String): String {
            val hawkCredentials = HawkCredentials.Builder()
                    .keyId(id)
                    .key(key)
                    .algorithm(HawkCredentials.Algorithm.SHA256)
                    .build()

            val hawkClient = HawkClient.Builder().credentials(hawkCredentials).build()
            return hawkClient.generateAuthorizationHeader(URI.create(url), method, null, null, null, null)
        }
    }
}

fun main() {
    ImpersonationService.init()
    val service = ImpersonationService()
    service.start(ImpersonationService.id,"work")

}