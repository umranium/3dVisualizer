/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.Color;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import visualizer.utils.Vector3f;
import visualizer.renderer.ViewFrustrumHelper.Plane;

/**
 *
 * @author abd01c
 */
public class RenderableAxi extends RenderableComponent {
    
    private static final int SIZE_FLOAT = 4;
    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;
    private static final int[] ALL_AXI = new int[] {AXIS_X,AXIS_Y,AXIS_Z};
    private static final int SIZE_COORD = 3;
    private static final int SIZE_COLOR = 4;
    
    private static enum AXIS_LINE_TYPE {
        MAIN("Main Axis"), MAJOR_GUIDELINE("Major Guideline"), MINOR_GUIDELINE("Minor Guideline");
        
        String description;

        private AXIS_LINE_TYPE(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
        
    }
    
    private static String[] AXIS_NAMES = new String[] {
        "X", "Y", "Z"
    };
    
    private final int[] GUIDELINE_AXIS = new int[] {
        AXIS_Y,
        AXIS_Z,
        AXIS_X
    };
    
    public static enum AXIS_DRAW_TYPE {
        MAIN_AXIS_ONLY,
        MAIN_AXIS_AND_MAJOR_GUIDELINES,
        MAIN_AXIS_MAJOR_AND_MINOR_GUIDELINES,
    }
    
    private class ObjectInfo implements RenderableObjectInfo {
        private int axis;
        private AXIS_LINE_TYPE axisLineType;
        private GLU glu;
        private int mouseX, mouseY;
        private ViewInfo viewInfo;
        private String description = null;

        public ObjectInfo(int axis, AXIS_LINE_TYPE axisLineType, GLU glu, int mouseX, int mouseY, ViewInfo viewInfo) {
            this.axis = axis;
            this.axisLineType = axisLineType;
            this.glu = glu;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.viewInfo = viewInfo;
            this.description = "Unknown";
        }
        
        public void computeAssignDescription(double depth) {
            double[] pos = RendererUtils.unproject(glu, mouseX, mouseY, depth, viewInfo);
            switch (axisLineType) {
                case MAIN:
                {
                    this.description = pos[axis]+" on main "+AXIS_NAMES[axis]+" axis";
                    break;
                }
                case MAJOR_GUIDELINE:
                {
                    this.description = pos[axis]+" on major "+AXIS_NAMES[axis]+" guideline";
                    break;
                }
                case MINOR_GUIDELINE:
                {
                    this.description = pos[axis]+" on minor "+AXIS_NAMES[axis]+" guideline";
                    break;
                }
                default:
                {
                    this.description = "Unknown";
                    break;
                }
            }
        }

        @Override
        public String toString() {
            return "(axis="+axis+", line-type="+axisLineType+")";
        }

        @Override
        public String getDescription() {
            return description;
        }
        
        @Override
        public Renderable getRenderableObject() {
            return RenderableAxi.this;
        }
    }
    
    private class SelectionInfo extends RenderableSelectionInfo {

        public SelectionInfo(ObjectInfo objectInfo) {
            super(objectInfo);
        }

        @Override
        public void setDepth(double depth) {
            super.setDepth(depth);
            ((ObjectInfo)getObjectInfo()).computeAssignDescription(depth);
        }
        
    }
    
    public class AxisParams {

        private int axisIndex;
        private float start;
        private float center;
        private float end;
        private float majorIncrements;
        private int minorIncrementsPerMajorIncrement;
        
        public AxisParams(int axisIndex) {
            this.axisIndex = axisIndex;
            start = -1.0e2f;
            center = 0.0f;
            end = +1.0e2f;
            majorIncrements = 1.0f;
            minorIncrementsPerMajorIncrement = 4;
            
            computeGuidelines(false);
        }

        public float getStart() {
            return start;
        }

        public void setStart(float start) {
            this.start = start;
            computeGuidelines(true);
        }

        public float getCenter() {
            return center;
        }

        public void setCenter(float center) {
            this.center = center;
            computeGuidelines(true);
        }
        
        public float getEnd() {
            return end;
        }

        public void setEnd(float end) {
            this.end = end;
            computeGuidelines(true);
        }

