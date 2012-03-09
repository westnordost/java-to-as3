package com.sojostudios.as3.visitors;

/**
 * For printing out indented languages.
 * 
 * @author Kurtis Kopf
 *
 */
public class IndentedPrinter
{
	private String indentSpace = "\t";
	private int indents = 0;
	private boolean indented = false;
	private StringBuilder buffer = new StringBuilder();
	
	public IndentedPrinter()
	{
		
	}
	
	public IndentedPrinter(String indentSpace)
	{
		this.indentSpace = indentSpace;
	}
	
	public void indent()
	{
		indents++;
	}
	
	public void unindent()
	{
		indents--;
	}
	
	private void makeIndent()
	{
		for (int i = 0; i < indents; i++)
		{
			buffer.append(indentSpace);
		}
	}
	
	public void print(String arg)
	{
		if (!indented)
		{
			makeIndent();
			indented = true;
		}
		buffer.append(arg);
	}
	
	public void printLn(String arg)
	{
		print(arg);
		printLn();
	}
	
	public void printLn()
	{
		buffer.append("\n");
		indented = false;
	}
	
	@Override
	public String toString()
	{
		return buffer.toString();
	}
}