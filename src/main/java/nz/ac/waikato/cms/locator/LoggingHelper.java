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
 * LoggingHelper.java
 * Copyright (C) 2013-2017 University of Waikato, Hamilton, New Zealand
 */
package nz.ac.waikato.cms.locator;

import java.util.logging.Level;

/**
 * Helper class for logging related stuff.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 13987 $
 */
public class LoggingHelper {

  /** the environment variable suffix of the log level to look for. */
  public final static String LOGLEVEL_SUFFIX = ".LOGLEVEL";

  /**
   * Returns the log level for the specified class. E.g., for the class
   * "hello.world.App" the environment variable "hello.world.App.LOGLEVEL"
   * is inspected and "{OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST}"
   * returned. Default is WARNING.
   *
   * @param cls		the class to return the debug level for
   * @return		the logging level
   */
  public static Level getLevel(Class cls) {
    return getLevel(cls, Level.WARNING);
  }

  /**
   * Returns the log level for the specified class. E.g., for the class
   * "hello.world.App" the environment variable "hello.world.App.LOGLEVEL"
   * is inspected and "{OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST}" 
   * returned. Default is WARNING.
   *
   * @param cls		the class to return the debug level for
   * @param defLevel	the default level to use
   * @return		the logging level
   */
  public static Level getLevel(Class cls, Level defLevel) {
    Level	result;
    String	levelStr;

    result = defLevel;

    levelStr = System.getenv(cls.getName() + LOGLEVEL_SUFFIX);
    if (levelStr != null) {
      switch (levelStr) {
	case "ALL":
	  return Level.ALL;
	case "OFF":
	  return Level.OFF;
	case "INFO":
	  return Level.INFO;
	case "CONFIG":
	  return Level.CONFIG;
	case "FINE":
	  return Level.FINE;
	case "FINER":
	  return Level.FINER;
	case "FINEST":
	  return Level.FINEST;
	case "WARNING":
	  return Level.WARNING;
	case "SEVERE":
	  return Level.SEVERE;
      }
    }

    return result;
  }
}
