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

package net.curre.jjeopardy.ui.laf;

/**
 * Known LAF theme IDs.
 * @author Yevgeny Nyden
 */
public enum LafThemeId {

  /** Game default theme based on the FlatLightLaf theme but with
   * a more vibrant set of colors. */
  DEFAULT,

  /** Based on javax.swing.plaf.nimbus.NimbusLookAndFeel. */
  NIMBUS,

  /** Based on com.formdev.flatlaf.FlatLightLaf. */
  FLAT_LIGHT,

  /** Based on com.formdev.flatlaf.FlatDarkLaf. */
  FLAT_DARK,

  /** Based on com.formdev.flatlaf.themes.FlatMacDarkLaf. */
  FLAT_MAC_DARK
}
