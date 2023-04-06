open module org.zalando.logbook.json {
    exports org.zalando.logbook.json;

    uses org.zalando.logbook.api.BodyFilter;

    provides org.zalando.logbook.api.BodyFilter
            with org.zalando.logbook.json.CompactingJsonBodyFilter, org.zalando.logbook.json.AccessTokenBodyFilter;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires lombok;
    requires org.apiguardian.api;
    requires org.zalando.logbook.common;
    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires jsr305;
    requires org.slf4j;
    requires json.path;
}
