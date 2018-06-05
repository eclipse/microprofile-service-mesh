package application.rest;

import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;

@RequestScoped
@Path("serviceA")
public class ServiceAEndpoint {

    @Inject
    @ConfigProperty(name = "svcBHost", defaultValue = "localhost")
    private String serviceBHost;

    @Inject
    @ConfigProperty(name = "svcBPort", defaultValue = "9080")
    private String serviceBPort;

    StringBuilder url;
    static int callCount;
    int tries;

    @GET
    @Retry
    @Fallback(fallbackMethod="serviceAFallback")
    @Produces(MediaType.TEXT_PLAIN)
    public String callServiceB() {

      ++callCount;
      ++tries;

      url = new StringBuilder();
      url.append("http://")
          .append(serviceBHost)
          .append(":")
          .append(serviceBPort)
          .append("/mp-istio-sample/serviceB");

      return "Hello from serviceA (" + this + ")\n" + callService(url);
    }

    public String serviceAFallback() {

        return "Hello from serviceAFallback at " + new Date() + " (ServiceA call count: " + callCount + ")\nCompletely failed to call " + url + " after " + tries + " tries";
    }

    private String callService(StringBuilder url) {

        StringBuilder sb = new StringBuilder();

        sb.append("Calling service at: ")
            .append(url)
            .append(" (ServiceA call count: " + callCount + ", tries: " + tries)
            .append(")");

        System.out.println(sb.toString());

        sb.append("\n");

        String result = null;
        
        try {
          result = ClientBuilder.newClient()
                            .target(url.toString())
                            .request(MediaType.TEXT_PLAIN)
                            .get(String.class);
        } catch (Exception e) {
          System.out.println("Caught exception");
          e.printStackTrace();
          throw e;
        }

        return sb.append(result).toString();
    }
}
