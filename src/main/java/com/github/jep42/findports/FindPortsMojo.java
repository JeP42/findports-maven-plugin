package com.github.jep42.findports;

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo to find available ports and propagate these via Maven properties
 *
 */
@Mojo( name = "findPorts", defaultPhase = LifecyclePhase.INITIALIZE )
public class FindPortsMojo extends AbstractMojo {
    
	/**
     * Fail the build if a valid port range could not be found
     */
	@Parameter( defaultValue = "true", property = "failIfPortsNotAvailable", required = false)
    private boolean failIfPortsNotAvailable;

	/**
     * Specifies the starting offset. If set to 0 then the first iteration uses the port numbers as configured in {@link FindPortsMojo#ports}
     */
	@Parameter( defaultValue = "0", property = "offset", required = false )
    private Integer offset;
	
	/**
     * The value by which port numbers are increased in each iteration 
     */
	@Parameter( defaultValue = "100", property = "steps", required = false)
    private Integer steps;
	
	/**
     * The number of iterations when trying to find available ports  
     */
	@Parameter( defaultValue = "10", property = "maxIterations", required = false )
    private Integer maxIterations;
	
    
    /**
     * The ports to be tested. If a valid port offset can be found then for each port a corresponding Maven property is published 
     * which contains the free port number.
     */
	@Parameter( property = "ports", required = true )
    private Properties ports;
	
	
	@Parameter(defaultValue = "${project}")
	private MavenProject project;
	
    private PortTester portTester;
    
    
    
    public FindPortsMojo() {
		super();
		this.portTester = PortTesterFactory.getPortTester();
	}
    
    public FindPortsMojo(PortTester portTester) {
		super();
		this.portTester = portTester;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
    	this.validateConfiguration();
    	
    	Integer portOffset = this.findAvailablePortOffset();
    	
    	if (portOffset != null) {
    		this.findPortsSucceeded(portOffset);
    	} else {
    		this.findPortsFailed();
    	}
    }
    
    private void validateConfiguration() throws MojoExecutionException {
    	if (this.ports.size() == 0) {
			throw new MojoExecutionException("No ports. At least one port must be configured.");
		}
	}

	/**
     * Iterates over the configured ports, adds an offset and checks if these ports are available 
     * 
     * @return
     * @throws MojoExecutionException 
     */
	private Integer findAvailablePortOffset() throws MojoExecutionException {
		
		int iterations = 0;
		while (iterations++ < this.maxIterations && !this.isAvailable() ) {
			this.offset += this.steps;
		}
		
		if (iterations > this.maxIterations) {
			return null;
		} else {
			return this.offset;
		}
	}

	
	private boolean isAvailable() throws MojoExecutionException {
		getLog().info("Checking ports with offset " + offset + "...");
		for (Entry<Object, Object> port : this.ports.entrySet()) {
			try {
				this.portTester.testPort(Integer.parseInt(port.getValue().toString()) + offset);
			} catch (PortNotAvailableException e) {
				getLog().debug("The port-offset " + offset + " is not available due to: " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	
    /**
     * Set Maven properties 
     *  
     * @param portOffset
     */
    private void findPortsSucceeded(Integer portOffset) {
    	getLog().info("Port-Offset found: " + portOffset);
    	for (Entry<Object, Object> port : this.ports.entrySet()) {
    		project.getProperties().put( port.getKey().toString(), String.valueOf(Integer.parseInt(port.getValue().toString()) + portOffset));
    	}
	}

	private void findPortsFailed() throws MojoFailureException {
    	String message = "Could not find range of free ports.";	
		getLog().warn(message);
		if (this.failIfPortsNotAvailable) {
			throw new MojoFailureException(message);
		}
	}

}
