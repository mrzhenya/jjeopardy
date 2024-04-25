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

package net.curre.jjeopardy.images;

import javax.swing.ImageIcon;
import javax.validation.constraints.NotNull;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Objects;

/**
 * Represents various icons used by the app. The image files are expected to be in a resource
 * directory under net/curre/jjeopardy/images/. You can load them using the <code>toImageIcon</code>
 * or <code>toImage</code> method on this enum.
 *
 * @author Yevgeny Nyden
 */
public enum ImageEnum {

  /** Down arrow medium size icon. */
  ARROW_DOWN_32("arrow_down-32.png"),

  /** Disabled down arrow medium size icon. */
  ARROW_DOWN_32_DISABLED("arrow_down-32-disabled.png"),

  /** Down arrow medium size hover icon. */
  ARROW_DOWN_32_HOVER("arrow_down-32-hover.png"),

  /** Left arrow medium size icon. */
  ARROW_LEFT_32("arrow_left-32.png"),

  /** Disabled left arrow medium size icon. */
  ARROW_LEFT_32_DISABLED("arrow_left-32-disabled.png"),

  /** Left arrow medium size hover icon. */
  ARROW_LEFT_32_HOVER("arrow_left-32-hover.png"),

  /** Right arrow medium size icon. */
  ARROW_RIGHT_32("arrow_right-32.png"),

  /** Disabled right arrow medium size icon. */
  ARROW_RIGHT_32_DISABLED("arrow_right-32-disabled.png"),

  /** Right arrow medium size hover icon. */
  ARROW_RIGHT_32_HOVER("arrow_right-32-hover.png"),

  /** Up arrow medium size icon. */
  ARROW_UP_32("arrow_up-32.png"),

  /** Disabled up arrow medium size icon. */
  ARROW_UP_32_DISABLED("arrow_up-32-disabled.png"),

  /** Up arrow medium size hover icon. */
  ARROW_UP_32_HOVER("arrow_up-32-hover.png"),

  /** Remove (x) medium size icon. */
  REMOVE_32("remove-32.png"),

  /** Disabled remove (x) medium size icon. */
  REMOVE_32_DISABLED("remove-32-disabled.png"),

  /** Hover remove (x) medium size icon. */
  REMOVE_32_HOVER("remove-32-hover.png"),

  /** Plus one medium size icon. */
  PLUS_ONE_32("plus_one-32.png"),

  /** Hover plus one medium size icon. */
  PLUS_ONE_32_HOVER("plus_one-32-hover.png"),

  /** Disabled plus one medium size icon. */
  PLUS_ONE_32_DISABLED("plus_one-32-disabled.png"),

  /** Small success icon. */
  SUCCESS_24("success-24.png"),

  /** Large success icon. */
  SUCCESS_64("success-64.png"),

  /** Small failure icon. */
  FAILURE_24("fail-24.png"),

  /** Large failure icon. */
  FAILURE_64("fail-64.png"),

  /** Small info icon. */
  INFO_24("info-24.png"),

  /** Large info icon. */
  INFO_64("info-64.png"),

  /** Small warning icon. */
  WARN_24("warn-24.png"),

  /** Large warning icon. */
  WARN_64("warn-64.png"),

  /** Small error icon. */
  ERROR_24("error-24.png"),

  /** Large error icon. */
  ERROR_64("error-64.png"),

  /** Open file icon. */
  OPEN_FILE_24("open_file-24.png"),

  /** Open file hover icon. */
  OPEN_FILE_24_HOVER("open_file-24-hover.png"),

  /** Trash edit icon. */
  TRASH_24("trash-24.png"),

  /** Trash edit hover icon. */
  TRASH_24_HOVER("trash-24-hover.png"),

  /** Small edit icon. */
  EDIT_24("edit-24.png"),

  /** Small edit hover icon. */
  EDIT_24_HOVER("edit-24-hover.png"),

  /** Large blue question icon. */
  QUESTION_BLUE_64("question-blue-64.png"),

  /** Winner medal icon. */
  WINNER_64("winner-64.png"),

  /** Blue button icon. */
  CELL_DEFAULT("cell_default.png"),

  /** Blue-light button icon. */
  CELL_DEFAULT_HOVER("cell_default-light.png"),

  /** Blue-dark button icon. */
  CELL_DEFAULT_EMPTY("cell_default-dark.png"),

  /** Light button icon. */
  CELL_LIGHT("cell_light.png"),

  /** Light-hover button icon. */
  CELL_LIGHT_HOVER("cell_light-light.png"),

  /** Light-dark button icon. */
  CELL_LIGHT_EMPTY("cell_light-dark.png"),

  /** Nimbus default button icon. */
  CELL_NIMBUS("cell_nimbus.png"),

  /** Nimbus hover button icon. */
  CELL_NIMBUS_HOVER("cell_nimbus-light.png"),

  /** Nimbus empty button icon. */
  CELL_NIMBUS_EMPTY("cell_nimbus-dark.png"),

  /** Mac Dark empty button icon. */
  CELL_MACDARK_EMPTY("cell_macdark-dark.png"),

  /** Size icon - extra large 64x64. */
  SIZE_XL_64("size-xlarge-64.png"),

  /** Size icon - large 64x64. */
  SIZE_L_64("size-large-64.png"),

  /** Size icon - medium 64x64. */
  SIZE_M_64("size-medium-64.png"),

  /** Size icon - small 64x64. */
  SIZE_S_64("size-small-64.png"),

  /** Size icon - extra large 24x24. */
  SIZE_XL_24("size-xlarge-24.png"),

  /** Size icon - large 24x24. */
  SIZE_L_24("size-large-24.png"),

  /** Size icon - medium 24x24. */
  SIZE_M_24("size-medium-24.png"),

  /** Size icon - small 24x24. */
  SIZE_S_24("size-small-24.png"),

  /** User small icon. */
  USER_24("user-24.png"),

  /** Image placeholder large icon. */
  IMAGE_64("image-64.png"),

  /** Image download failure large icon. */
  IMAGE_FAILURE_64("image-failure-64.png"),

  /** Image download failure small icon. */
  IMAGE_FAILURE_24("image-failure-24.png"),

  /** Vertical line spacer image icon. */
  VERTICAL_SPACER_24("vertical_spacer-24.png");

  /** Icon's filename. */
  private final String fileName;

  /**
   * Ctor.
   * @param fileName the icon's filename
   */
  ImageEnum(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Gets the icon's filename.
   * @return the icon's filename
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * Loads an image for this icon and creates an ImageIcon object.
   * @return image icon initialized with an appropriate image asset
   */
  public @NotNull ImageIcon toImageIcon() {
    return new ImageIcon(Objects.requireNonNull(ImageEnum.class.getResource(this.getFileName())));
  }

  /**
   * Loads an image for this icon and creates an ImageIcon object.
   * @return image icon initialized with an appropriate image asset
   */
  public Image toImage() {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    return toolkit.getImage(ImageEnum.class.getResource(this.getFileName()));
  }

  @Override
  public String toString() {
    return this.fileName;
  }
}
