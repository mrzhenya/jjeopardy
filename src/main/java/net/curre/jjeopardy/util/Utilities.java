/*
 * Copyright 2024 Yevgeny Nyden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.curre.jjeopardy.util;

import net.curre.jjeopardy.service.ServiceException;
import org.apache.commons.lang3.RegExUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Properties;

/**
 * A set of common utilities.
 *
 * @author Yevgeny Nyden
 */
public class Utilities {

  /** Enumeration tha represents a platform/os type. */
  public enum PlatformType {
    MAC_OS, LINUX, WINDOWS, UNKNOWN
  }

  /**
   * Determines if we are running on Mac OS X.
   * @return True if we are on Mac OS; false otherwise
   */
  public static boolean isMacOs() {
    return getPlatformType() == PlatformType.MAC_OS;
  }

  /**
   * Determines the platform/os type we are running on.
   * @return A PlatformType enumeration that represents the platform/os
   */
  public static PlatformType getPlatformType() {
    if (System.getProperty("mrj.version") == null) {
      String osProp = System.getProperty("os.name").toLowerCase();
      if (osProp.startsWith("windows")) {
        return PlatformType.WINDOWS;
      } else if (osProp.startsWith("mac")) {
        return PlatformType.MAC_OS;
      } else if (osProp.startsWith("linux")) {
        return PlatformType.LINUX;
      } else {
        return PlatformType.UNKNOWN;
      }
    }
    return PlatformType.MAC_OS;
  }

  /**
   * Fetches a string property value (with all end whitespace removed) from the given property object.
   * @param props    property object to use
   * @param propName property name
   * @return property string value or null if property with a given name is not found
   */
  public static @Nullable String getPropertyOrNull(Properties props, String propName) {
    try {
      return removeEndsWhitespace(getProperty(props, propName));
    } catch (ServiceException e) {
      return null;
    }
  }

  /**
   * Fetches a string property value (with all end whitespace removed) from the given property object.
   * @param props    property object to use
   * @param propName property name
   * @return non-null property string value with all end whitespace removed
   * @throws ServiceException if the property is not present
   */
  public static String getProperty(@NotNull Properties props, String propName) throws ServiceException {
    final String propStr = props.getProperty(propName);
    if (propStr == null) {
      throw new ServiceException("String property \"" + propName + "\" is not found!");
    }
    return removeEndsWhitespace(propStr);
  }

  /**
   * Fetches a property from the given property object
   * and returns its int value.
   * @param props    property object to use
   * @param propName property name
   * @return property int value
   * @throws ServiceException if the property is not present or does not represent an integer
   */
  public static int getIntProperty(@NotNull Properties props, String propName) throws ServiceException {
    final String propStr = props.getProperty(propName);
    if (propStr == null) {
      throw new ServiceException("Int property \"" + propName + "\" is not found!");
    }
    try {
      return Integer.parseInt(propStr.trim());
    } catch (NumberFormatException e) {
      throw new ServiceException("Int property \"" + propName + "\" is not an integer!");
    }
  }

  /**
   * Removes extra leading and trailing whitespace.
   * @param propStr property string to clean
   * @return a new string w/o leading and trailing whitespace
   */
  public static String removeEndsWhitespace(String propStr) {
    return RegExUtils.replacePattern(propStr, "(^\\h*\\n?\\h*)|(\\h*\\n?\\h*$)", "");
  }
}
