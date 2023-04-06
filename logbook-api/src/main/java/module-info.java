import org.zalando.logbook.api.internal.ExceptionThrowingLogbookFactory;

open module org.zalando.logbook.api {
    exports org.zalando.logbook.api;

    uses org.zalando.logbook.api.LogbookFactory;
    provides org.zalando.logbook.api.LogbookFactory
            with ExceptionThrowingLogbookFactory;

    requires lombok;
    requires org.apiguardian.api;
    requires jsr305;
    requires Paguro;
    requires gag;
}
