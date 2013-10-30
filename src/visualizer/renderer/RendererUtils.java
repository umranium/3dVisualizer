/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import javax.media.opengl.glu.GLU;
import visualizer.utils.Vector3f;

/**
 *
 * @author umran
 */
public class RendererUtils {
    
    public static double[] unproject(GLU glu, double winX, double winY, double winZ, ViewInfo viewInfo) {
        
        int[] viewport = viewInfo.viewport;
        double[] pos = new double[3];
        
        winY = (double)viewport[3] - winY;
        
        glu.gluUnProject(
                winX, winY, winZ,
                viewInfo.modelViewMatrix,
                0,
                viewInfo.projectionMatrix,
                0,
                viewInfo.viewport,
                0,
                pos,
                0);
        
//        System.out.println("pos: "+pos[0]+", "+pos[1]+", "+pos[2]);
        
        return pos;
    }

    public static double[] extractPitchBearingDist(Vector3f lookAtLoc, double[] eyeLoc) {
        double deltaX = eyeLoc[0]-lookAtLoc.x;
        double deltaY = eyeLoc[1]-lookAtLoc.y;
        double deltaZ = eyeLoc[2]-lookAtLoc.z;
        double len = Math.sqrt(deltaX*deltaX+deltaY*deltaY+deltaZ*deltaZ);
        
        double pitch = Math.toDegrees(Math.sin(deltaY / len));
        double bearing = Math.toDegrees(Math.atan2(deltaX, deltaZ));
        
        //System.out.println("pitch=" + pitch+", bearing="+bearing);
        
        return new double[]{pitch,bearing,len};
    }
        
}
