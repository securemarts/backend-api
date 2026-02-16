package com.securemarts.domain.rider.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds SSE connections per rider and broadcasts delivery events to them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiderSseRegistry {

    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L; // 5 minutes

    private final ObjectMapper objectMapper;

    private final Map<String, Set<SseEmitter>> riderEmitters = new ConcurrentHashMap<>();

    public SseEmitter createAndRegister(String riderPublicId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitter.onCompletion(() -> remove(riderPublicId, emitter));
        emitter.onTimeout(() -> remove(riderPublicId, emitter));
        riderEmitters.computeIfAbsent(riderPublicId, k -> ConcurrentHashMap.newKeySet()).add(emitter);
        return emitter;
    }

    public void remove(String riderPublicId, SseEmitter emitter) {
        Set<SseEmitter> set = riderEmitters.get(riderPublicId);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) {
                riderEmitters.remove(riderPublicId);
            }
        }
    }

    public void sendToRider(String riderPublicId, Object payload) {
        Set<SseEmitter> set = riderEmitters.get(riderPublicId);
        if (set == null || set.isEmpty()) return;
        String json = toJson(payload);
        if (json == null) return;
        for (SseEmitter emitter : Set.copyOf(set)) {
            try {
                emitter.send(SseEmitter.event().data(json));
            } catch (IOException e) {
                log.warn("SSE send failed for rider {}: {}", riderPublicId, e.getMessage());
                remove(riderPublicId, emitter);
            }
        }
    }

    public void sendToRiders(List<String> riderPublicIds, Object payload) {
        if (riderPublicIds == null || riderPublicIds.isEmpty()) return;
        for (String riderPublicId : riderPublicIds) {
            sendToRider(riderPublicId, payload);
        }
    }

    private String toJson(Object payload) {
        try {
            return payload instanceof String ? (String) payload : objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("SSE payload serialize failed: {}", e.getMessage());
            return null;
        }
    }
}
