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

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/** Tab key listener to transfer focus to the next focusable component. */
public class TabKeyListener implements KeyListener {

  /** Does nothing. */
  @Override
  public void keyTyped(KeyEvent e) {
  }

  /** Handles tab key press event by transferring focus to the next focusable component. */
  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_TAB) {
      e.consume();
      KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
      manager.focusNextComponent();
    }
  }

  /** Does nothing. */
  @Override
  public void keyReleased(KeyEvent e) {
  }
}
