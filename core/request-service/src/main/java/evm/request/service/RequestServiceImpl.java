package evm.request.service;

import evm.common.exceptions.ConflictException;
import evm.common.exceptions.NotFoundException;
import evm.request.dto.EventInfo;
import evm.request.dto.ParticipationRequestDto;
import evm.request.mapper.RequestMapper;
import evm.request.model.Request;
import evm.request.model.Status;
import evm.request.port.EventLookupPort;
import evm.request.repository.RequestRepositoryJpa;
import evm.users.model.User;
import evm.users.repository.UserRepositoryJpa;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final UserRepositoryJpa userRepository;
    private final RequestRepositoryJpa requestRepository;
    private final EventLookupPort eventLookupPort;   // ← было EventRepository
    private final RequestMapper mapper;

    @Override
    public List<ParticipationRequestDto> findById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        List<Request> requestsList = requestRepository.findAllByRequesterId(userId);

        if (requestsList.isEmpty()) {
            return Collections.emptyList();
        }

        return requestsList.stream()
            .map(mapper::toDto)
            .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto save(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(
                "Пользователь с ID " + userId + " не найден"
            ));

        EventInfo event = eventLookupPort.findById(eventId);
        if (event == null) {
            throw new NotFoundException("Мероприятия с ID " + eventId + " не найдено");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException(
                "Нельзя отправить повторную заявку на участие в мероприятии."
            );
        }

        if (event.getInitiatorId().equals(userId)) {
            throw new ConflictException(
                "Инициатор события не может добавить запрос на участие в своём событии."
            );
        }

        if (!"PUBLISHED".equals(event.getState()) || event.getPublishedOn() == null) {
            throw new ConflictException(
                "Нельзя участвовать в неопубликованном событии."
            );
        }

        Status status;
        if (!Boolean.TRUE.equals(event.getRequestModeration()) ||
            event.getParticipantLimit() == null || event.getParticipantLimit() == 0) {
            status = Status.CONFIRMED;
        } else {
            status = Status.PENDING;
        }

        if (status == Status.CONFIRMED && event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(
                eventId,
                Status.CONFIRMED
            );

            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException(
                    "У события достигнут лимит запросов на участие"
                );
            }
        }

        Request request = mapper.mapToRequest(user, eventId);   // ← eventId вместо event
        request.setStatus(status);

        return mapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Optional<Request> findRequest = requestRepository.findById(requestId);

        if (findRequest.isEmpty()) {
            throw new NotFoundException("Запроса с ID " + requestId + " не найдено");
        }

        if (!findRequest.get().getRequester().getId().equals(userId)) {
            throw new ConflictException(
                "Заявка с ID " + requestId + " не принадлежит пользователю " + userId
            );
        }

        findRequest.get().setStatus(Status.CANCELED);

        Request update = requestRepository.save(findRequest.get());

        return mapper.toDto(update);
    }
}