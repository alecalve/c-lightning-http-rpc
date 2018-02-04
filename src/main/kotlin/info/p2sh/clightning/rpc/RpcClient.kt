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

package info.p2sh.clightning.rpc

import com.google.gson.Gson
import info.p2sh.clightning.http.JsonRpcError
import info.p2sh.clightning.http.JsonRpcRequest
import info.p2sh.clightning.http.JsonRpcResponse
import jnr.unixsocket.UnixSocketAddress
import jnr.unixsocket.UnixSocketChannel
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.CharBuffer
import java.nio.channels.Channels

class RpcClient(private val path: String, private val gson: Gson) {
    fun sendRequest(request: JsonRpcRequest): JsonRpcResponse {
        val socketFile = File(path)
        val address = UnixSocketAddress(socketFile)
        val channel = UnixSocketChannel.open(address)

        val result = PrintWriter(Channels.newOutputStream(channel)).use {
            it.print(gson.toJson(request))
            it.flush()

            InputStreamReader(Channels.newInputStream(channel)).use {
                val builder = StringBuilder()

                do {
                    val result = CharBuffer.allocate(8192)
                    it.read(result)
                    result.flip()
                    builder.append(result)
                } while (result.length == 8192)

                builder.toString()
            }
        }

        channel.close()

        // If c-lightning doesn't return anything, it means it couldn't parse
        // the request
        return if (result.isNotEmpty()) {
            gson.fromJson(result, JsonRpcResponse::class.java)
        } else {
            JsonRpcResponse(
                request.id,
                null,
                JsonRpcError(-32700, "Parse error")
            )
        }
    }
}