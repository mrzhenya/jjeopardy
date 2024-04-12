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

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Mouse listener to set a proper image on the JButton on mouse hover.
 *
 * @author Yevgeny Nyden
 */
public class ButtonMouseHoverListener extends MouseAdapter implements MouseListener {

  /** Non-hovered button icon. */
  private final Icon icon;

  /** Hovered button icon. */
  private final Icon hoverIcon;

  /**
   * Ctor.
   * @param icon non-hovered button icon
   * @param hoverIcon hovered button icon
   */
  public ButtonMouseHoverListener(Icon icon, Icon hoverIcon) {
    this.icon = icon;
    this.hoverIcon = hoverIcon;
  }

  /**
   * Sets the button icon to the non-hovered icon.
   * @param e the event to be processed
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    JButton button = (JButton) e.getComponent();
    button.setIcon(this.hoverIcon);
  }

  /**
   * Sets the button icon to the hovered icon.
   * @param e the event to be processed
   */
  @Override
  public void mouseExited(MouseEvent e) {
    JButton button = (JButton) e.getComponent();
    button.setIcon(this.icon);
  }
}
