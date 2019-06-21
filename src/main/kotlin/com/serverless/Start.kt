package com.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.mashape.unirest.http.Unirest
import com.wealdtech.hawk.HawkClient
import com.wealdtech.hawk.HawkCredentials
import com.wealdtech.hawk.jersey.HawkAuthorizationFilter
import org.apache.logging.log4j.LogManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Start : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
        LOG.info("received: " + input.keys.toString())

        val id: String = System.getenv("keyId")
        val key = System.getenv("key")

        val hawkCredentials = HawkCredentials.Builder()
                .keyId(id)
                .key(key)
                .algorithm(HawkCredentials.Algorithm.SHA256)
                .build()

        val hawkClient = HawkClient.Builder().credentials(hawkCredentials).build()


        val workTime = LocalDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
        val payload = mapOf(
                "userId" to id,
                "start" to workTime,
                "end" to "2019-06-14T14:15:00.000Z",
                "timezoneName" to "CEST",
                "timezone" to "+0200",
                "type" to "work")

        val response = Unirest.post("https://app.absence.io/api/v2/timespans/create")
                .header("Authorization", HawkAuthorizationFilter(hawkClient).toString())
                .body(payload)

        return ApiGatewayResponse.build {
            statusCode = 200
            objectBody = GatewayResponse("Started to work at: ", input)
            headers = mapOf("X-Powered-By" to "AWS Lambda & serverless")
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(Start::class.java)
    }

}
