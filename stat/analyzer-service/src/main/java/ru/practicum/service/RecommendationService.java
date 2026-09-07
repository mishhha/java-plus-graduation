package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserInteraction;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserInteractionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final EventSimilarityRepository similarityRepository;
    private final UserInteractionRepository interactionRepository;

    /**
     * Метод 1: Найти мероприятия, похожие на указанное, исключая те, с которыми пользователь уже взаимодействовал.
     * Используется, когда пользователь лайкнул мероприятие и хочет увидеть похожие.
     */
    @Transactional(readOnly = true)
    public List<Map.Entry<Long, Double>> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        // 1. Получаем все пары сходств, где участвует наше мероприятие
        List<EventSimilarity> similarities = similarityRepository.findAllByEventId(eventId);

        List<Long> interactedEvents = interactionRepository.findEventIdsByUserId(userId);
        Set<Long> interactedSet = new HashSet<>(interactedEvents);

        return similarities.stream()
                .map(sim -> {
                    // Определяем "другое" мероприятие в паре (не то, которое запросили)
                    Long otherEventId = sim.getEventA().equals(eventId) ? sim.getEventB() : sim.getEventA();
                    return Map.entry(otherEventId, sim.getScore());
                })
                .filter(entry -> !interactedSet.contains(entry.getKey())) // Исключаем просмотренные
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // Сортировка по убыванию сходства
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Метод 2: Получить рекомендации для пользователя на основе его последних взаимодействий.
     * Используется для главной страницы — "похожие на то, что вы недавно смотрели".
     */
    @Transactional(readOnly = true)
    public List<Map.Entry<Long, Double>> getRecommendationsForUser(Long userId, int maxResults) {
        List<UserInteraction> recentInteractions = interactionRepository
                .findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, maxResults));

        if (recentInteractions.isEmpty()) {
            log.info("Пользователь {} еще не взаимодействовал ни с одним мероприятием", userId);
            return Collections.emptyList();
        }

        List<Long> interactedEvents = interactionRepository.findEventIdsByUserId(userId);
        Set<Long> interactedSet = new HashSet<>(interactedEvents);

        Map<Long, Double> candidateScores = new HashMap<>();

        for (UserInteraction interaction : recentInteractions) {
            Long interactedEventId = interaction.getEventId();
            List<EventSimilarity> similarities = similarityRepository.findAllByEventId(interactedEventId);

            for (EventSimilarity sim : similarities) {
                Long otherEventId = sim.getEventA().equals(interactedEventId) ? sim.getEventB() : sim.getEventA();

                if (interactedSet.contains(otherEventId)) {
                    continue;
                }

                candidateScores.merge(otherEventId, sim.getScore(), Math::max);
            }
        }

        return candidateScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Метод 3: Получить сумму весов взаимодействий для списка мероприятий (популярность).
     */
    @Transactional(readOnly = true)
    public List<Map.Entry<Long, Double>> getInteractionsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> results = interactionRepository.sumWeightsByEventIds(eventIds);

        return results.stream()
                .map(row -> Map.entry((Long) row[0], (Double) row[1]))
                .collect(Collectors.toList());
    }
}