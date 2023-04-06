module org.zalando.logbook.okhttp.two {
    exports org.zalando.logbook.okhttp2;

    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires okio;
    requires org.apiguardian.api;
    requires jsr305;
    requires lombok;
    requires faux.pas;
    requires okhttp;
}
