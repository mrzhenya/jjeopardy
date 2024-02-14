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

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.MainService;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Quit app action.
 */
public class QuitAppAction extends AbstractAction {
  public QuitAppAction() {
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    AppRegistry.getInstance().getMainService().quitApp();
  }
}
