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
 * ClassCache.java
 * Copyright (C) 2010-2018 University of Waikato, Hamilton, New Zealand
 */
package nz.ac.waikato.cms.locator;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * For traversing the classpath.
 * <br>
 * Use "nz.ac.waikato.cms.locator.ClassPathTraversal.LOGLEVEL" with a value of
 * "{OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST}" to set custom
 * logging level.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ClassPathTraversal
  extends AbstractClassTraversal {

  /** for serialization. */
  private static final long serialVersionUID = -2973185784363491578L;

  /**
   * For filtering classes.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   */
  public static class ClassFileFilter
    implements FileFilter {

    /**
     * Checks whether the file is a class.
     *
     * @param pathname	the file to check
     * @return		true if a class file
     */
    public boolean accept(File pathname) {
      return pathname.getName().endsWith(".class");
    }
  }

  /**
   * For filtering classes.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   */
  public static class DirectoryFilter
    implements FileFilter {

    /**
     * Checks whether the file is a directory.
     *
     * @param pathname	the file to check
     * @return		true if a directory
     */
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  }

  /**
   * Container class for maintaining the state while traversing.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   */
  public static class TraversalState {

    /** the current URL. */
    protected URL m_URL;

    /** the traversal listener. */
    protected TraversalListener m_Listener;

    /**
     * Initializes the state with the listener.
     *
     * @param listener	the listener to use
     */
    public TraversalState(TraversalListener listener) {
      m_URL      = null;
      m_Listener = listener;
    }

    /**
     * Sets the current URL.
     *
     * @param value	the URL
     */
    public void setURL(URL value) {
      m_URL = value;
    }

    /**
     * Returns the current URL.
     *
     * @return		the URL
     */
    public URL getURL() {
      return m_URL;
    }

    /**
     * Returns the traversal listener.
     *
     * @return		the listener
     */
    public TraversalListener getListener() {
      return m_Listener;
    }
  }

  /**
   * Traverses the class, calls the listener available through the state.
   *
   * @param classname	the classname, automatically removes ".class" and
   * 			turns "/" or "\" into "."
   * @param state 	the traversal state
   */
  public void traverse(String classname, TraversalState state) {
    classname = cleanUp(classname);
    state.getListener().traversing(classname, state.getURL());
  }

  /**
   * Fills the class cache with classes in the specified directory.
   *
   * @param prefix	the package prefix so far, null for default package
   * @param dir		the directory to search
   * @param state 	the traversal state
   */
  protected void traverseDir(String prefix, File dir, TraversalState state) {
    File[]	files;

    // check classes
    files = dir.listFiles(new ClassFileFilter());
    if (files != null) {
      for (File file : files) {
	if (prefix == null)
	  traverse(file.getName(), state);
	else
	  traverse(prefix + "." + file.getName(), state);
      }
    }

    // descend in directories
    files = dir.listFiles(new DirectoryFilter());
    if (files != null) {
      for (File file : files) {
	if (prefix == null)
	  traverseDir(file.getName(), file, state);
	else
	  traverseDir(prefix + "." + file.getName(), file, state);
      }
    }
  }

  /**
   * Fills the class cache with classes in the specified directory.
   *
   * @param dir		the directory to search
   * @param state 	the traversal state
   */
  protected void traverseDir(File dir, TraversalState state) {
    if (isLoggingEnabled())
      getLogger().log(Level.INFO, "Analyzing directory: " + dir);
    traverseDir(null, dir, state);
  }

  /**
   * Analyzes the MANIFEST.MF file of a jar whether additional jars are
   * listed in the "Class-Path" key.
   * 
   * @param manifest	the manifest to analyze
   * @param state 	the traversal state
   */
  protected void traverseManifest(Manifest manifest, TraversalState state) {
    Attributes	atts;
    String	cp;
    String[]	parts;

    if (manifest == null)
      return;

    atts = manifest.getMainAttributes();
    cp   = atts.getValue("Class-Path");
    if (cp == null)
      return;
    
    parts = cp.split(" ");
    for (String part: parts) {
      if (part.trim().length() == 0)
	return;
      if (part.toLowerCase().endsWith(".jar") || !part.equals("."))
        traverseClasspathPart(part, state);
    }
  }
  
  /**
   * Fills the class cache with classes from the specified jar.
   *
   * @param file		the jar to inspect
   * @param state 	the traversal state
   */
  protected void traverseJar(File file, TraversalState state) {
    JarFile		jar;
    JarEntry		entry;
    Enumeration		enm;

    if (isLoggingEnabled())
      getLogger().log(Level.INFO, "Analyzing jar: " + file);

    if (!file.exists()) {
      getLogger().log(Level.WARNING, "Jar does not exist: " + file);
      return;
    }
    
    try {
      jar = new JarFile(file);
      enm = jar.entries();
      while (enm.hasMoreElements()) {
        entry = (JarEntry) enm.nextElement();
        if (entry.getName().endsWith(".class"))
          traverse(entry.getName(), state);
      }
      traverseManifest(jar.getManifest(), state);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to inspect: " + file, e);
    }
  }

  /**
   * Analyzes a part of the classpath.
   * 
   * @param part	the part to analyze
   * @param state 	the traversal state
   */
  protected void traverseClasspathPart(String part, TraversalState state) {
    File		file;

    file = null;
    if (part.startsWith("file:")) {
      part = part.replace(" ", "%20");
      try {
	file = new File(new java.net.URI(part));
      }
      catch (URISyntaxException e) {
	getLogger().log(Level.SEVERE, "Failed to generate URI: " + part, e);
      }
    }
    else {
      file = new File(part);
    }
    if (file == null) {
      if (isLoggingEnabled())
	getLogger().log(Level.INFO, "Skipping: " + part);
      return;
    }

    // find classes
    if (file.isDirectory())
      traverseDir(file, state);
    else if (file.exists())
      traverseJar(file, state);
  }
  
  /**
   * Traverses the classpath.
   *
   * @param listener 	the listener to use
   */
  public void traverse(TraversalListener listener) {
    String		part;
    URLClassLoader 	sysLoader;
    List<URL> 		urls;
    TraversalState	state;
    String[] 		parts;
    List<String> 	elements;
    File		file;
    File[]		jars;

    // determine URLs of classpath
    if (JavaVersion.atLeast9()) {
      parts = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
      elements = new ArrayList<>();
      for (String p: parts) {
        if (p.endsWith("*")) {
          file = new File(p.substring(0, p.length() - 1));
          jars = file.listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File pathname) {
	      return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".jar");
	    }
	  });
          if (jars != null) {
            for (File jar : jars)
              elements.add(jar.getAbsolutePath());
          }
	}
	else {
          elements.add(p);
	}
      }
      urls = new ArrayList<>();
      for (String el: elements) {
        try {
          file = new File(el);
          urls.add(file.toURI().toURL());
	}
	catch (Exception e) {
          System.err.println("[" + getClass().getName() + "] Failed to turn '" + el + "' into URL: " + e);
	}
      }
    }
    else {
      sysLoader = (URLClassLoader) getClass().getClassLoader();
      urls = new ArrayList<>(Arrays.asList(sysLoader.getURLs()));
    }

    state = new TraversalState(listener);
    for (URL url : urls) {
      state.setURL(url);
      if (isLoggingEnabled())
	getLogger().log(Level.INFO, "Classpath URL: " + url);
      part = url.toString();
      traverseClasspathPart(part, state);
    }
  }
}
