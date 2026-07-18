package evm.stat.client;

import evm.stat.dto.HitDto;
import evm.stat.dto.ViewStatsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class StatsClient extends BaseClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(String serverUrl) {
        super(serverUrl);
    }

    //записывает обращение к эндпоинту в сервис статистики
    public void hit(HitDto hitDto) {
        try {
            log.debug("Отправляем hit: app={}, uri={}, ip={}", hitDto.getApp(), hitDto.getUri(), hitDto.getIp());
            post("/hit", hitDto);
        } catch (Exception e) {
            log.warn("Не удалось отправить hit в сервис статистики: {}", e.getMessage());
        }
    }

    //возвращает статистику за указанный период
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> builder.queryParam("uris", uri));
        }

        // build(false) — не кодировать повторно
        String url = builder.build(false).toUriString();
        return getList(url, ViewStatsDto[].class);
    }
}