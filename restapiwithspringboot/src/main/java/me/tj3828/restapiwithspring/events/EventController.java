package me.tj3828.restapiwithspring.events;

import me.tj3828.restapiwithspring.accounts.Account;
import me.tj3828.restapiwithspring.accounts.CurrentUser;
import me.tj3828.restapiwithspring.common.ErrorResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author tj3828
 */

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventService eventService;

    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator, EventService eventService) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {
        if(errors.hasErrors()) {
            return badRequestError(errors);
        }

         eventValidator.validate(eventDto, errors);

        if(errors.hasErrors()) {
            return badRequestError(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        eventService.update(event);
        event.setManager(currentUser);
        Event newEvent = this.eventRepository.save(event);
        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdURl = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));
        return ResponseEntity.created(createdURl).body(eventResource);
    }

    @GetMapping
    public ResponseEntity queryEvent(Pageable pageable,
                                     PagedResourcesAssembler<Event> assembler,
                                     @CurrentUser Account account) {
        Page<Event> all = eventRepository.findAll(pageable);
        PagedResources<Resource<Event>> pagedResources = assembler.toResource(all, EventResource::new);
        pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
        pagedResources.add(linkTo(EventController.class).withRel("events"));
        if(account != null) {
            pagedResources.add(linkTo(EventController.class).withRel("create-event"));
        }
        return ResponseEntity.ok().body(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id,
                                   @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if(!optionalEvent.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
        if(event.getManager().equals(currentUser)) {
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update"));
        }
        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id, @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if(!optionalEvent.isPresent()) {
            return ResponseEntity.notFound().build();
        }


        if(errors.hasErrors()) {
            return badRequestError(errors);
        }

        eventValidator.validate(eventDto, errors);

        if(errors.hasErrors()) {
            return badRequestError(errors);
        }

        Event existingEvent = optionalEvent.get();
        if(!existingEvent.getManager().equals(currentUser)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        this.modelMapper.map(eventDto, existingEvent);
        Event saveEvent = this.eventRepository.save(existingEvent);
        EventResource eventResource = new EventResource(saveEvent);
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));
        return ResponseEntity.ok(eventResource);
    }

    private ResponseEntity<ErrorResource> badRequestError(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorResource(errors));
    }

}
