package at.jku.dke.etutor.factory;

import at.jku.dke.etutor.service.dto.permission.PermissionDTO;

/**
 * Factory class for permissions.
 *
 * @author fne
 */
public abstract class PermissionFactory {
    /**
     * Private constructor because this class is not
     * intended to get instantiated.
     */
    private PermissionFactory() {
        // Not used
    }

    /**
     * Creates a default permission DTO where everything
     * is forbidden.
     *
     * @return the default permission DTO
     */
    public static PermissionDTO getDefaultPermissions() {
        return new PermissionDTO(false);
    }

    /**
     * Returns the admin permissions for an object.
     *
     * @return the default admin permissions
     */
    public static PermissionDTO getAdminPermissions() {
        return new PermissionDTO(true);
    }

    /**
     * Returns the permissions for an object's owner.
     *
     * @return the object's owner default permissions
     */
    public static PermissionDTO getPermissionsForOwner() {
        return new PermissionDTO(true);
    }
}
