# Spring Boot Virtual Threads Benchmark

A small Spring Boot 4.1 app that shows, with numbers, what Java 21 **virtual threads** buy you over classic **platform threads** when requests block on I/O.

## The result

2,000 requests to `GET /thread`, 1,000 at a time. Each request blocks for 2 seconds, like a slow database query or remote API call.

| Profile | Threads | Total time | Throughput | Median latency | p99 latency |
|---|---|---|---|---|---|
| `platform` | 50 Tomcat OS threads | 80.5 s | 25 req/s | 40.1 s | 42.2 s |
| `virtual` | one virtual thread per request | 4.5 s | 443 req/s | 2.2 s | 2.4 s |

Measured on a Windows 11 laptop with Temurin 21, using the Java load client described below. Your numbers will vary, but the shape will not.

With 50 platform threads, only 50 requests can sleep at once. The other 950 wait in a queue, so each request spends about 40 seconds waiting for a thread and 2 seconds doing work. With virtual threads, a blocked request parks its virtual thread and frees the underlying carrier thread. All 1,000 requests sleep at the same time and finish in about 2 seconds.

## How it works

* `GET /thread` sleeps for `demo.blocking-delay` (default `2s`) and returns the thread that served it:
  ```json
  {"thread":"Thread[#52,http-nio-8080-exec-2,5,main]","virtual":false}
  {"thread":"VirtualThread[#58,tomcat-handler-1]/runnable@ForkJoinPool-1-worker-1","virtual":true}
  ```
* Two Spring profiles switch the threading model:
  * `virtual` (the default): `spring.threads.virtual.enabled=true`
  * `platform`: `spring.threads.virtual.enabled=false` and `server.tomcat.threads.max=50`
* `server.tomcat.accept-count=1000` lets the OS queue connections, so the benchmark measures threads rather than refused connections.
* Actuator exposes JVM thread metrics. Watch them while a load test runs:
  ```
  http://localhost:8080/actuator/metrics/jvm.threads.live
  http://localhost:8080/actuator/metrics/jvm.threads.peak
  ```

## Run it

Requires Java 21 or newer. The Maven wrapper downloads everything else.

```bash
# Virtual threads (default)
./mvnw spring-boot:run

# Platform threads, 50-thread pool
./mvnw spring-boot:run -Dspring-boot.run.profiles=platform
```

Then, in a second terminal:

```bash
curl http://localhost:8080/thread                 # one request: ~2 s either way
scripts/load-test.sh 2000 1000                    # 2,000 requests, 1,000 concurrent
```

On Windows, use `scripts/load-test.ps1 -Requests 2000 -Concurrency 1000` in PowerShell 7.
Both scripts use [`hey`](https://github.com/rakyll/hey) when it is installed, for example `hey -n 2000 -c 1000 http://localhost:8080/thread`, and fall back to curl otherwise.

A single request takes 2 seconds on either profile. The difference only appears under concurrent load.

## Tests

```bash
./mvnw verify
```

| Test | What it checks |
|---|---|
| `VirtualThreadDemoApplicationTests` | Default profile serves requests on virtual threads, and Actuator exposes `jvm.threads.live`. |
| `VirtualThreadLoadIT` | 200 concurrent 2-second requests on the `virtual` profile finish in under 15 s (about 2 s in practice, versus 400 s if serialized). |
| `PlatformThreadLoadIT` | The same 200 requests on the `platform` profile take at least 8 s, because they queue in waves of 50. |

The `*IT` load tests run in the `verify` phase through the Maven Failsafe plugin.

## Tech stack

* Java 21
* Spring Boot 4.1 (Spring MVC on embedded Tomcat 11, Actuator)
* Maven

## License

[MIT](LICENSE)
