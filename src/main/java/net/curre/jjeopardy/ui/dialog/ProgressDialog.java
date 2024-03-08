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

package net.curre.jjeopardy.ui.dialog;

import net.curre.jjeopardy.service.LocaleService;

import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Cursor;
import java.util.concurrent.TimeUnit;

/**
 * A simple wrapper around the <code>ProgressMonitor</code>.
 * To use it, instantiate it and start a task by calling <code>start</code>,
 * increment progress with <code>incrementProgress</code>. To complete a task call either
 * <code>completeAndFinish</code> or <code>finish</code>.
 *
 * @author Yevgeny Nyden
 */
public class ProgressDialog {

  /** Max progress value. */
  private static final int FULL_PROGRESS = 100;

  /** Reference to the parent component. */
  private final Component parent;

  /** Progress dialog title. */
  private final String titleMessage;

  /** Task name. */
  private final String taskName;

  /** Reference to the progress monitor. */
  private ProgressMonitor progressMonitor;

  /** Progress of the task (0-100). */
  private int progress;

  /**
   * Ctor.
   * @param parent reference to the parent component
   */
  public ProgressDialog(Component parent, String titleMessage, String taskName) {
    this.parent = parent;
    this.titleMessage = titleMessage;
    this.taskName = taskName;
    this.progress = 0;
  }

  /**
   * Starts the progress task.
   */
  public void start(Runnable task) {
    this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    UIManager.put("ProgressMonitor.progressText", this.titleMessage);
    this.progressMonitor = new ProgressMonitor(
        this.parent, this.taskName + "                  \t\n\n",
        LocaleService.getString("jj.dialog.copy.starting"), 0, FULL_PROGRESS);
    new Thread(() -> {
      // Decide after 100 millis whether to show popup or not.
      this.progressMonitor.setMillisToDecideToPopup(100);

      // After deciding if predicted time is longer than 100 show popup.
      this.progressMonitor.setMillisToPopup(100);

      task.run();
    }).start();
  }

  /**
   * Increments the current progress, assumes full progress is 100.
   * @param progressIncrement progress increment to add
   */
  public void incrementProgress(int progressIncrement) {
    this.progress += progressIncrement;
    if (this.progress >= FULL_PROGRESS) {
      this.progress = FULL_PROGRESS - 1;
    }
    this.progressMonitor.setNote(
        LocaleService.getString("jj.dialog.copy.progress", String.valueOf(this.progress)));
    this.progressMonitor.setProgress(this.progress);
  }

  /**
   * Completes the progress, waits a second (so that the user sees the completed progress),
   * and closes the progress dialog.
   */
  public void completeAndFinish() {
    this.progressMonitor.setNote(
        LocaleService.getString("jj.dialog.copy.progress", String.valueOf(FULL_PROGRESS)));
    this.progressMonitor.setProgress(FULL_PROGRESS - 1);
    try {
      // Delay for a second to show the completed progress.
      TimeUnit.MILLISECONDS.sleep(1000);
    } catch (InterruptedException e) {
      // Ignore the exception, which doesn't matter.
    }
    this.finish();
  }

  /**
   * Marks the current task as finished and closes the progress dialog.
   * The progress is not updated in this method.
   */
  public void finish() {
    this.progressMonitor.setNote(LocaleService.getString("jj.dialog.copy.finished"));
    this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    this.progressMonitor.close();
  }
}
