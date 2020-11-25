package org.zalando.logbook.jaxws;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class Buffering implements State {

    private final byte[] stream;

    @Override
    public State without() {
        return new Ignoring(this);
    }

    @Override
    public byte[] getBody() {
        return stream;
    }

}
