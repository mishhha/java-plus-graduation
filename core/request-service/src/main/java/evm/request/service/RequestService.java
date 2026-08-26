package evm.request.service;

import evm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> findById(Long userId);

    ParticipationRequestDto save(Long userId, Long eventId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

}
