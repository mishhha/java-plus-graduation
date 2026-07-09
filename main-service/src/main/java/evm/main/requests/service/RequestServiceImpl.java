package evm.main.requests.service;

import evm.main.event.model.Event;
import evm.main.event.repository.EventRepositoryJPA;
import evm.main.exceptions.NotFoundException;
import evm.main.requests.dto.ResponseRequestDto;
import evm.main.requests.mapper.RequestMapper;
import evm.main.requests.model.Request;
import evm.main.requests.repository.RequestRepositoryJPA;
import evm.main.users.model.User;
import evm.main.users.repository.UserRepositoryJpa;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserRepositoryJpa userRepository;
    private final RequestRepositoryJPA requestRepository;
    private final EventRepositoryJPA eventRepository;
    private final RequestMapper mapper;

    @Override
    public List<ResponseRequestDto> findById(Long userId) {

        boolean findUser = userRepository.existsById(userId);

        if(!findUser) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        List<Request> requestsList = requestRepository.findAllByUserId(userId);

        if(requestsList.isEmpty()) {
            return Collections.emptyList();
        }

        return requestsList.stream()
            .map(mapper::mapToResponseRequestDto)
            .toList();

    }

    @Override
    public ResponseRequestDto save(Long userId, Long eventId) {

        Optional<User> findUser = userRepository.findById(userId);

        if(findUser.isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Optional<Event> findEvent = eventRepository.findById(eventId);

        if(findEvent.isEmpty()) {
            throw new NotFoundException("Мероприятия с ID " + userId + " не найден");
        }

        Request request = mapper.mapToRequest(findUser.get(), findEvent.get());

        return mapper.mapToResponseRequestDto(requestRepository.save(request));

    }

    @Override
    public ResponseRequestDto cancel(Long userId, Long eventId) {

        boolean findUser = userRepository.existsById(userId);

        if(!findUser) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        boolean findEvent = eventRepository.existsById(eventId);

        if(!findEvent) {
            throw new NotFoundException("Мероприятия с ID " + userId + " не найден");
        }

        requestRepository.
    }

}
