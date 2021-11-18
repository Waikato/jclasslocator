/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ClassLocator.java
 * Copyright (C) 2005-2021 University of Waikato, Hamilton, New Zealand
 *
 */

package nz.ac.waikato.cms.locator;

import java.awt.HeadlessException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used for discovering classes that implement a certain
 * interface or a derived from a certain class. Based on the
 * <code>weka.core.ClassDiscovery</code> class.
 * <br>
 * Use "nz.ac.waikato.cms.locator.ClassLocator.LOGLEVEL" with a value of
 * "{OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST}" to set custom
 * logging level.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @see StringCompare
 */
public class ClassLocator 
  implements Serializable {

  /** for serialization. */
  private static final long serialVersionUID = 6443115424919701746L;

  /** the cache for subclass checks. */
  protected static Map<String,Boolean> m_CheckSubClass;
  static {
    m_CheckSubClass = new HashMap<>();
  }

  /** the cache for interface checks. */
  protected static Map<String,Boolean> m_CheckInterface;
  static {
    m_CheckInterface = new HashMap<>();
  }

  /** the logger in use. */
  protected transient Logger m_Logger;

  /** for caching queries (classname-packagename &lt;-&gt; List with classnames). */
  protected HashMap<String,List<String>> m_CacheNames;

  /** for caching queries (classname-packagename &lt;-&gt; List with classes). */
  protected HashMap<String,List<Class>> m_CacheClasses;

  /** for caching failed instantiations (classnames). */
  protected HashSet<String> m_BlackListed;

  /** the overall class cache. */
  protected ClassCache m_Cache;

  /** whether to allow only classes with the default constructor. */
  protected boolean m_OnlyDefaultConstructor;

  /** whether to allow only serializable classes. */
  protected boolean m_OnlySerializable;

  /** the singleton. */
  protected static Map<Class<? extends ClassTraversal>,ClassLocator> m_Singleton;

  /**
   * Initializes the class locator. Uses default traversal scheme for
   * initalizing the class cache.
   */
  protected ClassLocator() {
    this(null);
  }

  /**
   * Initializes the class locator. Uses the specified traversal scheme for
   * initializing the class cache.
   *
   * @param traversal the traversal instance to use, can be null
   */
  protected ClassLocator(ClassTraversal traversal) {
    super();
    initCache(traversal);
  }

  /**
   * Returns whether logging is enabled.
   *
   * @return		true if enabled
   */
  public boolean isLoggingEnabled() {
    return true;
  }

  /**
   * Returns the logger in use.
   *
   * @return		the logger
   */
  public synchronized Logger getLogger() {
    if (m_Logger == null) {
      m_Logger = Logger.getLogger(getClass().getName());
      m_Logger.setLevel(LoggingHelper.getLevel(getClass()));
    }
    return m_Logger;
  }

  /**
   * Sets whether to allow only classes with default constructor.
   *
   * @param value	true if only default allowed
   */
  public void setOnlyDefaultConstructor(boolean value) {
    m_OnlyDefaultConstructor = value;
  }

  /**
   * Returns whether to allow only classes with default constructor.
   *
   * @return		true if only default allowed
   */
  public boolean isOnlyDefaultConstructor() {
    return m_OnlyDefaultConstructor;
  }

  /**
   * Sets whether to allow only serializable classes.
   *
   * @param value	true if only serializable
   */
  public void setOnlySerializable(boolean value) {
    m_OnlySerializable = value;
  }

  /**
   * Returns whether to allow only serializable classes.
   *
   * @return		true if only serializable
   */
  public boolean isOnlySerializable() {
    return m_OnlySerializable;
  }

  /**
   * Checks the given packages for classes that inherited from the given class,
   * in case it's a class, or implement this class, in case it's an interface.
   *
   * @param classname       the class/interface to look for
   * @param pkgnames        the packages to search in
   * @return                a list with all the found classnames
   */
  public List<String> findNames(String classname, String[] pkgnames) {
    List<String>	result;
    Class		cls;

    result = new ArrayList<>();

    try {
      cls    = Class.forName(classname);
      result = findNames(cls, pkgnames);
    }
    catch (Throwable t) {
      getLogger().log(Level.SEVERE, "Failed to instantiate '" + classname + "'/" + ClassUtils.arrayToString(pkgnames) + " (findNames):", t);
    }

    return result;
  }

  /**
   * Checks the given packages for classes that inherited from the given class,
   * in case it's a class, or implement this class, in case it's an interface.
   *
   * @param classname       the class/interface to look for
   * @param pkgnames        the packages to search in
   * @return                a list with all the found classes
   */
  public List<Class> findClasses(String classname, String[] pkgnames) {
    List<Class>	result;
    Class		cls;

    result = new ArrayList<>();

    try {
      cls    = Class.forName(classname);
      result = findClasses(cls, pkgnames);
    }
    catch (Throwable t) {
      getLogger().log(Level.SEVERE, "Failed to instantiate '" + classname + "'/" + ClassUtils.arrayToString(pkgnames) + " (findClasses):", t);
    }

    return result;
  }

  /**
   * Checks the given packages for classes that inherited from the given class,
   * in case it's a class, or implement this class, in case it's an interface.
   *
   * @param cls             the class/interface to look for
   * @param pkgnames        the packages to search in
   * @return                a list with all the found classnames
   */
  public List<String> findNames(Class cls, String[] pkgnames) {
    List<String>	result;
    int			i;
    HashSet<String>	names;

    result = new ArrayList<>();

    names = new HashSet<>();
    for (i = 0; i < pkgnames.length; i++)
      names.addAll(findNamesInPackage(cls, pkgnames[i]));

    // sort result
    result.addAll(names);
    Collections.sort(result, new StringCompare());

    return result;
  }

  /**
   * Checks the given packages for classes that inherited from the given class,
   * in case it's a class, or implement this class, in case it's an interface.
   *
   * @param cls             the class/interface to look for
   * @param pkgnames        the packages to search in
   * @return                a list with all the found classes
   */
  public List<Class> findClasses(Class cls, String[] pkgnames) {
    List<Class>		result;
    int			i;
    HashSet<Class> 	classes;

    result = new ArrayList<>();

    classes = new HashSet<>();
    for (i = 0; i < pkgnames.length; i++)
      classes.addAll(findClassesInPackage(cls, pkgnames[i]));

    // sort result
    result.addAll(classes);
    Collections.sort(result, new ClassCompare());

    return result;
  }

  /**
   * Checks the given package for classes that inherited from the given class,
   * in case it's a class, or implement this class, in case it's an interface.
   *
   * @param classname       the class/interface to look for
   * @param pkgname         the package to search in
   * @return                a list with all the found classnames
   */
  public List<String> findNamesInPackage(String classname, String pkgname) {
    List<String>	result;
    Class		cls;

    result = new ArrayList<>();

    try {
      cls    = Class.forName(classname);
      result = findNamesInPackage(cls, pkgname);
    }
    catch (Throwable t) {
      getLogger().log(Level.SEVERE, "Failed to instantiate '" + classname + "'/" + pkgname + " (findNamesInPackage):", t);
    }

    return result;
  }

  /**
   * Checks the given package for classes that inherited from the given class,
   * in case it's a class, or implement this class, in case it's an interface.
   *
   * @param classname       the class/interface to look for
   * @param pkgname         the package to search in
   * @return                a list with all the found classes
   */
  public List<Class> findClassesInPackage(String classname, String pkgname) {
    List<Class>		result;
    Class		cls;

    result = new ArrayList<>();

    try {
      cls    = Class.forName(classname);
      result = findClassesInPackage(cls, pkgname);
    }
    catch (Throwable t) {
      getLogger().log(Level.SEVERE, "Failed to instantiate '" + classname + "'/" + pkgname + " (findClassesInPackage):", t);
    }

    return result;
  }

  /**
   * Checks the given package for classes that inherited from the given class,
   * in case it's a class, or implement this class, in case it's an interface.
   *
   * @param cls             the class/interface to look for
   * @param pkgname         the package to search in
   * @return                a list with all the found classnames
   */
  public List<String> findNamesInPackage(Class cls, String pkgname) {
    List<String>	result;
    List<Class>		classes;
    int			i;
    Class		clsNew;

    // already cached?
    result = getNameCache(cls, pkgname);

    if (result == null) {
      getLogger().info("Searching for '" + cls.getName() + "' in '" + pkgname + "':");

      result  = new ArrayList<>();
      classes = new ArrayList<>();
      if (m_Cache.getClassnames(pkgname) != null)
	result.addAll(m_Cache.getClassnames(pkgname));

      // check classes
      i = 0;
      while (i < result.size()) {
	try {
	  // no inner classes
	  if (result.get(i).indexOf('$') > -1) {
	    result.remove(i);
	    continue;
	  }
	  // blacklisted?
	  if (isBlacklisted(result.get(i))) {
	    result.remove(i);
	    continue;
	  }

	  clsNew = Class.forName(result.get(i));

	  // no abstract classes
	  if (Modifier.isAbstract(clsNew.getModifiers())) {
	    m_Cache.setAbstract(result.get(i), true);
	    result.remove(i);
	    continue;
	  }

	  // only classes with default constructor?
	  if (m_OnlyDefaultConstructor) {
	    try {
	      clsNew.getConstructor();
	    }
	    catch (Exception e) {
	      m_Cache.remove(result.get(i));
	      result.remove(i);
	      continue;
	    }
	  }

	  // only serializable classes?
	  if (m_OnlySerializable) {
	    if (!ClassLocator.hasInterface(Serializable.class, clsNew)) {
	      m_Cache.remove(result.get(i));
	      result.remove(i);
	      continue;
	    }
	  }

	  // must implement interface
	  if ( (cls.isInterface()) && (!hasInterface(cls, clsNew)) ) {
	    result.remove(i);
	  }
	  // must be derived from class
	  else if ( (!cls.isInterface()) && (!isSubclass(cls, clsNew)) ) {
	    result.remove(i);
	  }
	  else {
	    classes.add(clsNew);
	    i++;
	  }
	}
	catch (Throwable t) {
	  if ((t.getCause() != null) && (t.getCause() instanceof HeadlessException)) {
	    getLogger().warning("Cannot instantiate '" + result.get(i) + "' in headless environment - skipped.");
	  }
	  else {
	    getLogger().log(Level.SEVERE, "Failed to instantiate '" + result.get(i) + "' (find):", t);
	  }
	  blacklist(result.get(i));
	  result.remove(i);
	}
      }

      // sort result
      if (result.size() != classes.size())
	throw new IllegalStateException(
	  "Differing number of classnames and classes: " + result.size() + " != " + classes.size());

      Collections.sort(result, new StringCompare());
      Collections.sort(classes, new ClassCompare());

      // add to cache
      addCache(cls, pkgname, result, classes);
    }

    return result;
  }

  /**
   * Checks the given package for classes that inherited from the given class,
   * in case it's a class, or implement this class, in case it's an interface.
   *
   * @param cls             the class/interface to look for
   * @param pkgname         the package to search in
   * @return                a list with all the found classes
   */
  public List<Class> findClassesInPackage(Class cls, String pkgname) {
    // to fill cache
    findNamesInPackage(cls, pkgname);
    return getClassCache(cls, pkgname);
  }

  /**
   * Lists all packages it can find in the classpath.
   *
   * @return                a list with all the found packages
   */
  public List<String> findPackages() {
    List<String>	result;
    Iterator<String> packages;

    result   = new ArrayList<>();
    packages = m_Cache.packages();
    while (packages.hasNext())
      result.add(packages.next());
    Collections.sort(result, new StringCompare());

    return result;
  }


  /**
   * Returns the classpath parts that contain the specified class (eg to find duplicates).
   *
   * @param cls	the class to look for
   * @return		the parts of the classpath the class was located in
   */
  public List<URL> classpathPartsForClass(Class cls) {
    return classpathPartsForClass(cls.getName());
  }

  /**
   * Returns the classpath parts that contain the specified class (eg to find duplicates).
   *
   * @param classname	the class to look for
   * @return		the parts of the classpath the class was located in
   */
  public List<URL> classpathPartsForClass(String classname) {
    return m_Cache.classpathPartsForClass(classname);
  }

  /**
   * Returns a new instance of the {@link ClassCache}.
   *
   * @param traversal	how to traverse the classes, can be null
   * @return		the instance
   */
  protected ClassCache newClassCache(ClassTraversal traversal) {
    return new ClassCache(traversal);
  }

  /**
   * initializes the cache for the classnames.
   *
   * @param traversal	how to traverse the classes, can be null
   */
  protected void initCache(ClassTraversal traversal) {
    if (m_CacheNames == null)
      m_CacheNames = new HashMap<>();
    if (m_CacheClasses == null)
      m_CacheClasses = new HashMap<>();
    if (m_BlackListed == null)
      m_BlackListed = new HashSet<>();
    if (m_Cache == null)
      m_Cache = newClassCache(traversal);
  }

  /**
   * adds the list of classnames to the cache.
   *
   * @param cls		the class to cache the classnames for
   * @param pkgname	the package name the classes were found in
   * @param classnames	the list of classnames to cache
   */
  protected void addCache(Class cls, String pkgname, List<String> classnames, List<Class> classes) {
    m_CacheNames.put(cls.getName() + "-" + pkgname, classnames);
    m_CacheClasses.put(cls.getName() + "-" + pkgname, classes);
  }

  /**
   * returns the list of classnames associated with this class and package, if
   * available, otherwise null.
   *
   * @param cls		the class to get the classnames for
   * @param pkgname	the package name for the classes
   * @return		the classnames if found, otherwise null
   */
  protected List<String> getNameCache(Class cls, String pkgname) {
    return m_CacheNames.get(cls.getName() + "-" + pkgname);
  }

  /**
   * returns the list of classes associated with this class and package, if
   * available, otherwise null.
   *
   * @param cls		the class to get the classes for
   * @param pkgname	the package name for the classes
   * @return		the classes if found, otherwise null
   */
  protected List<Class> getClassCache(Class cls, String pkgname) {
    return m_CacheClasses.get(cls.getName() + "-" + pkgname);
  }

  /**
   * Blacklists the given classname.
   *
   * @param classname	the classname to blacklist
   */
  protected void blacklist(String classname) {
    m_BlackListed.add(classname);
  }

  /**
   * Returns whether this classname has been blacklisted.
   *
   * @param classname	the classname to check
   * @return		true if blacklisted
   */
  public boolean isBlacklisted(String classname) {
    return m_BlackListed.contains(classname);
  }

  /**
   * Returns the class cache.
   *
   * @return		the cache
   */
  public ClassCache getCache() {
    return m_Cache;
  }

  /**
   * Returns the singleton, instantiates it if necessary.
   *
   * @return		the singleton
   */
  public static synchronized ClassLocator getSingleton() {
    return getSingleton(null);
  }

  /**
   * Returns the singleton, instantiates it if necessary.
   *
   * @param traversal 	the class traversal scheme to use, can be null for default one
   * @return		the singleton
   */
  public static synchronized ClassLocator getSingleton(ClassTraversal traversal) {
    Class<? extends ClassTraversal>	cls;

    cls = (traversal == null) ? null : traversal.getClass();
    if (m_Singleton == null)
      m_Singleton = new HashMap<>();
    if (!m_Singleton.containsKey(cls))
      m_Singleton.put(cls, new ClassLocator(traversal));
    
    return m_Singleton.get(cls);
  }
  
  /**
   * Checks whether the "otherclass" is a subclass of the given "superclass".
   *
   * @param superclass      the superclass to check against
   * @param otherclass      this class is checked whether it is a subclass
   *                        of the the superclass
   * @return                TRUE if "otherclass" is a true subclass
   */
  public static boolean isSubclass(String superclass, String otherclass) {
    String	key;

    key = superclass + "-" + otherclass;
    if (m_CheckSubClass.containsKey(key))
      return m_CheckSubClass.get(key);

    try {
      return isSubclass(Class.forName(superclass), Class.forName(otherclass));
    }
    catch (Throwable t) {
      return false;
    }
  }

  /**
   * Checks whether the "otherclass" is a subclass of the given "superclass".
   *
   * @param superclass      the superclass to check against
   * @param otherclass      this class is checked whether it is a subclass
   *                        of the the superclass
   * @return                TRUE if "otherclass" is a true subclass
   */
  public static boolean isSubclass(Class superclass, Class otherclass) {
    Class       currentclass;
    boolean     result;
    String	key;

    key = superclass.getName() + "-" + otherclass.getName();
    if (m_CheckSubClass.containsKey(key))
      return m_CheckSubClass.get(key);

    currentclass = otherclass;
    do {
      result = currentclass.equals(superclass);

      // topmost class reached?
      if (currentclass.equals(Object.class) || (currentclass.getSuperclass() == null))
        break;

      if (!result)
        currentclass = currentclass.getSuperclass();
    }
    while (!result);

    m_CheckSubClass.put(key, result);

    return result;
  }

  /**
   * Checks whether the given class implements the given interface.
   *
   * @param intf      the interface to look for in the given class
   * @param cls       the class to check for the interface
   * @return          TRUE if the class contains the interface
   */
  public static boolean hasInterface(String intf, String cls) {
    String	key;

    key = intf + "-" + cls;
    if (m_CheckInterface.containsKey(key))
      return m_CheckInterface.get(key);

    try {
      return hasInterface(Class.forName(intf), Class.forName(cls));
    }
    catch (Throwable t) {
      return false;
    }
  }

  /**
   * Checks whether the given class implements the given interface.
   *
   * @param intf      the interface to look for in the given class
   * @param cls       the class to check for the interface
   * @return          TRUE if the class contains the interface
   */
  public static boolean hasInterface(Class intf, Class cls) {
    boolean	result;
    String	key;

    key = intf.getName() + "-" + cls.getName();
    if (m_CheckInterface.containsKey(key))
      return m_CheckInterface.get(key);

    result = intf.isAssignableFrom(cls);
    m_CheckInterface.put(key, result);

    return result;
  }

  /**
   * Checks whether the "otherclass" is a subclass of the given "superclassOrIntf"
   * or whether it implements "superclassOrIntf".
   *
   * @param superclassOrIntf      the superclass/interface to check against
   * @param otherclass            this class is checked whether it is a subclass
   *                              of the the superclass
   * @return                      TRUE if "otherclass" is a true subclass or implements the interface
   */
  public static boolean matches(String superclassOrIntf, String otherclass) {
    return isSubclass(superclassOrIntf, otherclass) || hasInterface(superclassOrIntf, otherclass);
  }

  /**
   * Checks whether the "otherclass" is a subclass of the given "superclassOrIntf"
   * or whether it implements "superclassOrIntf".
   *
   * @param superclassOrIntf      the superclass/interface to check against
   * @param otherclass            this class is checked whether it is a subclass
   *                              of the the superclass
   * @return                      TRUE if "otherclass" is a true subclass or implements the interface
   */
  public static boolean matches(Class superclassOrIntf, Class otherclass) {
    return isSubclass(superclassOrIntf, otherclass) || hasInterface(superclassOrIntf, otherclass);
  }

  /**
   * Possible calls:
   * <ul>
   *    <li>
   *      adams.core.ClassLocator &lt;packages&gt;<br>
   *      Prints all the packages in the current classpath
   *    </li>
   *    <li>
   *      adams.core.ClassLocator &lt;classname&gt; &lt;packagename(s)&gt;<br>
   *      Prints the classes it found.
   *    </li>
   * </ul>
   *
   * @param args	the commandline arguments
   */
  public static void main(String[] args) {
    List<String>	list;
    List<String>	packages;
    int         	i;
    StringTokenizer	tok;

    if ((args.length == 1) && (args[0].equals("packages"))) {
      list = getSingleton().findPackages();
      for (i = 0; i < list.size(); i++)
	System.out.println(list.get(i));
    }
    else if (args.length == 2) {
      // packages
      packages = new ArrayList<>();
      tok = new StringTokenizer(args[1], ",");
      while (tok.hasMoreTokens())
        packages.add(tok.nextToken());

      // search
      list = getSingleton().findNames(
	args[0],
	packages.toArray(new String[packages.size()]));

      // print result, if any
      System.out.println(
          "Searching for '" + args[0] + "' in '" + args[1] + "':\n"
          + "  " + list.size() + " found.");
      for (i = 0; i < list.size(); i++)
        System.out.println("  " + (i+1) + ". " + list.get(i));
    }
    else {
      System.out.println("\nUsage:");
      System.out.println(
	  ClassLocator.class.getName() + " packages");
      System.out.println("\tlists all packages in the classpath");
      System.out.println(
	  ClassLocator.class.getName() + " <classname> <packagename(s)>");
      System.out.println("\tlists classes derived from/implementing 'classname' that");
      System.out.println("\tcan be found in 'packagename(s)' (comma-separated list)");
      System.out.println();
      System.exit(1);
    }
  }
}
