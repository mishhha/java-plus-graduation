package evm.stat.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

//Базовый HTTP-клиент на основе RestClient
public abstract class BaseClient {

    protected final RestClient restClient;

    protected BaseClient(String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    protected ResponseEntity<Void> post(String path, Object body) {
        try {
            restClient.post()
                    .uri(path)
                    .body(body)
                    .retrieve()             //выполнить запрос и получить ответ
                    .toBodilessEntity();    //возвращает только статус
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    protected <T> List<T> getList(String path, Class<T[]> itemClass) {
        //Class<T[]> itemClass - передаем класс массива StatsDto[].class,
        //чтобы дессерилиазовать в json массив из ответа
        try {
            T[] result = restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(itemClass);   //десереализуем тело ответа в Т[]
            return result != null ? List.of(result) : List.of();
        } catch (HttpClientErrorException e) {
            return List.of();
        }
    }
}
