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

apply plugin: 'dev.soostrator.resolver'
```

#### Adding the dsl to the dependencies section:

```groovy
dependencies {
    implementation files('path/to/dsl.jar')
}
```

#### Setting up a ResolverTask:

```groovy
tasks.register("ResolveBuild", dev.soostrator.cti.plugin.ResolverTask) {
    setGroup(rootProject.name)

    String jarName = "${rootProject.name}-${version}"
    String libsDir = libsDirectory.get().asFile.absolutePath + File.separator

    inputJar = new File("${libsDir}${jarName}.jar")
    outputJar = new File("${libsDir}${jarName}-RESOLVED.jar")
}
```

#### Linking the resolve task to the build task:

```groovy
tasks.build.finalizedBy tasks.ResolveBuild
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

## Goals:

- [x] Resolve all child classes of a superclass (Aka make it actually work)
- [ ] Also resolve classes that inherit a child class and not the superclass directly
- [ ] Add option to use Class.forName to allow out of scope classes
- [ ] Add actual logging
- [ ] Cleanup the nesting and general code
- [ ] Write tests