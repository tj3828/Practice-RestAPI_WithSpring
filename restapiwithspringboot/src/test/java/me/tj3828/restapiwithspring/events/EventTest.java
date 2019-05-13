package me.tj3828.restapiwithspring.events;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tj3828
 */

public class EventTest {

    @Test
    public void eventBuild() {
        Event event = Event.builder()
                .name("Spring")
                .description("Rest API With Spring")
                .build();
        assertThat(event).isNotNull();

    }

    @Test
    public void eventJavaBean() {
        //given
        String name = "Spring";
        String description = "Rest API With Spring";

        //when
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }

}