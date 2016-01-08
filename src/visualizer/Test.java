/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer;

import java.awt.Color;
import visualizer.renderer.RenderableVector;
import visualizer.renderer.RenderableGroup;
import visualizer.renderer.RenderableComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import visualizer.utils.RenderGroupUtils;

/**
 *
 * @author Umran
 */
public class Test {

  private static Rotation[] interp(Rotation start, Rotation end, final int steps) {
    if (steps <= 1) {
      throw new AssertionError("steps has to be more than 1");
    }

    Rotation[] rotations = new Rotation[steps];
    Rotation diff = start.applyInverseTo(end);
    Vector3D diffAxis = diff.getAxis();
    double diffAngle = diff.getAngle();
    for (int i = 0; i < steps; i++) {
      double interpAngle = (diffAngle * i) / (steps - 1);
      Rotation interpDiff = new Rotation(diffAxis, interpAngle);
      rotations[i] = interpDiff.applyTo(start);
    }

    return rotations;
  }

  public static void main(String[] args) {
    Vector3D[] origin = new Vector3D[]{
      Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K
    };
    Color[] originColors = new Color[]{
      Color.RED, Color.GREEN, Color.BLUE
    };
    String[] originNames = new String[]{
      "x", "y", "z"
    };

    Rotation[] rotations = interp(Rotation.IDENTITY,
            new Rotation(Vector3D.PLUS_I, Math.toRadians(90)),
            10);

    List<RenderableComponent> comps = new ArrayList<>(origin.length);
    for (int i = 0; i < origin.length; i++) {
      List<RenderableComponent> originVals = new ArrayList<>(rotations.length);
      for (int j = 0; j < rotations.length; j++) {
        Vector3D result = rotations[j].applyTo(origin[i]);
        originVals.add(new RenderableVector(
                Vector3D.ZERO.toArray(),
                result.toArray()));
      }
      RenderGroupUtils.assignTimeIndex(originVals, 1);
      RenderGroupUtils.assignNameIndex(originVals, 1);
      RenderGroupUtils.setColor(originVals, originColors[i]);
      comps.add(new RenderableGroup(originNames[i], originVals));
    }
    VisualizerFrame vf = new VisualizerFrame(1, Arrays.asList(new RenderableGroup("data", comps)));
    vf.setVisible(true);
  }
}
