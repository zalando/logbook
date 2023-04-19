package org.zalando.logbook.jaxrs.testing.support;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.glassfish.jersey.media.multipart.FormDataParam;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("testws")
public class TestWebService {

    @GET
    @Path("testGet/{param1}/textPlain")
    @Produces(TEXT_PLAIN)
    public String testGet(
            @PathParam("param1") final String param1,
            @QueryParam("param2") final String param2) {
        return "param1=" + param1 + " param2=" + param2;
    }

    @POST
    @Path("testPostForm")
    @Produces(TEXT_PLAIN)
    public String testFormPost(
            @FormDataParam("testFileFormField") final String testFileFormField,
            @FormDataParam("name") final String name,
            @FormDataParam("age") final int age) {
        return "name was " + name + " age was " + age + " file was " + testFileFormField;
    }

    @PUT
    @Path("testPutJson")
    @Produces(APPLICATION_JSON)
    public void getJsonPut(final TestModel testModel) {
        // we need to test a response without a body
    }

    @GET
    @Path("testGetJson")
    @Produces(APPLICATION_JSON)
    public TestModel getJson() {
        return new TestModel().setProperty1("1").setProperty2("2");
    }

}
