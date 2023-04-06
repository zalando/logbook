open module org.zalando.logbook.core {
    exports org.zalando.logbook.core;

    provides org.zalando.logbook.api.LogbookFactory
            with org.zalando.logbook.core.DefaultLogbookFactory;

    uses org.zalando.logbook.api.LogbookFactory;
    uses org.zalando.logbook.api.RequestFilter;
    uses org.zalando.logbook.api.ResponseFilter;
    uses org.zalando.logbook.api.QueryFilter;
    uses org.zalando.logbook.api.HeaderFilter;
    uses org.zalando.logbook.api.BodyFilter;

    requires org.zalando.logbook.api;
    requires faux.pas;
    requires java.xml;
    requires org.zalando.logbook.common;
    requires jsr305;
    requires org.slf4j;
    requires org.apiguardian.api;
    requires lombok;
}
