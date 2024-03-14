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

import java.io.InputStream;

/**
 * Represents various sound files used by the app.
 * The sounds files are expected to be in a resource directory under
 * net/curre/jjeopardy/sounds/. You can load them using the #getSoundFileStream
 * method on this enum.
 *
 * @see net.curre.jjeopardy.service.SoundService
 * @author Yevgeny Nyden
 */
public enum SoundEnum {

  /** Music for the opening of the game (app start). */
  OPENING("opening.wav", false),

  /** Question time is up music. */
  TIMES_UP("times_up.wav", true),

  /** Thinking music. */
  THINKING("short_dodododo.wav", false),

  /** Wrong answer sound. */
  BOO_0("boo.wav", true),

  /** Wrong answer sound. */
  BOO_1("buzzer_long.wav", true),

  /** Wrong answer sound. */
  BOO_2("buzzer_3.wav", true),

  /** Wrong answer sound. */
  BOO_3("flush.wav", true),

  /** Correct answer sound. */
  HOORAY_0("cheering.wav", true),

  /** Correct answer sound. */
  HOORAY_1("fanfare_0.wav", true),

  /** Correct answer sound. */
  HOORAY_2("fanfare_1.wav", true),

  /** Correct answer sound. */
  HOORAY_3("fanfare_2.wav", true),

  /** Music for the end of the game. */
  FINAL("fanfare_2.wav", true); // TODO - find final sound clip!

  /** Sounds associated with failure (wrong answer). */
  private static final SoundEnum[] BOOS = {
      BOO_0, BOO_1, BOO_2, BOO_3
  };

  /** Sounds associated with success (correct answer). */
  private static final SoundEnum[] HOORAYS = {
      HOORAY_0, HOORAY_1, HOORAY_2, HOORAY_3
  };

  /** Filename for this sound. */
  private final String fileName;

  /** Indicates this sound is a Sound FX. */
  private final boolean effect;

  /**
   * Creates a new <code>SoundFileEnum</code>.
   * @param fileName filename to set
   */
  SoundEnum(String fileName, boolean isEffect) {
    this.fileName = fileName;
    this.effect = isEffect;
  }

  /**
   * Determines if this sound is a sound effect.
   * @return true if this is a sound fx
   */
  public boolean isEffect() {
    return this.effect;
  }

  /**
   * Gets the corresponding sound file resource as an InputStream.
   * @return input stream for this sound file
   */
  public InputStream getSoundFileStream() {
    return SoundEnum.class.getResourceAsStream(this.fileName);
  }

  /**
   * Gets a random boo sound file.
   * @return random boo sound file enum
   */
  public static SoundEnum getRandomBoo() {
    int index = (int) (Math.random() * BOOS.length);
    return BOOS[index];
  }

  /**
   * Gets a random hooray sound file.
   * @return random hooray sound file enum
   */
  public static SoundEnum getRandomHooray() {
    int index = (int) (Math.random() * HOORAYS.length);
    return HOORAYS[index];
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return this.fileName;
  }
}
