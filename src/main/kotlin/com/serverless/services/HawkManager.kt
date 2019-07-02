package com.serverless.services

import com.wealdtech.hawk.HawkClient
import com.wealdtech.hawk.HawkCredentials
import java.net.URI

class HawkManager {
    companion object {
        fun generateCredentials(url: String, method: String): String {
            val hawkCredentials = HawkCredentials.Builder()
                    .keyId(ImpersonationService.id)
                    .key(ImpersonationService.key)
                    .algorithm(HawkCredentials.Algorithm.SHA256)
                    .build()

            val hawkClient = HawkClient.Builder().credentials(hawkCredentials).build()
            return hawkClient.generateAuthorizationHeader(URI.create(url), method, null, null, null, null)
        }
    }
}