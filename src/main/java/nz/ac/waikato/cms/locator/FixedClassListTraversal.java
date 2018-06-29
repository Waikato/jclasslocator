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
 * FixedClassListTraversal.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.locator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Simple class traversal scheme that uses a predefined list of classnames.
 * Automatically skips empty strings and strings starting with {@link #COMMENT}
 * (after trimming them).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class FixedClassListTraversal
  extends AbstractClassTraversal {

  /** line comment. */
  public static final String COMMENT = "#";

  /** the list of classes to traverse. */
  protected List<String> m_Classnames;

  /**
   * Initializes the traversal with the given list.
   *
   * @param classnames	the classes to use
   */
  public FixedClassListTraversal(List<String> classnames) {
    super();
    m_Classnames = new ArrayList<>(classnames);
  }

  /**
   * Initializes the traversal with the lines read from the given input stream
   * (eg a resource stream). The caller must close the stream.
   *
   * @param inputStream the stream to read from
   */
  public FixedClassListTraversal(InputStream inputStream) {
    super();
    m_Classnames = new ArrayList<>();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith(COMMENT))
          continue;
        m_Classnames.add(line);
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to read class names from input stream!", e);
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
