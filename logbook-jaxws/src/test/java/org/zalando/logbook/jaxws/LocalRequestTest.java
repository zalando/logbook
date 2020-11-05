package org.zalando.logbook.jaxws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Origin;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class LocalRequestTest {

	@Mock
	private SOAPMessageContext context;
	
	
	@Test
	void given_soap_11_message_should_return_information_of_request() throws UnsupportedEncodingException, IOException, SOAPException {
		//-- mocks
		when(context.getMessage()).thenReturn(SOAPMessageFactory.createSoap1_1());
		lenient().when(context.get(MessageContext.WSDL_OPERATION)).thenReturn(new QName("operation"));
		lenient().when(context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY)).thenReturn("http://localhost:8080/soap/operation");
		lenient().when(context.get(SOAPMessage.CHARACTER_SET_ENCODING)).thenReturn("UTF-8");
		lenient().when(context.get(SOAPMessageContext.QUERY_STRING)).thenReturn("id=12345");
		
		//-- underTest
		LocalRequest request = new LocalRequest(context);
		HttpHeaders headers = request.getHeaders();
		String method = request.getMethod();
		String remote = request.getRemote();
		String host = request.getHost();
		int port = request.getPort().get();
		String path = request.getPath();
		String contentType = request.getContentType();
		Charset charset = request.getCharset();
		String scheme = request.getScheme();
		Origin origin = request.getOrigin();
		String query = request.getQuery();
		String protocol = request.getProtocolVersion();

		//-- asserts
		assertThat(host, is("localhost"));
		assertThat(path, is("/soap/operation"));
		assertThat(method, is("operation"));
		assertThat(contentType, is("text/xml; charset=utf-8"));
		assertThat(charset, is(Charset.forName("UTF-8")));
		assertThat(scheme, is("http"));
		assertThat(remote, is("localhost"));
		assertThat(origin, is(Origin.LOCAL));
		assertThat(headers.size(), is(2));
		assertThat(port, is(8080));
		assertThat(query, is("id=12345"));
		assertThat(protocol, is("SOAP 1.1"));

	}
	
	@Test
	void given_soap_12_message_should_return_protocol() throws UnsupportedEncodingException, IOException, SOAPException {
		//-- mocks
		when(context.getMessage()).thenReturn(SOAPMessageFactory.createSoap1_2());
		
		//-- underTest
		LocalRequest request = new LocalRequest(context);
		String protocol = request.getProtocolVersion();
		
		//-- asserts
		assertThat(protocol, is("SOAP 1.2"));
	}
	
	@Test
	void given_soap_message_with_bad_URI_should_return_empty_URI() {
		//-- mocks
		when(context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY)).thenReturn("$|#");
		
		//-- underTest
		LocalRequest request = new LocalRequest(context);
		String method = request.getMethod();
		String host = request.getHost();
		int port = request.getPort().orElse(-1);
		String path = request.getPath();

		//-- asserts
		assertThat(host, is(""));
		assertThat(path, is(""));
		assertThat(method, is(""));
		assertThat(port, is(-1));
	}
}