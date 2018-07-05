package it;

import org.junit.Test;

public class HealthEndpointTest extends EndpointTest {

    @Test
    public void testHealth() {
        testEndpoint("/health", 200, "UP");
    }
}
