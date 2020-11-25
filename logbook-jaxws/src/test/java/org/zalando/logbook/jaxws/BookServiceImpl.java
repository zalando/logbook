package org.zalando.logbook.jaxws;

import javax.jws.WebService;

@WebService(endpointInterface = "org.zalando.logbook.jaxws.BookService")
public class BookServiceImpl implements BookService {

    @Override
    public Book getBook(final Integer id) {
        if (id < 0) {
            throw new IllegalArgumentException("Invalid book identifier");
        }

        final Book book = new Book();
        book.setId(id);
        book.setName("Logbook");
        return book;
    }

}