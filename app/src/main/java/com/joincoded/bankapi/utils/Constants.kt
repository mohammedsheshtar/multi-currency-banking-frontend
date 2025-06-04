package com.joincoded.bankapi.utils

class Constants {
    companion object {
        // Use localhost for same-device testing
//        const val baseUrl = "http://localhost:9000/"

         const val baseUrl = "http://192.168.123.54:9000/"
        // const val baseUrl = "http://192.168.123.54:9000/"  // Use this when testing from a different device
        const val signupEndpoint = "signup"
        const val depositEndpoint = "deposit"
        const val authorization = "Authorization"
    }

}