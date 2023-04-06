module org.zalando.logbook.autoconfigure.webflux {
    exports org.zalando.logbook.autoconfigure.webflux;

    opens org.zalando.logbook.autoconfigure.webflux to spring.core;

    requires org.apiguardian.api;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.webflux;
    requires spring.web;
    requires org.zalando.logbook.api;
    requires org.zalando.logbook.netty;
    requires org.zalando.logbook.spring.webflux;
    requires reactor.netty;
    requires reactor.netty.http;
    requires reactor.netty.core;
}
