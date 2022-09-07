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
 * AbstractClassTraversal.java
 * Copyright (C) 2010-2022 University of Waikato, Hamilton, New Zealand
 */
package nz.ac.waikato.cms.locator;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Ancestor for class traversal schemes.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractClassTraversal
  implements Serializable, ClassTraversal {

  /** for serialization. */
  private static final long serialVersionUID = -2973185784363491578L;

  /**
   * Extracts the package name from the (clean) classname.
   *
   * @param classname	the classname to extract the package from
   * @return		the package name
   */
  public static String extractPackage(String classname) {
    if (classname.contains("."))
      return classname.substring(0, classname.lastIndexOf("."));
    else
      return DEFAULT_PACKAGE;
  }

  /**
   * Fixes the classname, turns "/" and "\" into "." and removes ".class".
   *
   * @param classname	the classname to process
   * @return		the processed classname
   */
  public static String cleanUp(String classname) {
    String	result;

    result = classname;

    if (result.contains("/"))
      result = result.replace("/", ".");
    if (result.contains("\\"))
      result = result.replace("\\", ".");
    if (result.endsWith(".class"))
      result = result.substring(0, result.length() - 6);

    return result;
  }

  /** the key for the default package. */
  public final static String DEFAULT_PACKAGE = "DEFAULT";

  /** the logger in use. */
  protected transient Logger m_Logger;

  /**
   * Default constructor.
   *
   * @see #initialize()
   */
  protected AbstractClassTraversal() {
    initialize();
  }

  /**
   * For initializing the traversal.
   * <br>
   * Default implementation does nothing.
   */
  protected void initialize() {
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
   * Traverses the classpath.
   *
   * @param listener 	the listener to use
   */
  @Override
  public abstract void traverse(TraversalListener listener);
}
