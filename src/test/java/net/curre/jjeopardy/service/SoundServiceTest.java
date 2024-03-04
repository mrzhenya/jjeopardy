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

import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.sounds.SoundEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Tests for the sound service.
 *
 * @author Yevgeny Nyden
 */
public class SoundServiceTest {

  /** Reference to a sound service in test. */
  private SoundService testSoundService;

  /** Reference to the settings object. */
  private Settings settings;

  /**
   * Initializes the state before each test run.
   */
  @Before
  public void init() {
    this.testSoundService = new SoundService();
    AppRegistry testRegistry = new AppRegistry();
    this.settings = testRegistry.getSettingsService().getSettings();
    this.settings.enableAllSound();
    AppRegistry.initialize(testRegistry);
  }

  /**
   * Tests start playing a music file.
   */
  @Test
  public void testStartMusic() throws Exception {
    assertPlayHappened(SoundEnum.OPENING);
  }

  /**
   * Tests start playing a music FX file with sound effect on.
   */
  @Test
  public void testStartMusicWithSoundEffectsOn() throws Exception {
    assertTrue("Sound effect should be on", SoundEnum.HOORAY_1.isEffect());
    this.settings.enableSoundEffectsOnly();
    assertPlayHappened(SoundEnum.HOORAY_1);
  }

  /**
   * Tests start playing a music file with sound off.
   */
  @Test
  public void testStartMusicWithSoundOff() throws Exception {
    this.settings.disableAllSound();
    Clip clipMock = Mockito.mock(Clip.class, "testClipMock");
    try (MockedStatic<AudioSystem> audioSystemMock =
             Mockito.mockStatic(AudioSystem.class, Mockito.CALLS_REAL_METHODS)) {
      audioSystemMock.when(() -> AudioSystem.getLine(any(DataLine.Info.class)))
          .thenReturn(clipMock);

      this.testSoundService.startMusic(SoundEnum.HOORAY_1, 1);
      verify(clipMock, never()).open(any(AudioInputStream.class));
    }
  }

  /**
   * Tests start and stop playing a music file.
   */
  @Test
  public void testStartAndStopMusic() {
    Clip clipMock = Mockito.mock(Clip.class, "testClipMock");
    try (MockedStatic<AudioSystem> audioSystemMock =
             Mockito.mockStatic(AudioSystem.class, Mockito.CALLS_REAL_METHODS)) {
      audioSystemMock.when(() -> AudioSystem.getLine(any(DataLine.Info.class)))
          .thenReturn(clipMock);

      when(clipMock.isRunning()).thenReturn(false);
      this.testSoundService.startMusic(SoundEnum.HOORAY_1, 1);
      reset(clipMock);

      // First stop should stop the music.
      when(clipMock.isRunning()).thenReturn(true);
      this.testSoundService.stopAllMusic();
      verify(clipMock).stop();
      verify(clipMock).setMicrosecondPosition(0L);

      reset(clipMock);

      // Repeated stop should have no effect.
      when(clipMock.isRunning()).thenReturn(false);
      this.testSoundService.stopAllMusic();
      verify(clipMock, never()).stop();
      verify(clipMock, never()).setMicrosecondPosition(0L);
    }
  }

  /**
   * Asserts play happened.
   * @param soundEnum sound enum to play
   */
  private void assertPlayHappened(SoundEnum soundEnum) throws Exception {
    Clip clipMock = Mockito.mock(Clip.class, "testClipMock");
    try (MockedStatic<AudioSystem> audioSystemMock =
             Mockito.mockStatic(AudioSystem.class, Mockito.CALLS_REAL_METHODS)) {
      audioSystemMock.when(() -> AudioSystem.getLine(any(DataLine.Info.class)))
          .thenReturn(clipMock);

      this.testSoundService.startMusic(soundEnum, 1);
      verify(clipMock).open(any(AudioInputStream.class));
      when(clipMock.isRunning()).thenReturn(false);
      verify(clipMock).setMicrosecondPosition(0L);
      verify(clipMock).start();
    }
  }
}
