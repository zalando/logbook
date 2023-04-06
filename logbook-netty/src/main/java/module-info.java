module org.zalando.logbook.netty {
    exports org.zalando.logbook.netty;

    requires org.zalando.logbook.api;
    requires org.zalando.logbook.core;
    requires jsr305;
    requires io.netty.buffer;
    requires io.netty.codec.http;
    requires io.netty.codec;
    requires lombok;
    requires io.netty.transport;
    requires org.apiguardian.api;
    requires faux.pas;
    requires io.netty.handler;
}
