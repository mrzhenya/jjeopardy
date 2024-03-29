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

package net.curre.jjeopardy.ui.dialog;

import net.curre.jjeopardy.service.LocaleService;

/**
 * Represents the final game dialog that displays the winner.
 *
 * @author Yevgeny Nyden
 */
public class EndGameDialog extends InfoDialog {

  /** Scale factor for the text font. */
  private static final float SCALE_FACTOR = 2.5f;

  /**
   * Ctor.
   * @param winnerName winner's name
   * @param winnerScore winner's score
   */
  public EndGameDialog(String winnerName, int winnerScore) {
    super(LocaleService.getString("jj.game.enddialog.header", winnerName),
        LocaleService.getString("jj.game.enddialog.message", winnerName, String.valueOf(winnerScore)),
        InfoDialog.Type.END);
  }

  /** @inheritDoc */
  @Override
  protected float getFontSizeScale() {
    return SCALE_FACTOR;
  }
}
