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
 * TraversalListener.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.locator;

import java.net.URL;

/**
 * Interface for classes that listen to the traversal of the classes.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public interface TraversalListener {

  /**
   * Gets called when a class is being traversed.
   *
   * @param classname		the current classname
   * @param classPathPart	the current classpath part this classname is
   *                            located in, can be null
   */
  public void traversing(String classname, URL classPathPart);
}
