package vn.edu.crs.smartcommunity.visitor.api;

public interface VisitorReporting {

    VisitorMetrics getMetrics();

    long countActiveByResidentId(Long residentId);
}
