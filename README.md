Fiber
=====

A cooperative lightweight thread library for Java

Build
-----
Requirements:
* Java 5 or later
* Ant

This library depends on [Matthias Mann's Continuation library](http://www.matthiasmann.de/content/view/24/26/). To download the dependency run:
```
ant get-deps
```

Then to build just run ant
```
ant
```

Usage
-----
Example of using the Fiber library
```java
import de.matthiasmann.continuations.SuspendExecution;
import fiber.*;

/**
 *
 * @author grom
 */
public class Test {
    static public void main(String[] args) throws Exception {
        FiberScheduler scheduler = new FiberScheduler();
        scheduler.submit(new Fiber() {
            @Override
            public void coExecute() throws SuspendExecution {
                long start = System.currentTimeMillis();
                System.out.println("Hello");
                sleep(2000);
                long elapsed = System.currentTimeMillis() - start;
                System.out.println("World! " + elapsed);
            }
        });
        scheduler.start();
        scheduler.join();
    }
}
```

In order to build code that uses the Fiber library, the fiber classes need to be
instrumented. For example in your Ant build file:
```xml
  <taskdef onerror="report" name="continuations"
    classname="de.matthiasmann.continuations.instrument.InstrumentationTask"
    classpath="${lib}/Continuations_full_2013-02-17_03-52.jar:${lib}/asm-all-4.2.jar"/>

  <target name="compile" depends="init">
    <javac srcdir="${src}" destdir="${build}" classpath="${lib}/Continuations_runtime_2013-02-17_03-52.jar"/>
    <continuations verbose="true">
        <fileset dir="${build}"/>
    </continuations>
  </target>
```

Then to run the compiled class:
```
java -cp lib/Continuations_runtime_2013-02-17_03-52.jar:lib/Fiber.jar:. Test
```

Which will output:
```
Hello
World! 2000
```
