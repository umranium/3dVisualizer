/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.util.ArrayList;
import java.util.List;
import visualizer.utils.Vector3f;

/**
 *
 * @author Umran
 */
public class ViewFrustrumHelper {
    
    public static class FacePoints {
        public final Vector3f topLeft, topRight, bottomLeft, bottomRight;

        public FacePoints(Vector3f topLeft, Vector3f topRight, Vector3f bottomLeft, Vector3f bottomRight) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
        }
    }
    
    public static class Plane {
        public final Vector3f point;
        public final Vector3f normal;
        public final float d;
        public final float minX, maxX, minY, maxY, minZ, maxZ;

        public Plane(Vector3f a, Vector3f b, Vector3f c, Vector3f[] planeBoundaries) {
            this.point = a;
            this.normal = (b.subtract(a)).cross(c.subtract(a)).normalizeLocal();
            this.d = - normal.dot(a);
            
            float mnX, mxX, mnY, mxY, mnZ, mxZ;
            
            mnX = mnY = mnZ = Float.POSITIVE_INFINITY;
            mxX = mxY = mxZ = Float.NEGATIVE_INFINITY;
            
            for (Vector3f boundary:planeBoundaries) {
                if (boundary.x<mnX) {
                    mnX = boundary.x;
                }
                if (boundary.x>mxX) {
                    mxX = boundary.x;
                }
                if (boundary.y<mnY) {
                    mnY = boundary.y;
                }
                if (boundary.y>mxY) {
                    mxY = boundary.y;
                }
                if (boundary.z<mnZ) {
                    mnZ = boundary.z;
                }
                if (boundary.z>mxZ) {
                    mxZ = boundary.z;
                }
            }
            
            this.minX = mnX;
            this.minY = mnY;
            this.minZ = mnZ;
            this.maxX = mxX;
            this.maxY = mxY;
            this.maxZ = mxZ;
        }
        
        public double intersect(Vector3f lineOrigin, Vector3f normLineDir) {
            double num = (point.subtract(lineOrigin)).dot(normal);
            double denom = normLineDir.dot(normal);
            //System.out.println("num="+num+" denom="+denom);
            boolean numIsZero = Math.abs(num)<1.0e-10;
            boolean denomIsZero = Math.abs(denom)<1.0e-10;
            if (denomIsZero) {
                return Double.NaN;
            } else {
                double dist = num/denom;
                Vector3f pt = lineOrigin.add(normLineDir.mult((float)dist));
                if (pt.x>=minX && pt.x<=maxX &&
                        pt.y>=minY && pt.y<=maxY &&
                        pt.z>=minZ && pt.z<=maxZ) {
                    return dist;
                } else {
                    return Double.NaN;
                }
            }
        }
    }
    
    public static final String[] PLANE_NAMES = new String[] {
        "Near", "Far",
        "Left", "Right",
        "Bottom", "Top"
    };
    
    public static class FrustrumPlanes {
        public final Plane near, far, left, right, bottom, top;
        public final Plane[] allPlanes;

        public FrustrumPlanes(Plane near, Plane far, Plane left, Plane right, Plane bottom, Plane top) {
            this.near = near;
            this.far = far;
            this.left = left;
            this.right = right;
            this.bottom = bottom;
            this.top = top;
            
            this.allPlanes = new Plane[] {
                this.near, this.far,
                this.left, this.right,
                this.bottom, this.top
            };
        }
    }
    
    public static class FrustrumFaces {

        public final Vector3f eyeLoc;
        public final FacePoints nearFace;
        public final FacePoints farFace;
        public final FrustrumPlanes planes;

        public FrustrumFaces(Vector3f eyeLoc, FacePoints nearFace, FacePoints farFace) {
            this.eyeLoc = eyeLoc;
            this.nearFace = nearFace;
            this.farFace = farFace;
            this.planes = new FrustrumPlanes(
                    new Plane(nearFace.bottomLeft, nearFace.topLeft, nearFace.topRight,
                        new Vector3f[]{nearFace.bottomLeft, nearFace.bottomRight, nearFace.topLeft, nearFace.topRight}),
                    new Plane(farFace.bottomLeft, farFace.topRight, farFace.topLeft,
                        new Vector3f[]{farFace.bottomLeft, farFace.bottomRight, farFace.topLeft, farFace.topRight}),
                    new Plane(nearFace.bottomLeft, farFace.topLeft, nearFace.topLeft,
                        new Vector3f[]{nearFace.bottomLeft, nearFace.topLeft, farFace.bottomLeft, farFace.topLeft}),
                    new Plane(nearFace.bottomRight, nearFace.topRight, farFace.topRight,
                        new Vector3f[]{nearFace.bottomRight, nearFace.topRight, farFace.bottomRight, farFace.topRight}),
                    new Plane(nearFace.bottomLeft, nearFace.bottomRight, farFace.bottomRight,
                        new Vector3f[]{nearFace.bottomLeft, nearFace.bottomRight, farFace.bottomLeft, farFace.bottomRight}),
                    new Plane(nearFace.topRight, nearFace.topLeft, farFace.topRight,
                        new Vector3f[]{nearFace.topLeft, nearFace.topRight, farFace.topLeft, farFace.topRight}));

        }
    }
    
    public static FrustrumFaces computeFrustrumFaces(float viewportAspect, float[] lookAtEyeLoc, double[] lookAtLoc) {
        double fovRadians = Math.toRadians(RenderPanel.FOVY);
        double hNear = 2 * Math.tan(fovRadians / 2.0) * RenderPanel.Z_NEAR;
        double wNear = hNear * viewportAspect;
        
//        System.out.println("hNear = " + hNear);
//        System.out.println("wNear = " + wNear);
        
        double hFar = 2 * Math.tan(fovRadians / 2.0) * RenderPanel.Z_FAR;
        double wFar = hFar * viewportAspect;
        
//        System.out.println("hFar = " + hFar);
//        System.out.println("wFar = " + wFar);
        
        Vector3f lookAt = new Vector3f((float)lookAtLoc[0], (float)lookAtLoc[1], (float)lookAtLoc[2]);
        Vector3f eyeLoc = new Vector3f(lookAtEyeLoc[0], lookAtEyeLoc[1], lookAtEyeLoc[2]);
        Vector3f lookAtDir = lookAt.subtract(eyeLoc).normalizeLocal();
        Vector3f up = new Vector3f(RenderPanel.EYE_UP[0], RenderPanel.EYE_UP[1], RenderPanel.EYE_UP[2]);
        Vector3f left = lookAtDir.cross(up).normalizeLocal();
        up = left.cross(lookAtDir).normalizeLocal();
        
//        System.out.println("lookAtDir = " + lookAtDir);
//        System.out.println("up = " + up);
//        System.out.println("right = " + left);
        
        Vector3f nearCenter = eyeLoc.add(lookAtDir.mult(RenderPanel.Z_NEAR));
        Vector3f nearTopRight = nearCenter.add(up.mult((float)hNear*0.5f)).add(left.mult((float)wNear*0.5f));
        Vector3f nearTopLeft = nearCenter.add(up.mult((float)hNear*0.5f)).add(left.mult((float)wNear*-0.5f));
        Vector3f nearBottomRight = nearCenter.add(up.mult((float)hNear*-0.5f)).add(left.mult((float)wNear*0.5f));
        Vector3f nearBottomLeft = nearCenter.add(up.mult((float)hNear*-0.5f)).add(left.mult((float)wNear*-0.5f));
        
        Vector3f farCenter = eyeLoc.add(lookAtDir.mult(RenderPanel.Z_FAR));
        Vector3f farTopRight = farCenter.add(up.mult((float)hFar*0.5f)).add(left.mult((float)wFar*0.5f));
        Vector3f farTopLeft = farCenter.add(up.mult((float)hFar*0.5f)).add(left.mult((float)wFar*-0.5f));
        Vector3f farBottomRight = farCenter.add(up.mult((float)hFar*-0.5f)).add(left.mult((float)wFar*0.5f));
        Vector3f farBottomLeft = farCenter.add(up.mult((float)hFar*-0.5f)).add(left.mult((float)wFar*-0.5f));
        
        return new FrustrumFaces(
                eyeLoc,
                new FacePoints(nearTopLeft, nearTopRight,nearBottomLeft, nearBottomRight),
                new FacePoints(farTopLeft, farTopRight, farBottomLeft, farBottomRight)
                );
    }

    
}
