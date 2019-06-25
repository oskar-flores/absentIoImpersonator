package com.serverless

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.logging.log4j.LogManager


class Stop : RequestHandler<Map<String, Any>, ApiGatewayResponse> {
    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {
        LOG.info("received: " + input.keys.toString())
        ImpersonationService.init()
        val service = ImpersonationService()
        service.stop()

        return ApiGatewayResponse.build {
            statusCode = 200
            objectBody = GatewayResponse("Started to work at: ", input)
            headers = mapOf("X-Powered-By" to "AWS Lambda & serverless")
        }
    }

    companion object {
        private val LOG = LogManager.getLogger(Stop::class.java)
    }

}
