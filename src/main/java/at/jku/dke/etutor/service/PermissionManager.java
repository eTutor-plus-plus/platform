package at.jku.dke.etutor.service;

import org.springframework.stereotype.Service;

/**
 * Service for managing permissions.
 *
 * @author fne
 */
@Service
public class PermissionManager {
    private final UserService userService;

    /**
     * Constructor.
     *
     * @param userService the injected user service
     */
    public PermissionManager(UserService userService) {
        this.userService = userService;
    }
}

