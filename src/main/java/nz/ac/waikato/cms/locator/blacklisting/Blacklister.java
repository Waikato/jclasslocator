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
 * Blacklister.java
 * Copyright (C) 2022 University of Waikato, Hamilton, New Zealand
 */

package nz.ac.waikato.cms.locator.blacklisting;

import java.io.File;
import java.io.Serializable;

/**
 * Interface for classes that check whether dirs/files are blacklisted.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public interface Blacklister
  extends Serializable {

  /**
   * Checks whether the directory is blacklisted.
   *
   * @param dir		the directory to check
   * @return		true if blacklisted
   */
  public boolean isBlacklistedDir(File dir);

  /**
   * Checks whether the file is blacklisted.
   *
   * @param file	the file to check
   * @return		true if blacklisted
   */
  public boolean isBlacklistedFile(File file);
}
