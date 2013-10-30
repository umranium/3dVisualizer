/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

/**
 *
 * @author umran
 */
public class ViewInfo {
    
    public final int[] viewport;
    public final double[] projectionMatrix;
    public final double[] modelViewMatrix;

    public ViewInfo(int[] viewport, double[] projectionMatrix, double[] modelViewMatrix) {
        this.viewport = viewport;
        this.projectionMatrix = projectionMatrix;
        this.modelViewMatrix = modelViewMatrix;;
    }
    
}
