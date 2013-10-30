/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.utils;

import visualizer.renderer.RenderPanel;
import visualizer.renderer.RenderablePoint;
import visualizer.renderer.RenderableGroup;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import visualizer.VisualizerFrame;

/**
 *
 * @author abd01c
 */
public class PlaneMath {
    
    public static final float EPSILON = 1.0e-6f;
    
    public static class BaseLine {
        public final Vector3f origin;
        public final Vector3f dir;

        public BaseLine(Vector3f origin, Vector3f dir) {
            this.origin = origin;
            this.dir = dir;
        }
    }
    
    public static class BasePlane {
        public final Vector3f normal;
        public final float d;

        public BasePlane(Vector3f a, Vector3f b, Vector3f c) {
            this.normal = (b.subtract(a)).cross(c.subtract(a)).normalizeLocal();
            this.d = - normal.dot(a);
        }
        
        public BasePlane(float a, float b, float c, float d) {
            this.normal = new Vector3f(a, b, c);
            float len = this.normal.length();
            this.normal.divideLocal(len);
            this.d = d/len;
        }
        
        public BasePlane(double a, double b, double c, double d) {
            this((float)a,(float)d,(float)c,(float)d);
        }

        @Override
        public String toString() {
            return "("+normal.x+" x + "+normal.y+" y + "+normal.z+" z + "+d+")";
        }
        
        /**
         * Ref: http://paulbourke.net/geometry/pointlineplane/
         * 
         * @return Where the line meets the plane, from the origin of the line,
         * in the direction of the line. Or NaN if the line never meets 
         * (zero intersections) or is on the plane (infinite intersections).
         */
        public float intersection(BaseLine line) {
            float a = normal.x;
            float b = normal.y;
            float c = normal.z;
            float x1 = line.origin.x;
            float y1 = line.origin.y;
            float z1 = line.origin.z;
            float x1_x2 = line.dir.x;
            float y1_y2 = line.dir.y;
            float z1_z2 = line.dir.z;
            
            float denom = a*x1_x2 + b*y1_y2 + c*z1_z2;
            
            if (Math.abs(denom)<EPSILON) {
                return Float.NaN;
            }
            
            float num = a*x1 + b*y1 + c*z1 + d;
            
            return num / denom;
        }
        
        /**
         * Ref: http://paulbourke.net/geometry/pointlineplane/
         * 
         * @return The line that intersects this plane and the other plane. Null
         * if there is no intersection or infinite intersections.
         */
        public BaseLine intersection(BasePlane other) {
            Vector3f lineDir = this.normal.cross(other.normal);
            
            //  check if dir is zero (means the planes are parallel)
            if (Math.abs(lineDir.x)<EPSILON &&
                    Math.abs(lineDir.x)<EPSILON &&
                    Math.abs(lineDir.x)<EPSILON) {
                return null;
            }
            
            float d1 = -this.d;
            float d2 = -other.d;
            float n1_dot_n1 = this.normal.dot(this.normal);
            float n2_dot_n2 = other.normal.dot(other.normal);
            float n1_dot_n2 = this.normal.dot(other.normal);
            float det = n1_dot_n1*n2_dot_n2 - n1_dot_n2*n1_dot_n2;
            float c1 = (d1*n2_dot_n2 - d2*n1_dot_n2) / det;
            float c2 = (d2*n1_dot_n1 - d1*n1_dot_n2) / det;
            
            return new BaseLine(this.normal.mult(c1).add(other.normal.mult(c2)),
                    lineDir);
        }
        
        /**
         * @return True if this plane is parallel to the other plane.
         */
        public boolean parallelWith(BasePlane other) {
            Vector3f intersectionDir = this.normal.cross(other.normal);
            
            //  check if dir is zero (means the planes are parallel)
            if (Math.abs(intersectionDir.x)<EPSILON &&
                    Math.abs(intersectionDir.x)<EPSILON &&
                    Math.abs(intersectionDir.x)<EPSILON) {
                return true;
            } else {
                return false;
            }
        }
        
