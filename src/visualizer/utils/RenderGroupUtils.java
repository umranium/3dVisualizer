/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visualizer.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import visualizer.renderer.RenderableComponent;
import visualizer.renderer.RenderablePoint;
import visualizer.renderer.RenderableVector;

/**
 *
 * @author umran
 */
public class RenderGroupUtils {
  
  private static final double[] ORIGIN = new double[] {0,0,0};
  
  public static List<RenderableVector> toRendableVectors(Vector3D[] vecs) {
    return toRendableVectors(vecs, null);
  }
  
  public static List<RenderableVector> toRendableVectors(Vector3D[] vecs, String[] names) {
    return toRendableVectors(null, vecs, names);
  }
  
  public static List<RenderableVector> toRendableVectors(Vector3D[] fromVecs, Vector3D[] toVecs, String[] names) {
    if (fromVecs==null && toVecs==null) {
      throw new RuntimeException("Invalid parameters: both from and to vectors are NULL.");
    }
    if (fromVecs!=null && toVecs!=null && fromVecs.length!=toVecs.length) {
      throw new RuntimeException("Invalid parameters: from and to arrays have different lengths.");
    }
    
    final int len;
    if (fromVecs!=null) {
      len = fromVecs.length;
    } else {
      len = toVecs.length;
    }
    
    List<RenderableVector> renderables = new ArrayList<>(len);
    for (int i=0; i<len; ++i) {
      double[] fromArr = ORIGIN;
      double[] toArr = ORIGIN;
      if (fromVecs!=null) {
        fromArr = fromVecs[i].toArray();
      }
      if (toVecs!=null) {
        toArr = toVecs[i].toArray();
      }
      
      RenderableVector renderableVec = new RenderableVector(fromArr, toArr);
      
      if (names==null) {
        renderableVec.setName(Integer.toString(i));
      } else {
        renderableVec.setName(names[i]);
      }
      
      renderables.add(renderableVec);
    }
    return renderables;
  }
  
  public static List<RenderablePoint> toRendablePoints(Vector3D[] vecs) {
    return toRendablePoints(vecs, null);
  }
  
  public static List<RenderablePoint> toRendablePoints(Vector3D[] vecs, String[] names) {
    List<RenderablePoint> renderables = new ArrayList<>(vecs.length);
    for (int i=0; i<vecs.length; ++i) {
      Vector3D vec = vecs[i];
      RenderablePoint renderablePoint = new RenderablePoint(vec.toArray());
      if (names==null) {
        renderablePoint.setName(Integer.toString(i));
      } else {
        renderablePoint.setName(names[i]);
      }
      renderables.add(renderablePoint);
    }
    return renderables;
  }
  
  public static <T extends RenderableComponent> List<T> assignTimeIndex(List<T> comps, int offset) {
    for (int i = 0; i < comps.size(); ++i) {
      comps.get(i).setTimeLocs(Collections.singleton(i+offset));
    }
    return comps;
  }
  
  public static <T extends RenderableComponent> List<T> assignNameIndex(List<T> comps, int offset) {
    for (int i = 0; i < comps.size(); ++i) {
      comps.get(i).setName(Integer.toString(i+offset));
    }
    return comps;
  }
  
  public static <T extends RenderableComponent> List<T> assignTimeIndex(List<T> comps) {
    return assignTimeIndex(comps, 0);
  }
  
  public static <T extends RenderableComponent> List<T> setSize(List<T> comps, float size) {
    for (int i = 0; i < comps.size(); ++i) {
      comps.get(i).setSize(size);
    }
    return comps;
  }

  public static <T extends RenderableComponent> List<T> setColor(List<T> comps, Color col) {
    for (int i = 0; i < comps.size(); ++i) {
      comps.get(i).setColor(col);
    }
    return comps;
  }
  
  public static List<RenderableVector> setFrom(List<RenderableVector> comps, Vector3D from) {
    float[] fromArr = new float[]{(float)from.getX(), (float)from.getY(), (float)from.getZ()};
    for (int i = 0; i < comps.size(); ++i) {
      comps.get(i).setFrom(fromArr);
    }
    return comps;
  }

  public static List<RenderableVector> setTo(List<RenderableVector> comps, Vector3D to) {
    float[] toArr = new float[]{(float)to.getX(), (float)to.getY(), (float)to.getZ()};
    for (int i = 0; i < comps.size(); ++i) {
      comps.get(i).setTo(toArr);
    }
    return comps;
  }
  
  public static Color adjustHsb(Color col, double incrH, double incrS, double incrB) {
    float[] res = new float[3];
    Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), res);
    return Color.getHSBColor(res[0]+(float)incrH, res[1]+(float)incrS, res[2]+(float)incrB);
  }
  
}
