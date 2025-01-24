package handler.services;

import handler.brokerclients.BrokerClient;
import handler.exceptions.BadRequestException;
import handler.model.CreateSessionTemplateRequestData;
import handler.model.CreateSessionTemplateResponse;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.DescribeSessionTemplatesResponse;
import handler.model.FilterTokenStrict;
import handler.model.OsFamily;
import handler.model.PublishSessionTemplateResponse;
import handler.model.SessionTemplate;
import handler.model.SortToken;
import handler.model.Type;
import handler.persistence.SessionTemplatePublishedToUser;
import handler.persistence.SessionTemplateUserGroupId;
import handler.persistence.SessionTemplateUserId;
import handler.persistence.UserEntity;
import handler.repositories.PagingAndSortingCrudRepository;
import handler.repositories.SessionTemplatePublishedToUserGroupRepository;
import handler.repositories.SessionTemplatePublishedToUserRepository;
import handler.repositories.dto.RepositoryRequest;
import handler.repositories.dto.RepositoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SessionTemplateServiceTest {

    private final SessionTemplateService testSessionTemplateService;

    private final PagingAndSortingCrudRepository<SessionTemplate, String> mockSessionTemplateRepository;
    private final SessionTemplatePublishedToUserRepository mockSessionTemplatePublishedToUserRepository;
    private final SessionTemplatePublishedToUserGroupRepository mockSessionTemplatePublishedToUserGroupRepository;
    private final BrokerClient mockBrokerClient;


    private final static String testString = "test";
    private final static String userString = "test-user";
    private final static String testRequirements = "tag:test = 1";
    private final static String testSortKey = "Name";

    private final static String sessionTemplateId1 = "sessionTemplateId1";
    private final static String sessionTemplateId2 = "sessionTemplateId2";
    private final static String sessionTemplateId3 = "sessionTemplateId3";
    private final static String groupId1 = "group1";
    private final static String groupId2 = "group2";
    private final static String groupId3 = "group3";
    private final static String userId1 = "user1";
    private final static String userId2 = "user2";
    private final static List<String> userIds = List.of(userId1, userId2);
    private final static List<String> groupIds = List.of(groupId1, groupId2);

    private final static List<SessionTemplate> sessionTemplates = Stream.of(sessionTemplateId1, sessionTemplateId2, sessionTemplateId3).map(id -> new SessionTemplate().id(id)).toList();

    private final static Map<String, List<String>> sessionTemplatesPublished = new HashMap<>() {{
        put(sessionTemplateId1, List.of(groupId1, groupId2, userId1));
        put(sessionTemplateId2, List.of(groupId2, userId1, userId2));
        put(sessionTemplateId3, List.of(groupId3));
    }};

    // We have to initialize the mocks manually, otherwise @InjectMocks injects the wrong repositories
    public SessionTemplateServiceTest() {
        mockSessionTemplateRepository = mock(PagingAndSortingCrudRepository.class);
        mockSessionTemplatePublishedToUserRepository = mock(SessionTemplatePublishedToUserRepository.class);
        mockSessionTemplatePublishedToUserGroupRepository = mock(SessionTemplatePublishedToUserGroupRepository.class);
        mockBrokerClient = mock(BrokerClient.class);

        testSessionTemplateService = new SessionTemplateService(mockSessionTemplateRepository, mockSessionTemplatePublishedToUserRepository, mockSessionTemplatePublishedToUserGroupRepository, mockBrokerClient);
    }


    @Test
    public void createSessionTemplateSuccess() {
        CreateSessionTemplateRequestData request = new CreateSessionTemplateRequestData().name(testString);
        request.setOsFamily(OsFamily.LINUX);
        request.setType(Type.VIRTUAL);

        SessionTemplate sessionTemplate = new SessionTemplate().name(testString);
        sessionTemplate.setOsFamily(OsFamily.LINUX.getValue());
        sessionTemplate.setType(Type.VIRTUAL.getValue());

        doReturn(sessionTemplate).when(mockSessionTemplateRepository).save(any());
        CreateSessionTemplateResponse response = testSessionTemplateService.saveSessionTemplate(sessionTemplate, request, false, userString);
        assertEquals(sessionTemplate, response.getSessionTemplate());
        assertNull(response.getError());

        sessionTemplate.setRequirements(testRequirements);
        sessionTemplate.setAutorunFile(testString);
        request.setRequirements(testRequirements);
        response = testSessionTemplateService.saveSessionTemplate(sessionTemplate, request.autorunFile(testString), false, userString);
        assertEquals(sessionTemplate, response.getSessionTemplate());
        assertNull(response.getError());
    }

    @Test
    public void testCreateSessionTemplatesBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> testSessionTemplateService.saveSessionTemplate(null, null, false, userString));
    }

    @Test
    public void getUpdatedNameSessionTemplateSuccess() {
        String sessionTemplateId = UUID.nameUUIDFromBytes(testString.getBytes()).toString();
        SessionTemplate sessionTemplate = new SessionTemplate().id(sessionTemplateId);
        doReturn(Optional.of(sessionTemplate)).when(mockSessionTemplateRepository).findById(sessionTemplateId);
        SessionTemplate response = testSessionTemplateService.getUpdatedNameSessionTemplate(sessionTemplateId, testString);
        assertEquals(sessionTemplate, response);

        response = testSessionTemplateService.getUpdatedNameSessionTemplate(sessionTemplateId, "fail");
        assertEquals(sessionTemplate.id(null), response);
    }

    @Test
    public void testGetUpdatedNameSessionTemplateMissingResource() {
        doReturn(Optional.empty()).when(mockSessionTemplateRepository).findById(testString);
        assertThrowsExactly(MissingResourceException.class,
                () -> testSessionTemplateService.getUpdatedNameSessionTemplate(testString, testString));
    }

    @Test
    public void describeSessionTemplatesSuccess() {
        DescribeSessionTemplatesResponse response;

        ReflectionTestUtils.setField(testSessionTemplateService, "defaultMaxResults", 10);

        DescribeSessionTemplatesRequestData request = new DescribeSessionTemplatesRequestData();
        List<SessionTemplate> sessionTemplates = new ArrayList<>();
        SessionTemplate sessionTemplate = new SessionTemplate().id(testString);
        sessionTemplates.add(sessionTemplate);
        RepositoryResponse<SessionTemplate> mockRepositoryReponse = mock(RepositoryResponse.class);
        doReturn(sessionTemplates).when(mockRepositoryReponse).getItems();

        doReturn(mockRepositoryReponse).when(mockSessionTemplateRepository).findAll(any(RepositoryRequest.class));
        response = testSessionTemplateService.describeSessionTemplates(request);
        assertEquals(sessionTemplates, response.getSessionTemplates());
        assertNull(response.getError());
        assertEquals(response.getSessionTemplates().size(), 1);

        request.setSortToken(new SortToken().operator(SortToken.OperatorEnum.ASC).key(testSortKey));
        request.setMaxResults(5);
        response = testSessionTemplateService.describeSessionTemplates(request);
        assertEquals(sessionTemplates, response.getSessionTemplates());
        assertNull(response.getError());
        assertEquals(response.getSessionTemplates().size(), 1);
    }

    @Test
    public void testDescribeSessionTemplatesBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> testSessionTemplateService.describeSessionTemplates(null));

        assertThrowsExactly(BadRequestException.class,
                () -> testSessionTemplateService.describeSessionTemplates(new DescribeSessionTemplatesRequestData().sortToken(new SortToken())));
    }

    @Test
    public void testDeleteSessionTemplateSuccess() {
        testSessionTemplateService.deleteSessionTemplate(testString);
        verify(mockSessionTemplateRepository, times(1)).deleteById(testString);
    }

    @Test
    public void testDeleteSessionTemplateBadRequest() {
        assertThrowsExactly(BadRequestException.class,
                () -> testSessionTemplateService.deleteSessionTemplate(null));
    }

    @Test
    public void testUnpublishSessionTemplateSuccess() {


        doAnswer(invocation -> {
            List<SessionTemplateUserId> sessionTemplateUserIds = invocation.getArgument(0);

            assertEquals(userIds.size(), sessionTemplateUserIds.size());

            assertEquals(sessionTemplateUserIds.get(0).getSessionTemplateId(), sessionTemplateId1);
            assertEquals(sessionTemplateUserIds.get(0).getUserId(), userIds.get(0));

            assertEquals(sessionTemplateUserIds.get(1).getSessionTemplateId(), sessionTemplateId1);
            assertEquals(sessionTemplateUserIds.get(1).getUserId(), userIds.get(1));

            return null;
        }).when(mockSessionTemplatePublishedToUserRepository).deleteAllById(any());

        doAnswer(invocation -> {
            List<SessionTemplateUserGroupId> sessionTemplateGroupIds = invocation.getArgument(0);

            assertEquals(groupIds.size(), sessionTemplateGroupIds.size());

            assertEquals(sessionTemplateGroupIds.get(0).getSessionTemplateId(), sessionTemplateId1);
            assertEquals(sessionTemplateGroupIds.get(0).getUserGroupId(), groupIds.get(0));

            assertEquals(sessionTemplateGroupIds.get(1).getSessionTemplateId(), sessionTemplateId1);
            assertEquals(sessionTemplateGroupIds.get(1).getUserGroupId(), groupIds.get(1));

            return null;
        }).when(mockSessionTemplatePublishedToUserGroupRepository).deleteAllById(any());

        testSessionTemplateService.unpublishSessionTemplate(sessionTemplateId1, userIds, groupIds);
    }

    @Test
    public void testPublishSessionTemplateSuccess() {
        SessionTemplateUserId sessionTemplateUserId = new SessionTemplateUserId();
        sessionTemplateUserId.setUserId(userString);
        SessionTemplatePublishedToUser sessionTemplatePublishedToUser = new SessionTemplatePublishedToUser();
        sessionTemplatePublishedToUser.setId(sessionTemplateUserId);
        sessionTemplatePublishedToUser.setUser((UserEntity) new UserEntity().userId(userString));
        SessionTemplatePublishedToUser success = new SessionTemplatePublishedToUser();
        SessionTemplateUserId successId = new SessionTemplateUserId();
        successId.setUserId(testString);
        success.setId(successId);
        success.setUser((UserEntity) new UserEntity().userId(testString));

        when(mockSessionTemplatePublishedToUserRepository.findBySessionTemplateId(testString)).thenReturn(List.of(sessionTemplatePublishedToUser));
        when(mockSessionTemplatePublishedToUserRepository.saveAll(any())).thenReturn(List.of(success));
        PublishSessionTemplateResponse response = testSessionTemplateService.publishSessionTemplate(testString, List.of(testString, userString), new ArrayList<>());
        assertEquals(2, response.getSuccessfulUsersList().size());
        assertEquals(testString, response.getSuccessfulUsersList().get(0));
        assertEquals(userString, response.getSuccessfulUsersList().get(1));
        assertNull(response.getUnsuccessfulUsersList());

        assertNull(response.getSuccessfulGroupsList());
        assertNull(response.getUnsuccessfulGroupsList());

        response = testSessionTemplateService.publishSessionTemplate(testString, new ArrayList<>(), new ArrayList<>());
        assertNull(response.getSuccessfulUsersList());
        assertNull(response.getUnsuccessfulUsersList());
        assertNull(response.getSuccessfulGroupsList());
        assertNull(response.getUnsuccessfulGroupsList());
    }

    @Test
    public void testFilterByGroupId() {
        FilterTokenStrict includeFilterToken1 = new FilterTokenStrict().operator(FilterTokenStrict.OperatorEnum.EQUAL).value(groupId1);
        FilterTokenStrict includeFilterToken2 = new FilterTokenStrict().operator(FilterTokenStrict.OperatorEnum.NOT_EQUAL).value(groupId2);
        DescribeSessionTemplatesRequestData request = new DescribeSessionTemplatesRequestData()
                .addGroupsSharedWithItem(includeFilterToken1)
                .addGroupsSharedWithItem(includeFilterToken2);

        doAnswer(invocation -> {
            SessionTemplateUserGroupId id = invocation.getArgument(0);

            if (sessionTemplatesPublished.containsKey(id.getSessionTemplateId())) {
                return sessionTemplatesPublished.get(id.getSessionTemplateId()).contains(id.getUserGroupId());
            }

            return false;
        }).when(mockSessionTemplatePublishedToUserGroupRepository).existsById(any());

        List<SessionTemplate> response = testSessionTemplateService.filterByGroupId(request, sessionTemplates);
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(sessionTemplateId1, response.get(0).getId());
        assertEquals(sessionTemplateId3, response.get(1).getId());
    }

    @Test
    public void testFilterByUserId() {
        FilterTokenStrict includeFilterToken1 = new FilterTokenStrict().operator(FilterTokenStrict.OperatorEnum.EQUAL).value(userId1);
        FilterTokenStrict includeFilterToken2 = new FilterTokenStrict().operator(FilterTokenStrict.OperatorEnum.NOT_EQUAL).value(userId2);
        DescribeSessionTemplatesRequestData request = new DescribeSessionTemplatesRequestData()
                .addUsersSharedWithItem(includeFilterToken1)
                .addUsersSharedWithItem(includeFilterToken2);

        doAnswer(invocation -> {
            SessionTemplateUserId id = invocation.getArgument(0);

            if (sessionTemplatesPublished.containsKey(id.getSessionTemplateId())) {
                return sessionTemplatesPublished.get(id.getSessionTemplateId()).contains(id.getUserId());
            }

            return false;
        }).when(mockSessionTemplatePublishedToUserRepository).existsById(any());

        List<SessionTemplate> response = testSessionTemplateService.filterByUserId(request, sessionTemplates);
        assertNotNull(response);
        assertEquals(3, response.size());
        assertEquals(sessionTemplateId1, response.get(0).getId());
        assertEquals(sessionTemplateId2, response.get(1).getId());
        assertEquals(sessionTemplateId3, response.get(2).getId());
    }

    @Test
    public void testFilterNullTokens() {
        DescribeSessionTemplatesRequestData request = new DescribeSessionTemplatesRequestData();

        List<SessionTemplate> usersResponse = testSessionTemplateService.filterByUserId(request, sessionTemplates);
        assertEquals(usersResponse, sessionTemplates);
        List<SessionTemplate> groupsResponse = testSessionTemplateService.filterByGroupId(request, sessionTemplates);
        assertEquals(groupsResponse, sessionTemplates);
    }
}