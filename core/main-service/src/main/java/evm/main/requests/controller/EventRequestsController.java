package evm.main.requests.controller;

import evm.main.requests.dto.EventRequestStatusUpdateRequest;
import evm.main.requests.dto.EventRequestStatusUpdateResult;
import evm.main.requests.dto.ParticipationRequestDto;
import evm.main.requests.service.RequestManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventRequestsController {

    private final RequestManagementService requestManagementService;

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(
        @PathVariable Long userId, @PathVariable Long eventId) {
        return requestManagementService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(
        @PathVariable Long userId, @PathVariable Long eventId,
        @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return requestManagementService.changeRequestStatus(userId, eventId, updateRequest);
    }
}