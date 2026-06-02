package com.virtulab.platform.websocket.handler;

import java.net.URI;
import java.util.List;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class LiveProgressWebSocketHandler implements WebSocketHandler {

    private final JwtDecoder jwtDecoder;
    private final Flux<String> liveProgressFlux;

    public LiveProgressWebSocketHandler(JwtDecoder jwtDecoder, Flux<String> liveProgressFlux) {
        this.jwtDecoder = jwtDecoder;
        this.liveProgressFlux = liveProgressFlux.share();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        if (!isAuthorized(session)) {
            return session.close();
        }

        Flux<WebSocketMessage> welcome = Flux.just(session.textMessage(
                "{\"type\":\"connected\",\"message\":\"VirtuLab live progress channel\"}"));

        Flux<WebSocketMessage> events = liveProgressFlux
                .map(payload -> session.textMessage("{\"type\":\"progress\",\"payload\":" + payload + "}"));

        return session.send(Flux.concat(welcome, events));
    }

    private boolean isAuthorized(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();
        List<String> tokens = UriComponentsBuilder.fromUri(uri).build().getQueryParams().get("token");
        if (tokens == null || tokens.isEmpty()) {
            return false;
        }
        try {
            jwtDecoder.decode(tokens.get(0));
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}
