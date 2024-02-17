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

import org.apache.commons.lang3.RegExUtils;

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

  public static String removeExtraWhitespace(String propStr) {
    return RegExUtils.replacePattern(propStr, "\\s\\s+", " ");
  }
}
