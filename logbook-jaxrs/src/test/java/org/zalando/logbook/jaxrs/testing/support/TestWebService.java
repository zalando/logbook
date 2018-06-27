package org.zalando.logbook.jaxrs.testing.support;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.glassfish.jersey.media.multipart.FormDataParam;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("testws")
public class TestWebService {

  @GET
  @Path("testGet/{param1}/textPlain")
  @Produces(TEXT_PLAIN)
  public String testGet(
      @PathParam("param1") String param1,
      @QueryParam("param2") String param2
  ) {
    return "param1=" + param1 + " param2=" + param2;
  }

  @POST
  @Path("testPostForm")
  @Produces(TEXT_PLAIN)
  public String testFormPost(
      @FormDataParam("testFileFormField") String testFileFormField,
      @FormDataParam("name") String name,
      @FormDataParam("age") int age
  ) {
    return "name was " + name + " age was " + age + " file was " + testFileFormField;
  }

  @PUT
  @Path("testPutJson")
  @Produces(APPLICATION_JSON)
  public TestModel getJsonPut(
      TestModel testModel
  ) {
    return testModel;
  }
}
