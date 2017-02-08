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
 * ClassLister.java
 * Copyright (C) 2007-2017 University of Waikato, Hamilton, New Zealand
 */

package nz.ac.waikato.cms.locator;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Determines the classnames of superclasses that are to be displayed in
 * a GUI for instance.
 * <br><br>
 * <b>IMPORTANT NOTE:</b> Due to <a href="http://geekexplains.blogspot.com/2008/07/what-is-reentrant-synchronization-in.html" target="_blank">reentrant threads</a>,
 * the <code>getSingleton()</code> method is not allowed to be called from
 * <code>static {...}</code> blocks in classes that are managed by the
 * ClassLister class (and therefore the ClassLocator class). Since the
 * ClassLocator class loads classes into the JVM, the <code>static {...}</code>
 * block of these classes gets executed and the ClassLister gets initialized
 * once again. In this case, the previous singleton will most likely get
 * overwritten.
 * <br><br>
 * Usage examples:
 * <pre>
 * // initialize the class lister
 * Properties pkges = ...;  // class hierarchies
 * Properties blacklist = ...;  // any blacklisted classes
 * ClassLister lister = ClassLister.getSingleton();
 * lister.setPackages(pkges);
 * lister.setBlacklist(blacklist);
 * lister.initialize();
 * // use class lister
 * Class[] classes = lister.getClasses("my.SuperClass");
 * </pre>
 * <br>
 * Use "nz.ac.waikato.cms.locator.ClassLister.LOGLEVEL" with a value of
 * "{OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST}" to set custom
 * logging level.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 15209 $
 */
