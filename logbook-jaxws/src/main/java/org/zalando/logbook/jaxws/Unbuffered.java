package org.zalando.logbook.jaxws;

final class Unbuffered implements State {

    @Override
    public State with() {
        return new Offering();
    }

}
