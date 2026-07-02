package evm.stat.client;

import evm.stat.dto.HitDto;
import evm.stat.dto.ViewStatsDto;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

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
        //кодируем дату, что убирать пробелы
        String encodedStart = encode(start.format(FORMATTER));
        String encodedEnd = encode(end.format(FORMATTER));

        StringBuilder uriBuilder = new StringBuilder("/stats")
                .append("?start=").append(encodedStart)
                .append("&end=").append(encodedEnd)
                .append("&unique=").append(unique);

        //список URI для фильтрации (null — все URI)
        if (uris != null && !uris.isEmpty()) {
            // /stats?...&uris=/events/1&uris=/events/2
            StringJoiner joiner = new StringJoiner("&uris=", "&uris=", "");
            //добавляем каждый эл. в joiner
            uris.forEach(joiner::add);
            uriBuilder.append(joiner);
        }

        log.debug("Запрашиваем статистику: {}", uriBuilder);
        return getList(uriBuilder.toString(), ViewStatsDto[].class);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}