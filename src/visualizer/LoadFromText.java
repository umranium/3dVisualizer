/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoadFromText.java
 *
 * Created on Jan 8, 2013, 1:02:50 PM
 */
package visualizer;

import visualizer.utils.Vector3f;
import visualizer.renderer.RenderablePoint;
import visualizer.renderer.RenderableVector;
import visualizer.renderer.RenderableComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 *
 * @author abd01c
 */
public class LoadFromText extends javax.swing.JFrame {

    /** Creates new form LoadFromText */
    public LoadFromText() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        btnGrpInputType = new javax.swing.ButtonGroup();
        txtScrollPane = new javax.swing.JScrollPane();
        txtPane = new javax.swing.JTextPane();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        chkPoints = new javax.swing.JRadioButton();
        chk3dVectors = new javax.swing.JRadioButton();
        chk6dVectors = new javax.swing.JRadioButton();
        chkNormalizeVectors = new javax.swing.JCheckBox();
        btnView = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        txtScrollPane.setMinimumSize(new java.awt.Dimension(200, 200));
        txtScrollPane.setPreferredSize(new java.awt.Dimension(200, 200));
        txtScrollPane.setViewportView(txtPane);

        getContentPane().add(txtScrollPane, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Input Type"));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        btnGrpInputType.add(chkPoints);
        chkPoints.setSelected(true);
        chkPoints.setText("Points (3 dim)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(chkPoints, gridBagConstraints);

        btnGrpInputType.add(chk3dVectors);
        chk3dVectors.setText("Vectors from origin (3d)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(chk3dVectors, gridBagConstraints);

        btnGrpInputType.add(chk6dVectors);
        chk6dVectors.setText("Vectors (6 dim)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(chk6dVectors, gridBagConstraints);

        jPanel1.add(jPanel2);

        chkNormalizeVectors.setText("Normalize Vectors");
        jPanel1.add(chkNormalizeVectors);

        btnView.setText("View");
        btnView.setPreferredSize(new java.awt.Dimension(100, 25));
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });
        jPanel1.add(btnView);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed

    int expectedDims = 3;
    if (chk6dVectors.isSelected()) {
        expectedDims = 6;
    }
    
    List<float[]> values = new ArrayList<>();
    Scanner scan = new Scanner(txtPane.getText());
    int lineNumber = 0;
    while (scan.hasNextLine()) {
        String line = scan.nextLine();
        ++lineNumber;
        String[] strVals = line.split("\t");
        if (strVals.length!=expectedDims) {
            JOptionPane.showMessageDialog(rootPane, "Expected "+expectedDims
                    +" columns on line "+lineNumber+", but only "+
                    strVals.length+" found.");
            return;
        }
        float[] vals = new float[strVals.length];
        for (int c=0; c<strVals.length; ++c) {
            try {
                float v = Float.parseFloat(strVals[c]);
                vals[c] = v;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(rootPane,
                        "Number parsing error on line "+lineNumber+
                        ", column "+(c+1));
                return;
            }
        }
        values.add(vals);
    }
    
    boolean areVectors = true;
    if (chkPoints.isSelected()) {
        areVectors = false;
    }
    
    boolean normalize = false;
    if (areVectors) {
        normalize = chkNormalizeVectors.isSelected();
    }
            
    final List<RenderableComponent> renderables = new ArrayList<>();
    Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
    
    for (int i=0; i<values.size(); ++i) {
        RenderableComponent comp;
        if (areVectors) {
            Vector3f fromV;
            Vector3f toV;
            float[] vals = values.get(i);
            
            if (expectedDims==3) {
                fromV = origin;
                toV = new Vector3f(vals[0], vals[1], vals[2]);
            } else {
                fromV = new Vector3f(vals[0], vals[1], vals[2]);
                toV = new Vector3f(vals[3], vals[4], vals[5]);
            }
            
            if (normalize) {
                toV = fromV.add(toV.subtract(fromV).normalize());
            }
            
            float[] fromA = new float[] {fromV.x, fromV.y, fromV.z};
            float[] toA = new float[] {toV.x, toV.y, toV.z};
            
            comp = new RenderableVector(fromA, toA);
        } else {
            comp = new RenderablePoint(values.get(i));
        }
        
        renderables.add(comp);
    }
    
    java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                VisualizerFrame frame = new VisualizerFrame(1, renderables);
                frame.setVisible(true);
            }
        
    });
    
    
    
}//GEN-LAST:event_btnViewActionPerformed

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
            java.util.logging.Logger.getLogger(LoadFromText.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoadFromText.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoadFromText.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoadFromText.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new LoadFromText().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGrpInputType;
    private javax.swing.JButton btnView;
    private javax.swing.JRadioButton chk3dVectors;
    private javax.swing.JRadioButton chk6dVectors;
    private javax.swing.JCheckBox chkNormalizeVectors;
    private javax.swing.JRadioButton chkPoints;
    private javax.swing.JTextPane txtPane;
    private javax.swing.JScrollPane txtScrollPane;
    // End of variables declaration//GEN-END:variables
}
