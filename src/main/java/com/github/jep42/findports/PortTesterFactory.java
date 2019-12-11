package com.github.jep42.findports;

public final class PortTesterFactory {

    private PortTesterFactory() {
        super();
    }

    public static PortTester getPortTester() {
        return new DefaultPortTester();
    }

}
