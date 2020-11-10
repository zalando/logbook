package org.zalando.logbook.jaxws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public final class SOAPMessageFactory {

    private SOAPMessageFactory() {
    }

    private static String message() {
        return "<soap:Envelope><soap:Header></soap:Header><soap:Body><ns3:app><name>APP Name</name></ns3:app></soap:Body></soap:Envelope>";
    }

    public static SOAPMessage createSoap1_1() throws UnsupportedEncodingException, IOException, SOAPException {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Accept", "text/xml");
        headers.addHeader("Content-Type", "text/xml; charset=utf-8");
        return MessageFactory.newInstance().createMessage(headers,
                new ByteArrayInputStream(message().getBytes("UTF-8")));
    }

    public static SOAPMessage createSoap1_2() throws UnsupportedEncodingException, IOException, SOAPException {
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Accept", "application/soap+xml");
        headers.addHeader("Content-Type", "application/soap+xml; charset=utf-8");
        return MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(headers,
                new ByteArrayInputStream(message().getBytes("UTF-8")));
    }
}
