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

package net.curre.jjeopardy.ui.landing;

import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.validation.constraints.NotNull;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Label with an icon to display on a game line item.
 *
 * @author Yevgeny Nyden
 */
public class ItemIconButton extends JButton {

  /** Icon to use for the label. */
  private final ImageIcon icon;

  /** Icon to use for the label on mouse hover. */
  private final ImageIcon iconHover;

  /**
   * Ctor.
   * @param textOrNull text for the label or null if no text is present
   * @param icon icon to use
   * @param iconHover icon to use on hover or null if icon shouldn't be changed
   * @param tooltip tooltip text
   * @param task task to run on mouse click
   */
  public ItemIconButton(String textOrNull, @NotNull ImageEnum icon,
                        @NotNull ImageEnum iconHover, String tooltip, @NotNull Runnable task) {
    if (textOrNull != null) {
      LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
      Font font = lafTheme.getDialogTextFont();
      this.setFont(font);
      this.setText(textOrNull);
    }
    this.icon = icon.toImageIcon();
    this.iconHover = iconHover.toImageIcon();

    this.setIcon(this.icon);
    this.setToolTipText(tooltip);
    this.addMouseListener(new ItemIconButton.ItemIconMouseListener(task));
    this.setBorderPainted(false);
    this.setFocusable(false);
  }

  /** Mouse listener for the <code>ItemIconLabel</code>. */
  private class ItemIconMouseListener implements MouseListener {

    /** Task to run on mouse click. */
    private final Runnable taskOrNull;

    /**
     * Ctor.
     * @param taskOrNull the task to run on mouse click
     */
    public ItemIconMouseListener(Runnable taskOrNull) {
      this.taskOrNull = taskOrNull;
    }

    /** Runs the task. */
    @Override
    public void mouseClicked(MouseEvent e) {
      if (taskOrNull != null) {
        this.taskOrNull.run();
      }
    }

    /** Updates the label's icon to the hover icon if it's set. */
    @Override
    public void mouseEntered(MouseEvent e) {
      if (ItemIconButton.this.iconHover != null) {
        ItemIconButton.this.setIcon(ItemIconButton.this.iconHover);
      }
    }

    /** Updates the label's icon back to the original. */
    @Override
    public void mouseExited(MouseEvent e) {
      if (ItemIconButton.this.iconHover != null) {
        ItemIconButton.this.setIcon(ItemIconButton.this.icon);
      }
    }

    /** Does nothing. */
    @Override
    public void mousePressed(MouseEvent e) {}

    /** Does nothing. */
    @Override
    public void mouseReleased(MouseEvent e) {}
  }
}
