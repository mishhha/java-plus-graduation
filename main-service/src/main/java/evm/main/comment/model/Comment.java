package evm.main.comment.model;


import evm.main.event.model.Event;
import evm.main.users.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(nullable = false, length = 4000)
    String text;

    @Column(name = "published_date", nullable = false)
    LocalDateTime publishedDate;

    @ToString.Exclude
    @JoinColumn(name = "author_id")
    @ManyToOne(fetch = FetchType.LAZY)
    User author;

    @ToString.Exclude
    @JoinColumn(name = "event_id")
    @ManyToOne(fetch = FetchType.LAZY)
    Event event;


}
