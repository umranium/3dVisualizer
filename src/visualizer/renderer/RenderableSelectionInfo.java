/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

/**
 *
 * @author abd01c
 */
public class RenderableSelectionInfo {
    
    private RenderableObjectInfo objectInfo;
    private double depth;

    public RenderableSelectionInfo(RenderableObjectInfo objectInfo) {
        this.objectInfo = objectInfo;
        this.depth = Double.NaN;
    }

    public RenderableObjectInfo getObjectInfo() {
        return objectInfo;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }
    
    public double getDepth() {
        return depth;
    }
    
}