        public int getMinorIncrementsPerMajorIncrement() {
            return minorIncrementsPerMajorIncrement;
        }

        public void setMinorIncrementsPerMajorIncrement(int minorIncrementsPerMajorIncrement) {
            this.minorIncrementsPerMajorIncrement = minorIncrementsPerMajorIncrement;
            computeGuidelines(true);
        }
        
        public float getMinorIncrements() {
            return majorIncrements / minorIncrementsPerMajorIncrement;
        }

        public float getMajorIncrements() {
            return majorIncrements;
        }

        public void setMajorIncrements(float majorIncrements) {
            this.majorIncrements = majorIncrements;
            computeGuidelines(true);
        }
        
        private int getGuideLineStart() {
            float minorIncrements = getMinorIncrements();
            return (int)(Math.floor(Math.abs(start-center)/minorIncrements)
                    *Math.signum(start-center));
        }
        
        private int getGuideLineEnd() {
            float minorIncrements = getMinorIncrements();
            return (int)(Math.floor(Math.abs(end-center)/minorIncrements)*
                    Math.signum(end-center));
        }
        
        private void computeGuidelines(boolean isUpdate) {
            if (isUpdate) {
                RenderableAxi.this.computeVertexBuffers(true);
            }
        }
    }
    
    private AxisParams axi[];
    private float minorIncrementSize;
    private Color minorIncrementColour;
    private float majorIncrementSize;
    private Color majorIncrementColour;
    private Color textColor;
    private AXIS_DRAW_TYPE axisDrawType;
    private boolean drawAxis[];
    private boolean allowSelection;
    
    private RenderUnit[] mainAxi;
    private RenderUnit[] majorGuidelines;
    private RenderUnit[] minorGuidelines;
    
    public RenderableAxi() {
        axi = new AxisParams[] {
            new AxisParams(AXIS_X),
            new AxisParams(AXIS_Y),
            new AxisParams(AXIS_Z)
        };
        
        color = new Color(180, 180, 180);
        majorIncrementColour = new Color(128, 128, 128);
        minorIncrementColour = new Color(64, 64, 64);
        textColor = Color.CYAN;
        axisDrawType = AXIS_DRAW_TYPE.MAIN_AXIS_MAJOR_AND_MINOR_GUIDELINES;
        drawAxis = new boolean[] {true,true,true};
        allowSelection = false;
        
//        color = Color.RED;
//        majorIncrementColour = Color.GREEN;
//        minorIncrementColour = Color.BLUE;

        size = 1.0f;
        majorIncrementSize = 0.5f;
        minorIncrementSize = 0.1f;
        
        mainAxi = new RenderUnit[3];
        majorGuidelines = new RenderUnit[3];
        minorGuidelines = new RenderUnit[3];
        
        computeVertexBuffers(false);
    }

    public AxisParams getxAxis() {
        return axi[0];
    }

    public AxisParams getyAxis() {
        return axi[1];
    }

    public AxisParams getzAxis() {
        return axi[2];
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        computeVertexBuffers(true);
    }
    
    public float getMinorIncrementSize() {
        return minorIncrementSize;
    }

    public void setMinorIncrementSize(float minorIncrementSize) {
        this.minorIncrementSize = minorIncrementSize;
    }

    public Color getMinorIncrementColour() {
        return minorIncrementColour;
    }

    public void setMinorIncrementColour(Color minorIncrementColour) {
        this.minorIncrementColour = minorIncrementColour;
        computeVertexBuffers(true);
    }
    
    public float getMajorIncrementSize() {
        return majorIncrementSize;
    }

    public void setMajorIncrementSize(float majorIncrementSize) {
        this.majorIncrementSize = majorIncrementSize;
    }
    
    public Color getMajorIncrementColour() {
        return majorIncrementColour;
    }

    public void setMajorIncrementColour(Color majorIncrementColour) {
        this.majorIncrementColour = majorIncrementColour;
        computeVertexBuffers(true);
    }

    public AXIS_DRAW_TYPE getAxisDrawType() {
        return axisDrawType;
    }

    public void setAxisDrawType(AXIS_DRAW_TYPE axisDrawType) {
        this.axisDrawType = axisDrawType;
    }
    
    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }
    
