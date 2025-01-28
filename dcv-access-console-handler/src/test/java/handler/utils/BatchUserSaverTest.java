// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.utils;

import handler.persistence.UserEntity;
import handler.repositories.UserGroupUserMembershipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BatchUserSaverTest {

    @Mock
    private CrudRepository<UserEntity, String> mockUserRepository;
    @Mock
    private UserGroupUserMembershipRepository mockUserGroupUserMembershipRepository;

    private final static List<String> userIds = List.of(
            "userId0", "userId1", "userId2", "userId3", "userId4", "userId5", "userId6", "userId7", "userId8", "userId9"
    );

    @Test
    public void testSaveUsersWithOverwriteEmptyDB() {
        BatchUserSaver batchUserSaver = new BatchUserSaver(2, 10, mockUserRepository, mockUserGroupUserMembershipRepository);

        Set<UserEntity> userEntitiesSet = userIds.stream().map(userId -> (UserEntity) (new UserEntity().userId(userId))).collect(Collectors.toSet());

        when(mockUserRepository.findById(any())).thenReturn(Optional.empty());

        for (UserEntity user : userEntitiesSet) {
            batchUserSaver.saveUser(user, true);
        }

        verify(mockUserRepository, times(5)).saveAll(anyCollection());
    }

    @Test
    public void testSaveUsersWithOverwritePopulatedDB() {
        BatchUserSaver batchUserSaver = new BatchUserSaver(2, 10, mockUserRepository, mockUserGroupUserMembershipRepository);

        Set<UserEntity> userEntitiesSet = userIds.stream().map(userId -> (UserEntity) (new UserEntity().userId(userId).isImported(true))).collect(Collectors.toSet());
        when(mockUserRepository.findById(anyString()))
                .thenAnswer(invocation -> {
                    switch ((String) invocation.getArgument(0)) {
                        case "userId5":
                            return Optional.of((UserEntity) (new UserEntity().userId("userId5")));
                        case "userId7":
                            return Optional.of((UserEntity) (new UserEntity().userId("userId7")));
                        default:
                            return Optional.empty();
                    }
                });

        for (UserEntity user : userEntitiesSet) {
            batchUserSaver.saveUser(user, true);
        }
        batchUserSaver.sendBatchAndClear();

        verify(mockUserRepository, times(5)).saveAll(anyCollection());
        assertEquals(10, batchUserSaver.getSuccessfulUsersList().size());
    }

    @Test
    public void testSaveUsersWithNoOverwritePopulatedDB() {
        BatchUserSaver batchUserSaver = new BatchUserSaver(2, 10, mockUserRepository, mockUserGroupUserMembershipRepository);

        List<UserEntity> userEntitiesSet = userIds.stream().map(userId -> (UserEntity) (new UserEntity().userId(userId))).toList();
        when(mockUserRepository.findById(anyString()))
                .thenAnswer(invocation -> switch ((String) invocation.getArgument(0)) {
                    case "userId3" -> Optional.of((UserEntity) (new UserEntity().userId("userId3")));
                    case "userId5" -> Optional.of((UserEntity) (new UserEntity().userId("userId5")));
                    case "userId7" -> Optional.of((UserEntity) (new UserEntity().userId("userId7")));
                    default -> Optional.empty();
                });

        for (UserEntity user : userEntitiesSet) {
            batchUserSaver.saveUser(user, false);
        }
        batchUserSaver.sendBatchAndClear();
        verify(mockUserRepository, times(4)).saveAll(anyCollection());
        assertEquals(7, batchUserSaver.getSuccessfulUsersList().size());
        assertEquals(3, batchUserSaver.getUnsuccessfulUsersList().size());
    }
}
