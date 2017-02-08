# jclasslocator

Java library for analyzing the classpath of an application, e.g., used to 
determine dynamic class hierarchies (simply add a jar with additional classes
in packages that the `ClassLister` monitors and they are automatically located).  

## Example

The following code is taken from `nz.ac.waikato.cms.locator.example.ClassListerExample`:

```java
import nz.ac.waikato.cms.locator.ClassLister;
import java.util.Properties;

// configuring the class hierarchies
Properties pkgs = new Properties();
pkgs.put(AbstractAncestor.class.getName(), "nz.ac.waikato.cms.locator.example.pkgA,nz.ac.waikato.cms.locator.example.pkgB");
pkgs.put(SomeInterface.class.getName(), "nz.ac.waikato.cms.locator.example.pkgA,nz.ac.waikato.cms.locator.example.pkgB");

// blacklisted classes
Properties black = new Properties();
black.put(AbstractAncestor.class.getName(), ".*C");  // anything that ends with a capital "C"
black.put(SomeInterface.class.getName(), "nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplInternal");  // specific class

// initialize
ClassLister lister = ClassLister.getSingleton();
lister.setPackages(pkgs);
lister.setBlacklist(black);
lister.initialize();

Class[] classes;
// abstract class
System.out.println("\nAbstract super class: " + AbstractAncestor.class.getName());
classes = lister.getClasses(AbstractAncestor.class);
for (Class cls: classes)
  System.out.println("- " + cls.getName());
// interface
System.out.println("\nInterface: " + SomeInterface.class.getName());
classes = lister.getClasses(SomeInterface.class);
for (Class cls: classes)
  System.out.println("- " + cls.getName());
```

Generates this output:

```
Abstract super class: nz.ac.waikato.cms.locator.example.AbstractAncestor
- nz.ac.waikato.cms.locator.example.pkgA.ConcreteClassA
- nz.ac.waikato.cms.locator.example.pkgA.ConcreteClassB

Interface: nz.ac.waikato.cms.locator.example.SomeInterface
- nz.ac.waikato.cms.locator.example.pkgA.InterfaceImplA
- nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplB
- nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplC
```

## Maven

Add the following dependency to your `pom.xml`:

```xml
    <dependency>
      <groupId>com.github.waikato</groupId>
      <artifactId>jclasslocator</artifactId>
      <version>0.0.4</version>
    </dependency>
```
