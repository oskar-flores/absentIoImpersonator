package com.serverless

import com.mashape.unirest.http.ObjectMapper
import com.mashape.unirest.http.Unirest
import com.wealdtech.hawk.HawkClient
import com.wealdtech.hawk.HawkCredentials
import java.net.URI
import java.time.LocalDateTime

class ImpersonationService {
    fun startWork(id: String, key: String) {

        val hawkCredentials = HawkCredentials.Builder()
                .keyId(id)
                .key(key)
                .algorithm(HawkCredentials.Algorithm.SHA256)
                .build()

        val hawkClient = HawkClient.Builder().credentials(hawkCredentials).build()


        val workTime = LocalDateTime.now()
        val payload = mapOf(
                "userId" to id,
                "start" to workTime,
                "end" to "2019-06-24T14:15:00.000Z",
                "timezoneName" to "CEST",
                "timezone" to "+0200",
                "type" to "work")


        val generateAuthorizationHeader = hawkClient.generateAuthorizationHeader(URI.create("https://app.absence.io/api/v2/timespans/create"), "post", null, null, null, null)

        val response = Unirest.post("https://app.absence.io/api/v2/timespans/create")
                .header("Authorization", generateAuthorizationHeader)
                .header("Content-Type", "application/json")
                .body(payload)
                .asJson()
        System.out.println(response.body)
        System.out.println(response.status)
    }

    companion object {
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

fun main() {
    ImpersonationService.init()
    val service = ImpersonationService()
}