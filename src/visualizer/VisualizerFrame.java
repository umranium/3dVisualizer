/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import visualizer.renderer.RenderPanel;
import visualizer.renderer.RenderableNames;
import visualizer.renderer.TimeScale;
import visualizer.renderer.RenderablePoint;
import visualizer.renderer.RenderableGroup;
import visualizer.renderer.RenderableVector;
import visualizer.renderer.Renderable;
import visualizer.renderer.RenderableSelectionInfo;
import visualizer.renderer.RenderableComponent;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import visualizer.renderer.VisibilityListRenderer;

/**
 *
 * @author Umran
 */
public class VisualizerFrame extends javax.swing.JFrame {

  private static final PlaySpeed[] PLAY_SPEEDS = new PlaySpeed[]{
    new PlaySpeed(1, 64),
    new PlaySpeed(1, 32),
    new PlaySpeed(1, 16),
    new PlaySpeed(1, 8),
    new PlaySpeed(1, 4),
    new PlaySpeed(1, 2),
    new PlaySpeed(1, 1),
    new PlaySpeed(2, 1),};

  public static final int KEY_INCR_TIME = KeyEvent.VK_CLOSE_BRACKET;
  public static final int KEY_DECR_TIME = KeyEvent.VK_OPEN_BRACKET;

  public static final int[] ALL_KEYS = new int[]{
    KEY_INCR_TIME,
    KEY_DECR_TIME
  };

  private RenderableGroup groupRoot;
  private TimeScale timeScale;
  private RenderPanel renderPanel;
  private RenderableComponent previousSelection = null;
  private SpinnerNumberModel visibilityScrollerWndSizeSpnModel;
  private VisibilityCheckBoxListModel visibilityCheckBoxListModel;
  private VisibilityListRenderer visibilityListRenderer;
  private PlayThread playThread = null;
  private JDialog componentTreeDialog = null;

  /**
   * Creates new form Visualizer
   */
  public VisualizerFrame(int initTimeUnitsCount, List<? extends RenderableComponent> renderables) {
    initComponents();

    groupRoot = new RenderableGroup("root", renderables);
    timeScale = new TimeScale(initTimeUnitsCount);
//        currentCheckboxes = new ArrayList<>();

    this.renderPanel = (RenderPanel) displayPanel;
    this.renderPanel.setTimeScale(timeScale);
    this.renderPanel.setRootGroup(groupRoot);
    this.renderPanel.setVisibleTime(timeScale.getMinTime(), timeScale.getMaxTime());

    visibilityScroller.setMinimum(timeScale.getMinTime());
    visibilityScroller.setMaximum(timeScale.getMaxTime()+1);
    visibilityScroller.setUnitIncrement(1);
    visibilityScroller.setBlockIncrement(10);
    visibilityScroller.setVisibleAmount(timeScale.getTimeRange());
    visibilityScroller.setValue(timeScale.getMinTime());
    visibilityScroller.addAdjustmentListener(new AdjustmentListener() {
      @Override
      public void adjustmentValueChanged(AdjustmentEvent e) {
        refreshSceneTime();
      }
    });

    visibilityCheckBoxListModel = new VisibilityCheckBoxListModel();
    visibilityCheckBoxesList.setModel(visibilityCheckBoxListModel);
    visibilityListRenderer = new VisibilityListRenderer();
    visibilityCheckBoxesList.setCellRenderer(visibilityListRenderer);

    visibilityScrollerWndSizeSpnModel = new javax.swing.SpinnerNumberModel(
            timeScale.getTimeRange(), 1, timeScale.getTimeRange(), 1);
    visibilityScrollerWndSizeSpnModel.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        int start = visibilityScroller.getValue();
        int amount = visibilityScrollerWndSizeSpnModel.getNumber().intValue();
        if (start + amount - 1 > timeScale.getMaxTime()) {
          start = timeScale.getMaxTime() - amount + 1;
          visibilityScroller.setValue(start);
        }

        visibilityScroller.setVisibleAmount(amount);
        refreshSceneTime();
      }
    });
    spnVisibilityScrollerWndSize.setModel(visibilityScrollerWndSizeSpnModel);
    fixSpinner(spnVisibilityScrollerWndSize);

    //((JSpinner.DefaultEditor)spnPlaySpeed.getEditor()).getTextField().setEditable(false);
    spnPlaySpeed.setModel(new SpinnerListModel(Arrays.asList(PLAY_SPEEDS)));
    spnPlaySpeed.setValue(PLAY_SPEEDS[2]);
    spnPlaySpeed.setEditor(new JSpinner.DefaultEditor(spnPlaySpeed));

    for (final int key : RenderPanel.ALL_KEYS) {
      String actionMapKey = "RenderPanelInput" + key;

      int ctrlMask = KeyEvent.CTRL_MASK;

      if (OsCommon.isMacOs) {
        ctrlMask = KeyEvent.META_MASK;
      }

      this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
              KeyStroke.getKeyStroke(key, 0),
              actionMapKey);
      this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
              KeyStroke.getKeyStroke(key, ctrlMask),
              actionMapKey);
      this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
              KeyStroke.getKeyStroke(key, KeyEvent.SHIFT_MASK),
              actionMapKey);
      this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
              KeyStroke.getKeyStroke(key, ctrlMask | KeyEvent.SHIFT_MASK),
              actionMapKey);
      this.getRootPane().getActionMap().put(actionMapKey, new AbstractAction(actionMapKey) {
        @Override
        public void actionPerformed(ActionEvent e) {
          renderPanel.processKeys(key, e.getModifiers());
        }
      });
    }

    for (final int key : VisualizerFrame.ALL_KEYS) {
      final String actionMapKey = "VisualizerFrameInput" + key;

      int ctrlMask = KeyEvent.CTRL_MASK;

      if (OsCommon.isMacOs) {
        ctrlMask = KeyEvent.META_MASK;
      }

      this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
              KeyStroke.getKeyStroke(key, 0),
              actionMapKey);
