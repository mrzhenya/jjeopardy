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

package net.curre.jjeopardy.event;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Basic base action to use for button that support mouse click/action
 * and key press (enter).
 *
 * @author Yevgeny Nyden
 */
public abstract class ClickAndKeyAction extends AbstractAction implements KeyListener {

  /** Handles the action */
  protected abstract void handleAction();

  /**
   * Handles the mouse click action.
   * @param e the event to be processed
   */
  public void actionPerformed(ActionEvent e) {
    this.handleAction();
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  /**
   * Handles the key press event and handles action when Enter is pressed.
   * @param e the event to be processed
   */
  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      this.handleAction();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {}
}
