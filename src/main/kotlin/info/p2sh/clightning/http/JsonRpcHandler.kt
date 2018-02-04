/*
Copyright (C) 2018 Antoine Le Calvez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package info.p2sh.clightning.http

import com.google.gson.Gson
import com.google.gson.JsonParseException
import info.p2sh.clightning.rpc.RpcClient
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.util.Headers
import java.io.InputStreamReader

/**
 * This HTTP handler listens to POST requests on / and proxies them to the
 * c-lightning socket file.
 *
 * It performs some lite verification:
 *     - is the request correctly formatted JSON
 *     - does it use JSON RPC 2.0
 */
class JsonRpcHandler constructor(
    private val gson: Gson,
    private val client: RpcClient
) : RoutingHandler() {

    init {
        post("/", this::handleRequest)
    }

    override fun handleRequest(exchange: HttpServerExchange) {
        val response = try {
            val request = InputStreamReader(exchange.inputStream).use {
                gson.fromJson(it, JsonRpcRequest::class.java)
            }

            if (request.jsonrpc != "2.0") {
                JsonRpcResponse(
                    request.id,
                    null,
                    JsonRpcError(-32600, "Requests must use JSON RPC 2.0")
                )
            } else {
                client.sendRequest(request)
            }
        } catch (exception: JsonParseException) {
            JsonRpcResponse(1, null, JsonRpcError(-32700, "Parse error"))
        }

        exchange.responseHeaders.put(Headers.CONTENT_TYPE, contentType)
        exchange.statusCode = mapErrorToStatusCode(response.error)

        exchange.responseSender.send(gson.toJson(response))
    }

    private fun mapErrorToStatusCode(error: JsonRpcError?): Int {
        /*
        HTTP Status	code	message
        500	-32700	Parse error.
        400	-32600	Invalid Request.
        404	-32601	Method not found.
        500	-32602	Invalid params.
        500	-32603	Internal error.
        500	-32099..-32000	Server error.
         */
        if (error == null) {
            return 200
        }

        return when (error.code) {
            -32600 -> 400
            -32601 -> 404
            else -> 500
        }
    }

    companion object {
        private const val contentType = "application/json-rpc;charset=utf-8"
    }
}