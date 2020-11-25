package org.zalando.logbook.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import static javax.jws.soap.SOAPBinding.Style.DOCUMENT;

@WebService
@SOAPBinding(style = DOCUMENT)
public interface BookService {
    @WebMethod
    Book getBook(@WebParam(name = "bookId") Integer id);
}
