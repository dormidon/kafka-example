package ru.mail.polis.channel.service.log;

/**
 * Pauses between consumer's write attempts.
 */
final class Pause {
    private Pause() {
        // should not be instantiated
    }

    /**
     * Simple fixed delay pause implementation is
     * only for demo purposes.
     * In production one must use something like
     * https://en.wikipedia.org/wiki/Exponential_backoff
     */
    static void pause() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
