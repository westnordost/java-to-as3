package com.sojostudios.as3.visitors;

import japa.parser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple placeholder class for a variable mutation. Stores flags for
 * mutation references.
 * 
 * @author Kurtis Kopf
 *
 */
public class VarMutation
{
	public String name = null;
	public ClassOrInterfaceType type = null;
	public List<String> mutationFlags = new ArrayList<String>();
	
	public boolean hasFlag(String flag)
	{
		return mutationFlags.contains(flag);
	}
}
