package com.sojostudios.as3.ant.types;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.Project;

/**
 * Use as a sub-tag of the JavaToAS3Task.
 * Each of these represents a file to be compiled. Compiler options can
 * be overidden here.
 * 
 * @author Kurtis Kopf
 */
public class SourceTarget implements DynamicConfigurator
{
	private String src = null;
	private String dst = null;
	private Map<String,String> attributes = new HashMap<String,String>();
	
	public SourceTarget()
	{
		
	}
	
	public SourceTarget(Project project)
	{
		
	}

	@Override
	public void setDynamicAttribute(String key, String value)
			throws BuildException
	{
		attributes.put(key, value);
	}

	@Override
	public Object createDynamicElement(String arg0) throws BuildException
	{
		return null;
	}
	
	public Map<String,String> getAttributes()
	{
		return attributes;
	}

	/**
	 * @return the src
	 */
	public String getSrc()
	{
		return src;
	}

	/**
	 * @param src the src to set
	 */
	public void setSrc(String src)
	{
		this.src = src;
	}

	/**
	 * @return the dst
	 */
	public String getDst()
	{
		return dst;
	}

	/**
	 * @param dst the dst to set
	 */
	public void setDst(String dst)
	{
		this.dst = dst;
	}
}