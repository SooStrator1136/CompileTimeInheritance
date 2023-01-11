# CompileTimeInheritance

CompileTimeInheritance is a library to resolve subclasses at compile time, inspired
by [reflekt](https://github.com/JetBrains-Research/reflekt) which only works for kotlin classes and java 11+.

## Getting started:

**Note:** This is in a really early stage, currently the artifacts aren't available in any maven or ivy repository.
For now, you may include the plugin and dsl as file references in your project (as shown below).

#### Adding the plugin:

```groovy
buildscript {
    dependencies {
        classpath files('path/to/plugin.jar')
    }
}

apply plugin: 'me.soostrator.resolver'
```

#### Adding the dsl:

```groovy
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation 'com.github.SooStrator1136:CompileTimeInheritance:master-SNAPSHOT'
}
```

#### Setting up a ResolveJarTask:

```groovy
tasks.register("resolveBuild", me.soostrator.cti.plugin.ResolveJarTask) {
    setGroup(rootProject.name)

    String jarName = "${rootProject.name}-${version}"
    String libsDir = libsDirectory.get().asFile.absolutePath + File.separator

    inputJar = new File("${libsDir}${jarName}.jar")
    outputJar = new File("${libsDir}${jarName}-RESOLVED.jar")
}
```

#### Linking the resolve task to the build task:

```groovy
tasks.build.finalizedBy tasks.resolveBuild
```

#### You can configure some of the plugins behavior using the ``resolver`` extension:

```groovy
resolver {
    useClassForName = false
    resolveFoundJars = false
}
```

Now you can use the plugin to find subclasses in your project.

#### Example:

```java
public final class Bar {

    public Bar() {
        final Class<?>[] foos = InheritanceResolver.resolveAllHeirs(Foo.class);

        assertTrue(Arrays.asList(foos).contains(FooFan.class));
        assertTrue(Arrays.asList(foos).contains(Foo711.class));
    }

    class Foo {
    }

    final class FooFan extends Foo {
    }

    final class Foo711 extends Foo {
    }

}
```

After the resolve task ``InheritanceResolver.resolveAllHeirs(Foo.class)`` will be replaced
by ``new Class[]{Foo711.class, FooFan.class}``.

## Roadmap:

- [x] Resolve all child classes of a superclass (Aka make it actually work)
- [x] Also resolve classes that inherit a child class and not the superclass directly
- [x] Add option to use Class.forName to allow out of scope classes
- [x] Add task to process classes from a directory and not from a jar
- [ ] Add actual logging
- [ ] Cleanup the nesting and general code
- [ ] Write tests
- [ ] Document code