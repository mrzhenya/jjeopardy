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

package net.curre.jjeopardy.ui.game;

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.SoundService;
import net.curre.jjeopardy.sounds.SoundEnum;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.JLabel;
import java.awt.Graphics;
import java.text.DecimalFormat;

/**
 * Represent the timer label component.
 *
 * @author Yevgeny Nyden
 */
public class TimerLabel extends JLabel implements Runnable {

  /** Timer time increments (in msec). */
  private static final long SLEEP_TIME = 1000L;

  /** Start time for the question timer. */
  private static final int QUESTION_TIME = 15;

  /** Time counter string format. */
  private static final DecimalFormat FORMAT = new DecimalFormat("00");

  /** Timer start time. */
  private final int timerStartTime;

  /** Flag to indicate the timer status (on/off). */
  private volatile boolean keepRunning;

  /** Time counter string. */
  private volatile String counterStr;


  /** Constructs a new timer label object. */
  public TimerLabel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.timerStartTime = QUESTION_TIME;
    super.setForeground(lafTheme.getTimerLabelColor());
    super.setFont(lafTheme.getTimerLabelFont());
  }

  /** {@inheritDoc} */
  public void run() {
    startTimer();
  }

  /**
   * Paints the updated counter.
   * <br />
   *
   * @param g Graphics context.
   */
  @Override
  public void paint(Graphics g) {
    this.setText(this.counterStr);
    super.paint(g);
  }

  /**
   * Stopps timer.
   */
  public void stopTimer() {
    this.keepRunning = false;
  }

  /**
   * Resets timer to the .
   */
  public void resetTimer() {
    int counter = this.timerStartTime;
    this.counterStr = FORMAT.format(counter);
    super.invalidate();
    super.validate();
    super.repaint();
  }

  private void startTimer() {
    this.keepRunning = true;
    int counter = this.timerStartTime + 1;
    while (this.keepRunning) {
      counter--;
      this.counterStr = FORMAT.format(counter);
      repaint();

      if (counter == 0) {
        this.keepRunning = false;
        final SoundService sound = AppRegistry.getInstance().getSoundService();
        sound.stopAllMusic();
        sound.startMusic(SoundEnum.TIMES_UP, 1);
        return;
      }
      try {
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException x) {
        // ignoring
      }
    }
  }
}
