package com.hopeandsparks.kb.consumer;

import com.hopeandsparks.kb.service.KbDocumentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Consumer boundary for KB parse messages.
 */
@Component
public class KbParseConsumer {

    private final KbDocumentService kbDocumentService;

    public KbParseConsumer(KbDocumentService kbDocumentService) {
        this.kbDocumentService = kbDocumentService;
    }

    @Scheduled(
            fixedDelayString = "${hope.kb.parse.fixed-delay-ms:5000}",
            initialDelayString = "${hope.kb.parse.initial-delay-ms:5000}"
    )
    public void consumeScheduled() {
        kbDocumentService.consumePendingParseMessages();
    }

    /**
     * Manual hook for tests and demos.
     */
    public int consumeOnce() {
        return kbDocumentService.consumePendingParseMessages();
    }
}
