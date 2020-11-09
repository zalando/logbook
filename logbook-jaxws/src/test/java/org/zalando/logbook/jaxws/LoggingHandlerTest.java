package org.zalando.logbook.jaxws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.TestStrategy;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
@TestMethodOrder(OrderAnnotation.class)
public class LoggingHandlerTest {

	private final static HttpLogWriter WRITER = Mockito.mock(HttpLogWriter.class);
	private final static Logbook LOGBOOK = Logbook.builder().strategy(new TestStrategy()).sink(new DefaultSink(new DefaultHttpLogFormatter(), WRITER)).build();
	private static LoggingHandler underTest = new LoggingHandler(LOGBOOK);
	
	@Mock
	private SOAPMessageContext context;

	@Test
	@Order(1)
	void hanlder_with_soap_11_request() throws IOException, SOAPException {

		// -- mocks
		when(WRITER.isActive()).thenReturn(true);
		when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
		lenient().when(context.getMessage()).thenReturn(SOAPMessageFactory.createSoap1_1());
		lenient().when(context.get(MessageContext.WSDL_OPERATION)).thenReturn(new QName("operation"));
		lenient().when(context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY)).thenReturn("http://localhost:8080/soap/operation");
		lenient().when(context.get(SOAPMessage.CHARACTER_SET_ENCODING)).thenReturn("UTF-8");
		lenient().when(context.get(SOAPMessageContext.QUERY_STRING)).thenReturn("id=12345");

		// -- underTest
		boolean result = underTest.handleMessage(context);

		// -- assertions
		assertThat(result, is(true));
		verify(context).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		verify(context).get(MessageContext.WSDL_OPERATION);
		verify(context).get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
		verify(context, times(3)).get(SOAPMessage.CHARACTER_SET_ENCODING);
		verify(context).get(SOAPMessageContext.QUERY_STRING);
		verify(context, times(9)).getMessage();
	}

	@Test
	@Order(2)
	void hanlder_with_soap_11_response() throws IOException, SOAPException {

		// -- mocks
		when(WRITER.isActive()).thenReturn(true);
		when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
		lenient().when(context.getMessage()).thenReturn(SOAPMessageFactory.createSoap1_1());
		lenient().when(context.get(MessageContext.HTTP_RESPONSE_CODE)).thenReturn(200);

		// -- underTest
		boolean result = underTest.handleMessage(context);

		// -- assertions
		assertThat(result, is(true));
		verify(context).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		verify(context, times(9)).getMessage();
		verify(context, Mockito.times(2)).get(MessageContext.HTTP_RESPONSE_CODE);
	}

	@Test
	void handler_with_other_methods() throws NoSuchFieldException, SecurityException, Exception {
		// -- underTest
		boolean fault = underTest.handleFault(context);
		Set<QName> headers = underTest.getHeaders();
		underTest.close(context);
		
		//-- assertions
		assertThat(fault, is(false));
		assertThat(headers, nullValue());
	}

	@Test
	void hanlder_with_soap_request_throw_exception() throws IOException {

		// -- mocks
		Logbook mockLogbook = mock(Logbook.class);
		when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
		when(mockLogbook.process(any())).thenThrow(new IOException("ERROR"));

		// -- underTest
		LoggingHandler underTest = new LoggingHandler(mockLogbook);
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			underTest.handleMessage(context);
		});

		// -- assertions
		verify(context).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		verify(mockLogbook).process(any());
		assertThat(exception.getCause(), isA(IOException.class));
	}

	@Test
	void hanlder_with_soap_response_throw_exception() throws NoSuchFieldException, SecurityException, Exception {
		
		// -- mocks
		ResponseProcessingStage responseProcessingMock = mock(ResponseProcessingStage.class);
		@SuppressWarnings("unchecked")
		ThreadLocal<ResponseProcessingStage> stateMock = mock(ThreadLocal.class);
		setFinalStatic(LoggingHandler.class.getDeclaredField("STAGE_CONTEXT"), stateMock);
		when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
		when(stateMock.get()).thenReturn(responseProcessingMock);
		when(responseProcessingMock.process(Mockito.any())).thenThrow(new IOException("ERROR"));
		
		// -- underTest
		LoggingHandler underTest = new LoggingHandler(null);
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			underTest.handleMessage(context);
		});

		// -- assertions
		verify(context).get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		verify(stateMock).get();
		assertThat(exception.getCause(), isA(IOException.class));
	}
	
	private static void setFinalStatic(Field field, Object newValue) throws Exception {
		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(null, newValue);
	}
}
