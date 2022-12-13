package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.Authority;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.event.TaskOwnerProvider;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Service for managing permissions.
 *
 * @author fne
 */
@Service
public class PermissionManager {
    private final UserService userService;
    private Map<String, Set<String>> userCache;

    /**
     * Constructor.
     *
     * @param userService the injected user service
     */
    public PermissionManager(UserService userService) {
        this.userService = userService;

        userCache = new HashMap<>();
    }

    /**
     * Returns whether the given user is allowed to edit the given task.
     *
     * @param user              the user's name
     * @param taskId            the task assignment id
     * @param taskOwnerProvider the task owner provider
     * @return {@code true} if the user is allowed to edit the task, otherwise {@code false}
     */
    public boolean isUserAllowedToEditTaskAssignment(String user, String taskId, TaskOwnerProvider taskOwnerProvider) {
        if (!userCache.containsKey(user)) {
            var userWithAuthorities = userService.getUserWithAuthoritiesByLogin(user).get();
            var newAuthoritiesSet = StreamEx.of(userWithAuthorities.getAuthorities())
                .map(Authority::getName)
                .toSet();
            userCache.put(user, newAuthoritiesSet);
        }

        Set<String> userAuthorities = userCache.get(user);

        boolean isAdmin = userAuthorities.contains(AuthoritiesConstants.ADMIN);

        if (!isAdmin) {
            // Test if the current user is the author of the task.
            return user.equals(taskOwnerProvider.getTaskOwner());
        }

        return true;
    }
}