    public boolean isDrawXAxis() {
        return drawAxis[0];
    }

    public boolean isDrawYAxis() {
        return drawAxis[1];
    }

    public boolean isDrawZAxis() {
        return drawAxis[2];
    }
    
    public void setDrawXAxis(boolean v) {
        drawAxis[0] = v;
    }
    
    public void setDrawYAxis(boolean v) {
        drawAxis[1] = v;
    }
    
    public void setDrawZAxis(boolean v) {
        drawAxis[2] = v;
    }

    public boolean isAllowSelection() {
        return allowSelection;
    }

    public void setAllowSelection(boolean allowSelection) {
        this.allowSelection = allowSelection;
    }
    
    @Override
    public String getDescription() {
        return "AXI";
    }
    
    private final float[] tempStartCoord = new float[3];
    private final float[] tempEndCoord = new float[3];
    
    private void computeVertexBuffers(boolean isUpdate) {
        
        for (int axis=0; axis<3; ++axis) {
            tempStartCoord[axis] = axi[axis].center;
            tempEndCoord[axis] = axi[axis].center;
        }
        
        RenderUnit.Builder mainAxiBuilders[] = new RenderUnit.Builder[3];
        RenderUnit.Builder majorGuidelinesBuilders[] = new RenderUnit.Builder[3];
        RenderUnit.Builder minorGuidelinesBuilders[] = new RenderUnit.Builder[3];
        
        for (int axis=0; axis<3; ++axis) {
            int guideLineStart = axi[axis].getGuideLineStart();
            int guideLineEnd = axi[axis].getGuideLineEnd();
            
            int estNumOfMinorGuidelines = guideLineEnd-guideLineStart+1;
            int estNumOfMajorGuidelines = (guideLineStart/axi[axis].minorIncrementsPerMajorIncrement+1) +
                    (guideLineEnd/axi[axis].minorIncrementsPerMajorIncrement+1);
            
            mainAxiBuilders[axis] = new RenderUnit.Builder(
                    3*2,
                    SIZE_COORD,
                    false,
                    true, SIZE_COLOR);
            majorGuidelinesBuilders[axis] = new RenderUnit.Builder(
                    estNumOfMajorGuidelines,
                    SIZE_COORD,
                    false,
                    true, SIZE_COLOR);
            minorGuidelinesBuilders[axis] = new RenderUnit.Builder(
                    estNumOfMinorGuidelines,
                    SIZE_COORD,
                    false,
                    true, SIZE_COLOR);
        }
        
        float mainAxisColors[] = new float[] {
            color.getRed() / 255.0f,
            color.getGreen() / 255.0f,
            color.getBlue() / 255.0f,
            1.0f,
        };
        float majorAxisColors[] = new float[] {
            majorIncrementColour.getRed() / 255.0f,
            majorIncrementColour.getGreen() / 255.0f,
            majorIncrementColour.getBlue() / 255.0f,
            1.0f,
        };
        float minorAxisColors[] = new float[] {
            minorIncrementColour.getRed() / 255.0f,
            minorIncrementColour.getGreen() / 255.0f,
            minorIncrementColour.getBlue() / 255.0f,
            1.0f,
        };
        
        for (int axis=0; axis<3; ++axis) {
            
            tempStartCoord[GUIDELINE_AXIS[axis]] = axi[GUIDELINE_AXIS[axis]].start;
            tempEndCoord[GUIDELINE_AXIS[axis]] = axi[GUIDELINE_AXIS[axis]].end;
            
            int guideLineStart = axi[axis].getGuideLineStart();
            int guideLineEnd = axi[axis].getGuideLineEnd();
            int increments = axi[axis].minorIncrementsPerMajorIncrement;

            for (int index=guideLineStart; index<=guideLineEnd; ++index) {
                float loc = axi[axis].center +
                        index*axi[axis].majorIncrements / increments;

                tempStartCoord[axis] = loc;
                tempEndCoord[axis] = loc;
                
                if (index==0) {
                    mainAxiBuilders[GUIDELINE_AXIS[axis]].add(tempStartCoord, mainAxisColors, null);
                    mainAxiBuilders[GUIDELINE_AXIS[axis]].add(tempEndCoord, mainAxisColors, null);
                } else
                    if (index%increments==0) {
                        majorGuidelinesBuilders[axis].add(tempStartCoord, majorAxisColors, null);
                        majorGuidelinesBuilders[axis].add(tempEndCoord, majorAxisColors, null);
                    } else {
                        minorGuidelinesBuilders[axis].add(tempStartCoord, minorAxisColors, null);
                        minorGuidelinesBuilders[axis].add(tempEndCoord, minorAxisColors, null);
                    }
            }

            // reset back to zero
            tempStartCoord[GUIDELINE_AXIS[axis]] = axi[GUIDELINE_AXIS[axis]].center;
            tempEndCoord[GUIDELINE_AXIS[axis]] = axi[GUIDELINE_AXIS[axis]].center;
            tempStartCoord[axis] = axi[axis].center;
            tempEndCoord[axis] = axi[axis].center;
        }
        
        for (int axis=0; axis<3; ++axis) {
            mainAxi[axis] = mainAxiBuilders[axis].compile();
            majorGuidelines[axis] = majorGuidelinesBuilders[axis].compile();
            minorGuidelines[axis] = minorGuidelinesBuilders[axis].compile();
        }
        
    }
    
