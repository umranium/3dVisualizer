/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author umran
 */
public class MouseHandler {

    private RenderPanel renderPanel;
    private Point lastPressLoc = null;
    private double[] lastPressPitchBearing = null;
    
    public MouseHandler(RenderPanel renderPanel) {
        this.renderPanel = renderPanel;
        
    }
    
    public void mouseClicked(MouseEvent e) {
        final int button = e.getButton();
        if (button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3) {
            renderPanel.findItemAtLoc(e.getX(), e.getY(),
                    new RenderPanel.ItemAtLocCallback() {
                @Override
                public void itemAt(int locX, int locY, ViewInfo viewInfo,
                        List<RenderableSelectionInfo> itemInfo) {
                    if (!itemInfo.isEmpty()) {
                        RenderableSelectionInfo item = itemInfo.get(0);
                        double[] loc = RendererUtils.unproject(
                                renderPanel.getGLU(), locX, locY, item.getDepth(), viewInfo);
                        System.out.println("click-loc: " + Arrays.toString(loc) + " depth=" + item.getDepth());
                        switch (button) {
                            case MouseEvent.BUTTON1:
                                adjustEyeTo(loc);
                                break;
                            case MouseEvent.BUTTON3:
                                renderPanel.setLookAtLocation(loc[0], loc[1], loc[2]);
                                renderPanel.refreshScene();
                                break;
                        }
                    }
                }
            });
        }
    }

    private void adjustEyeTo(double[] loc) {
        double[] pitchBearingDist = RendererUtils.extractPitchBearingDist(
                renderPanel.getLookAtLocation(), loc);
        renderPanel.setPitchBearingDistance(
                pitchBearingDist[0],
                pitchBearingDist[1],
                pitchBearingDist[2] + 1.0);
        renderPanel.refreshScene();
    }
    
    public void mousePressed(MouseEvent e) {
        lastPressLoc = e.getPoint();
        lastPressPitchBearing = new double[] {
            renderPanel.getLookAtPitch(),
            renderPanel.getLookAtBearing()
        };
    }

    public void mouseReleased(MouseEvent e) {
        lastPressLoc = null;
        lastPressPitchBearing = null;
    }

    public void mouseDragged(MouseEvent e) {
        double width = renderPanel.getWidth();
        double height = renderPanel.getHeight();
        
        double scale = 360.0/Math.max(width, height);
        
        double pitch = lastPressPitchBearing[0] + (e.getY() - lastPressLoc.getY())*scale;
        double bearing = lastPressPitchBearing[1] - (e.getX() - lastPressLoc.getX())*scale;
        
        renderPanel.setPitchBearingDistance(pitch, bearing, Double.NaN);
        renderPanel.refreshScene();
    }
    
}
