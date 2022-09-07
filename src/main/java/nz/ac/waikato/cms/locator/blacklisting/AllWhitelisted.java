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
 * AllWhitelisted.java
 * Copyright (C) 2022 University of Waikato, Hamilton, New Zealand
 */

package nz.ac.waikato.cms.locator.blacklisting;

import java.io.File;

/**
 * Nothing is blacklisted.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class AllWhitelisted
  extends AbstractBlacklister {

  /**
   * Checks whether the directory is blacklisted.
   *
   * @param dir the directory to check
   * @return true if blacklisted
   */
  @Override
  public boolean isBlacklistedDir(File dir) {
    return false;
  }

  /**
   * Checks whether the file is blacklisted.
   *
   * @param file the file to check
   * @return true if blacklisted
   */
  @Override
  public boolean isBlacklistedFile(File file) {
    return false;
  }
}
