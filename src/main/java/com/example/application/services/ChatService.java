package com.example.application.services;

import com.example.application.config.HelpfulAIDeveloper;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@BrowserCallable
@AnonymousAllowed
@Service
public class ChatService {

    private final HelpfulAIDeveloper agent;

    public ChatService(HelpfulAIDeveloper agent) {
        this.agent = agent;
    }

    public Flux<String> chat(UUID uuid, String userMessage) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        agent.chat(uuid, userMessage).onNext(sink::tryEmitNext)
                .onComplete(aiMessageResponse -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
    }
}
