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

package net.curre.jjeopardy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service with locale related utilities.
 *
 * @author Yevgeny Nyden
 */
public class LocaleService {

  /** Default locale (assume en_US). */
  public static final Locale DEFAULT_LOCALE = Locale.US;

  /** Array of available locales in the application. */
  private static final List<Locale> AVAILABLE_LOCALES;

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(LocaleService.class.getName());

  static {
    AVAILABLE_LOCALES = new ArrayList<>();
    AVAILABLE_LOCALES.add(DEFAULT_LOCALE);
    String localesString = ResourceBundle.getBundle("locales").getString("jj.additional.locales");
    for (String localeString : localesString.split("\\s*,\\s*")) {
      String[] localeParts = localeString.split("_");
      AVAILABLE_LOCALES.add(new Locale(localeParts[0], localeParts[1]));
    }
  }

  /** Ctor. */
  private LocaleService() {}

  /**
   * Gets the string resource for the given key. If any keyArgs
   * are passed, they will replace the "{0}", "{1}", etc. string
   * sequences in this resource (if they exist) in the order
   * they appear on the argument list. Note, that only maximum of
   * 10 arguments are supported (indexes 0 through 9).
   * @param key     key for a resource to fetch
   * @param keyArgs array of arguments for the resource key (optional)
   * @return string resource for the given key
   */
  public static String getString(String key, String... keyArgs) {
    Locale locale = Locale.getDefault();
    String bundleName = "messages";
    if (!locale.equals(DEFAULT_LOCALE)) {
      bundleName += "_" + locale;
    }
    String message = ResourceBundle.getBundle(bundleName).getString(key);
    if (keyArgs != null) {
      int argIndex = 0;
      StringBuilder buffer = new StringBuilder(message);
      for (int index = 0; index < keyArgs.length; ++index) {
        String currArg = "{" + index + "}";
        argIndex = buffer.indexOf(currArg, argIndex);
        if (argIndex >= 0) {
          buffer.replace(argIndex, argIndex + 3, keyArgs[index]);
        }
      }
      return buffer.toString();
    }
    return message;
  }

  /**
   * Gets all currently available locales.
   * @return all available locales
   */
  public static List<Locale> getAvailableLocales() {
    return AVAILABLE_LOCALES;
  }

  /**
   * Setter for the current locale.
   * @param localeId Identifier for the locale (Locale.toString())
   * @param showDialog true if show restart info dialog on change
   */
  public static synchronized void setCurrentLocale(String localeId, boolean showDialog) {
    try {
      Locale prevLocale = Locale.getDefault();
      Locale locale = findLocaleById(localeId);
      Locale.setDefault(locale);
      SettingsService.saveSettings();
      if (!localeId.equals(prevLocale.toString()) && showDialog) {
        UiService.getInstance().showRestartGameDialog();
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to set locale: " + localeId, e);
      System.exit(1);
    }
  }

  /**
   * Gets a <code>LocaleExt</code> object given its corresponding ID. If locale
   * is not found, default locale is returned.
   * @param localeId Locale identifier (Locale.toString())
   * @return <code>LocaleExt</code> object given its corresponding language name (case-insensitive)
   * @throws RuntimeException if locale with the given ID is not found
   */
  public static Locale findLocaleById(String localeId) {
    for (Locale locale : AVAILABLE_LOCALES) {
      if (localeId.equals(locale.toString())) {
        return locale;
      }
    }
    LOGGER.warning("Locale  \"" + localeId + "\" is not found!");
    return DEFAULT_LOCALE;
  }
}
