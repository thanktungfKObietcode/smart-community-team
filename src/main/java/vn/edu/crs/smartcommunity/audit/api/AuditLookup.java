package vn.edu.crs.smartcommunity.audit.api;

import java.util.List;

public interface AuditLookup {

    List<AuditInfo> latest(int limit);
}
