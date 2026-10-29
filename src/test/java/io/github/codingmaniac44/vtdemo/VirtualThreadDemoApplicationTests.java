package io.github.codingmaniac44.vtdemo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * Fast checks against the default profile, which is "virtual".
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "demo.blocking-delay=10ms")
class VirtualThreadDemoApplicationTests {

	private final HttpClient client = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Test
	void defaultProfileServesRequestsOnVirtualThreads() throws Exception {
		assertThat(get("/thread")).contains("\"virtual\":true").contains("VirtualThread");
	}

	@Test
	void actuatorExposesThreadMetrics() throws Exception {
		assertThat(get("/actuator/metrics/jvm.threads.live")).contains("\"name\":\"jvm.threads.live\"");
	}

	private String get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode()).isEqualTo(200);
		return response.body();
	}

}
