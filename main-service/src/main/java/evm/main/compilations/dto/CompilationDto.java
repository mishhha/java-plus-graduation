package evm.main.compilations.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    private Boolean pinned;
    private String title;
    private List<EventDto> events; // тут наверное нужна какая-то DTO-шка от событий
}
