package com.sojostudios.as3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A test class.
 * 
 * @author Kurtis Kopf
 *
 */
public class TestObject
{
	public static final String FINAL_STRING = "my final string";
	
	private int hiddenInt = 12354;
	
	private List<String> hiddenList = new ArrayList<String>();
	private Map<String,String> hiddenMap = new HashMap<String,String>();
	
	private TestMap testMap = new TestMap();
	
	public TestObject()
	{
		
	}
	
	/**
	 * a test method
	 * 
	 * @param a an object
	 * @param b something
	 * @param c a list of stuff
	 * @return a boolean value
	 * @throws Exception when stuff breaks
	 */
	@SuppressWarnings(value = { "" })
	public boolean testMethod1(Object a, Class<? extends Object> b, List<String> c) throws Exception
	{
		c.add("cookies");
		c.get(0);
		
		hiddenList.add("hiddenString");
		hiddenList.get(0);
		hiddenMap.put("a", "b");
		hiddenMap.get("a");
		
		testMap.put("b","c");
		
		int myInt = 0;
		long myLong;
		double myDouble = 1235D;
		double myDouble2 = 12E3D;
		float myFloat = 123.5123F;
		byte myByte = 0x1;
		
		Map<String,String> myMap = new HashMap<String,String>();
		myMap.put("a", "b");
		myMap.get("a");
		myMap.remove("a");
		myInt = myMap.size();
		
		Map<Object,Object> myMap2 = (Map<Object,Object>)myMap;
		
		List<String> myList = new ArrayList<String>();
		myList.add("c");
		myList.get(0);
		myList.remove(0);
		myList = new ArrayList<String>();
		
		List<String> myList2 = (List<String>)myList;
		
		String[] myArray = new String[2];
		myArray[0] = "d";
		
		b = String.class;
		
		String myString = "asdf";
		
		if (myString.equals("abc"))
		{
			return;
		}
		if (!(myString.equals("asdf")))
		{
			return;
		}
		
		try
		{
			if (myString instanceof String)
			{
				myDouble = (double)myFloat;
			}
			else
			{
				throw new Exception("this should turn into an Error");
			}
		}
		catch(Exception e)
		{
			e.toString();
		}
		
		/*
		 * @AS3
		 * var cake:int = 0;
		 */

		return false || true;
	}
	
	public List<String> getList()
	{
		return new ArrayList<String>();
	}
}
