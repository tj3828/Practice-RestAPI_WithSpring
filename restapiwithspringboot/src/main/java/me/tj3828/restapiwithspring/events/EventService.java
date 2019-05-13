package me.tj3828.restapiwithspring.events;

import org.springframework.stereotype.Service;

/**
 * @author tj3828
 */

@Service
public class EventService {

    public void update(Event event) {
        if(event.getMaxPrice() == 0 && event.getBasePrice() == 0) {
            event.setFree(true);
        } else {
            event.setFree(false);
        }

        if(event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            event.setOffline(false);
        } else {
            event.setOffline(true);
        }
    }

}
