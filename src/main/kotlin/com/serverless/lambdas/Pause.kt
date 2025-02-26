package com.serverless.lambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.serverless.infrastructure.ApiGatewayResponse
import com.serverless.infrastructure.GatewayResponse
import com.serverless.services.ImpersonationService
import org.apache.logging.log4j.LogManager


class Pause : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
        LOG.info("received: " + input.keys.toString())
        ImpersonationService.init()
        val service = ImpersonationService()
        val response = service.pause()

        return ApiGatewayResponse.build {
            statusCode = response?.status ?: 200
            objectBody = GatewayResponse("Started to work at: ", mapOf("body" to response?.body.toString()))
            headers = mapOf("X-Powered-By" to "AWS Lambda & serverless")
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(Pause::class.java)
    }

}
