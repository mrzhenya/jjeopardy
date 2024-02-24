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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.Registry;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.ui.landing.LandingUi;
import net.curre.jjeopardy.util.JjDefaults;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Font;

/**
 * Dialog to show information about a game (selected in the library).
 *
 * @author Yevgeny Nyden
 */
public class GameInfoDialog extends BasicDialog {

  /** Preferred width of the dialog. */
  private static final int DIALOG_WIDTH = 600;

  /** Reference to the selected game. */
  private final GameData gameData;

  /**
   * Creates an instance of a dialog to show information about a game file.
   * @param gameData game data
   */
  public GameInfoDialog(GameData gameData) {
    super();
    this.gameData = gameData;
    this.initializeDialog(gameData.getGameName(), gameData.getGameSizeIconLarge());
  }

  @Override
  public Component getHeaderComponent() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    JPanel headerPanel = new JPanel(new TableLayout(new double[][] {
        {padding, TableLayout.FILL, padding}, {padding, TableLayout.PREFERRED, padding}}));
    JLabel label = new JLabel(this.gameData.getGameName());
    label.setFont(lafTheme.getDialogHeaderFont());
    headerPanel.add(label, new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));
    return headerPanel;
  }

  @Override
  public Component getContentComponent() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    JPanel panel = new JPanel(new TableLayout(new double[][] {
        {padding, DIALOG_WIDTH - 80, padding}, // columns
        {TableLayout.PREFERRED, padding, TableLayout.PREFERRED, padding, TableLayout.PREFERRED, padding,
            TableLayout.PREFERRED, padding}})); // rows

    Font textFont = lafTheme.getDialogTextFont();
    JTextArea textArea = BasicDialog.createDefaultTextArea(textFont);
    textArea.setText(this.gameData.getGameDescription());
    panel.add(textArea, new TableLayoutConstraints(
        1, 0, 1, 0, TableLayout.FULL, TableLayout.CENTER));

    int categoriesCount = this.gameData.getCategories().size();
    int questionsCount = this.gameData.getCategories().get(0).getQuestionsCount();
    int totalCount = questionsCount * categoriesCount;
    JLabel label1 = new JLabel(LocaleService.getString("jj.file.info.msg1",
        String.valueOf(totalCount), String.valueOf(categoriesCount),
        String.valueOf(categoriesCount), String.valueOf(questionsCount)));
    panel.add(label1, new TableLayoutConstraints(
        1, 2, 1, 2, TableLayout.CENTER, TableLayout.CENTER));

    int bonusQuestionsCount = this.gameData.getBonusQuestions().size();
    int playersCount = this.gameData.getPlayerNames().size();
    if (bonusQuestionsCount > 0 || playersCount > 0) {
      JLabel label2 = new JLabel(LocaleService.getString("jj.file.info.msg2",
          String.valueOf(bonusQuestionsCount), String.valueOf(playersCount)));
      panel.add(label2, new TableLayoutConstraints(
          1, 4, 1, 4, TableLayout.CENTER, TableLayout.CENTER));
    }

    int additionalQuestionTimeSec = 40;
    int estimatedMin = (totalCount + bonusQuestionsCount) * (JjDefaults.QUESTION_TIME + additionalQuestionTimeSec) / 60;
    JLabel label3 = new JLabel(LocaleService.getString("jj.file.info.msg3",
        String.valueOf(estimatedMin)));
    panel.add(label3, new TableLayoutConstraints(
        1, 6, 1, 6, TableLayout.CENTER, TableLayout.CENTER));

    return panel;
  }

  /**
   * Gets the button panel.
   * @return panel with buttons for this dialog.
   */
  public Component getButtonComponent() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    JPanel panel = new JPanel(new TableLayout(new double[][] {
        {TableLayout.PREFERRED, padding, TableLayout.PREFERRED}, // columns
        {TableLayout.PREFERRED}})); // rows

    JButton loadButton = new JButton();
    ClickAndKeyAction.createAndAddAction(loadButton, this::loadGame);
    loadButton.setText(LocaleService.getString("jj.playerdialog.button.load"));
    loadButton.setFont(lafTheme.getButtonFont());
    panel.add(loadButton, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));

    JButton defaultButton = createDefaultButton();
    panel.add(defaultButton, new TableLayoutConstraints(
        2, 0, 2, 0, TableLayout.CENTER, TableLayout.CENTER));
    SwingUtilities.invokeLater(defaultButton::requestFocus);
    return panel;
  }

  /**
   * Loads the selected game.
   */
  private void loadGame() {
    Registry registry = AppRegistry.getInstance();
    registry.getGameDataService().setCurrentGameData(this.gameData);
    LandingUi landingUi = registry.getLandingUi();
    landingUi.updateUiWithLoadedGameFile();
    landingUi.switchBetweenLibraryAndBackgroundCard();
    this.setVisible(false);
    this.dispose();
  }
}