//      this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
//              KeyStroke.getKeyStroke(key, ctrlMask),
//              actionMapKey);
//      this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
//              KeyStroke.getKeyStroke(key, KeyEvent.SHIFT_MASK),
//              actionMapKey);
//      this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
//              KeyStroke.getKeyStroke(key, ctrlMask | KeyEvent.SHIFT_MASK),
//              actionMapKey);
      this.getRootPane().getActionMap().put(actionMapKey, new AbstractAction(actionMapKey) {
        @Override
        public void actionPerformed(ActionEvent e) {
          //System.out.println("received: (actionMapKey="+actionMapKey+")");
          switch (key) {
            case KEY_INCR_TIME: {
              int currentTime = visibilityScroller.getValue();
              int amount = visibilityScrollerWndSizeSpnModel.getNumber().intValue();
              int max = timeScale.getMaxTime() - amount + 1;
              if (currentTime < max) {
                ++currentTime;
                visibilityScroller.setValue(currentTime);
              }
              break;
            }
            case KEY_DECR_TIME: {
              int currentTime = visibilityScroller.getValue();
              int min = timeScale.getMinTime();
              if (currentTime > min) {
                --currentTime;
                visibilityScroller.setValue(currentTime);
              }
              break;
            }
          }
        }
      });
    }

    renderPanel.setLookAtChangedEventHandler(
            new RenderPanel.LookAtChangedEventHandler() {
              @Override
              public void onLookAtChange() {
                lblLocation.setText(renderPanel.getLookAtDescription());
              }
            });
    renderPanel.setLookAtItemChangedEventHandler(
            new RenderPanel.LookAtItemChangedEventHandler() {
              @Override
              public void itemAt(int locX, int locY, List<RenderableSelectionInfo> items) {
                boolean needsRefresh = false;

                RenderableSelectionInfo itemInfo = null;
                if (!items.isEmpty()) {
                  itemInfo = items.get(0);
                }

                if (itemInfo == null) {
                  return;
                }

                if (previousSelection == itemInfo.getObjectInfo()) {
                  return;
                }

                if (previousSelection != null) {
                  previousSelection.setSelected(false);
                  previousSelection = null;
                  needsRefresh = true;
                }

                if (itemInfo == null) {
                  lblStatus.setText("");
                } else {
                  if (items.size() == 1) {
                    lblStatus.setText(itemInfo.getObjectInfo().getDescription());
                  } else {
                    lblStatus.setText(itemInfo.getObjectInfo().getDescription() + " (+ " + (items.size() - 1) + " other items)");
                  }

                  Renderable obj = itemInfo.getObjectInfo().getRenderableObject();
                  if (obj instanceof RenderableComponent) {
                    RenderableComponent comp = (RenderableComponent) obj;
                    comp.setSelected(true);
                    previousSelection = comp;
                    needsRefresh = true;
                  }
                }

                if (needsRefresh) {
                  renderPanel.refreshScene();
                }
              }
            });

    lblLocation.setText(renderPanel.getLookAtDescription());
    chkDisplayAxi.setSelected(renderPanel.getAxi().isVisible());

    setCurrentGroup(groupRoot);

    renderPanel.requestFocus();
  }

  private void fixSpinner(JSpinner spinner) {
    JComponent comp = spinner.getEditor();
    JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
    DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
    formatter.setCommitsOnValidEdit(true);
  }

  private void refreshSceneTime() {
    int start = visibilityScroller.getValue();
    int amount = visibilityScrollerWndSizeSpnModel.getNumber().intValue();
    int end = start + amount - 1;
    renderPanel.setVisibleTime(start, end);
    renderPanel.refreshScene();
  }

  public void setDisplayTime(int from, int to) {
    //visibilityScroller.setVisibleAmount(to-from+1);
    spnVisibilityScrollerWndSize.setValue(to - from + 1);
    visibilityScroller.setValue(from);
    //renderPanel.setVisibleTime(from, to);
  }

  public RenderPanel getRenderPanel() {
    return renderPanel;
  }

  final public void setCurrentGroup(RenderableComponent currentGroup) {
    initUiGroup(currentGroup);
  }

  private static final Map<TextAttribute, Integer> UNDERLINE_FONT_ATTR = new HashMap<TextAttribute, Integer>() {
    {
      put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    }
  };

  private void initUiGroup(RenderableComponent group) {
    visibilityCheckBoxListModel.currentGroupChanged(group);

    List<RenderableNames> names = group.getNameHierarchy();
    namePanel.removeAll();
    boolean first = true;
    for (RenderableNames r : names) {
      if (first) {
        first = false;
      } else {
        JLabel lbl = new JLabel(File.separator);
        lbl.setBorder(new EmptyBorder(0, 2, 0, 2));
        namePanel.add(lbl);
      }

      if (r.component != group && r.component.isGroup()) {
        final RenderableGroup childGroup = (RenderableGroup) r.component;
        JButton btn = new JButton(r.name);
        btn.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            setCurrentGroup(childGroup);
          }
        });
        btn.setFont(btn.getFont().deriveFont(UNDERLINE_FONT_ATTR));
        namePanel.add(btn);
      } else {
        JLabel lbl = new JLabel(r.name);
        namePanel.add(lbl);
      }
    }
    namePanel.revalidate();
    namePanel.repaint();

  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    visibilityCheckBoxesListPopup = new javax.swing.JPopupMenu();
    openMenuItem = new javax.swing.JMenuItem();
    toggleVisibilityMenuItem = new javax.swing.JMenuItem();
    makeVisibleMenuItem = new javax.swing.JMenuItem();
    makeInvisibleMenuItem = new javax.swing.JMenuItem();
    makeTreeVisibleMenuItem = new javax.swing.JMenuItem();
    makeTreeInvisibleMenuItem = new javax.swing.JMenuItem();
    displayPanel = new RenderPanel();
    javax.swing.JPanel bottomBar = new javax.swing.JPanel();
    javax.swing.JPanel controlPanel = new javax.swing.JPanel();
    namePanel = new javax.swing.JPanel();
    visibilityCheckBoxesScrollPanel = new javax.swing.JScrollPane();
    visibilityCheckBoxesList = new javax.swing.JList();
    javax.swing.JPanel visibilityScrollPanel = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
    btnPlay = new javax.swing.JToggleButton();
    spnPlaySpeed = new javax.swing.JSpinner();
    visibilityScroller = new javax.swing.JScrollBar();
    spnVisibilityScrollerWndSize = new javax.swing.JSpinner();
    javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
    btnDisplayAll = new javax.swing.JButton();
    btnDisplayNone = new javax.swing.JButton();
    chkDisplayAxi = new javax.swing.JCheckBox();
    openComponentTree = new javax.swing.JToggleButton();
    javax.swing.JPanel statusBar = new javax.swing.JPanel();
    lblLocation = new javax.swing.JLabel();
    lblStatus = new javax.swing.JLabel();

    openMenuItem.setText("Open");
    openMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openMenuItemActionPerformed(evt);
      }
    });
    visibilityCheckBoxesListPopup.add(openMenuItem);

    toggleVisibilityMenuItem.setLabel("Toggle Visibility");
    toggleVisibilityMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleVisibilityMenuItemActionPerformed(evt);
      }
    });
    visibilityCheckBoxesListPopup.add(toggleVisibilityMenuItem);

    makeVisibleMenuItem.setText("Make Visible");
    makeVisibleMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        makeVisibleMenuItemActionPerformed(evt);
      }
    });
    visibilityCheckBoxesListPopup.add(makeVisibleMenuItem);

    makeInvisibleMenuItem.setText("Make Invisible");
    makeInvisibleMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        makeInvisibleMenuItemActionPerformed(evt);
      }
    });
    visibilityCheckBoxesListPopup.add(makeInvisibleMenuItem);

    makeTreeVisibleMenuItem.setText("Make Tree Visible");
    makeTreeVisibleMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        makeTreeVisibleMenuItemActionPerformed(evt);
      }
    });
    visibilityCheckBoxesListPopup.add(makeTreeVisibleMenuItem);

    makeTreeInvisibleMenuItem.setText("Make Tree Invisible");
    makeTreeInvisibleMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        makeTreeInvisibleMenuItemActionPerformed(evt);
      }
    });
    visibilityCheckBoxesListPopup.add(makeTreeInvisibleMenuItem);

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    displayPanel.setPreferredSize(new java.awt.Dimension(800, 600));

    javax.swing.GroupLayout displayPanelLayout = new javax.swing.GroupLayout(displayPanel);
    displayPanel.setLayout(displayPanelLayout);
    displayPanelLayout.setHorizontalGroup(
      displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 822, Short.MAX_VALUE)
    );
    displayPanelLayout.setVerticalGroup(
      displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 312, Short.MAX_VALUE)
    );

    getContentPane().add(displayPanel, java.awt.BorderLayout.CENTER);

    bottomBar.setLayout(new java.awt.BorderLayout());

    controlPanel.setLayout(new java.awt.BorderLayout());

    namePanel.setPreferredSize(new java.awt.Dimension(400, 40));
    namePanel.setLayout(new java.awt.GridBagLayout());
    controlPanel.add(namePanel, java.awt.BorderLayout.NORTH);

    visibilityCheckBoxesScrollPanel.setMaximumSize(new java.awt.Dimension(200, 32767));
    visibilityCheckBoxesScrollPanel.setPreferredSize(new java.awt.Dimension(200, 150));

    visibilityCheckBoxesList.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "a", "b", "c", "d" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    visibilityCheckBoxesList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
    visibilityCheckBoxesList.setVisibleRowCount(-1);
    visibilityCheckBoxesList.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(java.awt.event.MouseEvent evt) {
        visibilityCheckBoxesListMousePressed(evt);
      }
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        visibilityCheckBoxesListMouseReleased(evt);
      }
    });
    visibilityCheckBoxesScrollPanel.setViewportView(visibilityCheckBoxesList);

    controlPanel.add(visibilityCheckBoxesScrollPanel, java.awt.BorderLayout.CENTER);

    visibilityScrollPanel.setLayout(new java.awt.BorderLayout());

    btnPlay.setText("P");
    btnPlay.setMaximumSize(new java.awt.Dimension(30, 29));
    btnPlay.setMinimumSize(new java.awt.Dimension(30, 29));
    btnPlay.setPreferredSize(new java.awt.Dimension(30, 29));
    btnPlay.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnPlayActionPerformed(evt);
      }
    });
    jPanel2.add(btnPlay);

    spnPlaySpeed.setMaximumSize(new java.awt.Dimension(80, 28));
    spnPlaySpeed.setMinimumSize(new java.awt.Dimension(80, 28));
    spnPlaySpeed.setPreferredSize(new java.awt.Dimension(80, 28));
    jPanel2.add(spnPlaySpeed);

    visibilityScrollPanel.add(jPanel2, java.awt.BorderLayout.WEST);

    visibilityScroller.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
    visibilityScroller.setToolTipText("Visibility Scroller");
    visibilityScrollPanel.add(visibilityScroller, java.awt.BorderLayout.CENTER);

    spnVisibilityScrollerWndSize.setToolTipText("Visibility Scroller Window Size");
    spnVisibilityScrollerWndSize.setPreferredSize(new java.awt.Dimension(100, 22));
    spnVisibilityScrollerWndSize.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        spnVisibilityScrollerWndSizeStateChanged(evt);
      }
    });
    visibilityScrollPanel.add(spnVisibilityScrollerWndSize, java.awt.BorderLayout.EAST);

    controlPanel.add(visibilityScrollPanel, java.awt.BorderLayout.PAGE_END);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    btnDisplayAll.setText("Display All");
    btnDisplayAll.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnDisplayAllActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    jPanel1.add(btnDisplayAll, gridBagConstraints);

    btnDisplayNone.setText("Display None");
    btnDisplayNone.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnDisplayNoneActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    jPanel1.add(btnDisplayNone, gridBagConstraints);

    chkDisplayAxi.setText("Display Axi");
    chkDisplayAxi.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        chkDisplayAxiActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    jPanel1.add(chkDisplayAxi, gridBagConstraints);

    openComponentTree.setText("Component Tree");
    openComponentTree.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openComponentTreeActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    jPanel1.add(openComponentTree, gridBagConstraints);

    controlPanel.add(jPanel1, java.awt.BorderLayout.EAST);

    bottomBar.add(controlPanel, java.awt.BorderLayout.CENTER);

    statusBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
    statusBar.setPreferredSize(new java.awt.Dimension(800, 50));
    statusBar.setLayout(new java.awt.GridLayout(0, 1));
    statusBar.add(lblLocation);
    statusBar.add(lblStatus);

    bottomBar.add(statusBar, java.awt.BorderLayout.SOUTH);

    getContentPane().add(bottomBar, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

private void btnDisplayAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisplayAllActionPerformed

  boolean atLeastOne = false;
  int[] indexes = visibilityCheckBoxesList.getSelectedIndices();
  for (int index : indexes) {
    RenderableComponent comp = visibilityCheckBoxListModel.getElementAt(index);
    if (!comp.isVisible()) {
      comp.setVisible(true);
      atLeastOne = true;
    }
  }
  if (atLeastOne) {
    visibilityCheckBoxListModel.itemsChanged(visibilityCheckBoxesList, indexes);
    renderPanel.refreshScene();
  }


}//GEN-LAST:event_btnDisplayAllActionPerformed

