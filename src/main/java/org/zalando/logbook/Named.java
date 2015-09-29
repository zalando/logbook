package org.zalando.logbook;

interface Named {

    default String getName() {
        // TODO this will use the system hash (hopefully), is this correct?
        return String.format("%s.FILTERED", this);
    }

}
