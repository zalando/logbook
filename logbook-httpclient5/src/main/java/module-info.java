module org.zalando.logbook.httpclient.five {
    exports org.zalando.logbook.httpclient5;

    requires lombok;
    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires faux.pas;
    requires jsr305;
    requires org.apiguardian.api;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.httpcomponents.client5.httpclient5;
}
