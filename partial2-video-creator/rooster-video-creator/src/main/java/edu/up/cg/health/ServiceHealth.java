package edu.up.cg.health;

public final class ServiceHealth {
    private final String serviceName;
    private final boolean healthy;
    private final String details;

    private ServiceHealth(String serviceName, boolean healthy, String details) {
        this.serviceName = serviceName;
        this.healthy = healthy;
        this.details = details;
    }

    public static ServiceHealth healthy(String serviceName, String details) {
        return new ServiceHealth(serviceName, true, details);
    }

    public static ServiceHealth unhealthy(String serviceName, String details) {
        return new ServiceHealth(serviceName, false, details);
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public String getDetails() {
        return details;
    }
}
