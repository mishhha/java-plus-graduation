package evm.main.event.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable //поля встраиваются в таблицу events через @Embedded в Event
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// Координаты места проведения
public class Location {
    private Float lat;  // широта
    private Float lon;  // долгота
}
