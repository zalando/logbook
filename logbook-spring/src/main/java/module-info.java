module org.zalando.logbook.spring {
    exports org.zalando.logbook.spring;

    requires lombok;
    requires spring.web;
    requires org.apiguardian.api;
    requires org.zalando.logbook.api;
    requires spring.core;
    requires jsr305;
    requires faux.pas;
}
