package vn.edu.crs.smartcommunity.identity.api;

import java.util.List;

/**
 * Public identity contract for workflows that need to provision or manage
 * accounts without accessing Identity persistence internals.
 */
public interface IdentityAccountProvisioning {

    IdentityUserInfo createStaffAccount(StaffAccountCommand command);

    IdentityUserInfo createResidentAccount(ResidentAccountCommand command);

    List<IdentityUserInfo> listStaffAccounts();

    IdentityUserInfo updateStaffAccountStatus(Long userId, boolean active);
}
