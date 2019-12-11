package com.github.jep42.findports;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPortTester implements PortTester {

    private static Logger logger = LoggerFactory.getLogger(DefaultPortTester.class);

    public void testPort(int portNumber) throws PortNotAvailableException, MojoExecutionException {
        ServerSocket server;
        try {
            server = new ServerSocket(portNumber);
        } catch (IOException e) {
            logger.debug("Port is " + portNumber + " not available due to: " + e.getMessage());
            throw new PortNotAvailableException("Port is " + portNumber + " not available", e);
        }

        try {
            server.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to release port " + portNumber, e);
        }
    }

}
