package com.github.jep42.findports;

import java.io.IOException;

public class PortNotAvailableException extends Exception {

    private static final long serialVersionUID = 5016642738464153148L;

    public PortNotAvailableException(String msg) {
        super(msg);
    }

    public PortNotAvailableException(String msg, IOException e) {
        super(msg, e);
    }

}
