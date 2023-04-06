module org.zalando.logbook.httpclient {
    exports org.zalando.logbook.httpclient;

    requires lombok;
    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires org.apache.httpcomponents.httpcore;
    requires faux.pas;
    requires org.apache.httpcomponents.httpasyncclient;
    requires org.apache.httpcomponents.httpcore.nio;
    requires org.apache.httpcomponents.httpclient;
    requires jsr305;
    requires org.apiguardian.api;
}
