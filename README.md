# htt2-hangs

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

The connection hang seems to be a symptom of not consuming data fast enough, and the issue perhaps lies within a Reactor Operator.