private void btnDisplayNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisplayNoneActionPerformed

  boolean atLeastOne = false;
  int[] indexes = visibilityCheckBoxesList.getSelectedIndices();
  for (int index : indexes) {
    RenderableComponent comp = visibilityCheckBoxListModel.getElementAt(index);
    if (comp.isVisible()) {
      comp.setVisible(false);
      atLeastOne = true;
    }
  }
  if (atLeastOne) {
    visibilityCheckBoxListModel.itemsChanged(visibilityCheckBoxesList, indexes);
    renderPanel.refreshScene();
  }

}//GEN-LAST:event_btnDisplayNoneActionPerformed

private void spnVisibilityScrollerWndSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnVisibilityScrollerWndSizeStateChanged

  visibilityScroller.setBlockIncrement((Integer) spnVisibilityScrollerWndSize.getValue());
  refreshSceneTime();

}//GEN-LAST:event_spnVisibilityScrollerWndSizeStateChanged

private void visibilityCheckBoxesListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_visibilityCheckBoxesListMousePressed

  if (evt.isPopupTrigger()) {
    processListPopup(evt);
  }

}//GEN-LAST:event_visibilityCheckBoxesListMousePressed

private void visibilityCheckBoxesListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_visibilityCheckBoxesListMouseReleased

  if (evt.isPopupTrigger()) {
    processListPopup(evt);
  }

}//GEN-LAST:event_visibilityCheckBoxesListMouseReleased

