package io.github.codingmaniac44.vtdemo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * 200 concurrent 2-second requests on virtual threads. Run serially they would
 * take 400 seconds; on virtual threads they should all finish in roughly 2.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("virtual")
class VirtualThreadLoadIT {

	private static final int REQUESTS = 200;

	@Value("${local.server.port}")
	private int port;

	@Test
	void concurrentBlockingRequestsFinishInAboutOneDelay() throws Exception {
		ConcurrencyTestSupport.Result result = ConcurrencyTestSupport.fire(port, REQUESTS);

		assertThat(result.responses()).hasSize(REQUESTS)
				.allSatisfy(response -> {
					assertThat(response.statusCode()).isEqualTo(200);
					assertThat(response.body()).contains("\"virtual\":true");
				});
		// 200 x 2s = 400s if serialized. Allow generous headroom for slow CI machines.
		assertThat(result.elapsed()).isLessThan(Duration.ofSeconds(15));
	}

}
