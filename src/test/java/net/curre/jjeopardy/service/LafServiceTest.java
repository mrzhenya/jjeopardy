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

import net.curre.jjeopardy.ui.laf.LafThemeId;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

/**
 * Tests for the LAF service.
 *
 * @author Yevgeny Nyden
 */
public class LafServiceTest {

  /** Laf service in test. */
  private LafService testLafService;

  /**
   * Initializes the state before each test run.
   */
  @Before
  public void init() {
    this.testLafService = new LafService();
  }

  /**
   * Tests initialization of the default object state.
   */
  @Test
  public void testDefault() {
    ArrayList<LafTheme> themes = this.testLafService.getSupportedThemes();
    assertNotNull("Null LAF themes list", themes);
    assertTrue("Should be at least 3 supported themes", themes.size() >= 3);

    LafTheme lafTheme = this.testLafService.getCurrentLafTheme();
    assertNotNull("Current theme is null", lafTheme);
    LafThemeId lafThemeId = this.testLafService.getCurrentLafThemeId();
    assertNotNull("Current theme Id is null", lafThemeId);
    assertEquals("Theme and theme id don't match", lafTheme.getId(), lafThemeId);
  }

  /**
   * Tests activateLafTheme.
   */
  @Test
  public void testActivateLafTheme() {
    ArrayList<LafTheme> themes = this.testLafService.getSupportedThemes();
    assertNotNull("Null LAF themes list", themes);
    assertTrue("Should be at least 3 supported themes", themes.size() >= 3);
    final LafTheme newTheme = themes.get(1);

    Window windowMock = Mockito.mock(Window.class, "windowMock");
    this.testLafService.registerUITreeForUpdates(windowMock);
    try (MockedStatic<SwingUtilities> seqGeneratorMock = mockStatic(SwingUtilities.class, Mockito.CALLS_REAL_METHODS)) {

      this.testLafService.activateLafTheme(newTheme.getId());

      seqGeneratorMock.verify(() -> SwingUtilities.updateComponentTreeUI(windowMock));
      verify(windowMock).pack();
    }

    assertEquals("Wrong current theme", newTheme, this.testLafService.getCurrentLafTheme());
    assertEquals("Wrong current theme id", newTheme.getId(), this.testLafService.getCurrentLafThemeId());
  }
}
