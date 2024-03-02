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

import net.curre.jjeopardy.bean.FileParsingResult;
import net.curre.jjeopardy.bean.GameData;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static net.curre.jjeopardy.service.LocaleService.DEFAULT_LOCALE;
import static org.junit.Assert.*;

/**
 * Tests the FileParsingResult class.
 *
 * @author Yevgeny Nyden
 */
public class FileParsingResultTest {

  /** Default test file name. */
  private static final String TEST_FILE_NAME = "BooTest";

  /** Reference to a result object that's reinitialized for each test run. */
  private FileParsingResult testResult;

  /** Initializes the state before each test run. */
  @Before
  public void init() {
    this.testResult = new FileParsingResult(TEST_FILE_NAME);
    Locale.setDefault(DEFAULT_LOCALE);
  }

  /** Tests initialization of the default object state. */
  @Test
  public void testDefault() {
    assertEquals("Wrong file path", TEST_FILE_NAME, this.testResult.getFileOrBundlePath());
    assertNotNull(this.testResult.getInfoMessages());
    assertEquals("Wrong initial size of info list", 0, this.testResult.getInfoMessages().size());
    assertNotNull(this.testResult.getWarningMessages());
    assertEquals("Wrong initial size of warn list", 0, this.testResult.getWarningMessages().size());
    assertNotNull(this.testResult.getErrorMessages());
    assertEquals("Wrong initial size of error list", 0, this.testResult.getErrorMessages().size());
    assertFalse("Short title is blank", StringUtils.isBlank(this.testResult.getResulTitleShort()));
    assertFalse("Long title is blank", StringUtils.isBlank(this.testResult.getResulTitleLong()));
  }

  /** Tests getFileOrBundlePath. */
  @Test
  public void testGetFileOrBundlePath() {
    assertEquals("Wrong file path", TEST_FILE_NAME, this.testResult.getFileOrBundlePath());
    GameData gameData = new GameData(TEST_FILE_NAME, "testBundlePath");
    this.testResult.setGameData(gameData);
    assertEquals("Wrong file path", "testBundlePath", this.testResult.getFileOrBundlePath());
  }

  /** Tests the message enums. */
  @Test
  public void testMessageEnums() {
    LocaleService localeService = new LocaleService();
    List<Locale> locales = localeService.getAvailableLocales();
    assertNotNull("List of locales is null", locales);
    for (Locale locale : locales) {
      Locale.setDefault(locale);
      assertResultMessagesSet();
    }
  }

  /** Tests adding info messages. */
  @Test
  public void testAddingInfoMessages() {
    assertNotNull(this.testResult.getInfoMessages());
    assertEquals("Wrong initial size of info list", 0, this.testResult.getInfoMessages().size());
    this.testResult.addInfoMessage(FileParsingResult.Message.MSG_QUESTIONS_PARSED);
    assertEquals("Wrong size of info list", 1, this.testResult.getInfoMessages().size());
    assertFalse("Info message should not be blank",
        StringUtils.isBlank(this.testResult.getInfoMessages().get(0)));
  }

  /** Tests adding warning messages. */
  @Test
  public void testAddingWarnMessages() {
    assertNotNull(this.testResult.getWarningMessages());
    assertEquals("Wrong initial size of warn list", 0, this.testResult.getWarningMessages().size());
    this.testResult.addWarningMessage(FileParsingResult.Message.MSG_TOO_FEW_PLAYERS);
    assertEquals("Wrong size of warn list", 1, this.testResult.getWarningMessages().size());
    assertFalse("Warn message should not be blank",
        StringUtils.isBlank(this.testResult.getWarningMessages().get(0)));
  }

  /** Tests adding error messages. */
  @Test
  public void testAddingErrorMessages() {
    assertNotNull(this.testResult.getErrorMessages());
    assertEquals("Wrong initial size of error list", 0, this.testResult.getErrorMessages().size());
    this.testResult.addErrorMessage(FileParsingResult.Message.MSG_NO_QUESTIONS);
    assertEquals("Wrong size of error list", 1, this.testResult.getErrorMessages().size());
    assertFalse("Error message should not be blank",
        StringUtils.isBlank(this.testResult.getErrorMessages().get(0)));
  }

  /**
   * Asserts all Message enums are valid (have corresponding properties for the current locale).
   */
  private static void assertResultMessagesSet() {
    Locale locale = Locale.getDefault();
    for (FileParsingResult.Message message : FileParsingResult.Message.values()) {
      String propName = message.getPropertyName();
      assertFalse("Property name for " + message + " should not be blank", StringUtils.isBlank(propName));
      assertFalse("Message property " + propName + " is not defined for locale " + locale,
          StringUtils.isBlank(LocaleService.getString(propName)));
    }
  }
}
