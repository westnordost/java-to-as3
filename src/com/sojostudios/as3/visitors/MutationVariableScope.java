package com.sojostudios.as3.visitors;

import japa.parser.ast.type.ClassOrInterfaceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class keeps track of variable scopes and mutation flags for those variables
 * at each scope.
 * 
 * @author Kurtis Kopf
 *
 */
public class MutationVariableScope
{
	private int scopeDepth = 0;
	private List<Map<String,VarMutation>> scope = new ArrayList<Map<String,VarMutation>>();
	
	public MutationVariableScope()
	{
		// depth 0 map
		scope.add(new HashMap<String,VarMutation>());
	}
	
	/**
	 * Increases the scope depth and creates a new scope container.
	 */
	public void pushScopeStack()
	{
		scopeDepth++;
		scope.add(new HashMap<String,VarMutation>());
	}
	
	/**
	 * Decreases the scope depth and destroys the current scope container.
	 */
	public void popScopeStack()
	{
		scope.remove(scopeDepth);
		scopeDepth--;
		if (scopeDepth < 0)
		{
			throw new RuntimeException("Stack underflow exception from Scope Stack!");
		}
	}
	
	/**
	 * Get a variable mutation. Looks up through the scope stack until the variable is found.
	 * 
	 * @param name the variable name.
	 * @return the corresponding VarMutation or null if the variable was not found
	 */
	public VarMutation getVar(String name)
	{
		// look at current level and up the stack until we find a matching name.
		for (int depth = scopeDepth; depth >= 0; depth--)
		{
			VarMutation temp = scope.get(depth).get(name);
			if (temp != null)
			{
				return temp;
			}
		}
		return null;
	}
	
	/**
	 * Look for a variable mutation at the current scope depth only.
	 * 
	 * @param name the variable name
	 * @return the corresponding VarMutation or null if not found.
	 */
	public VarMutation getVarCurScopeOnly(String name)
	{
		return scope.get(scopeDepth).get(name);
	}
	
	/**
	 * Add a variable with mutation tracking.
	 * 
	 * @param name the variable name in this scope
	 * @param type the class type of the variable
	 * @param mutationFlags the TRUE flags for mutations (Array, Dictionary by default)
	 */
	public void addVar(String name, ClassOrInterfaceType type, List<String> mutationFlags)
	{
		VarMutation mut = new VarMutation();
		mut.name = name;
		mut.type = type;
		if (mutationFlags != null)
		{
			mut.mutationFlags.addAll(mutationFlags);
		}
		scope.get(scopeDepth).put(name, mut);
	}
}
