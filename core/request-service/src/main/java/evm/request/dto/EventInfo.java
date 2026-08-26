package evm.request.dto;

import java.time.LocalDateTime;

public class EventInfo {
    private Long id;
    private Long initiatorId;
    private String state;
    private Integer participantLimit;
    private Boolean requestModeration;
    private LocalDateTime publishedOn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInitiatorId() { return initiatorId; }
    public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public Integer getParticipantLimit() { return participantLimit; }
    public void setParticipantLimit(Integer participantLimit) { this.participantLimit = participantLimit; }

    public Boolean getRequestModeration() { return requestModeration; }
    public void setRequestModeration(Boolean requestModeration) { this.requestModeration = requestModeration; }

    public LocalDateTime getPublishedOn() { return publishedOn; }
    public void setPublishedOn(LocalDateTime publishedOn) { this.publishedOn = publishedOn; }
}