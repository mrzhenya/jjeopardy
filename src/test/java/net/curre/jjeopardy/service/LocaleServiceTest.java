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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import static org.junit.Assert.*;

/**
 * Tests the locale service.
 *
 * @author Yevgeny Nyden
 */
public class LocaleServiceTest {

  /** Initializes the state before each test run. */
  @Before
  public void init() {
  }

  /** Tests setCurrentLocale. */
  @Test
  public void testSetCurrentLocale() {
    LocaleService localeService = new LocaleService();
    List<Locale> locales = localeService.getAvailableLocales();
    assertNotNull("List of locales is null", locales);
    assertEquals("Wrong number of supported locales", 2, locales.size());
    assertEquals("Wrong first locale", LocaleService.DEFAULT_LOCALE, locales.get(0));

    localeService.setCurrentLocale(locales.get(1).toString(), false);
    assertEquals("Wrong locale", locales.get(1), LocaleService.getCurrentLocale());
  }

  /** Tests findLocaleById. */
  @Test
  public void testFindLocaleById() {
    assertEquals("Wrong default locale", "en_US", LocaleService.DEFAULT_LOCALE.toString());

    LocaleService localeService = new LocaleService();
    Locale locale = localeService.findLocaleById(LocaleService.DEFAULT_LOCALE.toString());
    assertNotNull("Locale is null", locale);
    assertEquals("Wrong default locale", "en_US", locale.toString());
  }

  /** Tests getString with a non-existing property. */
  @Test(expected = MissingResourceException.class)
  public void testGetStringNonExisting() {
    LocaleService.getString("not.existing.test.property");
  }

  /** Tests getString with a non-existing property. */
  @Test
  public void testGetStringValidSingleString() {
    String appName = LocaleService.getString("jj.app.name");
    assertFalse("App name should not be blank", StringUtils.isBlank(appName));
  }
}
