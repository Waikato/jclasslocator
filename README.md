# jclasslocator

Java library for analyzing the classpath of an application, e.g., used to 
determine dynamic class hierarchies (simply add a jar with additional classes
in packages that the `ClassLister` monitors and they are automatically located).  

Used by [ADAMS](https://adams.cms.waikato.ac.nz/).

## Class hierarchies

Defining class hierarchies is very easy. Just supply a `java.util.Properties`
object to the `ClassLister.setPackages(Properties)` method that lists for
each superclass (or interface) the packages that need inspecting.

Here is the basic format:

```INI
class.name=package1,package2,...
```

## Blacklisting

Blacklisting of classes is extremely easy. You just need to supply a
`java.util.Properties` object to the `ClassLister.setBlacklist(Properties)`
method that lists a comma-separated list of regular expressions for matching
class names to exclude. The simplest is to use the full class name of the
class to exclude.

Here is the basic format:

```INI
class.name=regexp1,regexp2,...
```

## Example (manual setup)

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

## Example (using .props files)

Instead of manually creating `java.util.Properties` objects, you can also
use `.props` files and load them instead.

Listing packages (`ClassLister.props`):
```INI
nz.ac.waikato.cms.locator.example.AbstractAncestor=\
  nz.ac.waikato.cms.locator.example.pkgA,\
  nz.ac.waikato.cms.locator.example.pkgB

nz.ac.waikato.cms.locator.example.SomeInterface=\
  nz.ac.waikato.cms.locator.example.pkgA,\
  nz.ac.waikato.cms.locator.example.pkgB
```

Blacklisting classes (`ClassLister.blacklist`)
```INI
nz.ac.waikato.cms.locator.example.AbstractAncestor=.*C

nz.ac.waikato.cms.locator.example.SomeInterface=nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplInternal
```

```java
import nz.ac.waikato.cms.locator.ClassLister;
import java.util.Properties;

// loading the props files (path might need adjusting)
Properties pkgs = ClassLister.load("ClassLister.props");
Properties black = ClassLister.load("ClassLister.blacklist");

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


## Logging

Logging is by default restricted to `WARNING` or higher (like `SEVERE`).
However, you can enable logging for debugging purposes via environment 
variables:

* `nz.ac.waikato.cms.locator.ClassCache.LOGLEVEL`
* `nz.ac.waikato.cms.locator.ClassLister.LOGLEVEL`
* `nz.ac.waikato.cms.locator.ClassLocator.LOGLEVEL`
* `nz.ac.waikato.cms.locator.ClassPathTraversal.LOGLEVEL`

These environment variables can take the following values:
```
{OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST}
```

## No classpath

### Fixed list of classnames
In case the classpath is empty, i.e., `System.getProperty("java.class.path")` 
returns an empty string, you can initialize the `ClassCache` instance with a
fixed list of class names. For that you need to provide an instance of the 
`FixedClassListTraversal` class to the `ClassLister.getSingleton(ClassTraversal)`
method. `FixedClassListTraversal` can be instantiated either with a 
`java.util.List<String>` that lists all class names or with an `java.io.InputStream`
object, from which to read the class names. Empty lines and lines starting with
`#` get automatically skipped.

The example classes could be stored in the file `nz/ac/waikato/cms/locator/example/fixed.classes` 
like this:

```
# first package
nz.ac.waikato.cms.locator.example.pkgA.ConcreteClassA
nz.ac.waikato.cms.locator.example.pkgA.ConcreteClassB
nz.ac.waikato.cms.locator.example.pkgA.InterfaceImplA

# second package
nz.ac.waikato.cms.locator.example.pkgB.ConcreteClassC
nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplB
nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplC
nz.ac.waikato.cms.locator.example.pkgB.InterfaceImplInternal
```

The code therefore looks like this for initializing the `ClassLister`:

```java
FixedClassListTraversal fixed = new FixedClassListTraversal(
  ClassLoader.getSystemResourceAsStream("nz/ac/waikato/cms/locator/example/fixed.classes"));
ClassLister lister = ClassLister.getSingleton(fixed);
```

The above code is taken from `nz.ac.waikato.cms.locator.example.ClassListerExampleFixedClassList`.


### Classnames from properties

Example `nz.ac.waikato.cms.locator.example.ClassListerExamplePropertiesBasedClassList` shows
how to initialize from a `java.utils.Properties` object. Each of the properties contains a
comma-separated list of classnames.


## Other useful methods

* `ClassLister.toProperties()` - returns the class hierarchies as a 
  `java.util.Properties` object. The key of a property is the superclass name
  and the associated value is the comma-separated list of classnames.
* `ClassLister.toPackages()` - returns the class hierarchies as a 
  `java.util.Properties` object. The key of a property is the superclass name
  and the associated value is the comma-separated list of packages.


## Maven

Add the following dependency to your `pom.xml`:

```xml
    <dependency>
      <groupId>com.github.waikato</groupId>
      <artifactId>jclasslocator</artifactId>
      <version>0.0.22</version>
    </dependency>
```
