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
 * PropertiesBasedClassListTraversal.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.locator;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Initializes from a properties file. Expects each key in the properties
 * to contain a comma-separated list of class names.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PropertiesBasedClassListTraversal
  extends AbstractClassTraversal {

  /** the list of classes to traverse. */
  protected List<String> m_Classnames;

  /**
   * Initializes the traversal with the classname lists (comma-separated lists)
   * stored in each property.
   *
   * @param props the properties to use
   */
  public PropertiesBasedClassListTraversal(Properties props) {
    super();
    m_Classnames = new ArrayList<>();
    Enumeration<?> keys = props.propertyNames();
    while (keys.hasMoreElements()) {
      String key = "" + keys.nextElement();
      String list = props.getProperty(key, "");
      if (!list.isEmpty()) {
        try {
          for (String item: list.split(",")) {
            item = item.trim();
            if (!item.isEmpty())
              m_Classnames.add(item);
          }
        }
        catch (Exception e) {
          getLogger().log(Level.SEVERE, "Failed to process class names from key: " + key, e);
        }
      }
    }
  }

  /**
   * Traverses the classpath.
   *
   * @param listener 	the listener to use
   */
  @Override
  public void traverse(TraversalListener listener) {
    for (String classname: m_Classnames)
      listener.traversing(classname, null);
  }
}