private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed

  int index = visibilityCheckBoxesList.getSelectedIndex();
  if (index >= 0) {
    RenderableComponent comp = visibilityCheckBoxListModel.getElementAt(index);
    if (comp.isGroup()) {
      setCurrentGroup(comp);
    }
  }

}//GEN-LAST:event_openMenuItemActionPerformed

private void makeVisibleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeVisibleMenuItemActionPerformed

  setSelectedCheckboxesVisible(false, true, false);

}//GEN-LAST:event_makeVisibleMenuItemActionPerformed

private void makeInvisibleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeInvisibleMenuItemActionPerformed

  setSelectedCheckboxesVisible(false, false, false);

}//GEN-LAST:event_makeInvisibleMenuItemActionPerformed

private void makeTreeVisibleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeTreeVisibleMenuItemActionPerformed

  setSelectedCheckboxesVisible(true, true, false);

}//GEN-LAST:event_makeTreeVisibleMenuItemActionPerformed

private void makeTreeInvisibleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeTreeInvisibleMenuItemActionPerformed

  setSelectedCheckboxesVisible(true, false, false);

}//GEN-LAST:event_makeTreeInvisibleMenuItemActionPerformed

    private void chkDisplayAxiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDisplayAxiActionPerformed

      renderPanel.getAxi().setVisible(chkDisplayAxi.isSelected());
      renderPanel.refreshScene();

    }//GEN-LAST:event_chkDisplayAxiActionPerformed

    private void btnPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayActionPerformed

      if (playThread != null) {
        playThread.quit();
      }

      if (btnPlay.isSelected()) {
        playThread = new PlayThread();
        playThread.start();
      }

    }//GEN-LAST:event_btnPlayActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
      if (playThread != null) {
        playThread.quit();
      }
      renderPanel.destroy();

    }//GEN-LAST:event_formWindowClosing

  private void toggleVisibilityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleVisibilityMenuItemActionPerformed

    setSelectedCheckboxesVisible(false, false, true);

  }//GEN-LAST:event_toggleVisibilityMenuItemActionPerformed

  private void openComponentTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openComponentTreeActionPerformed

    //System.out.println("openComponentTree.isSelected()="+openComponentTree.isSelected());
    if (openComponentTree.isSelected() == (componentTreeDialog != null)) {
      return;
    }

    if (openComponentTree.isSelected()) {
      VisibilityTreePanel panel = new VisibilityTreePanel(groupRoot);
      panel.setVisibilityChangeListener(new VisibilityTreePanel.ComponentVisibilityChangeListener() {
        @Override
        public void componentsVisibilityChanged(List<RenderableComponent> components) {
          renderPanel.refreshScene();

          RenderableComponent currentGroup = visibilityCheckBoxListModel.currentGroup;

          int[] changedChildren = new int[currentGroup.getChildren().size()];
          int changedCount = 0;
          for (RenderableComponent comp : components) {
            if (comp.getParentComponent() == currentGroup) {
              int index = currentGroup.getChildren().indexOf(comp);
              changedChildren[changedCount] = index;
              ++changedCount;
            }
          }

          if (changedCount > 0) {
            visibilityCheckBoxListModel.itemsChanged(visibilityCheckBoxesList,
                    Arrays.copyOf(changedChildren, changedCount)
            );
          }
        }
      });

//      JDialog dialog = new JDialog();
      JDialog dialog = new JDialog(this, false);
      dialog.setContentPane(panel);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.pack();
      dialog.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          componentTreeDialog = null;
          openComponentTree.setSelected(false);
        }
      });
      dialog.setVisible(true);
      componentTreeDialog = dialog;
    } else {
      componentTreeDialog.setVisible(false);
      componentTreeDialog = null;
    }

  }//GEN-LAST:event_openComponentTreeActionPerformed

  private void setSelectedCheckboxesVisible(boolean tree, boolean visible, boolean toggle) {
    boolean atLeastOne = false;
    int[] indexes = visibilityCheckBoxesList.getSelectedIndices();
    for (int index : indexes) {
      RenderableComponent comp = visibilityCheckBoxListModel.getElementAt(index);
      if (tree) {
        comp.setTreeVisible(visible);
      } else {
        if (toggle) {
          comp.setVisible(!comp.isVisible());
        } else {
          comp.setVisible(visible, true);
        }
      }
      atLeastOne = true;
    }

    if (atLeastOne) {
      visibilityCheckBoxListModel.itemsChanged(visibilityCheckBoxesList, indexes);
      renderPanel.refreshScene();
    }
  }

  private void processListPopup(java.awt.event.MouseEvent evt) {
//        int locItemIndex = visibilityCheckBoxesList.locationToIndex(evt.getPoint());
//        if (locItemIndex>=0) {
//            visibilityCheckBoxesList.addSelectionInterval(locItemIndex, locItemIndex);
//        }

    int countSelectedVisible = 0;
    int countSelectedInvisible = 0;
    int countGroups = 0;

    for (int index : visibilityCheckBoxesList.getSelectedIndices()) {
      RenderableComponent comp = visibilityCheckBoxListModel.getElementAt(index);
      if (comp.isVisible()) {
        ++countSelectedVisible;
      } else {
        ++countSelectedInvisible;
      }
      if (comp.isGroup()) {
        ++countGroups;
      }
    }

    int countSelected = countSelectedVisible + countSelectedInvisible;

    openMenuItem.setEnabled(countSelected == 1 && countGroups == 1);
    makeVisibleMenuItem.setEnabled(countSelected > 0);
    makeInvisibleMenuItem.setEnabled(countSelected > 0);
    makeTreeVisibleMenuItem.setEnabled(countSelected > 0);
    makeTreeInvisibleMenuItem.setEnabled(countSelected > 0);

    visibilityCheckBoxesListPopup.show(visibilityCheckBoxesList, evt.getX(), evt.getY());
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(VisualizerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(VisualizerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(VisualizerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(VisualizerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        RenderablePoint point1 = new RenderablePoint(new float[]{-1.0f, 0.0f, 0.0f});
        RenderablePoint point2 = new RenderablePoint(new float[]{0.0f, -1.0f, 0.0f});
        RenderablePoint point3 = new RenderablePoint(new float[]{0.0f, 0.0f, -1.0f});

        RenderableVector vec1 = new RenderableVector(new float[]{2.0f, 0.0f, 0.0f}, new float[]{3.0f, 0.0f, 0.0f});
        RenderableVector vec2 = new RenderableVector(new float[]{0.0f, 2.0f, 0.0f}, new float[]{0.0f, 3.0f, 0.0f});
        RenderableVector vec3 = new RenderableVector(new float[]{0.0f, 0.0f, 2.0f}, new float[]{0.0f, 0.0f, 3.0f});

//                point1.setSize(1.0f);
//                point2.setSize(1.0f);
//                point3.setSize(1.0f);
//                vec1.setSize(0.1f);
        vec1.setColor(Color.RED);
//                vec2.setSize(0.1f);
        vec2.setColor(Color.GREEN);
//                vec3.setSize(0.1f);
        vec3.setColor(Color.BLUE);

        VisualizerFrame vf = new VisualizerFrame(1, Arrays.asList(vec1, vec2, vec3));
//                vf.renderPanel.getAxi().setAllowSelection(true);
        vf.setVisible(true);
      }
    });
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnDisplayAll;
  private javax.swing.JButton btnDisplayNone;
  private javax.swing.JToggleButton btnPlay;
  private javax.swing.JCheckBox chkDisplayAxi;
  private javax.swing.JPanel displayPanel;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JLabel lblLocation;
  private javax.swing.JLabel lblStatus;
  private javax.swing.JMenuItem makeInvisibleMenuItem;
  private javax.swing.JMenuItem makeTreeInvisibleMenuItem;
  private javax.swing.JMenuItem makeTreeVisibleMenuItem;
  private javax.swing.JMenuItem makeVisibleMenuItem;
  private javax.swing.JPanel namePanel;
  private javax.swing.JToggleButton openComponentTree;
  private javax.swing.JMenuItem openMenuItem;
  private javax.swing.JSpinner spnPlaySpeed;
  private javax.swing.JSpinner spnVisibilityScrollerWndSize;
  private javax.swing.JMenuItem toggleVisibilityMenuItem;
  private javax.swing.JList visibilityCheckBoxesList;
  private javax.swing.JPopupMenu visibilityCheckBoxesListPopup;
  private javax.swing.JScrollPane visibilityCheckBoxesScrollPanel;
  private javax.swing.JScrollBar visibilityScroller;
  // End of variables declaration//GEN-END:variables

  private static class PlaySpeed {

    private final int num;
    private final int denom;

    public PlaySpeed(int num, int denom) {
      this.num = num;
      this.denom = denom;
    }

    @Override
    public String toString() {
      if (denom == 1) {
        return Integer.toString(num) + "x";
      } else {
        return Integer.toString(num) + "/" + Integer.toString(denom) + "x";
      }
    }

  }

  private class VisibilityCheckBoxListModel extends AbstractListModel<RenderableComponent> {

    private RenderableComponent currentGroup;

    public void currentGroupChanged(RenderableComponent group) {
      int prevCount = 0;
      if (currentGroup != null) {
        prevCount = currentGroup.getChildren().size();
      }
      currentGroup = group;

      int updateCount = Math.min(prevCount, currentGroup.getChildren().size());
      int addedCount = currentGroup.getChildren().size() - updateCount;
      int removedCount = prevCount - updateCount;

      if (updateCount > 0) {
        fireContentsChanged(this, 0, updateCount - 1);
      }
      if (addedCount > 0) {
        fireIntervalAdded(this, updateCount, updateCount + addedCount - 1);
      }
      if (removedCount > 0) {
        fireIntervalRemoved(this, updateCount, updateCount + removedCount - 1);
      }

    }

    public void itemsChanged(Object source, int[] indexes) {
      Arrays.sort(indexes);

      List<int[]> ranges = new ArrayList<>();
      int currentStart = -1, currentEnd = -1;
      for (int i : indexes) {
        if (currentStart == -1) {
          currentStart = i;
          currentEnd = i;
        } else {
          if (i <= currentEnd + 1) {
            currentEnd = i;
          } else {
            ranges.add(new int[]{currentStart, i});

            currentStart = -1;
            currentEnd = -1;
          }
        }
      }
      if (currentStart >= 0) {
        ranges.add(new int[]{currentStart, currentEnd});
      }

      for (int[] range : ranges) {
        fireContentsChanged(range, range[0], range[1]);
      }
    }

    @Override
    public int getSize() {
      return currentGroup == null ? 0 : currentGroup.getChildren().size();
    }

    @Override
    public RenderableComponent getElementAt(int index) {
      if (currentGroup == null) {
        return null;
      }

      return currentGroup.getChildren().get(index);
    }
  }

  private class PlayThread extends Thread {

    private boolean quit;

    public void quit() {
      this.quit = true;
      try {
        this.join();
      } catch (InterruptedException ex) {
      }
    }

    @Override
    public void run() {
      try {
        if (timeScale.getMinTime() == Integer.MIN_VALUE
                || timeScale.getMaxTime() == Integer.MAX_VALUE) {
          return;
        }

        final long[] lastVal = new long[]{visibilityScroller.getValue()};
        final long[] lastTime = new long[]{System.currentTimeMillis()};

        final int[] amount = new int[]{visibilityScrollerWndSizeSpnModel.getNumber().intValue()};
        while (!quit) {
          java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              PlaySpeed speed = (PlaySpeed) spnPlaySpeed.getValue();

              long currentTime = System.currentTimeMillis();
              long elapsedTime = currentTime - lastTime[0];
              int frames = (int) ((Common.FREQ * elapsedTime * speed.num) / (1000 * speed.denom));
              if (frames == 0) {
                return;
              }

              lastVal[0] = visibilityScroller.getValue() + frames;
              lastTime[0] = currentTime;

              if (lastVal[0] + amount[0] < timeScale.getMaxTime()) {
                visibilityScroller.setValue((int) lastVal[0]);
              } else {
                visibilityScroller.setValue((int) 0);
                //quit = true;
              }
            }
          });
          amount[0] = visibilityScrollerWndSizeSpnModel.getNumber().intValue();
          try {
            Thread.sleep(50);
          } catch (InterruptedException ex) {
            // ignore
          }
        }
      } finally {
        btnPlay.setSelected(false);
        playThread = null;
        System.out.println("Play thread done.");
      }
    }

  }

}
