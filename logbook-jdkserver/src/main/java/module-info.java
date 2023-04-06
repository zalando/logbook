module org.zalando.logbook.jdkserver {
    exports org.zalando.logbook.jdkserver;

    requires jdk.httpserver;
    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires lombok;
    requires faux.pas;
    requires org.apiguardian.api;
}