    @Override
    public void render(RenderPanel renderTool, GLAutoDrawable glautodrawable, RenderedSelection selection) {
        if (selection!=null && !allowSelection) {
            return;
        }
        
        if (selection!=null) {
            System.out.println("AXI selection");
        }
        
        GL2 gl = glautodrawable.getGL().getGL2();
        
        boolean drawMainAxi = false;
        boolean drawMajorGuidelines = false;
        boolean drawMinorGuidelines = false;
        
        switch (axisDrawType) {
            case MAIN_AXIS_ONLY:
                drawMainAxi = true;
                break;
            case MAIN_AXIS_AND_MAJOR_GUIDELINES:
                drawMainAxi = true;
                drawMajorGuidelines = true;
                break;
            case MAIN_AXIS_MAJOR_AND_MINOR_GUIDELINES:
                drawMainAxi = true;
                drawMajorGuidelines = true;
                drawMinorGuidelines = true;
                break;
        }
        
        if (drawMinorGuidelines) {
            gl.glLineWidth(minorIncrementSize);
            for (int axis=0; axis<3; ++axis) {
                if (drawAxis[axis]) {
                    if (allowSelection && selection!=null) {
                        selection.register(gl,
                                new SelectionInfo(
                                new ObjectInfo(axis,
                                AXIS_LINE_TYPE.MINOR_GUIDELINE,
                                renderTool.getGLU(),
                                selection.getMouseX(), selection.getMouseY(),
                                selection.getViewInfo())));
                    }
                    minorGuidelines[axis].render(gl, GL2.GL_LINES);
                }
            }
        }
        
        if (drawMajorGuidelines) {
            gl.glLineWidth(majorIncrementSize);
            for (int axis=0; axis<3; ++axis) {
                if (drawAxis[axis]) {
                    if (allowSelection && selection!=null) {
                        selection.register(gl, new SelectionInfo(
                                new ObjectInfo(axis,
                                AXIS_LINE_TYPE.MAJOR_GUIDELINE,
                                renderTool.getGLU(),
                                selection.getMouseX(), selection.getMouseY(),
                                selection.getViewInfo())));
                    }
                    majorGuidelines[axis].render(gl, GL2.GL_LINES);
                }
            }
        }
        
        if (drawMainAxi) {
            gl.glLineWidth(size);
            for (int axis=0; axis<3; ++axis) {
                if (allowSelection && selection!=null) {
                    selection.register(gl, new SelectionInfo(
                            new ObjectInfo(axis,
                            AXIS_LINE_TYPE.MAIN,
                            renderTool.getGLU(),
                                selection.getMouseX(), selection.getMouseY(),
                            selection.getViewInfo())));
                }
                mainAxi[axis].render(gl, GL2.GL_LINES);
            }
        }
        
    }
    
