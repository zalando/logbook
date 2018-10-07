package org.zalando.logbook;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.function.Predicate;

import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.xpath.XPathConstants.NODESET;
import static org.zalando.fauxpas.FauxPas.throwingSupplier;

@Slf4j
class XmlCompactingBodyFilter implements BodyFilter {

    private final Predicate<String> contentTypes = MediaTypeQuery.compile("*/xml", "*/*+xml");
    private final DocumentBuilderFactory documentBuilderFactory = documentBuilderFactory();
    private final Transformer transformer = transformer();

    @Override
    public String filter(@Nullable final String contentType, final String body) {
        return contentTypes.test(contentType) ? compact(body) : body;
    }

    private String compact(final String body) {
        if (body.trim().isEmpty()) return body;
        try {
            final StringWriter output = new StringWriter();
            final Document document = parseDocument(body);
            transformer.transform(new DOMSource(document), new StreamResult(output));
            return output.toString();
        } catch (Exception e) {
            log.trace("Unable to compact body, is it a XML?. Keep it as-is: `{}`", e.getMessage());
            return body;
        }
    }

    private Document parseDocument(final String body) throws Exception {
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(new ByteArrayInputStream(body.getBytes()));
        removeEmptyTextNodes(document);
        return document;
    }

    private void removeEmptyTextNodes(final Document document) throws Exception {
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xpath = xPathFactory.newXPath();
        final NodeList empty = (NodeList) xpath.evaluate("//text()[normalize-space(.) = '']", document, NODESET);
        for (int i = 0; i < empty.getLength(); i++) {
            final Node node = empty.item(i);
            node.getParentNode().removeChild(node);
        }
    }

    private Transformer transformer() {
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transformer = throwingSupplier(factory::newTransformer).get();
        transformer.setOutputProperty(INDENT, "no");
        transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
        return transformer;
    }

    /**
     * @return configured {@link DocumentBuilderFactory} against XML External Entity (XXE).
     */
    private DocumentBuilderFactory documentBuilderFactory() {
        return throwingSupplier(() -> {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            return factory;
        }).get();
    }
}
