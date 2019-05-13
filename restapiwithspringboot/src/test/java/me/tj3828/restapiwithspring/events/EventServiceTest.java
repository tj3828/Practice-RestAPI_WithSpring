package me.tj3828.restapiwithspring.events;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tj3828
 */

@RunWith(JUnitParamsRunner.class)
public class EventServiceTest {

    private EventService eventService;

    @Before
    public void setUp() {
        eventService = new EventService();
    }

    @Test
    @Parameters
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        //given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        //when
        eventService.update(event);

        //then
        assertThat(event.isFree()).isEqualTo(isFree);
    }

    private Object[] parametersForTestFree() {
        return new Object[] {
                new Object[] {0, 0, true},
                new Object[] {100, 0, false},
                new Object[] {0, 100, false},
                new Object[] {100, 200 , false}
        };
    }

    @Test
    @Parameters
    public void testOffline(String location, boolean isLocation) {
        //given
        Event event = Event.builder()
                .location(location)
                .build();

        //when
        eventService.update(event);

        //then
        assertThat(event.isOffline()).isEqualTo(isLocation);
    }

    private Object[] parametersForTestOffline() {
        return new Object[] {
                new Object[] {"신림동 서울대학교", true},
                new Object[] {null, false}
        };
    }

}
