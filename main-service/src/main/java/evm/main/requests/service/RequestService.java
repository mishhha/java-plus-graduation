package evm.main.requests.service;

import evm.main.requests.dto.ResponseRequestDto;

import java.util.List;

public interface RequestService {

    List<ResponseRequestDto> findById(Long userId);

    ResponseRequestDto save(Long userId, Long eventId);

    ResponseRequestDto cancel(Long userId, Long eventId);

}
