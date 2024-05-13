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
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.event.BonusQuestionAction;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.event.YesNoAnswerAction;
import net.curre.jjeopardy.images.ImageUtilities;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.List;

/**
 * UI for the main content panel inside the Question dialog.
 *
 * @author Yevgeny Nyden
 */
public class QuestionPane extends JPanel {

  /** String unique ID to use with CardLayout for the question card. */
  private static final String CARD_QUESTION_ID = "question";

  /** String unique ID to use with CardLayout for the bonus intro card. */
  private static final String CARD_BONUS_INTRO_ID = "bonusIntro";

  /** String unique ID to use with CardLayout for the answer card. */
  private static final String CARD_ANSWER_ID = "answer";

  /** String unique ID to use with CardLayout for the bonus answer card. */
  private static final String CARD_BONUS_ANSWER_ID = "bonusAnswer";

  /** Identifies currently displayed card. */
  private String currentCard;

  /** Vertical padding for the text field (e.g. where question or answer if displayed). */
  private static final int TEXT_PANE_V_PADDING = 40;

  /** Width of the yes/no buttons on the bonus question pane. */
  private static final int BONUS_BUTTON_WIDTH = 100;

  /** Maximum question image width. */
  private static final int MAX_IMAGE_WIDTH = 500;

  /** Maximum question image height. */
  private static final int MAX_IMAGE_HEIGHT = 400;

  /** Reference to the question dialog. */
  private final QuestionDialog questionDialog;

  /** Question label. */
  private JTextArea questionLabel;

  /** Question image label. */
  private JLabel questionImageLabel;

  /** Answer label. */
  private JTextArea answerLabel;

  /** Answer image label. */
  private JLabel answerImageLabel;

  /** Bonus answer label. */
  private JTextArea bonusAnswerLabel;

  /** Bonus answer image label. */
  private JLabel bonusAnswerImageLabel;

  /** True if bonus questions round is active. */
  private boolean isBonusQuestionsRound;

  /** Answer's yes/no buttons panel. */
  private JPanel answerYesNoButtonsPanel;

  /** Reference to the close button on the answer pane. */
  private JButton closeButton;

  /**
   * Ctor.
   * @param questionDialog reference to the question dialog
   */
  public QuestionPane(QuestionDialog questionDialog)     {
    this.questionDialog = questionDialog;
    this.isBonusQuestionsRound = false;

    this.setLayout(new CardLayout());

    // The question card.
    JPanel questionPanel = this.createQuestionPanel();
    this.add(questionPanel, CARD_QUESTION_ID);
    this.currentCard = CARD_QUESTION_ID;

    // The bonus into panel.
    JPanel bonusQuestionPanel = this.createBonusIntroPanel();
    this.add(bonusQuestionPanel, CARD_BONUS_INTRO_ID);

    // The answer panel.
    JPanel answerPanel = this.createAnswerPanel();
    this.add(answerPanel, CARD_ANSWER_ID);

    // The bonus answer panel.
    JPanel bonusAnswerPanel = this.createBonusAnswerPanel();
    this.add(bonusAnswerPanel, CARD_BONUS_ANSWER_ID);
  }

