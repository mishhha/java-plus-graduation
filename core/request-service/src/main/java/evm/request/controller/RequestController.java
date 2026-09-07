package evm.request.controller;

import evm.request.dto.ParticipationRequestDto;
import evm.request.service.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import evm.stat.client.CollectorGrpcClient;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService service;
    private final CollectorGrpcClient collectorClient;

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> findById(@PathVariable Long userId) {
        return service.findById(userId);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto save(
        @PathVariable Long userId,
        @RequestParam(value = "eventId") Long eventId
    ) {
        log.info("POST /users/{}/requests — eventId={}", userId, eventId);

        ParticipationRequestDto requestDto = service.save(userId, eventId);

        try {
            collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
            log.debug("Отправлено действие REGISTER: userId={}, eventId={}", userId, eventId);
        } catch (Exception e) {
            log.warn("Не удалось отправить действие REGISTER в collector-service: {}", e.getMessage());
        }

        return requestDto;
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancel(
        @PathVariable Long userId,
        @PathVariable Long requestId
    ) {
        return service.cancel(userId, requestId);
    }

}
