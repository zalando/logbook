module org.zalando.logbook.spring.webflux {
    exports org.zalando.logbook.spring.webflux;

    requires org.reactivestreams;
    requires spring.core;
    requires spring.web;
    requires reactor.core;
    requires lombok;
    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires jsr305;
    requires spring.webflux;
    requires org.apiguardian.api;
    requires faux.pas;

    opens org.zalando.logbook.spring.webflux to spring.core;
}
