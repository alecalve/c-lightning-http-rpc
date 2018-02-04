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

package info.p2sh.clightning

import com.beust.jcommander.JCommander
import com.google.gson.GsonBuilder
import info.p2sh.clightning.http.JsonRpcHandler
import info.p2sh.clightning.rpc.RpcClient
import io.undertow.Undertow
import io.undertow.server.handlers.BlockingHandler
import java.io.File

fun main(args: Array<String>) {
    val arguments = Arguments()

    JCommander
        .newBuilder()
        .addObject(arguments)
        .build()
        .parse(*args)

    val rpcFile = arguments.cLightningRpc!!

    if (!File(rpcFile).exists()) {
        System.err.println("RPC file does not exist")
        return System.exit(1)
    }

    val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    val rpcClient = RpcClient(rpcFile, gson)

    val server = Undertow.builder()
        .addHttpListener(arguments.httpPort, arguments.httpHost)
        .setHandler(BlockingHandler(JsonRpcHandler(gson, rpcClient)))
        .build()

    Runtime.getRuntime().addShutdownHook(Thread { server.stop() })

    server.start()
}