/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.awt.Color;
import java.util.Arrays;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import visualizer.utils.Vector3f;

/**
 *
 * @author Umran
 */
public class RenderableVector extends RenderableComponent {

    
    private class ObjectInfo implements RenderableObjectInfo {
        
        @Override
        public Renderable getRenderableObject() {
            return RenderableVector.this;
        }

        @Override
        public String getDescription() {
            return RenderableVector.this.getDescription();
        }

    }
    
    private float[] from;
    private float[] to;
    private RenderUnit renderUnit;
    private final ObjectInfo objectInfo = new ObjectInfo();
    
    public RenderableVector(float[] from, float[] to) {
        super();
        this.color = Color.BLUE;
        this.from = from;
        this.to = to;
        this.size = 0.1f;
        this.name = "Vector from "+Arrays.toString(from)+" to "+Arrays.toString(to);
        build();
    }
    
    private void build() {
        Vector3f back;    
        Vector3f perp1;
        Vector3f perp2;
        
        Vector3f fromV = new Vector3f(from[0], from[1], from[2]);
        Vector3f toV = new Vector3f(to[0], to[1], to[2]);
    
        back = toV.subtract(fromV).normalizeLocal();
        perp1 = findPerpVec(back);
        perp2 = perp1.cross(back);
        
        float headSize = Math.min(size, fromV.distance(toV)/2.0f);
        
        Vector3f fullPerp1 = perp1.mult(headSize/2.0f);
        Vector3f fullPerp2 = perp2.mult(headSize/2.0f);
        Vector3f halfPerp1 = perp1.mult(headSize/4.0f);
        Vector3f halfPerp2 = perp2.mult(headSize/4.0f);
        
        Vector3f headStart = toV.subtract(back.mult(headSize));
        
        Vector3f fullHead0 = headStart.subtract(fullPerp1);
        Vector3f fullHead1 = headStart.subtract(fullPerp2);
        Vector3f fullHead2 = headStart.add(fullPerp1);
        Vector3f fullHead3 = headStart.add(fullPerp2);
        Vector3f halfHead0 = headStart.subtract(halfPerp1);
        Vector3f halfHead1 = headStart.subtract(halfPerp2);
        Vector3f halfHead2 = headStart.add(halfPerp1);
        Vector3f halfHead3 = headStart.add(halfPerp2);
        Vector3f halfBack0 = fromV.subtract(halfPerp1);
        Vector3f halfBack1 = fromV.subtract(halfPerp2);
        Vector3f halfBack2 = fromV.add(halfPerp1);
        Vector3f halfBack3 = fromV.add(halfPerp2);
        
        RenderUnit.Builder builder = new RenderUnit.Builder(
                16*3,
                3,
                true,
                false, 0);
        
//        builder.add(toV, tmpColor);
//        builder.add(fullHead0, tmpColor);
//        builder.add(fullHead3, tmpColor);
        addTriangle(builder, toV, fullHead0, fullHead3);
        
//        builder.add(toV, tmpColor);
//        builder.add(fullHead1, tmpColor);
//        builder.add(fullHead0, tmpColor);
        addTriangle(builder, toV, fullHead1, fullHead0);
        
//        builder.add(toV, tmpColor);
//        builder.add(fullHead2, tmpColor);
//        builder.add(fullHead1, tmpColor);
        addTriangle(builder, toV, fullHead2, fullHead1);
        
//        builder.add(toV, tmpColor);
//        builder.add(fullHead3, tmpColor);
//        builder.add(fullHead2, tmpColor);
        addTriangle(builder, toV, fullHead3, fullHead2);
        
//        builder.add(fullHead0, tmpColor);
//        builder.add(fullHead1, tmpColor);
//        builder.add(fullHead2, tmpColor);
        addTriangle(builder, fullHead0, fullHead1, fullHead2);
//        builder.add(fullHead0, tmpColor);
//        builder.add(fullHead2, tmpColor);
//        builder.add(fullHead3, tmpColor);
        addTriangle(builder, fullHead0, fullHead2, fullHead3);
        
//        builder.add(halfBack0, tmpColor);
//        builder.add(halfBack1, tmpColor);
//        builder.add(halfBack2, tmpColor);
        addTriangle(builder, halfBack0, halfBack1, halfBack2);
//        builder.add(halfBack0, tmpColor);
//        builder.add(halfBack2, tmpColor);
//        builder.add(halfBack3, tmpColor);
        addTriangle(builder, halfBack0, halfBack2, halfBack3);
        
//        builder.add(halfBack0, tmpColor);
//        builder.add(halfHead0, tmpColor);
//        builder.add(halfBack1, tmpColor);
        addTriangle(builder, halfBack0, halfHead0, halfBack1);
//        builder.add(halfHead0, tmpColor);
//        builder.add(halfHead1, tmpColor);
//        builder.add(halfBack1, tmpColor);
        addTriangle(builder, halfHead0, halfHead1, halfBack1);
        
//        builder.add(halfBack1, tmpColor);
//        builder.add(halfHead1, tmpColor);
//        builder.add(halfBack2, tmpColor);
        addTriangle(builder, halfBack1, halfHead1, halfBack2);
//        builder.add(halfHead1, tmpColor);
//        builder.add(halfHead2, tmpColor);
//        builder.add(halfBack2, tmpColor);
        addTriangle(builder, halfHead1, halfHead2, halfBack2);
        
//        builder.add(halfBack2, tmpColor);
//        builder.add(halfHead2, tmpColor);
//        builder.add(halfBack3, tmpColor);
        addTriangle(builder, halfBack2, halfHead2, halfBack3);
//        builder.add(halfHead2, tmpColor);
//        builder.add(halfHead3, tmpColor);
//        builder.add(halfBack3, tmpColor);
        addTriangle(builder, halfHead2, halfHead3, halfBack3);
        
//        builder.add(halfBack3, tmpColor);
//        builder.add(halfHead3, tmpColor);
//        builder.add(halfBack0, tmpColor);
        addTriangle(builder, halfBack3, halfHead3, halfBack0);
//        builder.add(halfHead3, tmpColor);
//        builder.add(halfHead0, tmpColor);
//        builder.add(halfBack0, tmpColor);
        addTriangle(builder, halfHead3, halfHead0, halfBack0);
        
        this.renderUnit = builder.compile();
        this.renderUnit.setDiffuseColor(new float[]{
            color.getRed()/255.0f,
            color.getGreen()/255.0f,
            color.getBlue()/255.0f,
            1.0f,
        });
    }

    
    private void addTriangle(RenderUnit.Builder builder, Vector3f a, Vector3f b, Vector3f c) {
        
        Vector3f normal = (b.subtract(a)).cross(c.subtract(a)).normalizeLocal();
        //normal = Vector3f.ZERO.subtract(normal);
        
        builder.add(a, null, normal);
        builder.add(b, null, normal);
        builder.add(c, null, normal);
    }
    
