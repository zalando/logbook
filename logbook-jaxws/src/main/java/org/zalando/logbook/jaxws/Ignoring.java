package org.zalando.logbook.jaxws;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class Ignoring implements State {

    private final Buffering buffering;

    @Override
    public State with() {
        return buffering;
    }

}
