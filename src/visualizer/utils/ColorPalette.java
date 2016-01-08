/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visualizer.utils;

import java.awt.Color;

/**
 *
 * @author umran
 */
public class ColorPalette {
  
  private static int nextPow2(int currentVal) {
    if (currentVal<=0) {
      throw new RuntimeException("Invalid parameter value "+currentVal);
    }
    return (int)Math.pow(2.0, Math.ceil(Math.log(currentVal)/Math.log(2.0)));
  }
  
  private int currentColor = -1;
  private int currentNumColors = 0;
  
  public synchronized Color nextColor() {
    currentColor += 2;
    if (currentColor>=currentNumColors) {
      currentNumColors = nextPow2(currentNumColors+1);
      currentColor = 1;
      //System.out.println("\treset to "+currentColor+"/"+currentNumColors);
    }

    //System.out.println("found:"+currentColor+"/"+currentNumColors);
    Color col = Color.getHSBColor(
      currentColor/(float)(currentNumColors),
      1.0f,
      1.0f
    );
    
    return col;
  }
  
  public static void main(String[] args) {
    ColorPalette cp = new ColorPalette();
    for (int i = 0; i < 100; ++i) {
      Color col = cp.nextColor();
      System.out.println(i+"\t"+col.getRed()+"\t"+col.getGreen()+"\t"+col.getBlue());
    }
  }
  
}
