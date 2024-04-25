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

import net.curre.jjeopardy.ui.edit.EditTable;
import net.curre.jjeopardy.ui.edit.EditableCell;
import net.curre.jjeopardy.ui.edit.OverlayActionLabel;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Handler for various edit table mouse actions (clicking on cells, moving over cells).
 *
 * @author Yevgeny Nyden
 */
public class EditTableMouseListener extends MouseAdapter implements MouseListener, MouseMotionListener {

  /** Reference to the edit table. */
  private final EditTable editTable;

  /** Indicates that the edit mode is enabled. */
  private boolean editEnabled;

  /**
   * Ctor.
   * @param editTable reference to the edit table
   * @param editEnabled true if the editing is enabled
   */
  public EditTableMouseListener(EditTable editTable, boolean editEnabled) {
    this.editTable = editTable;
    this.editEnabled = editEnabled;
  }

  /**
   * Sets the editing enabled bit on this listener.
   * @param editEnabled true if the actions should be enabled on this listener; false if disabled
   */
  public void setEditEnabled(boolean editEnabled) {
    this.editEnabled = editEnabled;
  }

  /** Does nothing. */
  @Override
  public void mouseClicked(MouseEvent e) {}

  /** Does nothing. */
  @Override
  public void mousePressed(MouseEvent e) {}

  /**
   * Opens the Edit question dialog for the cell over which the mouse is released.
   * <br><br>
   * @inheritDoc
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    if (this.editEnabled && !e.isConsumed()) {
      EditableCell cell = this.getEditCellFromEvent(e);
      if (cell != null) {
        cell.showEditDialog();
      }
    }
  }

  /**
   * Highlights the hovered cell.
   * <br><br>
   * @inheritDoc
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    if (this.editEnabled) {
      EditableCell cell = this.getEditCellFromEvent(e);
      if (cell != null) {
        if (e.getComponent() instanceof OverlayActionLabel) {
          // Highlight the whole column effect on header overlay buttons.
          if (((OverlayActionLabel) e.getComponent()).isColumnHover()) {
            this.editTable.decorateColumnHoverState(cell.getColumnIndex(), true);
          }
        }
        cell.decorateHoverState(true, true);
      }
    }
  }

  /**
   * Clears highlighted cell when mouse exits it.
   * <br><br>
   * @inheritDoc
   */
  @Override
  public void mouseExited(MouseEvent e) {
    if (this.editEnabled) {
      EditableCell cell = this.getEditCellFromEvent(e);
      if (cell != null) {
        if (e.getComponent() instanceof OverlayActionLabel) {
          // Highlight the whole column effect on header overlay buttons.
          if (((OverlayActionLabel) e.getComponent()).isColumnHover()) {
            this.editTable.decorateColumnHoverState(cell.getColumnIndex(), false);
          }
        }
        cell.decorateHoverState(false, true);
      }
    }
  }

  /** Does nothing. */
  @Override
  public void mouseMoved(MouseEvent e) {}

  /**
   * Finds the EditCell the mouse event has occurred.
   * @param event mouse event
   * @return Edit cell reference or null if unable to find
   */
  private @Nullable EditableCell getEditCellFromEvent(@NotNull MouseEvent event) {
    Component component = event.getComponent();
    if (component instanceof EditableCell) {
      return ((EditableCell) component);
    } else if (component.getParent() instanceof EditableCell)  {
      // Handle events on the child components (text panes).
      return ((EditableCell) component.getParent());
    } else if (component.getParent().getParent() instanceof EditableCell) {
      // Handle events on the overlays.
      return ((EditableCell) component.getParent().getParent());
    }
    return null;
  }
}
