module org.zalando.logbook.okhttp {
    exports org.zalando.logbook.okhttp;

    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires okhttp3;
    requires okio;
    requires org.apiguardian.api;
    requires jsr305;
    requires lombok;
    requires faux.pas;
}
