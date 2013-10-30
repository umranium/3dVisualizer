/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author abd01c
 */
public class VisibilityListRenderer extends JCheckBox implements ListCellRenderer<RenderableComponent> {

    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;
    
    private static final Map<TextAttribute, Integer> UNDERLINE_FONT_ATTR = new HashMap<TextAttribute, Integer>() {
        {
            put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
    };


    @Override
    public Component getListCellRendererComponent(JList<? extends RenderableComponent> list,
            RenderableComponent value, int index, boolean isSelected, boolean cellHasFocus) {
        
        this.setText(value.getName());
        this.setToolTipText(value.getDescription());
        this.setSelected(value.isVisible());
        
        if (isSelected) {
            this.setOpaque(true);
            this.setForeground(list.getSelectionForeground());
            this.setBackground(list.getSelectionBackground());
        } else {
            this.setOpaque(false);
            this.setForeground(list.getForeground());
            this.setBackground(list.getBackground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        if (value.isGroup()) {
            setFont(list.getFont().deriveFont(UNDERLINE_FONT_ATTR));
        }
        
//        Border border = null;
//        if (cellHasFocus) {
//            if (isSelected) {
//                border = DefaultLookup.getBorder(this, ui, "List.focusSelectedCellHighlightBorder");
//            }
//            if (border == null) {
//                border = DefaultLookup.getBorder(this, ui, "List.focusCellHighlightBorder");
//            }
//        } else {
//            border = getNoFocusBorder();
//        }
//        setBorder(border);

        return this;
    }

//    private Border getNoFocusBorder() {
//        Border border = DefaultLookup.getBorder(this, ui, "List.cellNoFocusBorder");
//        if (System.getSecurityManager() != null) {
//            if (border != null) {
//                return border;
//            }
//            return SAFE_NO_FOCUS_BORDER;
//        } else {
//            if (border != null
//                    && (noFocusBorder == null
//                    || noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
//                return border;
//            }
//            return noFocusBorder;
//        }
//    }
}
