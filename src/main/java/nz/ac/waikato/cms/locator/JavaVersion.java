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
 * JavaVersion.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package nz.ac.waikato.cms.locator;

/**
 * Helper class for the Java versions.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class JavaVersion {

  /**
   * Checks whether the Java version is at least 9.
   *
   * @return		true if 9 or higher
   */
  public static boolean atLeast9() {
    return (Double.parseDouble(System.getProperty("java.vm.specification.version")) >= 1.9);
  }
}
