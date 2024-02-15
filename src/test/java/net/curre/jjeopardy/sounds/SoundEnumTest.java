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

package net.curre.jjeopardy.sounds;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Yevgeny Nyden
 */
public class SoundEnumTest {

  /** Initializes the state before each test run. */
  @Before
  public void init() {
  }

  /** Verifies that all sound enums have valid corresponding sound files. */
  @Test
  public void testAllSoundEnumsHaveValidFiles() {
    for (SoundEnum sound : SoundEnum.values()) {
      assertSoundEnumNotBlank(sound);
    }
  }

  /** Tests getRandomBoo. */
  @Test
  public void testGetRandomBoo() {
    assertSoundEnumNotBlank(SoundEnum.getRandomBoo());
    assertSoundEnumNotBlank(SoundEnum.getRandomBoo());
    assertSoundEnumNotBlank(SoundEnum.getRandomBoo());
  }

  /** Tests getRandomHooray. */
  @Test
  public void testGetRandomHooray() {
    assertSoundEnumNotBlank(SoundEnum.getRandomHooray());
    assertSoundEnumNotBlank(SoundEnum.getRandomHooray());
    assertSoundEnumNotBlank(SoundEnum.getRandomHooray());
  }

  /**
   * Asserts a sound enum has valid data.
   * @param sound sound enum to validate
   */
  private static void assertSoundEnumNotBlank(SoundEnum sound) {
    assertNotNull("Sound enum reference is null", sound);
    assertFalse("Sound enum toString is blank", StringUtils.isBlank(sound.toString()));
    InputStream stream = sound.getSoundFileStream();
    assertNotNull("Unable to find sound file for enum: " + sound, stream);
  }
}
