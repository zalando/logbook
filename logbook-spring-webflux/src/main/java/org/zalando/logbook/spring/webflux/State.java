package org.zalando.logbook.spring.webflux;

import lombok.RequiredArgsConstructor;

interface State {

    default State with() {
        return this;
    }

    default State without() {
        return this;
    }

    default State buffer(final byte[] message) {
        return this;
    }

    default byte[] getBody() {
        return new byte[0];
    }


    class Buffering implements State {

        private byte[] body;

        @Override
        public State without() {
            return new Ignoring(this);
        }

        @Override
        public State buffer(byte[] message) {
            body = message;
            return this;
        }

        @Override
        public byte[] getBody() {
            return body;
        }
    }

    class Unbuffered implements State {
        @Override
        public State with() {
            return new Offering();
        }
    }

    class Offering implements State {
        @Override
        public State without() {
            return new Unbuffered();
        }

        @Override
        public State buffer(byte[] message) {
            return new Buffering().buffer(message);
        }
    }

    @RequiredArgsConstructor
    class Ignoring implements State {

        private final Buffering buffering;

        @Override
        public State with() {
            return buffering;
        }

        @Override
        public State buffer(byte[] message) {
            return buffering.buffer(message);
        }
    }
}