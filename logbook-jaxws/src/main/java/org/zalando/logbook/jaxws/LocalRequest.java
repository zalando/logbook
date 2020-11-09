package org.zalando.logbook.jaxws;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Origin;

public final class LocalRequest extends AbstractMessage implements HttpRequest {

	private URI uri;

	public LocalRequest(SOAPMessageContext context) {
		super(context);
		uri = createURI(context);
	}

	@Override
	public Origin getOrigin() {
		return Origin.LOCAL;
	}

	@Override
	public String getRemote() {
		return getHost();
	}

	@Override
	public String getMethod() {
		QName op = Optional.ofNullable((QName) context.get(MessageContext.WSDL_OPERATION)).orElse(new QName(""));
		return op.getLocalPart();
	}

	@Override
	public String getScheme() {
		return Optional.ofNullable(uri.getScheme()).orElse("");
	}

	@Override
	public String getHost() {
		return Optional.ofNullable(uri.getHost()).orElse("");

	}

	@Override
	public Optional<Integer> getPort() {
		final int port = uri.getPort();
		return port == -1 ? Optional.empty() : Optional.of(port);
	}

	@Override
	public String getPath() {
		return Optional.ofNullable(uri.getPath()).orElse("");
	}

	@Override
	public String getQuery() {
		return Optional.ofNullable(uri.getQuery()).orElse("");
	}

	@Override
	public HttpRequest withBody() throws IOException {
		state.updateAndGet(State::with);
		return this;
	}

	@Override
	public HttpRequest withoutBody() {
		state.updateAndGet(State::without);
		return this;
	}

	private static URI createURI(SOAPMessageContext context) {
		String address = (String) context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
		if (address != null) {
			String query = (String) context.get(SOAPMessageContext.QUERY_STRING);
			if (query != null) {
				address += "?" + query;
			}
		}
		try {
			return URI.create(address == null ? "" : address);
		} catch (IllegalArgumentException exception) {
			return URI.create("");
		}
	}
}