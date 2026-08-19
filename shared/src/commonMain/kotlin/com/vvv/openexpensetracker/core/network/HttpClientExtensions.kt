package com.vvv.openexpensetracker.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.isSuccess

suspend inline fun <reified T> HttpClient.get(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = get(urlString, block)
    return if (response.status.isSuccess()) {
        response.body()
    } else {
        throw ClientRequestException(response, "")
    }
}

suspend inline fun <reified T> HttpClient.post(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = post(urlString, block)
    return if (response.status.isSuccess()) {
        response.body()
    } else {
        throw ClientRequestException(response, "")
    }
}

suspend inline fun <reified T> HttpClient.put(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = put(urlString, block)
    return if (response.status.isSuccess()) {
        response.body()
    } else {
        throw ClientRequestException(response, "")
    }
}

suspend inline fun <reified T> HttpClient.patch(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = patch(urlString, block)
    return if (response.status.isSuccess()) {
        response.body()
    } else {
        throw ClientRequestException(response, "")
    }
}

suspend inline fun <reified T> HttpClient.delete(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = delete(urlString, block)
    return if (response.status.isSuccess()) {
        response.body()
    } else {
        throw ClientRequestException(response, "")
    }
}
