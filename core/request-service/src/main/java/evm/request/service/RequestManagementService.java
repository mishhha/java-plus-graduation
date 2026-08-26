package evm.request.service;

import evm.common.exceptions.ConflictException;
import evm.common.exceptions.NotFoundException;
import evm.request.dto.EventInfo;
import evm.request.dto.EventRequestStatusUpdateRequest;
import evm.request.dto.EventRequestStatusUpdateResult;
import evm.request.dto.ParticipationRequestDto;
import evm.request.mapper.RequestMapper;
import evm.request.model.Request;
import evm.request.model.Status;
import evm.request.port.EventLookupPort;
import evm.request.repository.RequestRepositoryJpa;
import evm.users.repository.UserRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestManagementService {

    private final EventLookupPort eventLookupPort;
    private final RequestRepositoryJpa requestRepository;
    private final RequestMapper mapper;
    private final UserRepositoryJpa userRepository;

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        checkUserExists(userId);
        EventInfo event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        return requestRepository.findAllByEventId(eventId).stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest dto) {
        checkUserExists(userId);
        EventInfo event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        List<Long> requestedIds = dto.getRequestIds();

        List<Request> requests = requestRepository.findAllByIdInAndEventId(dto.getRequestIds(), eventId);

        if (requests.size() != requestedIds.size()) {
            throw new IllegalArgumentException("Обнаружены ID заявок, не принадлежащих данному событию.");
        }

        if (requests.isEmpty()) {
            throw new NotFoundException("Заявки не найдены для события " + eventId);
        }

        // Если модерация отключена или лимит = 0 — подтверждение не требуется
        if (!Boolean.TRUE.equals(event.getRequestModeration()) ||
            event.getParticipantLimit() == null || event.getParticipantLimit() == 0) {
            requests.forEach(r -> r.setStatus(Status.CONFIRMED));
            requestRepository.saveAll(requests);

            return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requests.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList()))
                .rejectedRequests(List.of())
                .build();
        }

        Long confirmed = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);

        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0 &&
            confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников достигнут");
        }

        // Проверяем что все заявки в статусе PENDING
        requests.forEach(r -> {
            if (!Status.PENDING.equals(r.getStatus())) {
                throw new ConflictException("Заявка с id=" + r.getId() +
                    " должна находиться в статусе PENDING, текущий статус: " + r.getStatus()
                );
            }
        });

        Status newStatus = Status.valueOf(dto.getStatus());

        List<Request> confirmedList = List.of();
        List<Request> rejectedList = List.of();

        if (Status.CONFIRMED.equals(newStatus)) {
            long available = event.getParticipantLimit() - confirmed;

            confirmedList = requests.stream()
                .limit(available)
                .peek(r -> r.setStatus(Status.CONFIRMED))
                .toList();

            rejectedList = requests.stream()
                .skip(available)
                .peek(r -> r.setStatus(Status.REJECTED))
                .collect(Collectors.toList());

            if (confirmedList.size() == available) {
                requestRepository.findAllByEventId(eventId).stream()
                    .filter(r -> Status.PENDING.equals(r.getStatus()))
                    .forEach(r -> r.setStatus(Status.REJECTED));
            }
        } else {
            rejectedList = requests.stream()
                .peek(r -> r.setStatus(Status.REJECTED))
                .collect(Collectors.toList());
        }

        requestRepository.saveAll(requests);

        List<Request> finalConfirmed = confirmedList;
        List<Request> finalRejected = rejectedList;

        return EventRequestStatusUpdateResult.builder()
            .confirmedRequests(finalConfirmed.stream()
                .map(mapper::toDto).collect(Collectors.toList()))
            .rejectedRequests(finalRejected.stream()
                .map(mapper::toDto).collect(Collectors.toList()))
            .build();
    }

    private EventInfo getEventOrThrow(Long eventId) {
        EventInfo info = eventLookupPort.findById(eventId);
        if (info == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        return info;
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}