  /**
   * Shows the question UI (card).
   * @param question question to ask
   * @param isBonus true if this is a bonus question
   */
  public void showQuestion(@NotNull Question question, boolean isBonus) {
    String bundlePath = AppRegistry.getInstance().getGameDataService().getCurrentGameData().getBundlePath();
    this.questionLabel.setText(question.getQuestion());
    this.isBonusQuestionsRound = isBonus;
    ImageUtilities.updateLabelIconImage(
        this.questionImageLabel, question.getQuestionImage(), bundlePath, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
    if (isBonus) {
      ImageUtilities.updateLabelIconImage(
          this.bonusAnswerImageLabel, question.getAnswerImage(), bundlePath, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
      this.bonusAnswerLabel.setText(question.getAnswer());
      this.answerYesNoButtonsPanel.setVisible(false);
    } else {
      ImageUtilities.updateLabelIconImage(
          this.answerImageLabel, question.getAnswerImage(), bundlePath, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
      this.answerLabel.setText(question.getAnswer());
      this.answerYesNoButtonsPanel.setVisible(true);
    }
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_QUESTION_ID);
    this.currentCard = CARD_QUESTION_ID;
    SwingUtilities.invokeLater(this.questionDialog::pack);
  }

  /**
   * Shows the bonus intro UI (card).
   */
  public void showBonusIntro() {
    this.questionDialog.pack();
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_BONUS_INTRO_ID);
    this.currentCard = CARD_BONUS_INTRO_ID;
  }

  /**
   * Shows the appropriate answer UI (card) - regular or bonus answer.
   */
  protected void showAnswer() {
    // Clearing the previous card's UI so that we can resize the dialog.
    this.clearQuestionUi();
    this.questionDialog.pack();
    this.questionDialog.setLocationRelativeTo(this.questionDialog.getOwner());

    if (this.isBonusQuestionsRound) {
      this.switchToBonusAnswerCard();
    } else {
      this.switchToRegularAnswerCard();
    }
  }

  /**
   * Clears text labels to assist with transitioning to the new content.
   */
  protected void clearTextAndImageLabels() {
    this.clearQuestionUi();
    this.answerLabel.setText("");
    this.answerImageLabel.setIcon(null);
    this.answerImageLabel.setPreferredSize(new Dimension(0, 0));
    this.bonusAnswerLabel.setText("");
    this.bonusAnswerImageLabel.setIcon(null);
  }

  /**
   * Determines if answer is shown.
   * @return true if the current view displays the answer; false if otherwise
   */
  protected boolean isAnswerCardShown() {
    return this.currentCard.equals(CARD_ANSWER_ID);
  }

  /**
   * Creates the UI for the question UI.
   * @return created and initialized JPanel
   */
  private @NotNull JPanel createQuestionPanel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    JPanel questionPanel = new JPanel();
    questionPanel.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL}, // columns
      {TEXT_PANE_V_PADDING, TableLayout.FILL, TEXT_PANE_V_PADDING, TableLayout.PREFERRED, TEXT_PANE_V_PADDING, TableLayout.PREFERRED}})); // rows

    // ******** Question text and image.
    this.questionLabel = BasicDialog.createDefaultTextArea(lafTheme.getQuestionTextFont());
    this.questionImageLabel = new JLabel();
    JPanel questionContainer = createTextImageContainer(this.questionLabel, this.questionImageLabel);
    questionPanel.add(questionContainer, new TableLayoutConstraints(
      0, 1, 0, 1, TableLayout.FULL, TableLayout.FULL));

    // ******* Question's Yes/No answer panel - hidden for bonus answers.
    this.answerYesNoButtonsPanel = new JPanel();
    this.answerYesNoButtonsPanel.setLayout(new TableLayout(new double[][] {
        {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
            TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}, // columns
        {TableLayout.FILL}})); // rows

    List<Player> players = AppRegistry.getInstance().getGameDataService().getCurrentPlayers();
    for (int index = 0; index < players.size(); index++) {
      Player player = players.get(index);
      JPanel aPanel = this.createYesNoAnswerPanel(player);
      this.answerYesNoButtonsPanel.add(aPanel, new TableLayoutConstraints(
          index, 0, index, 0, TableLayout.FULL, TableLayout.FULL));
    }
    questionPanel.add(this.answerYesNoButtonsPanel, new TableLayoutConstraints(
        0, 3, 0, 3, TableLayout.CENTER, TableLayout.FULL));

    // ******** Show answer button.
    JButton answerButton = new JButton();
    ClickAndKeyAction.createAndAddAction(answerButton, this::handleShowAnswerAction);
    answerButton.setFont(lafTheme.getButtonFont());
    answerButton.setText(LocaleService.getString("jj.game.question.buttons.show.name"));
    questionPanel.add(answerButton, new TableLayoutConstraints(
      0, 5, 0, 5, TableLayout.CENTER, TableLayout.CENTER));

    return questionPanel;
  }

  /**
   * Creates the UI for the bonus intro UI.
   * @return created and initialized JPanel
   */
  private @NotNull JPanel createBonusIntroPanel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final JPanel panel = new JPanel();
    panel.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL}, // columns
      {TEXT_PANE_V_PADDING, TableLayout.FILL, TEXT_PANE_V_PADDING}})); // rows

    JLabel label = new JLabel();
    label.setText(LocaleService.getString("jj.bonus.ready"));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setFont(lafTheme.getQuestionTextFont());
    label.setForeground(lafTheme.getGameTableHeaderColor());
    panel.add(label, new TableLayoutConstraints(
      0, 1, 0, 1, TableLayout.FULL, TableLayout.FULL));

    return panel;
  }

  /**
   * Creates the UI for the answer UI.
   * @return created and initialized JPanel
   */
  private @NotNull JPanel createAnswerPanel() {
    JPanel answerPanel = new JPanel();

    // Creating panel layout.
    answerPanel.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL}, // columns
      {TEXT_PANE_V_PADDING, TableLayout.FILL, TEXT_PANE_V_PADDING, TableLayout.PREFERRED}})); // rows

    // ******** Answer text and image.
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.answerLabel = BasicDialog.createDefaultTextArea(lafTheme.getQuestionTextFont());
    this.answerImageLabel = new JLabel();
    JPanel answerContainer = createTextImageContainer(this.answerLabel, this.answerImageLabel);
    answerPanel.add(answerContainer, new TableLayoutConstraints(
        0, 1, 0, 1, TableLayout.FULL, TableLayout.FULL));

    // ******** Close dialog button.
    this.closeButton = new JButton();
    ClickAndKeyAction.createAndAddAction(this.closeButton, this::handleCloseDialogAction);
    this.closeButton.setFont(lafTheme.getButtonFont());
    this.closeButton.setText(LocaleService.getString("jj.game.question.buttons.close.name"));
    answerPanel.add(this.closeButton, new TableLayoutConstraints(
        0, 3, 0, 3, TableLayout.CENTER, TableLayout.CENTER));

    return answerPanel;
  }

  /**
   * Creates the bonus answer UI.
   * @return bonus answer JPanel
   */
  private @NotNull JPanel createBonusAnswerPanel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final JPanel panel = new JPanel();

    // Creating panel layout.
    panel.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL}, // columns
      {TEXT_PANE_V_PADDING, TableLayout.FILL, TEXT_PANE_V_PADDING, TableLayout.PREFERRED}})); // rows

    // ******* Answer text.
    this.bonusAnswerLabel = BasicDialog.createDefaultTextArea(lafTheme.getQuestionTextFont());
    this.bonusAnswerImageLabel = new JLabel();
    JPanel answerContainer = createTextImageContainer(this.bonusAnswerLabel, this.bonusAnswerImageLabel);
    panel.add(answerContainer, new TableLayoutConstraints(
      0, 1, 0, 1, TableLayout.FULL, TableLayout.FULL));

    // ******* Yes/No buttons.
    final JPanel yesNoPanel = new JPanel();
    yesNoPanel.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL, BONUS_BUTTON_WIDTH, lafTheme.getButtonSpacing(), BONUS_BUTTON_WIDTH, TableLayout.FILL},
      {TableLayout.PREFERRED}}));

    // Yes button.
    final JButton yesButton = new JButton();
    BonusQuestionAction yesAction = new BonusQuestionAction(this.questionDialog, true);
    yesButton.setAction(yesAction);
    yesButton.addKeyListener(yesAction);
    yesButton.setFont(lafTheme.getButtonFont());
    yesButton.setText(LocaleService.getString("jj.game.answer.button.yes"));
    yesNoPanel.add(yesButton, new TableLayoutConstraints(
      1, 0, 1, 0, TableLayout.FULL, TableLayout.FULL));

    // No button.
    final JButton noButton = new JButton();
    BonusQuestionAction noAction = new BonusQuestionAction(this.questionDialog, false);
    noButton.setAction(noAction);
    noButton.addKeyListener(noAction);
    noButton.setFont(lafTheme.getButtonFont());
    noButton.setText(LocaleService.getString("jj.game.answer.button.no"));
    yesNoPanel.add(noButton, new TableLayoutConstraints(
      3, 0, 3, 0, TableLayout.FULL, TableLayout.FULL));

    panel.add(yesNoPanel, new TableLayoutConstraints(
      0, 3, 0, 3, TableLayout.CENTER, TableLayout.FULL));

    return panel;
  }

  /**
   * Creates a component that represents Yes/No answer action UI
   * for a single player.
   * @param player player to create the UI for
   * @return created JPanel
   */
  private @NotNull JPanel createYesNoAnswerPanel(@NotNull Player player) {
    final LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();

    // Preparing the container and the layout.
    final JPanel panel = new JPanel();
    panel.setLayout(new TableLayout(new double[][] {
      {10, 60, 10}, // columns
      {5, 40, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5}})); // rows

    // Player name label.
    JTextPane textPane = UiService.createDefaultTextPane();
    textPane.setBackground(lafTheme.getDefaultBackgroundColor());
    textPane.setText(player.getName());
    panel.add(textPane, new TableLayoutConstraints(
      0, 1, 2, 1, TableLayout.CENTER, TableLayout.CENTER));

    // Yes button.
    JButton yesButton = new JButton();
    yesButton.setFont(lafTheme.getButtonFont());
    YesNoAnswerAction yesAction = new YesNoAnswerAction(this.questionDialog, player.getIndex(), true);
    yesButton.setAction(yesAction);
    yesButton.addKeyListener(yesAction);
    yesButton.setText(LocaleService.getString("jj.game.answer.button.yes"));
    panel.add(yesButton, new TableLayoutConstraints(
      1, 4, 1, 4, TableLayout.FULL, TableLayout.FULL));

    // No button.
    JButton noButton = new JButton();
    noButton.setFont(lafTheme.getButtonFont());
    YesNoAnswerAction noAction = new YesNoAnswerAction(this.questionDialog, player.getIndex(), false);
    noButton.setAction(noAction);
    noButton.addKeyListener(noAction);
    noButton.setText(LocaleService.getString("jj.game.answer.button.no"));
    panel.add(noButton, new TableLayoutConstraints(
      1, 6, 1, 6, TableLayout.FULL, TableLayout.FULL));

    JPanel containerPanel = new JPanel();
    containerPanel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
    containerPanel.add(panel);

    return containerPanel;
  }

  /**
   * Handles closing of the question dialog.
   */
  private void handleCloseDialogAction() {
    this.questionDialog.resetAndCloseQuestionDialog();
  }

  /**
   * Handles the Show answer button action.
   */
  private void handleShowAnswerAction() {
    AppRegistry.getInstance().getSoundService().stopAllMusic();
    this.questionDialog.stopTimer();
    this.showAnswer();
  }

  /**
   * Shows the answer UI (card).
   */
  private void switchToRegularAnswerCard() {
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_ANSWER_ID);
    this.currentCard = CARD_ANSWER_ID;
    SwingUtilities.invokeLater(() -> QuestionPane.this.closeButton.requestFocus());
  }

  /**
   * Shows the bonus answer UI (card).
   */
  private void switchToBonusAnswerCard() {
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_BONUS_ANSWER_ID);
    this.currentCard = CARD_BONUS_ANSWER_ID;
  }

  /**
   * Creates a container for the text and label (positioned below the text).
   * @param textLabel text area component to add to the container
   * @param imageLabel label that contains the image
   * @return created container
   */
  private @NotNull JPanel createTextImageContainer(JTextArea textLabel, JLabel imageLabel) {
    JPanel answerContainer = new JPanel();
    answerContainer.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {TableLayout.FILL, 15, TableLayout.PREFERRED}})); // rows
    answerContainer.add(textLabel, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.FULL, TableLayout.FULL));
    answerContainer.add(imageLabel, new TableLayoutConstraints(
        0, 2, 0, 2, TableLayout.CENTER, TableLayout.CENTER));
    return answerContainer;
  }

  /**
   * Clears question UI data.
   */
  private void clearQuestionUi() {
    this.questionLabel.setText("");
    this.questionImageLabel.setIcon(null);
    this.questionImageLabel.setPreferredSize(new Dimension(0, 0));
  }
}