    public float[] getFrom() {
        return from;
    }

    public void setFrom(float[] from) {
        this.from = from;
        build();
    }

    public float[] getTo() {
        return to;
    }

    public void setTo(float[] to) {
        this.to = to;
        build();
    }

    @Override
    public void setSize(float size) {
        super.setSize(size);
        build();
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        renderUnit.setDiffuseColor(new float[]{
            color.getRed()/255.0f,
            color.getGreen()/255.0f,
            color.getBlue()/255.0f,
            1.0f,
        });
    }
    
    
    @Override
    public void render(RenderPanel renderTool, GLAutoDrawable glautodrawable, RenderedSelection selection) {
//        System.out.println("rendering vector "+name+" "+visibility.isVisible()+"\t"+visibility.isHierarchyVisible());
        
        GL2 gl = glautodrawable.getGL().getGL2();
        
        if (selection!=null) {
            selection.register(gl, new RenderableSelectionInfo(objectInfo));
        }
        
        if (selected) {
            this.renderUnit.setEmissionColor(SELECTED_EMISSION_COL);
        } else {
            this.renderUnit.setEmissionColor(UNSELECTED_EMISSION_COL);
        }
        renderUnit.render(gl, GL2.GL_TRIANGLES);
    }
    
    private Vector3f findPerpVec(Vector3f normV) {
        //	get the minimum axis
        int imin = 0;
        for (int i = 1; i < 3; ++i) {
            if (Math.abs(normV.get(i)) < Math.abs(normV.get(imin))) {
                imin = i;
            }
        }
        
        //	get the scaling value
        float dt = normV.get(imin);
        
        //	subtract the scaled value from the unit vector of the minimum axis
        Vector3f unitVecAtMinAxis = new Vector3f();
        unitVecAtMinAxis.set(imin, 1.0f);
        
        Vector3f perp = unitVecAtMinAxis.subtract(normV.mult(dt));
        
//        System.out.println("perpendicular of "+normV+" is "+perp);
        
        return perp;
    }

}