public class ClassLister
  implements Serializable {

  /** for serialization. */
  private static final long serialVersionUID = 8482163084925911272L;

  /** the logger in use. */
  protected transient Logger m_Logger;

  /** the properties (superclass/packages). */
  protected Properties m_Packages;

  /** the cache (superclass/classnames). */
  protected HashMap<String,HashSet<String>> m_CacheNames;

  /** the list (superclass/classnames). */
  protected HashMap<String,List<String>> m_ListNames;

  /** the cache (superclass/classes). */
  protected HashMap<String,HashSet<Class>> m_CacheClasses;

  /** the list (superclass/classes). */
  protected HashMap<String,List<Class>> m_ListClasses;

  /** the properties with the blacklisted classes. */
  protected Properties m_Blacklist;

  /** the singleton. */
  protected static ClassLister m_Singleton;

  /**
   * Initializes the classlister.
   */
  protected ClassLister() {
    super();

    m_Packages  = load("nz/ac/waikato/cms/locator/ClassLister.props");
    m_Blacklist = load("nz/ac/waikato/cms/locator/ClassLister.blacklist");
  }

  /**
   * Loads the properties from the classpath.
   *
   * @param props	the path, e.g., "nz/ac/waikato/cms/locator/ClassLister.props"
   * @return		the properties
   */
  public static Properties load(String props) {
    return load(props, new Properties());
  }

  /**
   * Loads the properties from the classpath.
   *
   * @param props	the path, e.g., "nz/ac/waikato/cms/locator/ClassLister.props"
   * @return		the properties, the default ones if failed to load
   */
  public static Properties load(String props, Properties defProps) {
    Properties		result;
    InputStream		is;

    result = new Properties(defProps);
    is     = null;
    try {
      is = ClassLoader.getSystemResourceAsStream(props);
      result.load(is);
    }
    catch (Exception e) {
      result = defProps;
    }
    finally {
      try {
	if (is != null)
	  is.close();
      }
      catch (Exception e) {
	// ignored
      }
    }

    return result;
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
   * Sets the properties with the superclass/packages relation.
   *
   * @param value	the properties
   */
  public void setPackages(Properties value) {
    m_Packages = value;
  }

  /**
   * Sets the properties with the blacklisted classes.
   *
   * @param value	the properties
   */
  public void setBlacklist(Properties value) {
    m_Blacklist = value;
  }

  /**
   * Updates cache for the superclass.
   *
   * @param cache	the cache to update
   * @param superclass	the superclass
   * @param names	the names to add
   */
  protected void updateClassnames(HashMap<String,HashSet<String>> cache, String superclass, HashSet<String> names) {
    if (!cache.containsKey(superclass))
      cache.put(superclass, names);
    else
      cache.get(superclass).addAll(names);
  }

  /**
   * Updates list for the superclass.
   *
   * @param list	the list to update
   * @param superclass	the superclass
   * @param names	the names to add
   */
  protected void updateClassnames(HashMap<String,List<String>> list, String superclass, List<String> names) {
    if (!list.containsKey(superclass)) {
      list.put(superclass, names);
    }
    else {
      for (String name: names) {
        if (!list.get(superclass).contains(name))
          list.get(superclass).add(name);
      }
    }
  }

  /**
   * Updates cache for the superclass.
   *
   * @param cache	the cache to update
   * @param superclass	the superclass
   * @param names	the names to add
   */
  protected void updateClasses(HashMap<String,HashSet<Class>> cache, String superclass, HashSet<Class> names) {
    if (!cache.containsKey(superclass))
      cache.put(superclass, names);
    else
      cache.get(superclass).addAll(names);
  }

  /**
   * Updates list for the superclass.
   *
   * @param list	the list to update
   * @param superclass	the superclass
   * @param classes	the names to add
   */
  protected void updateClasses(HashMap<String,List<Class>> list, String superclass, List<Class> classes) {
    if (!list.containsKey(superclass)) {
      list.put(superclass, classes);
    }
    else {
      for (Class cls : classes) {
        if (!list.get(superclass).contains(cls))
          list.get(superclass).add(cls);
      }
    }
  }

  /**
   * Returns the {@link ClassLocator} instance to use.
   *
   * @return		the instance
   */
  protected ClassLocator getClassLocator() {
    return ClassLocator.getSingleton();
  }

  /**
   * Adds/appends a class hierarchy.
   *
   * @param superclass	the superclass
   * @param packages	the packages
   */
  public void addHierarchy(String superclass, String[] packages) {
    List<String> 	names;
    List<Class>		classes;
    String[]		patterns;
    int			i;
    Pattern		p;

    names      = getClassLocator().findNames(superclass, packages);
    classes    = getClassLocator().findClasses(superclass, packages);
    // remove blacklisted classes
    if (m_Blacklist.containsKey(superclass)) {
      try {
        patterns = m_Blacklist.getProperty(superclass).replaceAll(" ", "").split(",");
        for (String pattern: patterns) {
          p = Pattern.compile(pattern);
          // names
          i = 0;
          while (i < names.size()) {
            if (p.matcher(names.get(i)).matches())
              names.remove(i);
            else
              i++;
          }
          // classes
          i = 0;
          while (i < classes.size()) {
            if (p.matcher(classes.get(i).getName()).matches())
              classes.remove(i);
            else
              i++;
          }
        }
      }
      catch (Exception ex) {
        getLogger().log(Level.SEVERE, "Failed to blacklist classes for superclass '" +  superclass + "':", ex);
      }
    }
    // create class list
    updateClassnames(m_CacheNames, superclass, new HashSet<>(names));
    updateClassnames(m_ListNames, superclass, new ArrayList<>(names));
    updateClasses(m_CacheClasses, superclass, new HashSet<>(classes));
    updateClasses(m_ListClasses, superclass, new ArrayList<>(classes));
  }

  /**
   * loads the props file and interpretes it.
   */
  public void initialize() {
    Enumeration		enm;
    String		superclass;
    String[]		packages;

    try {
      m_CacheNames   = new HashMap<>();
      m_ListNames    = new HashMap<>();
      m_CacheClasses = new HashMap<>();
      m_ListClasses  = new HashMap<>();

      enm = m_Packages.propertyNames();
      while (enm.hasMoreElements()) {
        superclass = (String) enm.nextElement();
        packages   = m_Packages.getProperty(superclass).replaceAll(" ", "").split(",");
        addHierarchy(superclass, packages);
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to determine packages/classes:", e);
      m_Packages = new Properties();
    }
  }

  /**
   * Returns all the classnames that were found for this superclass.
   *
   * @param superclass	the superclass to return the derived classes for
   * @return		the classnames of the derived classes
   */
  public String[] getClassnames(Class superclass) {
    List<String>	list;

    list = m_ListNames.get(superclass.getName());
    if (list == null)
      return new String[0];
    else
      return list.toArray(new String[list.size()]);
  }

  /**
   * Returns all the classs that were found for this superclass.
   *
   * @param superclass	the superclass to return the derived classes for
   * @return		the classes of the derived classes
   */
  public Class[] getClasses(Class superclass) {
    return getClasses(superclass.getName());
  }

  /**
   * Returns all the classes that were found for this superclass.
   *
   * @param superclass	the superclass to return the derived classes for
   * @return		the classes of the derived classes
   */
  public Class[] getClasses(String superclass) {
    List<Class>	list;

    list = m_ListClasses.get(superclass);
    if (list == null)
      return new Class[0];
    else
      return list.toArray(new Class[list.size()]);
  }

  /**
   * Returns the superclasses that the specified classes was listed under.
   *
   * @param cls		the class to look up its superclasses
   * @return		the superclass(es)
   */
  public String[] getSuperclasses(Class cls) {
    return getSuperclasses(cls.getName());
  }

  /**
   * Returns the superclasses that the specified classes was listed under.
   *
   * @param cls		the class to look up its superclasses
   * @return		the superclass(es)
   */
  public String[] getSuperclasses(String cls) {
    List<String>	result;

    result = new ArrayList<>();

    for (String superclass: m_CacheNames.keySet()) {
      if (m_CacheNames.get(superclass).contains(cls))
        result.add(superclass);
    }

    if (result.size() > 1)
      Collections.sort(result);

    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns the all superclasses that define class hierarchies.
   *
   * @return		the superclasses
   */
  public String[] getSuperclasses() {
    List<String>	result;

    result = new ArrayList<>(m_CacheNames.keySet());
    Collections.sort(result);

    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns all the packages that were found for this superclass.
   *
   * @param superclass	the superclass to return the packages for
   * @return		the packages
   */
  public String[] getPackages(Class superclass) {
    return getPackages(superclass.getName());
  }

  /**
   * Returns all the packages that were found for this superclass.
   *
   * @param superclass	the superclass to return the packages for
   * @return		the packages
   */
  public String[] getPackages(String superclass) {
    String	packages;

    packages = m_Packages.getProperty(superclass);
    if ((packages == null) || (packages.length() == 0))
      return new String[0];
    else
      return packages.split(",");
  }

  /**
   * Returns the superclass-packages relation.
   *
   * @return		the properties object listing the packages
   */
  public Properties getPackages() {
    return m_Packages;
  }

  /**
   * Only prints the generated props file with all the classnames, based on
   * the package names for the individual packages.
   *
   * @return		the props file with the classnames
   */
  @Override
  public String toString() {
    StringBuilder	result;
    List<String>	keys;

    result = new StringBuilder();

    keys = new ArrayList<>(m_ListNames.keySet());
    Collections.sort(keys);
    for (String key: keys) {
      result.append(key).append("\n");
      result.append(ClassUtils.flatten(m_ListNames.get(key), ",")).append("\n\n");
    }

    return result.toString();
  }

  /**
   * Returns the singleton instance of the class lister.
   *
   * @return		the singleton
   */
  public static synchronized ClassLister getSingleton() {
    if (m_Singleton == null)
      m_Singleton = new ClassLister();

    return m_Singleton;
  }
}
