package io.github.codingmaniac44.vtdemo.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulates a blocking I/O call (a slow database query or remote API) and
 * reports which kind of thread served the request.
 */
@RestController
public class ThreadController {

    private final Duration blockingDelay;

    public ThreadController(@Value("${demo.blocking-delay:2s}") Duration blockingDelay) {
        this.blockingDelay = blockingDelay;
    }

    @GetMapping("/thread")
    public ThreadInfo threadInfo() throws InterruptedException {
        Thread.sleep(blockingDelay);
        Thread current = Thread.currentThread();
        return new ThreadInfo(current.toString(), current.isVirtual());
    }

    public record ThreadInfo(String thread, boolean virtual) {
    }
}
