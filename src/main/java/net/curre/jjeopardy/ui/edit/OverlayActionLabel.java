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

package net.curre.jjeopardy.ui.edit;

import net.curre.jjeopardy.event.EditOverlayLabelMouseListener;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.LocaleService;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.validation.constraints.NotNull;

/**
 * Represents an action label (icon) that's displayed inside of an edit cell overlay.
 *
 * @author Yevgeny Nyden
 */
public class OverlayActionLabel extends JLabel {

  /** Default icon for this action label. */
  private final ImageIcon icon;

  /** Disabled icon for this action label. */
  private final ImageIcon iconDisabled;

  /** Tooltip to display on this action label. */
  private final String tooltip;

  /** Indicates whether the whole column should be highlighted on this label hover. */
  private final boolean columnHover;

  /** Indicates whether the whole row should be highlighted on this label hover. */
  private final boolean rowHover;

  /** Indicates whether this label is enabled/disabled. */
  private boolean enabled;

  /** Mouse listener for the action label. */
  private final EditOverlayLabelMouseListener mouseListener;

  /**
   * Ctor.
   * @param icon default icon for this label
   * @param iconHover icon to use on label hover
   * @param iconDisabled icon for disabled label (or null if the icon should be hidden)
   * @param tooltipProp tooltip text localized property name
   * @param clickHandler click handler
   * @param columnHover true if the whole column should be decorated as hovered on this label hover
   * @param rowHover true if the whole row should be decorated as hovered on this label hover
   */
  public OverlayActionLabel(@NotNull ImageEnum icon, @NotNull ImageEnum iconHover, @NotNull ImageEnum iconDisabled,
                            @NotNull String tooltipProp, @NotNull Runnable clickHandler,
                            boolean columnHover, boolean rowHover) {
    this.icon = icon.toImageIcon();
    this.iconDisabled = iconDisabled.toImageIcon();
    this.tooltip = LocaleService.getString(tooltipProp);
    this.columnHover = columnHover;
    this.rowHover = rowHover;

    this.mouseListener = new EditOverlayLabelMouseListener(icon, iconHover, clickHandler);
    this.addMouseListener(this.mouseListener);
    this.setEnabled(true);
  }

  /**
   * Enables/disables the action on this action label.
   * @param isEnabled true if the action should be enabled
   */
  public void setEnabled(boolean isEnabled) {
    this.enabled = isEnabled;
    this.setIcon(isEnabled ? this.icon : this.iconDisabled);
    this.setToolTipText(isEnabled ? this.tooltip : null);
    this.mouseListener.setEnabled(isEnabled);
  }

  /**
   * Indicates whether the whole column should be highlighted on this label hover.
   * @return true if the currently hovered column should be highlighted
   */
  public boolean isColumnHover() {
    return this.enabled && this.columnHover;
  }

  /**
   * Indicates whether the whole row should be highlighted on this label hover.
   * @return true if the currently hovered row should be highlighted
   */
  public boolean isRowHover() {
    return this.enabled && this.rowHover;
  }
}
