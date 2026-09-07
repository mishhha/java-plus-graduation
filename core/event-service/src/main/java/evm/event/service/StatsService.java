package evm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import evm.stat.client.CollectorGrpcClient;
import evm.stat.client.RecommendationsGrpcClient;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;
import ru.practicum.ewm.stats.proto.dashboard.RecommendedEventProto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final CollectorGrpcClient collectorClient;
    private final RecommendationsGrpcClient recommendationsClient;

    /**
     * Отправить действие "просмотр" мероприятия.
     */
    public void sendViewAction(Long userId, Long eventId) {
        try {
            collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
            log.debug("Отправлено действие VIEW: userId={}, eventId={}", userId, eventId);
        } catch (Exception e) {
            log.error("Ошибка отправки действия VIEW: {}", e.getMessage(), e);
        }
    }

    /**
     * Отправить действие "лайк" мероприятия.
     */
    public void sendLikeAction(Long userId, Long eventId) {
        try {
            collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
            log.debug("Отправлено действие LIKE: userId={}, eventId={}", userId, eventId);
        } catch (Exception e) {
            log.error("Ошибка отправки действия LIKE: {}", e.getMessage(), e);
        }
    }

    /**
     * Получить похожие мероприятия.
     */
    public List<RecommendedEventProto> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        try {
            return recommendationsClient.getSimilarEvents(eventId, userId, maxResults);
        } catch (Exception e) {
            log.error("Ошибка получения похожих событий: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Получить персональные рекомендации.
     */
    public List<RecommendedEventProto> getRecommendations(Long userId, int maxResults) {
        try {
            return recommendationsClient.getRecommendationsForUser(userId, maxResults);
        } catch (Exception e) {
            log.error("Ошибка получения рекомендаций: {}", e.getMessage(), e);
            return List.of();
        }
    }
}