package com.github.jep42.portoffsetfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.util.reflection.FieldSetter;

import com.github.jep42.findports.FindPortsMojo;
import com.github.jep42.findports.PortNotAvailableException;
import com.github.jep42.findports.PortTester;


public class FindPortOffsetMojoTest {

	
    @Test
    public void execute() throws Exception {
    	//GIVEN
    	FindPortsMojo mojoUnderTest = new FindPortsMojo(mock(PortTester.class));
    	
    	Properties ports = new Properties();
    	ports.put("httpPort", 8080);
    	ports.put("anotherPort", 10200);
    	
    	Properties projectProperties = mock(Properties.class);
    	MavenProject project = mock(MavenProject.class);
    	doReturn(projectProperties).when(project).getProperties();
    	
    	this.setMojoProperties(mojoUnderTest, ports, project, true);
    	    	
    	ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    	ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    	
    	//WHEN
    	mojoUnderTest.execute();
    	
    	//THEN
    	verify(projectProperties, times(2)).put(keyCaptor.capture(), valueCaptor.capture());
    	
    	List<String> capturedKeys = keyCaptor.getAllValues();
    	List<String> capturedValues = valueCaptor.getAllValues();
    	assertEquals("anotherPort", capturedKeys.get(0));
    	assertEquals("httpPort", capturedKeys.get(1));
    	assertEquals("10200", capturedValues.get(0));
    	assertEquals("8080", capturedValues.get(1));
    }
    
    
	@Test
    public void execute_noFreePorts_fail() throws Exception {
    	//GIVEN
    	PortTester tester = mock(PortTester.class);
    	doThrow(new PortNotAvailableException("port not available")).when(tester).testPort(anyInt());
    	FindPortsMojo mojoUnderTest = new FindPortsMojo(tester);
    	
    	Properties ports = new Properties();
    	ports.put("httpPort", 8080);
    	ports.put("anotherPort", 10200);
    	
    	Properties projectProperties = mock(Properties.class);
    	MavenProject project = mock(MavenProject.class);
    	doReturn(projectProperties).when(project).getProperties();
    	
    	this.setMojoProperties(mojoUnderTest, ports, project, true);
    	
    	ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    	ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    	
    	//WHEN
    	try {
    		mojoUnderTest.execute();
    		fail("MojoFailureException expected");
    	} catch (MojoFailureException e) {
    		assertTrue(e.getMessage().contains("Could not find range of free ports."));
    	}
    	
    	//THEN
    	verify(projectProperties, never()).put(keyCaptor.capture(), valueCaptor.capture());
    }
	
	@Test
    public void execute_noFreePorts_nofail() throws Exception {
    	//GIVEN
    	PortTester tester = mock(PortTester.class);
    	doThrow(new PortNotAvailableException("port not available")).when(tester).testPort(anyInt());
    	FindPortsMojo mojoUnderTest = new FindPortsMojo(tester);
    	
    	Properties ports = new Properties();
    	ports.put("httpPort", 8080);
    	ports.put("anotherPort", 10200);
    	
    	Properties projectProperties = mock(Properties.class);
    	MavenProject project = mock(MavenProject.class);
    	doReturn(projectProperties).when(project).getProperties();
    	
    	this.setMojoProperties(mojoUnderTest, ports, project, false);
    	
    	ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    	ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    	
    	//WHEN
    	mojoUnderTest.execute();
    	
    	//THEN
    	verify(projectProperties, never()).put(keyCaptor.capture(), valueCaptor.capture());
    }
	
	
	private void setMojoProperties(FindPortsMojo mojoUnderTest, Properties ports, MavenProject project, boolean doFail) throws NoSuchFieldException, SecurityException {
    	new FieldSetter(mojoUnderTest, mojoUnderTest.getClass().getDeclaredField("failIfPortsNotAvailable")).set(doFail);
    	new FieldSetter(mojoUnderTest, mojoUnderTest.getClass().getDeclaredField("offset")).set(0);
    	new FieldSetter(mojoUnderTest, mojoUnderTest.getClass().getDeclaredField("steps")).set(10);
    	new FieldSetter(mojoUnderTest, mojoUnderTest.getClass().getDeclaredField("maxIterations")).set(5);
    	new FieldSetter(mojoUnderTest, mojoUnderTest.getClass().getDeclaredField("ports")).set(ports);
    	new FieldSetter(mojoUnderTest, mojoUnderTest.getClass().getDeclaredField("project")).set(project);
	}
    
}
