package me.tj3828.restapiwithspring.events;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.tj3828.restapiwithspring.accounts.Account;
import me.tj3828.restapiwithspring.accounts.AccountSerializer;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author tj3828
 */

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@EqualsAndHashCode(of = "id") @Builder
@Entity
public class Event {

    @Id @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location;
    private boolean offline;
    private int basePrice;
    private int maxPrice;
    private boolean free;
    private int limitOfEnrollment;
    @Enumerated(value = EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;
    @ManyToOne
    @JsonSerialize(using = AccountSerializer.class)
    Account manager;

}
