open module org.zalando.logbook.servlet {
    exports org.zalando.logbook.servlet;

    requires lombok;
    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires jsr305;
    requires org.apiguardian.api;
    requires jakarta.servlet;
    requires org.zalando.logbook.common;
    requires faux.pas;
}
