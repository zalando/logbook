module org.zalando.logbook.logstash {
    exports org.zalando.logbook.logstash;

    requires org.zalando.logbook.api;
    requires org.zalando.logbook.json;
    requires com.fasterxml.jackson.core;
    requires logstash.logback.encoder;
    requires org.apiguardian.api;
    requires org.slf4j;
    requires lombok;
}
