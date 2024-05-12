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

import javax.swing.JLabel;
import javax.validation.constraints.Null;
import java.awt.Font;

/**
 * Label with an icon to display on a game line item.
 *
 * @author Yevgeny Nyden
 */
public class ItemIconLabel extends JLabel {

  /** Default ctor. */
  public ItemIconLabel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    Font font = lafTheme.getDialogTextFont();
    this.setFont(font);
  }

  /**
   * Ctor.
   * @param text text for the label or null if no text is present
   * @param icon icon to use
   * @param tooltip tooltip text
   */
  public ItemIconLabel(@Null String text, ImageEnum icon, String tooltip) {
    this();
    this.updateContent(text, icon, tooltip);
  }

  /**
   * Updates the UI.
   * @param text text for the label or null if no text is present
   * @param imageEnum image enum
   * @param tooltip tooltip text
   */
  public void updateContent(String text, ImageEnum imageEnum, String tooltip) {
    if (text != null) {
      this.setText(text);
    }
    this.setIcon(imageEnum.toImageIcon());
    this.setToolTipText(tooltip);
  }

  /**
   * Clears the UI of this item label.
   */
  public void clearContent() {
    this.setText(null);
    this.setIcon(null);
    this.setToolTipText(null);
  }
}
