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

import com.formdev.flatlaf.FlatDarkLaf;

/**
 * FlatLaf custom game theme that's based on the FlatDark theme.
 * See <a href="https://www.formdev.com/flatlaf/theme-editor/">theme-editor</a> for details.
 *
 * @author Yevgeny Nyden
 */
public class GameDefault extends FlatDarkLaf {

  /** Name of this theme. */
  public static final String NAME = "GameDefault";

  public static boolean setup() {
    return setup(new GameDefault());
  }

  public static void installLafInfo() {
    installLafInfo(NAME, GameDefault.class);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
