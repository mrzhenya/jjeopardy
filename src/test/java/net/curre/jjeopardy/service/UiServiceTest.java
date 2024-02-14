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

import net.curre.jjeopardy.ui.dialog.ConfirmDialog;
import net.curre.jjeopardy.ui.dialog.ParsingResultDialog;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

/**
 * Tests for the UI service.
 *
 * @author Yevgeny Nyden
 */
public class UiServiceTest {

  /** Reference to the UI service in test. */
  private UiService testUiService;

  /**
   * Initializes the state before each test run.
   */
  @Before
  public void init() {
    this.testUiService = new UiService();
  }

  /** Tests showInfoDialog. */
  @Test
  public void testShowInfoDialog() {
    try(MockedConstruction<ConfirmDialog> mockConfirmDialog = Mockito.mockConstruction(
        ConfirmDialog.class, (mock, context) -> {
          assertEquals("Wrong number of arguments", 3, context.arguments().size());
          assertEquals("Wrong title", "TestTitle", context.arguments().get(0));
          assertEquals("Wrong message", "TestMessage", context.arguments().get(1));
          assertEquals("Wrong dialog type", ConfirmDialog.Type.INFO, context.arguments().get(2));
        })) {

      this.testUiService.showInfoDialog("TestTitle", "TestMessage", null);
      assertEquals(1, mockConfirmDialog.constructed().size());
      verify(mockConfirmDialog.constructed().get(0)).showDialog(null);
    }
  }

  /** Tests showWarningDialog. */
  @Test
  public void testShowWarningDialog() {
    try(MockedConstruction<ConfirmDialog> mockConfirmDialog = Mockito.mockConstruction(
        ConfirmDialog.class, (mock, context) -> {
          assertEquals("Wrong number of arguments", 3, context.arguments().size());
          assertEquals("Wrong title", "TestTitle", context.arguments().get(0));
          assertEquals("Wrong message", "TestMessage", context.arguments().get(1));
          assertEquals("Wrong dialog type", ConfirmDialog.Type.WARNING, context.arguments().get(2));
        })) {

      this.testUiService.showWarningDialog("TestTitle", "TestMessage", null);
      assertEquals(1, mockConfirmDialog.constructed().size());
      verify(mockConfirmDialog.constructed().get(0)).showDialog(null);
    }
  }

  /** Tests showEndGameDialog. */
  @Test
  public void testShowEndGameDialog() {
    try(MockedConstruction<ConfirmDialog> mockConfirmDialog = Mockito.mockConstruction(
        ConfirmDialog.class, (mock, context) -> {
          assertEquals("Wrong number of arguments", 3, context.arguments().size());
          assertEquals("Wrong title", "TestTitle", context.arguments().get(0));
          assertEquals("Wrong message", "TestMessage", context.arguments().get(1));
          assertEquals("Wrong dialog type", ConfirmDialog.Type.END, context.arguments().get(2));
        })) {

      this.testUiService.showEndGameDialog("TestTitle", "TestMessage");
      assertEquals(1, mockConfirmDialog.constructed().size());
      verify(mockConfirmDialog.constructed().get(0)).showDialog(null);
    }
  }

  /** Tests showRestartGameDialog. */
  @Test
  public void testShowRestartGameDialog() {
    try(MockedConstruction<ConfirmDialog> mockConfirmDialog = Mockito.mockConstruction(
        ConfirmDialog.class, (mock, context) -> {
          assertEquals("Wrong number of arguments", 3, context.arguments().size());
          assertFalse("Title is blank", StringUtils.isBlank((String) context.arguments().get(0)));
          assertFalse("Message is blank", StringUtils.isBlank((String) context.arguments().get(1)));
          assertEquals("Wrong dialog type", ConfirmDialog.Type.INFO, context.arguments().get(2));
        })) {

      this.testUiService.showRestartGameDialog();
      assertEquals(1, mockConfirmDialog.constructed().size());
      verify(mockConfirmDialog.constructed().get(0)).showDialog(null);
    }
  }

  /** Tests showParsingResult. */
  @Test
  public void testShowParsingResult() {
    final FileParsingResult result = new FileParsingResult("testName");
    try(MockedConstruction<ParsingResultDialog> mockDialog = Mockito.mockConstruction(
        ParsingResultDialog.class, (mock, context) -> {
          assertEquals("Wrong number of arguments", 1, context.arguments().size());
          assertEquals("Wrong result", result, context.arguments().get(0));
        })) {

      this.testUiService.showParsingResult(result, null);
      assertEquals(1, mockDialog.constructed().size());
      verify(mockDialog.constructed().get(0)).showDialog(null);
    }
  }
}
