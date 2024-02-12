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

import net.curre.jjeopardy.sounds.SoundEnum;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Object of this class represents a sound service bean.
 *
 * @see SoundEnum
 * @author Yevgeny Nyden
 */
public class SoundService {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(SoundService.class.getName());

  /** Reference to the static instance to the sound service bean. */
  private static SoundService instance = new SoundService();

  /** Map to hold all currently opened clips. */
  private Map<SoundEnum, Clip> currentClips;

  /** Constant to indicate continuous playback mode. */
  public static final int LOOP_CONTINUOUSLY = Clip.LOOP_CONTINUOUSLY;

  /**
   * Gets a static instance of the audio service.
   * @return <code>SoundService</code> bean to use
   */
  public static SoundService getInstance() {
    return instance;
  }

  /** Creates a new <code>SoundService</code> object. */
  private SoundService() {
    this.currentClips = new EnumMap<SoundEnum, Clip>(SoundEnum.class);
  }

  /**
   * Starts a requested music file. The audio stream will be
   * opened if it hasn't been already.
   * @param soundEnum  what to start
   * @param count the number of times clip should be played, or
   *              <code>{@link #LOOP_CONTINUOUSLY}</code> to indicate that
   *              looping should continue until interrupted
   * @return true if the stream has been started successfully; false if otherwise
   */
  public boolean startMusic(SoundEnum soundEnum, int count) {
    try {
      Clip clip = this.currentClips.get(soundEnum);
      if (clip == null) {
        LOGGER.fine("Opening audio stream for \"" + soundEnum + "\".");
        clip = openAudioStreamHelper(soundEnum);
        this.currentClips.put(soundEnum, clip);
      }
      if (clip.isRunning()) {
        LOGGER.warning("Audio stream for \"" + soundEnum + "\" is already running!");
        return false;
      }

      clip.setMicrosecondPosition(0L);
      if (count == LOOP_CONTINUOUSLY) {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
      } else if (count == 1) {
        clip.start();
      } else {
        clip.loop(count - 1);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to open/start audio stream for \"" + soundEnum + "\"!", e);
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

    String eName = "";
    try {
      InputStream inStream = soundEnum.getSoundFileStream();
      AudioInputStream stream = AudioSystem.getAudioInputStream(inStream);
      AudioFormat format = stream.getFormat();
      stream = AudioSystem.getAudioInputStream(format, stream);
      DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(),
                                             ((int) stream.getFrameLength() * format.getFrameSize()));
      Clip clip = (Clip) AudioSystem.getLine(info);
      clip.open(stream); // may take some time
      return clip;

    } catch (MalformedURLException e) {
      eName = "MalformedURLException";
      LOGGER.log(Level.SEVERE, "MalformedURLException for when opening audio stream \"" + soundEnum + "\"!", e);
    } catch (IOException e) {
      eName = "IOException";
      LOGGER.log(Level.SEVERE, "IOException for when opening audio stream \"" + soundEnum + "\"!", e);
    } catch (LineUnavailableException e) {
      eName = "LineUnavailableException";
      LOGGER.log(Level.SEVERE,
        "LineUnavailableException for when opening audio stream \"" + soundEnum + "\"!", e);
    } catch (UnsupportedAudioFileException e) {
      eName = "UnsupportedAudioFileException";
      LOGGER.log(Level.SEVERE,
        "UnsupportedAudioFileException for when opening audio stream \"" + soundEnum + "\"!", e);
    }
    throw new RuntimeException(eName + " - unable to open audio stream for file \"" + soundEnum + "\"!");
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
        LOGGER.warning("Audio stream for \"" + what + "\" hasn't beed opened!");
        return false;
      }
      if (!clip.isRunning()) {
        LOGGER.warning("Audio stream for \"" + what + "\" is not running!");
        return false;
      }

      clip.stop();
      if (stop) {
        clip.setMicrosecondPosition(0L);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to " + (stop ? "stop" : "pause") +
                            " audio stream for \"" + what + "\"!", e);
      return false;
    }
    return true;
  }
}
