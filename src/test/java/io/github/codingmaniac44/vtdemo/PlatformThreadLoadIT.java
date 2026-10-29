package io.github.codingmaniac44.vtdemo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * The control group: the same 200 requests against a 50-thread Tomcat pool.
 * Requests queue in waves of 50, so the run takes at least 4 x 2s = 8 seconds.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("platform")
class PlatformThreadLoadIT {

	private static final int REQUESTS = 200;

	@Value("${local.server.port}")
	private int port;

	@Test
	void concurrentBlockingRequestsQueueBehindThePool() throws Exception {
		ConcurrencyTestSupport.Result result = ConcurrencyTestSupport.fire(port, REQUESTS);

		assertThat(result.responses()).hasSize(REQUESTS)
				.allSatisfy(response -> {
					assertThat(response.statusCode()).isEqualTo(200);
					assertThat(response.body()).contains("\"virtual\":false");
				});
		assertThat(result.elapsed()).isGreaterThanOrEqualTo(Duration.ofSeconds(8));
	}

}
