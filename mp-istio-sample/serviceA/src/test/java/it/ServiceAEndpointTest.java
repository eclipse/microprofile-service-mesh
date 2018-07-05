package it;

import org.junit.Test;

public class ServiceAEndpointTest extends EndpointTest {

    @Test
    public void testDeployment() {
      testEndpoint("/mp-istio-sample/serviceA", 200, "serviceAFallback");
    }
}
