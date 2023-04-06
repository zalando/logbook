module org.zalando.logbook.openfeign {
    exports org.zalando.logbook.openfeign;

    requires feign.core;
    requires lombok;
    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires jsr305;
    requires org.apiguardian.api;
    requires jdk.httpserver;
}
