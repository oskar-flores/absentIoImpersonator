package com.serverless.infrastructure

data class GatewayResponse(val message: String, val input: Map<String, Any>) : Response()
