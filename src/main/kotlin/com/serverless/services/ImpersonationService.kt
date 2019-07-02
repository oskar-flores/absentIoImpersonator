package com.serverless.services

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.ObjectMapper
import com.mashape.unirest.http.Unirest
import com.serverless.CREATE_ENDPOINT
import com.serverless.LIST_ENDPOINT
import com.serverless.services.HawkManager.Companion.generateCredentials
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

class ImpersonationService {
    fun start(id: String, type: String="work"): HttpResponse<JsonNode>? {


        val workTime = formatDate(Instant.now())
        val payload = JSONObject()
        payload.put("userId",id)
                .put("start",workTime)
                .put("timezoneName","CEST")
                .put("timezone","+0200")
                .put("type",type)


        val generateAuthorizationHeader = generateCredentials(CREATE_ENDPOINT, "post")

        val response = Unirest.post(CREATE_ENDPOINT)
                .header("Authorization", generateAuthorizationHeader)
                .header("Content-Type", "application/json")
                .body(payload)
                .asJson()

        return response
    }

    fun pause(): HttpResponse<JsonNode>? {
        stop()
        return start(id, "break")
    }


    fun stop(): HttpResponse<JsonNode>? {
        val todayDate = ZonedDateTime.now(ZoneId.of("UTC"))
        val timeSpanId = getNewestTimespanIdForDate(todayDate)

        val putCredentials = generateCredentials("$LIST_ENDPOINT/$timeSpanId", "put")
        val endDate = formatDate(todayDate.toInstant())

        val payload = JSONObject()
        payload.put("end", endDate)

        return Unirest.put("$LIST_ENDPOINT/$timeSpanId")
                .header("Authorization", putCredentials)
                .header("Content-Type", "application/json")
                .body(payload)
                .asJson()
    }

    private fun formatDate(instant: Instant): String {
        return DateTimeFormatter.ISO_INSTANT.format(instant.with(ChronoField.NANO_OF_SECOND, 0))
    }

    private fun getNewestTimespanIdForDate(todayDate: ZonedDateTime): String? {
        val today = todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val gte = "\$gte"

        /*
        Get all of today, with newest first. Hopefully we get the ongoin the first
         */
        val payload = """{
            "filter": {
                "userId": "$id",
                "start": {"$gte": "$today"}
            },
            "sortBy":{
                "start": -1
            },
            "limit": 10,
            "skip": 0
        }"""

        val generateAuthorizationHeader = generateCredentials(LIST_ENDPOINT, "post")

        val response = Unirest.post(LIST_ENDPOINT)
                .header("Authorization", generateAuthorizationHeader)
                .header("Content-Type", "application/json")
                .body(payload)
                .asJson()

        val timespanId: String? = (response.body.`object`.get("data") as JSONArray).getJSONObject(0).get("_id") as String?
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

    }
}
