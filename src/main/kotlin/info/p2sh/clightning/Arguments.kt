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

import com.beust.jcommander.Parameter

internal class Arguments {
    @Parameter(
        names = ["-c"],
        description = "Path to c-lightning's RPC socket file",
        required = true
    )
    var cLightningRpc: String? = null

    @Parameter(
        names = ["-p"],
        description = "Port used by the HTTP API"
    )
    var httpPort: Int = 8080

    @Parameter(
        names = ["-H"],
        description = "Host used by the HTTP API"
    )
    var httpHost: String = "localhost"
}
