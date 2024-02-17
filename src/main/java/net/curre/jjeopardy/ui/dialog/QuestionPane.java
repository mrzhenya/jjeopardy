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
import net.curre.jjeopardy.event.YesNoAnswerAction;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

  /** String unique ID to use with CardLayout for the empty card. */
  private static final String CARD_EMPTY_ID = "empty";

  /** Vertical padding for the text field (e.g. where question or answer if displayed). */
  private static final int TEXT_PANE_V_PADDING = 40;

  /** Width of the yes/no buttons on the bonus question pane. */
  private static final int BONUS_BUTTON_WIDTH = 100;

  /** Reference to the question dialog. */
  private final QuestionDialog questionDialog;

  /** Question label. */
  private JTextArea questionLabel;

  /** Answer label. */
  private JTextArea answerLabel;

  /** Bonus answer label. */
  private JTextArea bonusAnswerLabel;

  /** True if bonus questions round is active. */
  private boolean isBonusQuestionsRound;

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

    // The empty panel (to assist with transitions).
    JPanel emptyPanel = this.createEmptyPanel();
    this.add(emptyPanel, CARD_EMPTY_ID);
  }

  /**
   * Shows the question UI (card).
   * @param question question to ask
   * @param isBonus true if this is a bonus question
   */
  public void showQuestion(Question question, boolean isBonus) {
    this.questionLabel.setText(question.getQuestion());
    this.isBonusQuestionsRound = isBonus;
    if (isBonus) {
      this.bonusAnswerLabel.setText(question.getAnswer());
    } else {
      this.answerLabel.setText(question.getAnswer());
    }
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_QUESTION_ID);
  }

  /**
   * Shows the bonus intro UI (card).
   */
  public void showBonusIntro() {
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_BONUS_INTRO_ID);
  }

  /**
   * Shows the answer UI (card).
   */
  public void showAnswer() {
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_ANSWER_ID);
  }

  /**
   * Shows the bonus answer UI (card).
   */
  public void showBonusAnswer() {
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_BONUS_ANSWER_ID);
  }

  /**
   * Switches card layout to the empty card.
   */
  protected void switchToEmptyCard() {
    CardLayout clay = (CardLayout) this.getLayout();
    clay.show(this, CARD_EMPTY_ID);
  }

  /**
   * Clears text labels to assist with transitioning to the new content.
   */
  protected void clearTextLabels() {
    this.questionLabel.setText("");
    this.answerLabel.setText("");
    this.bonusAnswerLabel.setText("");
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
      {TEXT_PANE_V_PADDING, TableLayout.FILL, TEXT_PANE_V_PADDING, TableLayout.PREFERRED}})); // rows

    // Text are where the question is displayed.
    this.questionLabel = BasicDialog.createDefaultTextArea(lafTheme.getQuestionTextFont());
    questionPanel.add(questionLabel, new TableLayoutConstraints(
      0, 1, 0, 1, TableLayout.FULL, TableLayout.FULL));

    // ******** Show answer button.
    JButton answerButton = new JButton();
    ShowAnswerAction answerAction = new ShowAnswerAction();
    answerButton.setAction(answerAction);
    answerButton.addKeyListener(answerAction);
    answerButton.setFont(lafTheme.getButtonFont());
    answerButton.setText(LocaleService.getString("jj.game.question.buttons.show.name"));
    questionPanel.add(answerButton, new TableLayoutConstraints(
      0, 3, 0, 3, TableLayout.CENTER, TableLayout.CENTER));

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

    // ******* Answer text.
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.answerLabel = BasicDialog.createDefaultTextArea(lafTheme.getQuestionTextFont());
    answerPanel.add(answerLabel, new TableLayoutConstraints(
      0, 1, 0, 1, TableLayout.FULL, TableLayout.FULL));

    // ******* Main question Yes/No answer panels (for each player) - hidden for bonus answers.
    final JPanel mainAnswerPanel = new JPanel();
    mainAnswerPanel.setVisible(true);
    mainAnswerPanel.setLayout(new TableLayout(new double[][] {
      {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
        TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}, // columns
      {TableLayout.FILL}})); // rows

    List<Player> players = AppRegistry.getInstance().getGameDataService().getCurrentPlayers();
    for (int index = 0; index < players.size(); index++) {
      Player player = players.get(index);
      JPanel aPanel = this.createYesNoAnswerPanel(player);
      mainAnswerPanel.add(aPanel, new TableLayoutConstraints(
        index, 0, index, 0, TableLayout.FULL, TableLayout.FULL));
    }
    answerPanel.add(mainAnswerPanel, new TableLayoutConstraints(
      0, 3, 0, 3, TableLayout.CENTER, TableLayout.FULL));

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
    panel.add(bonusAnswerLabel, new TableLayoutConstraints(
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
   * Creates an empty JPanel to assist with transitions.
   * @return empty JPanel
   */
  private JPanel createEmptyPanel() {
    final JPanel panel = new JPanel();
    panel.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL}, // columns
      {TableLayout.FILL}})); // rows
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
    JButton yes = new JButton();
    yes.setFont(lafTheme.getButtonFont());
    yes.setAction(new YesNoAnswerAction(this.questionDialog, player.getIndex(), true));
    yes.setText(LocaleService.getString("jj.game.answer.button.yes"));
    panel.add(yes, new TableLayoutConstraints(
      1, 4, 1, 4, TableLayout.FULL, TableLayout.FULL));

    // No button.
    JButton no = new JButton();
    no.setFont(lafTheme.getButtonFont());
    no.setAction(new YesNoAnswerAction(this.questionDialog, player.getIndex(), false));
    no.setText(LocaleService.getString("jj.game.answer.button.no"));
    panel.add(no, new TableLayoutConstraints(
      1, 6, 1, 6, TableLayout.FULL, TableLayout.FULL));

    JPanel containerPanel = new JPanel();
    containerPanel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
    containerPanel.add(panel);

    return containerPanel;
  }

  /**
   * Handler for the Show answer button.
   */
  private class ShowAnswerAction extends AbstractAction implements KeyListener {

    /** Ctor. */
    private ShowAnswerAction() {}

    @Override
    public void actionPerformed(ActionEvent e) {
      this.performAction();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        this.performAction();
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    /** Performs the action. */
    private void performAction() {
      AppRegistry.getInstance().getSoundService().stopAllMusic();
      QuestionPane.this.questionDialog.stopTimer();
      if (QuestionPane.this.isBonusQuestionsRound) {
        QuestionPane.this.showBonusAnswer();
      } else {
        QuestionPane.this.showAnswer();
      }
    }
  }
}
