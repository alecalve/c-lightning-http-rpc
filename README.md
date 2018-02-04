# c-lightning-http-rpc
Provides a JSON RPC HTTP proxy to c-lightning's unix socket based interface.

## Building

To build, just run
```
./gradlew build
```

## Usage

```
java -jar build/libs/c-lightning-http-rpc-0.1.0.jar -c ~/.lightning/lightning-rpc
```

You can then execute RPC requests using HTTP:

```
curl -XPOST http://localhost:8080/ -d '{"method": "listpeers", "id": 1, "params": {}, "jsonrpc": "2.0"}'
```

returns

```
{
  "id": 1.0,
  "result": {
    "peers": []
  },
  "jsonrpc": "2.0"
}  
```

## Limitations

For now, only POST requests are supported. See issue [#1](https://github.com/alecalve/c-lightning-http-rpc/issues/1)

No authentication. See issue [#2](https://github.com/alecalve/c-lightning-http-rpc/issues/2)