    public void renderText(RenderPanel renderTool, GLAutoDrawable glautodrawable, RenderedSelection selection) {
        if (selection!=null && !allowSelection) {
            return;
        }
        
        GL2 gl = glautodrawable.getGL().getGL2();
        GLUT glut = renderTool.getGLUT();
        
        boolean drawMainAxi = false;
        boolean drawMajorGuidelines = false;
        boolean drawMinorGuidelines = false;
        
        switch (axisDrawType) {
            case MAIN_AXIS_ONLY:
                drawMainAxi = true;
                break;
            case MAIN_AXIS_AND_MAJOR_GUIDELINES:
                drawMainAxi = true;
                drawMajorGuidelines = true;
                break;
            case MAIN_AXIS_MAJOR_AND_MINOR_GUIDELINES:
                drawMainAxi = true;
                drawMajorGuidelines = true;
                drawMinorGuidelines = true;
                break;
        }
        
        if (drawMajorGuidelines) {
            gl.glColor3f(textColor.getRed()/255.0f, textColor.getGreen()/255.0f, textColor.getBlue()/255.0f);

            for (int axis=0; axis<3; ++axis) {
                tempStartCoord[axis] = axi[axis].center;
            }

            for (int axis=0; axis<3; ++axis) {
                if (drawAxis[axis]) {
                    int guideLineStart = axi[axis].getGuideLineStart();
                    int guideLineEnd = axi[axis].getGuideLineEnd();
                    int increments = axi[axis].minorIncrementsPerMajorIncrement;
                    for (int index=guideLineStart; index<=guideLineEnd; ++index) {
                        if (index%increments!=0) {
                            continue;
                        }

                        float loc = axi[axis].center + index*axi[axis].majorIncrements/increments;
                        tempStartCoord[axis] = loc;

                        gl.glRasterPos3f(tempStartCoord[0], tempStartCoord[1], tempStartCoord[2]);
                        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, Float.toString(loc));
                    }

                    tempStartCoord[axis] = axi[axis].center;
                }
            }
        }
        
        if (drawMainAxi) {
            gl.glColor3f(textColor.getRed()/255.0f, textColor.getGreen()/255.0f, textColor.getBlue()/255.0f);
            
            Vector3f origin = new Vector3f(axi[0].center, axi[1].center, axi[2].center);
            for (int axis=0; axis<3; ++axis) {
                tempStartCoord[axis] = 0.0f;
            }
            
            ViewFrustrumHelper.FrustrumFaces faces = renderTool.getFrustrumFaces();

            Vector3f eyeLoc = faces.eyeLoc;
            
            for (int axis=0; axis<3; ++axis) {
                tempStartCoord[axis] = 1.0f;
                Vector3f axisDir = new Vector3f(tempStartCoord[0], tempStartCoord[1], tempStartCoord[2]);
                tempStartCoord[axis] = 0.0f;
                
                //System.out.println("axis: "+axis+", dir="+axisDir);
                
                if (drawAxis[axis]) {
                    double minIntersectingDist = Double.POSITIVE_INFINITY;
                    int minP = -1;
                    Vector3f drawLoc = null;
                    
                    for (int p=0; p<faces.planes.allPlanes.length; ++p) {
                        Plane plane = faces.planes.allPlanes[p];
                        double dist = plane.intersect(origin, axisDir);
                        //System.out.println("\tface["+ViewFrustrumHelper.PLANE_NAMES[p]+"]: "+intersect+" x=["+plane.minX+","+plane.maxX+"] y=["+plane.minY+","+plane.maxY+"] z=["+plane.minZ+","+plane.maxZ+"]");
                        if (!Double.isNaN(dist) && dist>0.0f) {
                            if (dist<minIntersectingDist) {
                                minIntersectingDist = dist;
                                minP = p;
                                float drawDist = (float)dist - 0.1f;
                                drawLoc = origin.add(axisDir.mult(drawDist));
                            }
                        }
                    }
                    
                    if (drawLoc!=null) {
                        //System.out.println("\t\tdrawn dist="+minIntersectingDist+" face["+ViewFrustrumHelper.PLANE_NAMES[minP]+"] "+minInfo);
                        gl.glPushMatrix();
                        gl.glRasterPos3f(drawLoc.x, drawLoc.y, drawLoc.z);
                        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, AXIS_NAMES[axis]);
                        gl.glPopMatrix();
                    }
                }
            }
        }
    }
    
    
    
    
}
