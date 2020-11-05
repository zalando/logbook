package org.zalando.logbook.jaxws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.HttpMessage;

public abstract class AbstractMessage implements HttpMessage {

	private final static String HEADER_CONTENT_TYPE = "Content-type";
	private final static String DEFAULT_CHARSET = "UTF-8";
	
	protected final AtomicReference<State> state = new AtomicReference<>(new Unbuffered());
	protected SOAPMessageContext context;

	protected AbstractMessage(SOAPMessageContext context) {
		this.context = context;
	}

	@Override
	public String getProtocolVersion() {
		String contentType = getContentType();
		if (contentType.startsWith("application/soap+xml")) {
			return "SOAP 1.2";
		}

		return "SOAP 1.1";
	}

	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders httpHeaders = HttpHeaders.empty();
		MimeHeaders headers = context.getMessage().getMimeHeaders();
		Iterator<MimeHeader> all = headers.getAllHeaders();
		while (all.hasNext()) {
			MimeHeader lObject = (MimeHeader) all.next();
			httpHeaders = httpHeaders.update(lObject.getName(), Collections.singletonList(lObject.getValue()));
		}
		return httpHeaders;
	}

	@Override
	public String getContentType() {
		String[] contentType = context.getMessage().getMimeHeaders().getHeader(HEADER_CONTENT_TYPE);
		return Optional.ofNullable(contentType[0]).orElse("");
	}

	@Override
	public Charset getCharset() {
		String encoding = Optional.ofNullable((String) context.get(SOAPMessage.CHARACTER_SET_ENCODING)).orElse(DEFAULT_CHARSET);
		return Charset.forName(encoding);
	}

	@Override
	public byte[] getBody() throws IOException {
		State st = state.get().buffer(context.getMessage());
		UnaryOperator<State> twoDigits = (v) -> st;
		return state.updateAndGet(twoDigits).getBody();
	}

	/**
	 * Same strategy used by logbook
	 */
	protected interface State {

		default State with() {
			return this;
		}

		default State without() {
			return this;
		}

		default State buffer(final SOAPMessage context) throws IOException {
			return this;
		}

		default byte[] getBody() {
			return new byte[0];
		}
	}

	private static final class Unbuffered implements State {
		@Override
		public State with() {
			return new Offering();
		}
	}

	private static final class Offering implements State {
		@Override
		public State without() {
			return new Unbuffered();
		}

		@Override
		public State buffer(final SOAPMessage message) throws IOException {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				message.writeTo(stream);
			} catch (SOAPException e) {
				throw new IOException(e);
			} 
			
			return new Buffering(stream.toByteArray());
		}
	}

	private static final class Buffering implements State {

		private final byte[] stream;

		public Buffering(byte[] stream) {
			this.stream = stream;
		}

		@Override
		public State without() {
			return new Ignoring(this);
		}

		@Override
		public byte[] getBody() {
			return stream;
		}
	}

	private static final class Ignoring implements State {

		private final Buffering buffering;

		public Ignoring(Buffering buffering) {
			this.buffering = buffering;
		}

		@Override
		public State with() {
			return buffering;
		}
	}
}