package com.github.jep42.findports;

import org.apache.maven.plugin.MojoExecutionException;

public interface PortTester {

    void testPort(int portNumber) throws PortNotAvailableException, MojoExecutionException;

}
