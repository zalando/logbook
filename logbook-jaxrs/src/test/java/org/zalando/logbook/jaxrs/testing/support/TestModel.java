package org.zalando.logbook.jaxrs.testing.support;

public class TestModel {

    private String property1;
    private String property2;

    public String getProperty1() {
        return property1;
    }

    public TestModel setProperty1(String property1) {
        this.property1 = property1;
        return this;
    }

    public String getProperty2() {
        return property2;
    }

    public TestModel setProperty2(String property2) {
        this.property2 = property2;
        return this;
    }
}
