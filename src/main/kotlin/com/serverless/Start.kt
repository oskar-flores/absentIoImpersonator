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
