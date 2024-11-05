# http2-hangs

Minimum reproducible sample to display hanging HTTP/2 connections given a certain streaming input size.

Test Scenario

Given two services (client and server), the client calls a Flux producing endpoint on server (/get) with single paramater input (count), denoting how many items the server should produce. The client calls the server again with the produced output from (/get), on the /write endpoint, which is Flux accepting. The client windows the input to /write in batches of 50.

The scenario described above is an excerpt from a real code sample.

What is observed is that for certain values of count (for myself, > 3000), the calls to /write hang infinitely for some windows. This leads to eventual connection starvation if response timeouts are not applied.

Steps

1. Run both client and server locally
2. curl http://localhost:9991/probeGetAndConsume and note client log entry at which processing stalls at (e.g. /getAndConsume 2959)
3. Eventually the timeout handler will trigger and close the connection (with 200 OK)

Note

The connection hang seems to be a symptom of not consuming data fast enough, as increasing WRITE_WINDOW_CONCURRENCY from 1 to 50 prevents the connection from hanging with count = 5000.

Logs

Client

```
2024-11-05T12:17:11.262Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.server.HttpServerOperations     : [745b90a1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] New http connection, requesting read
2024-11-05T12:17:11.263Z DEBUG 24329 --- [ctor-http-nio-3] r.netty.transport.TransportConfig        : [745b90a1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] Initialized pipeline DefaultChannelPipeline{(reactor.left.httpCodec = io.netty.handler.codec.http.HttpServerCodec), (reactor.left.httpTrafficHandler = reactor.netty.http.server.HttpTrafficHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
2024-11-05T12:17:11.283Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.server.HttpServerOperations     : [745b90a1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] Increasing pending responses, now 1
2024-11-05T12:17:11.287Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.http.server.HttpServer     : [745b90a1-1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] Handler is being applied: org.springframework.http.server.reactive.ReactorHttpHandlerAdapter@506bdfb7
2024-11-05T12:17:11.305Z  INFO 24329 --- [ctor-http-nio-3] d.l.client.controller.ClientController   : /getAndConsume 3000
2024-11-05T12:17:11.330Z DEBUG 24329 --- [ctor-http-nio-3] r.n.resources.PooledConnectionProvider   : Creating a new [http2.custom] client pool [PoolFactory{evictionInterval=PT0S, leasingStrategy=fifo, maxConnections=24, maxIdleTime=-1, maxLifeTime=-1, metricsEnabled=false, pendingAcquireMaxCount=-1, pendingAcquireTimeout=45000}] for [localhost/<unresolved>:9990]
2024-11-05T12:17:11.333Z DEBUG 24329 --- [ctor-http-nio-3] r.n.resources.PooledConnectionProvider   : Creating a new [custom] client pool [PoolFactory{evictionInterval=PT0S, leasingStrategy=fifo, maxConnections=24, maxIdleTime=-1, maxLifeTime=-1, metricsEnabled=false, pendingAcquireMaxCount=48, pendingAcquireTimeout=45000}] for [localhost/<unresolved>:9990]
2024-11-05T12:17:11.335Z DEBUG 24329 --- [ctor-http-nio-3] r.n.resources.PooledConnectionProvider   : [14c6232c] Created a new pooled channel, now: 0 active connections, 0 inactive connections and 0 pending acquire requests.
2024-11-05T12:17:11.349Z DEBUG 24329 --- [ctor-http-nio-3] r.netty.transport.TransportConfig        : [14c6232c] Initialized pipeline DefaultChannelPipeline{(reactor.left.h2Flush = io.netty.handler.flush.FlushConsolidationHandler), (reactor.left.httpCodec = io.netty.handler.codec.http2.Http2FrameCodec), (reactor.left.h2MultiplexHandler = io.netty.handler.codec.http2.Http2MultiplexHandler), (reactor.left.httpTrafficHandler = reactor.netty.http.client.HttpTrafficHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
2024-11-05T12:17:11.360Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.ReactorNetty               : [745b90a1-1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] Non Removed handler: reactor.left.readTimeoutHandler, context: null, pipeline: DefaultChannelPipeline{(reactor.left.httpCodec = io.netty.handler.codec.http.HttpServerCodec), (reactor.left.httpTrafficHandler = reactor.netty.http.server.HttpTrafficHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
2024-11-05T12:17:11.360Z DEBUG 24329 --- [ctor-http-nio-3] r.netty.transport.TransportConnector     : [14c6232c] Connecting to [localhost/127.0.0.1:9990].
2024-11-05T12:17:11.361Z DEBUG 24329 --- [ctor-http-nio-3] r.n.r.DefaultPooledConnectionProvider    : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Registering pool release on close event for channel
2024-11-05T12:17:11.361Z DEBUG 24329 --- [ctor-http-nio-3] r.n.resources.PooledConnectionProvider   : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Channel connected, now: 1 active connections, 0 inactive connections and 0 pending acquire requests.
2024-11-05T12:17:11.362Z DEBUG 24329 --- [ctor-http-nio-3] r.n.r.DefaultPooledConnectionProvider    : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] onStateChange(PooledConnection{channel=[id: 0x14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990]}, [connected])
2024-11-05T12:17:11.365Z DEBUG 24329 --- [ctor-http-nio-3] r.n.r.DefaultPooledConnectionProvider    : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] onStateChange(PooledConnection{channel=[id: 0x14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990]}, [configured])
2024-11-05T12:17:11.365Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.http.client.Http2Pool      : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Channel activated
2024-11-05T12:17:11.365Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.http.client.Http2Pool      : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Channel deactivated
2024-11-05T12:17:11.368Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.HttpClientOperations     : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990](H2 - -1) New HTTP/2 stream
2024-11-05T12:17:11.368Z DEBUG 24329 --- [ctor-http-nio-3] r.netty.http.client.HttpClientConfig     : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990](H2 - -1) Initialized HTTP/2 stream pipeline AbstractHttp2StreamChannel$3{(reactor.left.h2ToHttp11Codec = io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec), (reactor.left.httpTrafficHandler = reactor.netty.http.client.Http2StreamBridgeClientHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
2024-11-05T12:17:11.369Z DEBUG 24329 --- [ctor-http-nio-3] r.netty.http.client.HttpClientConnect    : [14c6232c/1-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Handler is being applied: {uri=http://localhost:9990/get/3000, method=GET}
2024-11-05T12:17:11.373Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.Http2ConnectionProvider  : [14c6232c/1-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Stream opened, now: 1 active streams and 2147483647 max active streams.
2024-11-05T12:17:11.377Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.ReactorNetty               : [14c6232c/1-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Added encoder [reactor.left.responseTimeoutHandler] at the beginning of the user pipeline, full pipeline: [reactor.left.h2ToHttp11Codec, reactor.left.httpTrafficHandler, reactor.left.responseTimeoutHandler, reactor.right.reactiveBridge, DefaultChannelPipeline$TailContext#0]
2024-11-05T12:17:11.379Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.HttpClientOperations     : [14c6232c/1-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Received response (auto-read:false) : RESPONSE(decodeResult: success, version: HTTP/1.1)
HTTP/1.1 200 OK
content-type: <filtered>
x-http2-stream-id: <filtered>
transfer-encoding: <filtered>
2024-11-05T12:17:11.392Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.channel.FluxReceive        : [14c6232c/1-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] [terminated=false, cancelled=false, pending=0, error=null]: subscribing inbound receiver
2024-11-05T12:17:11.394Z  INFO 24329 --- [ctor-http-nio-3] d.l.client.controller.ClientController   : Processing window 1
2024-11-05T12:17:11.395Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.http.client.Http2Pool      : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Channel activated
2024-11-05T12:17:11.398Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.http.client.Http2Pool      : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Channel deactivated
2024-11-05T12:17:11.398Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.HttpClientOperations     : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990](H2 - -1) New HTTP/2 stream
2024-11-05T12:17:11.398Z DEBUG 24329 --- [ctor-http-nio-3] r.netty.http.client.HttpClientConfig     : [14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990](H2 - -1) Initialized HTTP/2 stream pipeline AbstractHttp2StreamChannel$3{(reactor.left.h2ToHttp11Codec = io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec), (reactor.left.httpTrafficHandler = reactor.netty.http.client.Http2StreamBridgeClientHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
2024-11-05T12:17:11.398Z DEBUG 24329 --- [ctor-http-nio-3] r.netty.http.client.HttpClientConnect    : [14c6232c/2-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Handler is being applied: {uri=http://localhost:9990/write, method=POST}
2024-11-05T12:17:11.399Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.Http2ConnectionProvider  : [14c6232c/2-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Stream opened, now: 2 active streams and 2147483647 max active streams.
2024-11-05T12:17:11.412Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.ReactorNetty               : [14c6232c/2-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Added encoder [reactor.left.responseTimeoutHandler] at the beginning of the user pipeline, full pipeline: [reactor.left.h2ToHttp11Codec, reactor.left.httpTrafficHandler, reactor.left.responseTimeoutHandler, reactor.right.reactiveBridge, DefaultChannelPipeline$TailContext#0]
2024-11-05T12:17:11.415Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.HttpClientOperations     : [14c6232c/2-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] Received response (auto-read:false) : RESPONSE(decodeResult: success, version: HTTP/1.1)
HTTP/1.1 200 OK
content-type: <filtered>
x-http2-stream-id: <filtered>
transfer-encoding: <filtered>
2024-11-05T12:17:11.415Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.channel.FluxReceive        : [14c6232c/2-1, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990] [terminated=false, cancelled=false, pending=0, error=null]: subscribing inbound receiver
2024-11-05T12:17:16.417Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.Http2ConnectionProvider  : [14c6232c/1-1, L:/127.0.0.1:60786 ! R:localhost/127.0.0.1:9990] Stream closed, now: 2 active streams and 2147483647 max active streams.
2024-11-05T12:17:16.419Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.ReactorNetty               : [14c6232c/1-1, L:/127.0.0.1:60786 ! R:localhost/127.0.0.1:9990] Non Removed handler: reactor.left.responseTimeoutHandler, context: ChannelHandlerContext(reactor.left.responseTimeoutHandler, [id: 0x14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990](H2 - 3)), pipeline: AbstractHttp2StreamChannel$3{(reactor.left.h2ToHttp11Codec = io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec), (reactor.left.httpTrafficHandler = reactor.netty.http.client.Http2StreamBridgeClientHandler), (reactor.left.responseTimeoutHandler = io.netty.handler.timeout.ReadTimeoutHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
2024-11-05T12:17:16.421Z  WARN 24329 --- [ctor-http-nio-3] reactor.netty.channel.FluxReceive        : [14c6232c/1-1, L:/127.0.0.1:60786 ! R:localhost/127.0.0.1:9990] An exception has been observed post termination

reactor.netty.http.client.PrematureCloseException: Connection prematurely closed DURING response

2024-11-05T12:17:16.438Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.HttpClientOperations     : [14c6232c/1-1, L:/127.0.0.1:60786 ! R:localhost/127.0.0.1:9990] Http client inbound receiver cancelled, closing channel.
2024-11-05T12:17:16.438Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.channel.FluxReceive        : [14c6232c/1-1, L:/127.0.0.1:60786 ! R:localhost/127.0.0.1:9990] [terminated=true, cancelled=true, pending=1, error=io.netty.handler.timeout.ReadTimeoutException]: dropping frame CONTENT(decodeResult: success, content: UnpooledSlicedByteBuf(ridx: 0, widx: 21, cap: 21/21, unwrapped: PooledUnsafeDirectByteBuf(ridx: 17405, widx: 17408, cap: 32768)))
2024-11-05T12:17:16.439Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.channel.FluxReceive        : [14c6232c/1-1, L:/127.0.0.1:60786 ! R:localhost/127.0.0.1:9990] [terminated=true, cancelled=true, pending=0, error=io.netty.handler.timeout.ReadTimeoutException]: dropping frame CONTENT(decodeResult: success, content: UnpooledSlicedByteBuf(ridx: 0, widx: 21, cap: 21/21, unwrapped: PooledUnsafeDirectByteBuf(ridx: 17405, widx: 17408, cap: 32768)))
2024-11-05T12:17:16.446Z ERROR 24329 --- [ctor-http-nio-3] a.w.r.e.AbstractErrorWebExceptionHandler : [745b90a1-1]  500 Server Error for HTTP GET "/getAndConsume/3000"

org.springframework.web.reactive.function.client.WebClientResponseException: 200 OK from POST http://localhost:9990/write
	at org.springframework.web.reactive.function.client.WebClientResponseException.create(WebClientResponseException.java:323) ~[spring-webflux-6.1.5.jar:6.1.5]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Error has been observed at the following site(s):
	*__checkpoint ⇢ Handler dev.lightfoot.client.controller.ClientController#getAndConsume(int) [DispatcherHandler]
	*__checkpoint ⇢ HTTP GET "/getAndConsume/3000" [ExceptionHandlingWebHandler]
Original Stack Trace:
		at org.springframework.web.reactive.function.client.WebClientResponseException.create(WebClientResponseException.java:323) ~[spring-webflux-6.1.5.jar:6.1.5]
		at org.springframework.web.reactive.function.client.DefaultClientResponse.lambda$createException$1(DefaultClientResponse.java:214) ~[spring-webflux-6.1.5.jar:6.1.5]
		at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:106) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxOnErrorReturn$ReturnSubscriber.onError(FluxOnErrorReturn.java:199) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onError(FluxDefaultIfEmpty.java:156) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onError(FluxMapFuseable.java:142) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onError(FluxContextWrite.java:121) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxMapFuseable$MapFuseableConditionalSubscriber.onError(FluxMapFuseable.java:340) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxFilterFuseable$FilterFuseableConditionalSubscriber.onError(FluxFilterFuseable.java:382) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.MonoCollect$CollectSubscriber.onError(MonoCollect.java:135) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxMap$MapSubscriber.onError(FluxMap.java:134) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxPeek$PeekSubscriber.onError(FluxPeek.java:222) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxMap$MapSubscriber.onError(FluxMap.java:134) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.Operators.error(Operators.java:198) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.netty.channel.FluxReceive.startReceiver(FluxReceive.java:177) ~[reactor-netty-core-1.1.17.jar:1.1.17]
		at reactor.netty.channel.FluxReceive.subscribe(FluxReceive.java:147) ~[reactor-netty-core-1.1.17.jar:1.1.17]
		at reactor.core.publisher.InternalFluxOperator.subscribe(InternalFluxOperator.java:68) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.netty.ByteBufFlux.subscribe(ByteBufFlux.java:340) ~[reactor-netty-core-1.1.17.jar:1.1.17]
		at reactor.core.publisher.Mono.subscribe(Mono.java:4568) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxOnErrorResume$ResumeSubscriber.onError(FluxOnErrorResume.java:103) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxHandle$HandleSubscriber.onError(FluxHandle.java:213) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxConcatArray$ConcatArraySubscriber.onError(FluxConcatArray.java:208) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxFlattenIterable$FlattenIterableSubscriber.drainAsync(FluxFlattenIterable.java:351) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxFlattenIterable$FlattenIterableSubscriber.drain(FluxFlattenIterable.java:724) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxFlattenIterable$FlattenIterableSubscriber.onError(FluxFlattenIterable.java:263) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxMap$MapSubscriber.onError(FluxMap.java:134) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxPeek$PeekSubscriber.onError(FluxPeek.java:222) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.core.publisher.FluxMap$MapSubscriber.onError(FluxMap.java:134) ~[reactor-core-3.6.4.jar:3.6.4]
		at reactor.netty.channel.FluxReceive.terminateReceiver(FluxReceive.java:480) ~[reactor-netty-core-1.1.17.jar:1.1.17]
		at reactor.netty.channel.FluxReceive.drainReceiver(FluxReceive.java:275) ~[reactor-netty-core-1.1.17.jar:1.1.17]
		at reactor.netty.channel.FluxReceive.onInboundError(FluxReceive.java:468) ~[reactor-netty-core-1.1.17.jar:1.1.17]
		at reactor.netty.channel.ChannelOperations.onInboundError(ChannelOperations.java:515) ~[reactor-netty-core-1.1.17.jar:1.1.17]
		at reactor.netty.channel.ChannelOperationsHandler.exceptionCaught(ChannelOperationsHandler.java:145) ~[reactor-netty-core-1.1.17.jar:1.1.17]
		at io.netty.channel.AbstractChannelHandlerContext.invokeExceptionCaught(AbstractChannelHandlerContext.java:346) ~[netty-transport-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.channel.AbstractChannelHandlerContext.invokeExceptionCaught(AbstractChannelHandlerContext.java:325) ~[netty-transport-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.channel.AbstractChannelHandlerContext.fireExceptionCaught(AbstractChannelHandlerContext.java:317) ~[netty-transport-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.handler.timeout.ReadTimeoutHandler.readTimedOut(ReadTimeoutHandler.java:98) ~[netty-handler-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.handler.timeout.ReadTimeoutHandler.channelIdle(ReadTimeoutHandler.java:90) ~[netty-handler-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.handler.timeout.IdleStateHandler$ReaderIdleTimeoutTask.run(IdleStateHandler.java:525) ~[netty-handler-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.handler.timeout.IdleStateHandler$AbstractIdleTask.run(IdleStateHandler.java:497) ~[netty-handler-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.util.concurrent.PromiseTask.runTask(PromiseTask.java:98) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.util.concurrent.ScheduledFutureTask.run(ScheduledFutureTask.java:153) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.util.concurrent.AbstractEventExecutor.runTask(AbstractEventExecutor.java:173) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:166) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:470) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:566) ~[netty-transport-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
		at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
		at java.base/java.lang.Thread.run(Thread.java:842) ~[na:na]
Caused by: io.netty.handler.timeout.ReadTimeoutException: null

2024-11-05T12:17:16.457Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.server.HttpServerOperations     : [745b90a1-1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] Decreasing pending responses, now 0
2024-11-05T12:17:16.457Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.server.HttpServerOperations     : [745b90a1-1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] Last HTTP packet was sent, terminating the channel
2024-11-05T12:17:16.457Z DEBUG 24329 --- [ctor-http-nio-3] r.netty.channel.ChannelOperations        : [745b90a1-1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] [HttpServer] Channel inbound receiver cancelled (operation cancelled).
2024-11-05T12:17:16.458Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.server.HttpServerOperations     : [745b90a1-1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] Last HTTP response frame
2024-11-05T12:17:16.459Z DEBUG 24329 --- [ctor-http-nio-3] r.n.http.client.Http2ConnectionProvider  : [14c6232c/2-1, L:/127.0.0.1:60786 ! R:localhost/127.0.0.1:9990] Stream closed, now: 1 active streams and 2147483647 max active streams.
2024-11-05T12:17:16.459Z DEBUG 24329 --- [ctor-http-nio-3] reactor.netty.ReactorNetty               : [14c6232c/2-1, L:/127.0.0.1:60786 ! R:localhost/127.0.0.1:9990] Non Removed handler: reactor.left.responseTimeoutHandler, context: ChannelHandlerContext(reactor.left.responseTimeoutHandler, [id: 0x14c6232c, L:/127.0.0.1:60786 - R:localhost/127.0.0.1:9990](H2 - 5)), pipeline: AbstractHttp2StreamChannel$3{(reactor.left.h2ToHttp11Codec = io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec), (reactor.left.httpTrafficHandler = reactor.netty.http.client.Http2StreamBridgeClientHandler), (reactor.left.responseTimeoutHandler = io.netty.handler.timeout.ReadTimeoutHandler), (reactor.right.reactiveBridge = reactor.netty.channel.ChannelOperationsHandler)}
2024-11-05T12:17:16.459Z TRACE 24329 --- [ctor-http-nio-3] r.netty.channel.ChannelOperations        : [745b90a1, L:/[0:0:0:0:0:0:0:1]:9991 - R:/[0:0:0:0:0:0:0:1]:60785] Disposing ChannelOperation from a channel

java.lang.Exception: ChannelOperation terminal stack
	at reactor.netty.channel.ChannelOperations.terminate(ChannelOperations.java:492) ~[reactor-netty-core-1.1.17.jar:1.1.17]
	at io.netty.util.concurrent.AbstractEventExecutor.runTask(AbstractEventExecutor.java:173) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
	at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:166) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:470) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:566) ~[netty-transport-4.1.107.Final.jar:4.1.107.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.107.Final.jar:4.1.107.Final]
	at java.base/java.lang.Thread.run(Thread.java:842) ~[na:na]
```

Client logs with wiretap enabled are commited at _logs_wiretap_.
