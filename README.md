Java to AS3 Compiler v1.0.9
===========================

This software was developed by [Sojo Studios, Inc.](http://www.sojostudios.com/)
Please visit the [project's home page](https://github.com/Sojo-Studios/java-to-as3).

This is a tool that will take Java 1.5 source code and make a best-effort translation of that 
code into AS3 code. The resulting AS3 code can then be compiled into a SWF or SWC using
[Adobe's Flex SDK](http://opensource.adobe.com/wiki/display/flexsdk/Flex+SDK).

The compiler uses the [javaparser](http://code.google.com/p/javaparser/) library to 
construct an Abstract Syntax Tree from the input Java code. There are then two passes 
made over the tree. The first is a `mutation` pass where incompatible nodes are replaced 
with compatible nodes. The second is a `dump` pass, where the AS3 source code is assembled
into a string of text. 


Features
--------
The major features of this compiler revolve around the ability to perform basic mutations
from Java-specific classes into AS3 equivalents. The three major targets are:

* Arrays
* Vectors
* Dictionaries

The use of these classes is fundamentally different within the context of AS3, but there
are obviously Java counterparts:

* Arrays and Lists
* Lists / Collections
* Maps

The default mutations include:

* The conversion of any array, typed as "L[Class]" in Java, into an AS3 Array
* The conversion of some Lists into AS3 Vectors, including template-typing
* The conversion of some Maps into AS3 Dictionaries

There is also the ability to specify any additional Class type as a something that needs to 
be converted to one of the above. This is useful if you extend a Map or List.

The additional basic features provide some utilities for conversions between compatible classes
that do not need mutations.

* Forcing a class to extend Sprite or MovieClip (forceSprite, forceMovieClip)
* Regex matching package names and converting them (packageToPackage)
* Regex matching imports and converting them (importsToImports)
* Regex matching class names and converting them (classesToClasses)
* Ability to completely ignore regex matched imports such as `java.*` (importsToIgnore)
* Ability to force additional imports (forcedImports)
* Ability to specify additional classes to be converted to Arrays (classesToArrays)
* Ability to specify additional classes to be converted to Vectors (classesToVectors)
* Ability to specify additional classes to be converted to Dictionaries (classesToDictionaries)


Limitations
-----------
Because this compiler only parses one file at a time, it does not do type checking, and does 
not track typing information from imports. This means that if you use an external function that
returns a type that needs to be mutated, such as a string, the compiler will not be able to
detect the need for a mutation.

For example: 

```java
if(ExternalClass.getAString().equals("x"))
```

Will not be properly translated into

```actionscript
if(ExternalClass.getAString() == "x")
```

The workaround for these typing situations is to store the value in a local variable first. This 
makes the code more terse, but it does work.

```java
String myVal = ExternalClass.getAString();
if(myVal.equals("x"))
```

Will be properly translated into

```actionscript
var myVal:String = ExternalClass.getAString();
if(myVal == "x")
```


Using With Ant
--------------

In addition to the `sojo-java-to-as3` jar, you will need to have `log4j` and `javaparser` included 
in the classpath to use this task in Ant.

```xml
<taskdef name="javaToAs3" classname="com.sojostudios.as3.ant.JavaToAS3Task">
	<classpath refid="some.path" />
</taskdef>
<target name="compile">
	<javaToAs3 includeDefaultMutations="true">
		<sourceTarget src="MyClass.java" dst="MyClass.as" forceMovieClip="true" />
 		<sourceTarget src="MyHelperClass.java" dst="MyHelperClass.as" />
	</javaToAs3>
</target>
```


Including with Ivy
------------------

You will need to add a `url` entry to your repositories list in your 
[Ivy settings file](http://ant.apache.org/ivy/history/latest-milestone/use/settings.html).
Example settings file:

```xml
<ivysettings>
        <settings defaultResolver="ibiblio"/>
        <resolvers>
                <ibiblio name="ibiblio" m2compatible="true"/>
                <url name="sojo-studios.github.com">
					<ivy pattern="http://sojo-studios.github.com/repo/[module]/[revision]/ivy.xml" />
					<artifact pattern="http://sojo-studios.github.com/repo/[module]/[revision]/[artifact]-[revision].[ext]" />
				</url>
        </resolvers>
        <modules>
                <module organisation="com.sojostudios" name=".*" resolver="sojo-studios.github.com" />
        </modules>
</ivysettings>
```

You then need to add a dependency for `sojo-java-to-as3` to your `ivy.xml` file that points to `master`.

```xml
<configurations>
	<conf name="compile" visibility="public" description="libraries used during compilation."/>
	<conf name="runtime" visibility="public" description="libraries used during runtime." extends="compile"/>
</configurations>
<dependencies>
	<dependency org="com.sojostudios" name="sojo-java-to-as3" rev="1.0.5" conf="compile->master(*);runtime->default"/>
</dependencies>
```

Also, don't forget to load your Ivy settings file in your Ant buildfile. Example assuming file is `ivysettings.xml` 
in the root project build directory:

```xml
<ivy:settings file="ivysettings.xml" />
```

Then add a task to your Ant buildfile to resolve dependencies:

```xml
<target name="resolve" description="resolve dependencies">
	<ivy:retrieve conf="compile"/>
</target>
```


Building from Source
--------------------

You will need Ant and Ivy installed to build this project. Once they are installed, just run `ant` in the
root project directory and will create a .jar file in the jar/ directory.


Changelog
---------
v1.0.11
* added ability to modify mutation output class names

v1.0.10
* fixed an issue with scientific notation on doubles, where E was removed from the output string

v1.0.9
* added support for loglevel attribute in ant task

v1.0.8
* added support for extending array/vector/dictionary types

v1.0.7
* fixed scoping/mutation problem with class field declarations

v1.0.6a
* removed comments for Map declarations, Map&lt;a,Map&lt;b,c&gt;&gt; were creating syntax errors

v1.0.6
* added all standard Collection and Map classes for conversion

v1.0.5
* initial public release
