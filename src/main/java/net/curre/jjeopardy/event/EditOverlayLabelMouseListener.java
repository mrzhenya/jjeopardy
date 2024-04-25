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

import net.curre.jjeopardy.images.ImageEnum;

import javax.validation.constraints.NotNull;
import java.awt.event.MouseEvent;

/**
 * Listener for the action labels on the Edit table cell overlay.
 *
 * @author Yevgeny Nyden
 */
public class EditOverlayLabelMouseListener extends IconMouseHoverListener {

  /** Indicates whether this listener should be enabled or disabled. */
  private boolean enabled;

  /** Code to run on the user click action. */
  private final Runnable actionFn;

  /**
   * Ctor.
   * @param icon icon to set on the label in a non-hovered state
   * @param hoverIcon icon to set on the label in a hovered state
   */
  public EditOverlayLabelMouseListener(ImageEnum icon, ImageEnum hoverIcon, Runnable actionFn) {
    super(icon.toImageIcon(), hoverIcon.toImageIcon());
    this.enabled = true;
    this.actionFn = actionFn;
  }

  /**
   * Sets this listener to enabled/disabled mode.
   * @param isEnabled true if this listener should be enabled; false if otherwise
   */
  public void setEnabled(boolean isEnabled) {
    this.enabled = isEnabled;
  }

  /** @inheritDoc */
  @Override
  public void mouseReleased(MouseEvent e) {
    e.consume();
    if (this.enabled) {
      this.actionFn.run();
    }
  }

  /** @inheritDoc */
  @Override
  public void mouseEntered(@NotNull MouseEvent e) {
    if (this.enabled) {
      super.mouseEntered(e);
    }
  }

  /** @inheritDoc */
  @Override
  public void mouseExited(@NotNull MouseEvent e) {
    if (this.enabled) {
      super.mouseExited(e);
    }
  }
}
