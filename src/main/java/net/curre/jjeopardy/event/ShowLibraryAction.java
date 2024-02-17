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

import net.curre.jjeopardy.ui.landing.LandingUi;

/**
 * Action to handle switching to the Library view (card) and
 * back to the background image on the Landing UI.
 *
 * @author Yevgeny Nyden
 */
public class ShowLibraryAction extends ClickAndKeyAction {

  /** Reference to the landing UI. */
  private final LandingUi landingUi;

  /**
   * Ctor.
   * @param landingUi reference to the landing UI
   */
  public ShowLibraryAction(LandingUi landingUi) {
    this.landingUi = landingUi;
  }

  @Override
  protected void handleAction() {
    this.landingUi.switchBetweenLibraryAndBackgroundCard();
  }
}
