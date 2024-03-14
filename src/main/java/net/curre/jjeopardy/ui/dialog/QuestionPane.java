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
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.event.BonusQuestionAction;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.event.YesNoAnswerAction;
import net.curre.jjeopardy.images.ImageUtilities;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;
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
  public void showQuestion(Question question, boolean isBonus) {
    this.questionLabel.setText(question.getQuestion());
    this.isBonusQuestionsRound = isBonus;
    updateLabelIconImage(question.getQuestionImage(), this.questionImageLabel);
    if (isBonus) {
      updateLabelIconImage(question.getAnswerImage(), this.bonusAnswerImageLabel);
      this.bonusAnswerLabel.setText(question.getAnswer());
      this.answerYesNoButtonsPanel.setVisible(false);
    } else {
      updateLabelIconImage(question.getAnswerImage(), this.answerImageLabel);
      this.answerLabel.setText(question.getAnswer());
      this.answerYesNoButtonsPanel.setVisible(true);
    }
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_QUESTION_ID);
    SwingUtilities.invokeLater(this.questionDialog::pack);
  }

  /**
   * Shows the bonus intro UI (card).
   */
  public void showBonusIntro() {
    this.questionDialog.pack();
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_BONUS_INTRO_ID);
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
   * Creates the UI for the question UI.
   * @return created and initialized JPanel
   */
  private JPanel createQuestionPanel() {
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
  private JPanel createBonusIntroPanel() {
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
  private JPanel createAnswerPanel() {
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
    JButton closeButton = new JButton();
    ClickAndKeyAction.createAndAddAction(closeButton, this::handleCloseDialogAction);
    closeButton.setFont(lafTheme.getButtonFont());
    closeButton.setText(LocaleService.getString("jj.game.question.buttons.close.name"));
    answerPanel.add(closeButton, new TableLayoutConstraints(
        0, 3, 0, 3, TableLayout.CENTER, TableLayout.CENTER));

    return answerPanel;
  }

  /**
   * Creates the bonus answer UI.
   * @return bonus answer JPanel
   */
  private JPanel createBonusAnswerPanel() {
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
  private JPanel createYesNoAnswerPanel(Player player) {
    final LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();

    // Preparing the container and the layout.
    final JPanel panel = new JPanel();
    panel.setLayout(new TableLayout(new double[][] {
      {10, 60, 10}, // columns
      {5, 40, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5}})); // rows

    // Player name label.
    JTextPane textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setFocusable(false);
    textPane.setDragEnabled(false);
    textPane.setOpaque(true);
    textPane.setBackground(lafTheme.getDefaultBackgroundColor());
    StyledDocument doc = textPane.getStyledDocument();
    SimpleAttributeSet center = new SimpleAttributeSet();
    StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
    doc.setParagraphAttributes(0, doc.getLength(), center, false);
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
   * Updates labels icon image. If no image is provided, image data is erased on the label.
   * @param imagePath image path
   * @param label label on which the icon image is being updated
   */
  private void updateLabelIconImage(String imagePath, JLabel label) {
    ImageIcon imageIcon = null;
    GameData gameData = AppRegistry.getInstance().getGameDataService().getCurrentGameData();
    if (imagePath != null && imagePath.startsWith("http")) {
      imageIcon = ImageUtilities.downloadTempImageResource(imagePath);
    } else if (imagePath != null && gameData.getBundlePath() != null) {
      imageIcon = new ImageIcon(gameData.getBundlePath() + File.separatorChar + imagePath);
    }
    if (imageIcon == null) {
      label.setIcon(null);
      label.setPreferredSize(new Dimension(0, 0));
      label.setSize(new Dimension(0, 0));
    } else {
      int iconHeight = imageIcon.getIconHeight();
      int iconWidth = imageIcon.getIconWidth();
      // Resizing the image to the max width/height dimensions.
      boolean portraitOrient = iconHeight > iconWidth;
      if (portraitOrient) {
        iconWidth = (int) (iconWidth / ((double) iconHeight / MAX_IMAGE_HEIGHT));
        iconHeight = MAX_IMAGE_HEIGHT;
      } else {
        iconHeight = (int) (iconHeight / ((double) iconWidth / MAX_IMAGE_WIDTH));
        iconWidth = MAX_IMAGE_WIDTH;
      }
      imageIcon.setImage(imageIcon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_FAST));
      label.setIcon(imageIcon);
      label.setPreferredSize(new Dimension(iconWidth, iconHeight));
    }
    label.repaint();
  }

  /**
   * Handles closing of the question dialog.
   */
  private void handleCloseDialogAction() {
    this.questionDialog.hideQuestionDialog();
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
  }

  /**
   * Shows the bonus answer UI (card).
   */
  private void switchToBonusAnswerCard() {
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_BONUS_ANSWER_ID);
  }

  /**
   * Creates a container for the text and label (positioned below the text).
   * @param textLabel text area component to add to the container
   * @param imageLabel label that contains the image
   * @return created container
   */
  private JPanel createTextImageContainer(JTextArea textLabel, JLabel imageLabel) {
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
