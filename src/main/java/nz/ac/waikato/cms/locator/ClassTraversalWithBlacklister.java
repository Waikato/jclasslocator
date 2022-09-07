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
 * ClassTraversalWithBlacklister.java
 * Copyright (C) 2022 University of Waikato, Hamilton, New Zealand
 */

package nz.ac.waikato.cms.locator;

import nz.ac.waikato.cms.locator.blacklisting.Blacklister;

/**
 * Interface for class traversal schemes that support blacklisting.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public interface ClassTraversalWithBlacklister
  extends ClassTraversal {

  /**
   * Sets the blacklister to use.
   *
   * @param value	the blacklister
   */
  public void setBlacklister(Blacklister value);

  /**
   * Returns the current blacklister.
   *
   * @return		the blacklister
   */
  public Blacklister getBlacklister();
}