        /**
         * Ref: http://paulbourke.net/geometry/pointlineplane/
         * 
         * @return The point at which this plane, and the 2 other planes meet.
         * Or null if any of the planes is parallel to any of the others (hence
         * never forms a point).
         */
        public Vector3f intersection(BasePlane p2, BasePlane p3) {
            if (this.parallelWith(p2) || this.parallelWith(p3) ||
                p2.parallelWith(p3)) {
                return null;
            }

            float d1 = -this.d;
            float d2 = -p2.d;
            float d3 = -p3.d;
            
            Vector3f n1_cross_n2 = this.normal.cross(p2.normal);
            Vector3f n2_cross_n3 = p2.normal.cross(p3.normal);
            Vector3f n3_cross_n1 = p3.normal.cross(this.normal);
            
            float denom = this.normal.dot(n2_cross_n3);
            
            //System.out.println("denom="+denom);
            
            if (Math.abs(denom)<EPSILON) {
                return null;
            }
            
            Vector3f num = n2_cross_n3.mult(d1).add(
                    n3_cross_n1.mult(d2)).add(
                    n1_cross_n2.mult(d3));
            
            return num.divide(denom);
        }
    }
    
    public static class QuadrilateralBoundaries {
        Vector3f cornerPoints[];
        public final float minX, maxX, minY, maxY, minZ, maxZ;

