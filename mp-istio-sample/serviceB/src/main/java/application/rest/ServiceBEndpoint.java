package application.rest;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Retry;

@Path("serviceB")
@RequestScoped
public class ServiceBEndpoint {

  @Inject
  @ConfigProperty(name = "failFrequency", defaultValue = "0")
  private int failFrequency;

  private static int callCount;

  @GET
  @Retry
  public String hello() throws Exception {
    String hostname;

    ++callCount;

    if (failFrequency > 0 && callCount % failFrequency == 0) {
      throw new Exception("ServiceB deliberately caused to fail. Call count: " + callCount + ", failFrequency: " + failFrequency);
    }

    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch(java.net.UnknownHostException e) {
      hostname = e.getMessage();
    }

    return "Hello from serviceB (" + this + ") at " + new Date() + " on " + hostname + " (ServiceB call count: " + callCount + ", failFrequency: " + failFrequency + ")";
  }
}
