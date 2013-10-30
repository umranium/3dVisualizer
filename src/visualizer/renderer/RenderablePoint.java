/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.awt.Color;
import java.util.Arrays;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import visualizer.utils.Vector3f;

/**
 *
 * @author Umran
 */
public class RenderablePoint extends RenderableComponent {
    
    public enum PointType {
        DIAMOND, SPHERE
    }
    
    private class ObjectInfo implements RenderableObjectInfo {
        @Override
        public Renderable getRenderableObject() {
            return RenderablePoint.this;
        }

        @Override
        public String getDescription() {
            return RenderablePoint.this.getDescription();
        }
    }
    
    private float[] location;
    private RenderUnit renderUnit;
    private PointType pointType;
    private ObjectInfo objectInfo = new ObjectInfo();

    public RenderablePoint(float[] location) {
        super();
        this.location = location;
        this.color = Color.RED;
        this.size = 0.1f;
        this.pointType = PointType.DIAMOND;
        this.name = "Point at "+Arrays.toString(location);
        
        build();
    }
    
    private void build() {
        switch (pointType) {
            case DIAMOND:
                buildDiamond(4);
                break;
            case SPHERE:
                break;
        }
        this.renderUnit.setDiffuseColor(new float[]{
            color.getRed()/255.0f,
            color.getGreen()/255.0f,
            color.getBlue()/255.0f,
            1.0f,
        });
    }
    
    public void buildSphere(int faces, int stacks) {
        float rad = size / 2.0f;
        
        RenderUnit.Builder builder = new RenderUnit.Builder(
                ((stacks*2+1)*faces)*2*3,
                3,
                true,
                false, 0);
        
        float[] top = Arrays.copyOf(location, 3);
        top[1] -= rad;
        
        float[] bottom = Arrays.copyOf(location, 3);
        bottom[1] += rad;
        
    }
    
    private void buildDiamond(int faces) {
        float rad = size / 2.0f;
        float[][] tmpCoords = new float[2][3];
        
        RenderUnit.Builder builder = new RenderUnit.Builder(
                faces*2*3,
                3,
                true,
                false, 0);
        
        float[] top = Arrays.copyOf(location, 3);
        top[1] -= rad;
        
        float[] bottom = Arrays.copyOf(location, 3);
        bottom[1] += rad;
        
        int prevCoordIndex = 0;
        int currentCoordIndex = 1;
        assign(tmpCoords[prevCoordIndex], rad, 0.0f, 0.0f);
        shift(location, tmpCoords[prevCoordIndex], 1.0f, tmpCoords[prevCoordIndex]);
        
        for (int i=0; i<=faces; ++i) {
            float angle = (float)((i+1)*Math.PI*2/faces);
            
            assign(tmpCoords[currentCoordIndex], rad, angle, 0.0f);
            shift(location, tmpCoords[currentCoordIndex], 1.0f, tmpCoords[currentCoordIndex]);
            
            // top half
            addTriangle(builder, top, tmpCoords[currentCoordIndex], tmpCoords[prevCoordIndex]);
            
            // bottom half
            addTriangle(builder, bottom, tmpCoords[prevCoordIndex], tmpCoords[currentCoordIndex]);
            
            int tmp = prevCoordIndex;
            prevCoordIndex = currentCoordIndex;
            currentCoordIndex = tmp;
        }
        
        renderUnit = builder.compile();
    }
    
    private void addTriangle(RenderUnit.Builder builder, float[] a, float[] b, float[] c) {
        Vector3f aV = new Vector3f(a[0], a[1], a[2]);
        Vector3f bV = new Vector3f(b[0], b[1], b[2]);
        Vector3f cV = new Vector3f(c[0], c[1], c[2]);
        
        Vector3f normal = (bV.subtract(aV)).cross(cV.subtract(aV)).normalizeLocal();
        
        builder.add(a, null, normal);
        builder.add(b, null, normal);
        builder.add(c, null, normal);
    }
    
    private void assign(float[] coord, float rad, float bearingRadians, double pitchRadians) {
//        double bearingRadians = Math.toRadians(bearing);
//        double pitchRadians = Math.toRadians(pitch);
        double cosLookAtBearing = Math.cos(bearingRadians);
        double sinLookAtBearing = Math.sin(bearingRadians);
        double cosLookAtPitch = Math.cos(pitchRadians);
        double sinLookAtPitch = Math.sin(pitchRadians);
        coord[0] = (float)(rad*sinLookAtBearing*cosLookAtPitch);
        coord[1] = (float)(rad*sinLookAtPitch);
        coord[2] = (float)(rad*cosLookAtBearing*cosLookAtPitch);
    }
    
    
    private void shift(float[] from, float[] by, float scale, float[] res) {
        for (int i=0; i<3; ++i)
            res[i] = from[i] + by[i]*scale;
    }
    
    public float[] getLocation() {
        return location;
    }

    public void setLocation(float[] location) {
        this.location = location;
        build();
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        this.renderUnit.setDiffuseColor(new float[]{
            color.getRed()/255.0f,
            color.getGreen()/255.0f,
            color.getBlue()/255.0f,
            1.0f,
        });
    }

    @Override
    public void setSize(float size) {
        super.setSize(size);
        build();
    }
    
    @Override
    public void render(RenderPanel renderTool, GLAutoDrawable glautodrawable, RenderedSelection selection) {
//        System.out.println("rendering point "+name);
        
        GL2 gl = glautodrawable.getGL().getGL2();
        
        if (selection!=null) {
            selection.register(gl, new RenderableSelectionInfo(objectInfo));
        }
        
        if (selected) {
            this.renderUnit.setEmissionColor(SELECTED_EMISSION_COL);
        } else {
            this.renderUnit.setEmissionColor(UNSELECTED_EMISSION_COL);
        }
        renderUnit.render(gl, GL.GL_TRIANGLES);
        
//        gl.glColor3f(color.getRed()/255.0f, color.getGreen()/255.0f, color.getBlue()/255.0f);
//        gl.glPointSize(size);
//        gl.glBegin( GL.GL_POINTS );
//        gl.glVertex3f( location[0], location[1], location[2] );
//        gl.glEnd();
    }
    
}