        public QuadrilateralBoundaries(List<Vector3f> cornerPoints) {
            this.cornerPoints = new Vector3f[cornerPoints.size()];
            this.cornerPoints = cornerPoints.toArray(this.cornerPoints);
            
            float mnX, mxX, mnY, mxY, mnZ, mxZ;
            
            mnX = mnY = mnZ = Float.POSITIVE_INFINITY;
            mxX = mxY = mxZ = Float.NEGATIVE_INFINITY;
            
            for (Vector3f point:cornerPoints) {
                if (point.x<mnX) {
                    mnX = point.x;
                }
                if (point.x>mxX) {
                    mxX = point.x;
                }
                if (point.y<mnY) {
                    mnY = point.y;
                }
                if (point.y>mxY) {
                    mxY = point.y;
                }
                if (point.z<mnZ) {
                    mnZ = point.z;
                }
                if (point.z>mxZ) {
                    mxZ = point.z;
                }
            }
            
            this.minX = mnX;
            this.minY = mnY;
            this.minZ = mnZ;
            this.maxX = mxX;
            this.maxY = mxY;
            this.maxZ = mxZ;
        }
    }
    
    
    private static void multMat(double[] res, double[] a, double[] b) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                res[i * 4 + j] = 0.0;
                for (int k = 0; k < 4; k++) {
                    res[i * 4 + j] += a[i * 4 + k] * b[k * 4 + j];
                }
            }
        }
    }
    
    public static final String[][] PLANE_NAMES = new String[][] {
        {"Left", "Right"},
        {"Bottom", "Top"},
        {"Near", "Far"},
    };
    
    private static class ViewFrustrum {
        private double[] clip;
        private BasePlane near;
        private BasePlane far;
        private BasePlane left;
        private BasePlane right;
        private BasePlane bottom;
        private BasePlane top;
        private BasePlane[][] allPlanes;
        private QuadrilateralBoundaries[][] faceBoundaries;

        public ViewFrustrum(double[] modelViewMat, double[] projectionMat) {
            clip = new double[16];
            multMat(clip, modelViewMat, projectionMat);
            
            double[] frustum = new double[4];
            
            frustum[0] = clip[ 3] - clip[ 0];
            frustum[1] = clip[ 7] - clip[ 4];
            frustum[2] = clip[11] - clip[ 8];
            frustum[3] = clip[15] - clip[12];
            right = new BasePlane(frustum[0], frustum[1], frustum[2], frustum[3]);
   
            frustum[0] = clip[ 3] + clip[ 0];
            frustum[1] = clip[ 7] + clip[ 4];
            frustum[2] = clip[11] + clip[ 8];
            frustum[3] = clip[15] + clip[12];
            left = new BasePlane(frustum[0], frustum[1], frustum[2], frustum[3]);
            
            frustum[0] = clip[ 3] + clip[ 1];
            frustum[1] = clip[ 7] + clip[ 5];
            frustum[2] = clip[11] + clip[ 9];
            frustum[3] = clip[15] + clip[13];
            bottom = new BasePlane(frustum[0], frustum[1], frustum[2], frustum[3]);
            
            frustum[0] = clip[ 3] - clip[ 1];
            frustum[1] = clip[ 7] - clip[ 5];
            frustum[2] = clip[11] - clip[ 9];
            frustum[3] = clip[15] - clip[13];
            top = new BasePlane(frustum[0], frustum[1], frustum[2], frustum[3]);
            
            frustum[0] = clip[ 3] - clip[ 2];
            frustum[1] = clip[ 7] - clip[ 6];
            frustum[2] = clip[11] - clip[10];
            frustum[3] = clip[15] - clip[14];
            far = new BasePlane(frustum[0], frustum[1], frustum[2], frustum[3]);
            
            frustum[0] = clip[ 3] + clip[ 2];
            frustum[1] = clip[ 7] + clip[ 6];
            frustum[2] = clip[11] + clip[10];
            frustum[3] = clip[15] + clip[14];
            near = new BasePlane(frustum[0], frustum[1], frustum[2], frustum[3]);
            
            
//            near = new BasePlane(
//				 m(3,1) + m(4,1),
//				 m(3,2) + m(4,2),
//				 m(3,3) + m(4,3),
//				 m(3,4) + m(4,4));
//            
//            far = new BasePlane(
//				-m(3,1) + m(4,1),
//				-m(3,2) + m(4,2),
//				-m(3,3) + m(4,3),
//				-m(3,4) + m(4,4));
//            left = new BasePlane(
//				 m(1,1) + m(4,1),
//				 m(1,2) + m(4,2),
//				 m(1,3) + m(4,3),
//				 m(1,4) + m(4,4));
//            right = new BasePlane(
//				-m(1,1) + m(4,1),
//				-m(1,2) + m(4,2),
//				-m(1,3) + m(4,3),
//				-m(1,4) + m(4,4));        
//            bottom = new BasePlane(
//				 m(2,1) + m(4,1),
//				 m(2,2) + m(4,2),
//				 m(2,3) + m(4,3),
//				 m(2,4) + m(4,4));
//            top = new BasePlane(
//				-m(2,1) + m(4,1),
//				-m(2,2) + m(4,2),
//				-m(2,3) + m(4,3),
//				-m(2,4) + m(4,4));
            
            allPlanes = new BasePlane[][] {
                {left, right},
                {bottom, top},
                {far, near},
            };
            faceBoundaries = new QuadrilateralBoundaries[3][2];
            
            for (int axis=0; axis<3; ++axis) {
                for (int side=0; side<2; ++side) {
                    System.out.println("Plane "+PLANE_NAMES[axis][side]+"\t"+allPlanes[axis][side]);
                }
            }
            
            for (int axis1=0; axis1<3; ++axis1) {
                for (int side1=0; side1<2; ++side1) {
                    List<Vector3f> corners = new ArrayList<>(4);
                    
                    for (int axis2=0; axis2<3; ++axis2) {
                        if (axis2==axis1) continue;
                        
                        for (int axis3=0; axis3<3; ++axis3) {
                            if (axis3==axis1 || axis3==axis2) continue;
                            
                            for (int side2=0; side2<2; ++side2) {
                                for (int side3=0; side3<2; ++side3) {
                                    Vector3f intersection = 
                                            allPlanes[axis1][side1].intersection(
                                            allPlanes[axis2][side2],
                                            allPlanes[axis3][side3]);
                                    
                                    if (intersection==null) {
                                        System.out.println("WARNING: Planes "+
                                                PLANE_NAMES[axis1][side1]+", "+
                                                PLANE_NAMES[axis2][side2]+", "+
                                                PLANE_NAMES[axis3][side3]+" don't intersect!");
                                    } else {
                                        corners.add(intersection);
                                    }
                                    
                                    
                                }
                            }
                            
                            break;
                        }
                        break;
                    }
                    
                    if (corners.size()!=4) {
                        System.out.println("WARNING: Found "+corners.size()+
                                " corners while processing plane "+PLANE_NAMES[axis1][side1]);
                    } else {
                        faceBoundaries[axis1][side1] = new QuadrilateralBoundaries(corners);
                    }
                }
            }
        }
        
        private double m(int m, int n) {
            return clip[m*4+n-5];
        }
    
    }
    
    private static float[][] FACE_COLORS = new float[][] {
        {1.0f, 0.0f, 0.0f},
        {1.0f, 1.0f, 0.0f},
        {0.0f, 1.0f, 0.0f},
        {0.0f, 1.0f, 1.0f},
        {0.0f, 0.0f, 1.0f},
        {1.0f, 0.0f, 1.0f},
    };
    
    private static void assign(float[] to, float[] vals, int skipDim) {
        int fromI = 0;
        for (int axis=0; axis<3; ++axis) {
            if (axis==skipDim) continue;
            to[axis] = vals[fromI];
            ++fromI;
        }
    }
    
    
    public static void main(String[] args) {
//Projection: [1.8106600046157837, 0.0, 0.0, 0.0, 0.0, 2.4142134189605713, 0.0, 0.0, 0.0, 0.0, -1.0010005235671997, -1.0, 0.0, 0.0, -0.020010003820061684, 0.0]
//ModelView : [0.866025447845459, 0.43301278352737427, 0.2500000298023224, 0.0, 0.0, 0.5000000596046448, -0.866025447845459, 0.0, -0.5000000596046448, 0.7500000596046448, 0.4330127239227295, 0.0, 3.5648113794195524E-8, 5.8457914065002115E-9, -6.0, 1.0]
        
//Projection: [1.8106600046157837, 0.0, 0.0, 0.0, 0.0, 2.4142134189605713, 0.0, 0.0, 0.0, 0.0, -1.0010005235671997, -1.0, 0.0, 0.0, -0.020010003820061684, 0.0]
//ModelView : [1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, -6.0, 1.0]

        double[] projectionMat = new double[] {
            1.8106600046157837, 0.0, 0.0, 0.0, 0.0, 2.4142134189605713, 0.0, 0.0, 0.0, 0.0, -1.0010005235671997, -1.0, 0.0, 0.0, -0.020010003820061684, 0.0
        };
        double[] modelViewMat = new double[] {
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, -6.0, 1.0
        };
        
        ViewFrustrum frustrum = new ViewFrustrum(modelViewMat, projectionMat);
        
        List<RenderableGroup> comps = new ArrayList<>();
        
        float[] tmp = new float[3];
        
        for (int axis=0; axis<3; ++axis) {
            for (int side=0; side<2; ++side) {
                BasePlane plane = frustrum.allPlanes[axis][side];
                List<RenderablePoint> planePoints = new ArrayList<>();
                
                for (int i=-20; i<=20; ++i) {
                    for (int j=-20; j<=20; ++j) {
                        assign(tmp, new float[]{i,j}, axis);
                        
                        float missingAxisVal = -plane.d;
                        for (int k=0; k<3; ++k) {
                            if (k==axis) continue;
                            missingAxisVal -= plane.normal.get(k)*tmp[k];
                        }
                        
                        tmp[axis] = missingAxisVal / plane.normal.get(axis);
                        
                        RenderablePoint pt = new RenderablePoint(Arrays.copyOf(tmp, 3));
                        float[] col = FACE_COLORS[axis*2+side];
                        pt.setColor(new Color(col[0], col[1], col[2], 1.0f));
                        planePoints.add(pt);
                    }
                }
                assign(tmp, new float[]{0.0f,0.0f}, axis);
                
                RenderableGroup group = new RenderableGroup(PLANE_NAMES[axis][side], planePoints);
                comps.add(group);
            }
        }
        
        VisualizerFrame frame = new VisualizerFrame(1, comps);
        RenderPanel renderPanel = frame.getRenderPanel();
        frame.setVisible(true);
        
//        List<RenderableVector> comps = new ArrayList<>();
//        for (int axis=0; axis<3; ++axis) {
//            for (int side=0; side<2; ++side) {
//                for (int corner=0; corner<4; ++corner) {
//                    Vector3f from = frustrum.faceBoundaries[axis][side].cornerPoints[corner];
//                    Vector3f to = frustrum.faceBoundaries[axis][side].cornerPoints[(corner+1)%4];
//                    
//                    RenderableVector vec = new RenderableVector(from.toArray(), to.toArray());
//                    float[] col = FACE_COLORS[axis*2+side];
//                    vec.setColor(new Color(col[0], col[1], col[2], 1.0f));
//                    comps.add(vec);
//                }
//            }
//        }
//        
//        
//        Vector3f lookAt = frustrum.faceBoundaries[2][0].cornerPoints[0];
//        VisualizerFrame frame = new VisualizerFrame(comps);
//        RenderPanel renderPanel = frame.getRenderPanel();
//        renderPanel.setLookAtLocation(lookAt.x, lookAt.y, lookAt.z);
//        frame.setVisible(true);
    }
}
