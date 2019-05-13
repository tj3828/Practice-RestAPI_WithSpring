package me.tj3828.restapiwithspring.events;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author tj3828
 */
public interface EventRepository extends JpaRepository<Event, Integer> {
}
