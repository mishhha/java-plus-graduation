package evm.comment.model;

import evm.users.model.User; // Связь с User оставляем
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author; // Прямая связь с User

    @Column(name = "event_id", nullable = false)
    private Long eventId; // Было: Event event

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "edited")
    private LocalDateTime edited;
}