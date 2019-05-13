package me.tj3828.restapiwithspring.events;

import me.tj3828.restapiwithspring.accounts.Account;
import me.tj3828.restapiwithspring.accounts.AccountRepository;
import me.tj3828.restapiwithspring.accounts.AccountRole;
import me.tj3828.restapiwithspring.accounts.AccountService;
import me.tj3828.restapiwithspring.common.AppProperties;
import me.tj3828.restapiwithspring.common.BaseControllerTest;
import me.tj3828.restapiwithspring.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author tj3828
 */

public class EventControllerTest extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @Before
    public void setUp() {
        eventRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("Rest API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019,5,10,14,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2019,5,11,14,21))
                .beginEventDateTime(LocalDateTime.of(2019,5,15,14,21))
                .endEventDateTime(LocalDateTime.of(2019,5,16,14,21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("신림동 서울대학교")
                .build();

        mockMvc.perform(post("/api/events/")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken(true))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(event))
                    )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("free").value(Matchers.not(true)))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("Accept Header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type Header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrolmment")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location Header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type Header")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                                fieldWithPath("free").description("it tells if this event is free or not"),
                                fieldWithPath("offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status")
                        )
                        )
                );
    }

    private String getAccessToken(boolean needToCreateAccount) throws Exception {
        // Given
        if(needToCreateAccount) {
            createAccount();
        }

        ResultActions perform = mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password")
        );
        String contentAsString = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(contentAsString).get("access_token").toString();
    }

    private Account createAccount() {
        Account tak = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Stream.of(AccountRole.ADMIN, AccountRole.USER).collect(Collectors.toSet()))
                .build();
        return this.accountService.saveAccount(tak);
    }

    @Test
    @TestDescription("입력 받을 수 없는 값이 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_BadRequest () throws Exception {
        Event event = buildEvent(100);

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("필요한 입력값이 비어있는 경우에 에러를 발생하는 테스트")
    public void createEvent_BadRequest_With_Empty () throws Exception {
        EventDto event = EventDto.builder().build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("비지니스 로직에 어긋난 값이 입력된 경우에 에러를 발생하는 테스트")
    public void createEvent_BadRequest_With_Wrong () throws Exception {
        EventDto event = EventDto.builder().name("Spring")
                .description("Rest API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019,5,10,14,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2019,5,11,14,21))
                .beginEventDateTime(LocalDateTime.of(2019,5,15,14,21))
                .endEventDateTime(LocalDateTime.of(2019,5,14,14,21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("신림동 서울대학교")
                .build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists());
    }

    @Test
    @TestDescription("목록과 Page 정보를 함께 받기")
    public void queryEvent() throws Exception {
        IntStream.range(0,30).forEach(this::generateEvent);

        mockMvc.perform(get("/api/events")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "name,DESC")
        )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("page").exists())
                    .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                    .andExpect(jsonPath("_links.profile").exists())
                    .andExpect(jsonPath("_links.self").exists())
                    .andDo(document("query-events"));
    }

    @Test
    @TestDescription("Event 1개 조회")
    public void getEvent() throws Exception {
        Account account = this.createAccount();
        Event event = generateEvent(100, account);

        mockMvc.perform(get("/api/events/{id}", event.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("_links.self").exists())
                    .andExpect(jsonPath("_links.profile").exists())
                    .andDo(document("get-an-event"));
    }

    @Test
    @TestDescription("없는 Event 조회")
    public void getEvent404() throws Exception {
        mockMvc.perform(get("/api/events/456465"))
                .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("수정시에 수정된 리소스 반환")
    public void updateEvent() throws Exception {
        Account account = this.createAccount();
        Event event = generateEvent(100, account);

        EventDto eventDto = modelMapper.map(event, EventDto.class);
        String eventName = "Updated Event";
        eventDto.setName(eventName);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken(false))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-an-event"));
    }

    @Test
    @TestDescription("수정하려는 이벤트가 없는 경우 404")
    public void updateEvent404() throws Exception {
        Event event = generateEvent(100);
        EventDto eventDto = modelMapper.map(event, EventDto.class);

        mockMvc.perform(put("/api/events/123123")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken(true))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(objectMapper.writeValueAsString(eventDto))
        )
                .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("수정시에 데이터 바인딩에 문제 있을 경우 400")
    public void updateEvent400_Empty() throws Exception {
        Event event = generateEvent(123123);

        EventDto eventDto = new EventDto();

        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto))
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("수정시에 로직에 어긋나는 경우 400")
    public void updateEvent400_Wrong() throws Exception {
        Event event = generateEvent(123123);

        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken(true))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(eventDto))
        )
                .andExpect(status().isBadRequest());
    }

    private Event generateEvent(int index, Account account) {
        Event event = buildEvent(index);
        event.setManager(account);
        this.eventRepository.save(event);

        return event;
    }


    private Event generateEvent(int index) {
        Event event = buildEvent(index);

        this.eventRepository.save(event);

        return event;
    }

    private Event buildEvent(int index) {
        return Event.builder()
                .name("event " + index)
                .description("Rest API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019, 5, 10, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2019, 5, 11, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2019, 5, 15, 14, 21))
                .endEventDateTime(LocalDateTime.of(2019, 5, 16, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("신림동 서울대학교")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();
    }
}