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

package net.curre.jjeopardy.bean;

import net.curre.jjeopardy.ui.laf.LafThemeId;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the Settings object methods.
 * @author Yevgeny Nyden
 */
public class SettingsTest {

  /** Tests initialization of the default object state. */
  @Test
  public void testDefault() {
    Settings settings = new Settings();
    assertTrue("Game window width should be > 0", settings.getGameWindowWidth() > 0);
    assertTrue("Game window height should be > 0", settings.getGameWindowHeight() > 0);
    assertNotNull("Laf theme should not be null", settings.getLafThemeId());
    assertFalse("Sound FX only setting is wrong", settings.isSoundEffectsOnly());
    assertFalse("All sound Off setting is wrong", settings.isAllSoundOff());
    assertNotNull("Locale id should not be null", settings.getLocaleId());
    assertFalse("Last current directory should not be blank", StringUtils.isBlank(settings.getLastCurrentDirectory()));
  }

  /** Tests the sound related methods. */
  @Test
  public void testSoundMethods() {
    Settings settings = new Settings();
    assertSoundSettings(settings, false, false, true);

    settings.enableAllSound();
    assertSoundSettings(settings, false, false, true);

    settings.disableAllSound();
    assertSoundSettings(settings, false, true, false);

    settings.enableSoundEffectsOnly();
    assertSoundSettings(settings, true, false, false);

    settings.disableAllSound();
    assertSoundSettings(settings, false, true, false);
  }

  /** Tests various setters and getters. */
  @Test
  public void testSettersAndGetters() {
    Settings settings = new Settings();
    settings.setGameWindowWidth(333);
    assertEquals("Game window width", 333, settings.getGameWindowWidth());

    settings.setGameWindowHeight(444);
    assertEquals("Game window height", 444, settings.getGameWindowHeight());

    settings.setLafThemeId(LafThemeId.NIMBUS);
    assertEquals("LAF theme id", LafThemeId.NIMBUS, settings.getLafThemeId());

    settings.setLastCurrentDirectory("testDirectory");
    assertEquals("Last current directory", "testDirectory", settings.getLastCurrentDirectory());
  }

  /**
   * Asserts sound settings.
   * @param settings settings object to test
   * @param fxOnly FX only value
   * @param allOff All off value
   * @param allOn All on value
   */
  private void assertSoundSettings(Settings settings, boolean fxOnly, boolean allOff, boolean allOn) {
    assertEquals("Sound FX only setting is wrong", fxOnly, settings.isSoundEffectsOnly());
    assertEquals("All sound Off setting is wrong", allOff, settings.isAllSoundOff());
    assertEquals("All sound On setting is wrong", allOn, settings.isAllSoundOn());
  }
}
