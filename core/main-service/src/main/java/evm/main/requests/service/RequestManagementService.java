package evm.main.requests.service;

import evm.common.exceptions.ConflictException;
import evm.common.exceptions.NotFoundException;
import evm.event.model.Event;
import evm.event.repository.EventRepository;
import evm.main.requests.dto.*;
import evm.main.requests.mapper.RequestMapper;
import evm.main.requests.model.Request;
import evm.main.requests.model.Status;
import evm.main.requests.repository.RequestRepositoryJpa;
import evm.users.repository.UserRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestManagementService {

    private final EventRepository eventRepository;
    private final RequestRepositoryJpa requestRepository;
    private final RequestMapper mapper;
    private final UserRepositoryJpa userRepository;

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        checkUserExists(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
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
        Event event = getEventOrThrow(eventId);

        // Проверка прав организатора
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        List<Long> requestedIds = dto.getRequestIds();

        // Получаем заявки только для этого события
        List<Request> requests = requestRepository.findAllByIdInAndEventId(dto.getRequestIds(), eventId);

        if (requests.size() != requestedIds.size()) {
            throw new IllegalArgumentException("Обнаружены ID заявок, не принадлежащих данному событию. " +
                "Проверьте корректность переданных requestIds.");
        }

        if (requests.isEmpty()) {
            throw new NotFoundException("Заявки не найдены для события " + eventId);
        }

        // Если модерация отключена или лимит = 0 — подтверждение не требуется
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            // Автоматически подтверждаем все заявки
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

        // Лимит уже достигнут — нельзя подтверждать новые заявки
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
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
            // Подтверждаем заявки пока не достигнем лимита
            long available = event.getParticipantLimit() - confirmed;

            confirmedList = requests.stream()
                .limit(available)
                .peek(r -> r.setStatus(Status.CONFIRMED))
                .toList();

            // Остальные автоматически отклоняем
            rejectedList = requests.stream()
                .skip(available)
                .peek(r -> r.setStatus(Status.REJECTED))
                .collect(Collectors.toList());

            // Если лимит исчерпан — отклоняем все ожидающие заявки на это событие
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

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }


}