package ru.mail.polis.channel.service.log;

/**
 * Pauses between consumer's write attempts.
 */
class Pause {
    private Pause() {

    }

    static void pause() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
