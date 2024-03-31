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

import net.curre.jjeopardy.App;
import net.curre.jjeopardy.bean.Settings;
import net.curre.jjeopardy.sounds.SoundEnum;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Sound service responsible for playing sound clips.
 *
 * @see SoundEnum
 * @author Yevgeny Nyden
 */
public class SoundService {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(SoundService.class.getName());

  /** Map to hold all currently opened clips. */
  private final Map<SoundEnum, Clip> currentClips;

  /** Ctor. */
  protected SoundService() {
    this.currentClips = new EnumMap<>(SoundEnum.class);
  }

  /**
   * Starts playing a requested music file. The audio stream will be
   * opened if it hasn't been opened already.
   * @param soundEnum  what to start
   * @param count the number of times clip should be played, or Clip.LOOP_CONTINUOUSLY
   *              to indicate that looping should continue until interrupted
   * @return true if the stream has been started successfully; false if otherwise
   */
  public boolean startMusic(SoundEnum soundEnum, int count) {
    Settings settings = AppRegistry.getInstance().getSettingsService().getSettings();
    if (settings.isAllSoundOff()) {
      return false;
    }
    if (settings.isSoundEffectsOnly() && !soundEnum.isEffect()) {
      return false;
    }

    try {
      Clip clip = this.currentClips.get(soundEnum);
      if (clip == null) {
        logger.info("Opening audio stream for \"" + soundEnum + "\".");
        clip = openAudioStreamHelper(soundEnum);
        this.currentClips.put(soundEnum, clip);
      }
      if (clip.isRunning()) {
        logger.warn("Audio stream for \"" + soundEnum + "\" is already running!");
        return false;
      }

      clip.setMicrosecondPosition(0L);
      if (count == Clip.LOOP_CONTINUOUSLY) {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
      } else if (count == 1) {
        clip.start();
      } else {
        clip.loop(count - 1);
      }
    } catch (Exception e) {
      logger.log(Level.ERROR, "Unable to open/start audio stream for \"" + soundEnum + "\"!", e);
      return false;
    }
    return true;
  }

  /**
   * Stops requested music file.
   * @param what what to start
   * @return true if the stream has been stopped successfully; false if otherwise
   */
  public boolean stopMusic(SoundEnum what) {
    return pauseOrStopMusicHelper(what, true);
  }

  /**
   * Stops requested music file.
   * @param what what to start
   * @return true if the stream has been stopped successfully; false if otherwise
   */
  public boolean pauseMusic(SoundEnum what) {
    return pauseOrStopMusicHelper(what, false);
  }

  /**
   * Opens an audio stream for the given file name.
   * @param soundEnum sound enum for the audio file
   * @return opened clip
   */
  private Clip openAudioStreamHelper(SoundEnum soundEnum) {
    try {
      InputStream inStream = soundEnum.getSoundFileStream();
      InputStream bufferedIn = new BufferedInputStream(inStream);
      AudioInputStream stream = AudioSystem.getAudioInputStream(bufferedIn);
      AudioFormat format = stream.getFormat();
      stream = AudioSystem.getAudioInputStream(format, stream);
      DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(),
                                             ((int) stream.getFrameLength() * format.getFrameSize()));
      Clip clip = (Clip) AudioSystem.getLine(info);
      clip.open(stream); // may take some time
      return clip;

    } catch (Exception e) {
      logger.log(Level.ERROR, "Exception when opening audio stream \"" + soundEnum + "\"!", e);
      throw new RuntimeException("Unable to open audio stream for file \"" + soundEnum + "\"!", e);
    }
  }

  /** Stops all playing music if there is any. */
  public void stopAllMusic() {
    for (Clip clip : this.currentClips.values()) {
      if (clip.isRunning()) {
        clip.stop();
        clip.setMicrosecondPosition(0L);
      }
    }
  }

  /**
   * Pauses or stops requested music file.
   * @param what what to pause or stop
   * @param stop if true the music will be stopped; false - paused
   * @return true if the stream has been stopped successfully; false if otherwise
   */
  private boolean pauseOrStopMusicHelper(SoundEnum what, boolean stop) {
    try {
      Clip clip = this.currentClips.get(what);
      if (clip == null) {
        logger.warn("Audio stream for \"" + what + "\" hasn't been opened!");
        return false;
      }
      if (!clip.isRunning()) {
        logger.warn("Audio stream for \"" + what + "\" is not running!");
        return false;
      }

      clip.stop();
      if (stop) {
        clip.setMicrosecondPosition(0L);
      }
    } catch (Exception e) {
      logger.log(Level.ERROR, "Unable to " + (stop ? "stop" : "pause") +
                            " audio stream for \"" + what + "\"!", e);
      return false;
    }
    return true;
  }
}
