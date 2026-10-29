package io.github.codingmaniac44.vtdemo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Fires N simultaneous GET requests at /thread and measures wall-clock time.
 * The client uses virtual threads itself so it is never the bottleneck.
 */
final class ConcurrencyTestSupport {

	record Result(Duration elapsed, List<HttpResponse<String>> responses) {
	}

	private ConcurrencyTestSupport() {
	}

	static Result fire(int port, int requests) throws Exception {
		URI uri = URI.create("http://localhost:" + port + "/thread");
		try (HttpClient client = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(Duration.ofSeconds(30))
				.build();
				ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Future<HttpResponse<String>>> futures = new ArrayList<>(requests);
			long start = System.nanoTime();
			for (int i = 0; i < requests; i++) {
				HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(60)).GET().build();
				futures.add(executor.submit(() -> client.send(request, HttpResponse.BodyHandlers.ofString())));
			}
			List<HttpResponse<String>> responses = new ArrayList<>(requests);
			for (Future<HttpResponse<String>> future : futures) {
				responses.add(future.get());
			}
			return new Result(Duration.ofNanos(System.nanoTime() - start), responses);
		}
	}
}
