package org.zalando.logbook.netty;

final class Unbuffered implements State {

    @Override
    public State with() {
        return new Offering();
    }

}
