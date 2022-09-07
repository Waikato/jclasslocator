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
 * SimpleBlacklister.java
 * Copyright (C) 2022 University of Waikato, Hamilton, New Zealand
 */

package nz.ac.waikato.cms.locator.blacklisting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Can blacklist absolute dirs and file names (without path).
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class SimpleBlacklister
    extends AbstractBlacklister {

  /** the blacklisted dirs. */
  protected Set<File> m_BlacklistedDirs;

  /** the blacklisted files. */
  protected Set<String> m_BlacklistedFiles;

  /** the blacklisted file patterns. */
  protected Set<String> m_BlacklistedFilePatterns;

  /** the blacklisted file patterns. */
  protected transient List<Pattern> m_BlacklistedFilePatternsCompiled;

  /**
   * For initializing members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_BlacklistedDirs     = new HashSet<>();
    m_BlacklistedFiles    = new HashSet<>();
    m_BlacklistedFilePatterns = new HashSet<>();
  }

  /**
   * Blacklists the directory.
   *
   * @param dir		the directory to blacklist
   */
  public void blacklistDir(File dir) {
    m_BlacklistedDirs.add(dir.getAbsoluteFile());
  }

  /**
   * Returns the blacklisted directories.
   *
   * @return		the directories
   */
  public File[] blacklistedDirs() {
    return m_BlacklistedDirs.toArray(new File[0]);
  }

  /**
   * Blacklists the file.
   *
   * @param file	the file to blacklist (no path)
   */
  public void blacklistFile(String file) {
    m_BlacklistedFiles.add(file);
  }

  /**
   * Returns the blacklisted files.
   *
   * @return		the files
   */
  public String[] blacklistedFiles() {
    return m_BlacklistedFiles.toArray(new String[0]);
  }

  /**
   * Blacklists the file pattern.
   *
   * @param pattern	the regexp to use for blacklisting file names (without path)
   */
  public void blacklistFilePattern(String pattern) {
    m_BlacklistedFilePatterns.add(pattern);
    m_BlacklistedFilePatternsCompiled = null;
  }

  /**
   * Returns the blacklisted file patterns.
   *
   * @return		the files
   */
  public String[] blacklistedFilePatterns() {
    return m_BlacklistedFilePatterns.toArray(new String[0]);
  }

  /**
   * Checks whether the directory is blacklisted.
   *
   * @param dir the directory to check
   * @return true if blacklisted
   */
  @Override
  public boolean isBlacklistedDir(File dir) {
    return m_BlacklistedDirs.contains(dir.getAbsoluteFile());
  }

  /**
   * Checks whether the file is blacklisted (excluding path).
   *
   * @param file the file to check
   * @return true if blacklisted
   */
  @Override
  public synchronized boolean isBlacklistedFile(File file) {
    boolean 	result;
    String	name;

    name   = file.getName();
    result = m_BlacklistedFiles.contains(name);

    if (!result) {
      // init patterns?
      if (m_BlacklistedFilePatternsCompiled == null) {
	m_BlacklistedFilePatternsCompiled = new ArrayList<>();
	for (String pattern: m_BlacklistedFilePatterns) {
	  try {
	    m_BlacklistedFilePatternsCompiled.add(Pattern.compile(pattern));
	  }
	  catch (Exception e) {
	    System.err.println(getClass().getName() + ": Failed to compile file pattern '" + pattern + "'!");
	    e.printStackTrace();
	  }
	}
      }

      for (Pattern pattern: m_BlacklistedFilePatternsCompiled) {
        if (pattern.matcher(name).matches()) {
          result = true;
          break;
	}
      }
    }

    return result;
  }
}
