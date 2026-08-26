package evm.comment.port;

public interface EventLookupPort {

    boolean isPublished(Long eventId);
}