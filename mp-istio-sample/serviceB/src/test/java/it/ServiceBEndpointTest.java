package it;

import org.junit.Test;

public class ServiceBEndpointTest extends EndpointTest {

    @Test
    public void testDeployment() {
        testEndpoint("/mp-istio-sample/serviceB", 200, "Hello from serviceB");
    }
}
