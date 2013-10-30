/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer;

/**
 *
 * @author umran
 */
public class OsCommon {
    
    public static boolean isMacOs;
    
    static {
        String osName = System.getProperty("os.name").toLowerCase();
        isMacOs = osName.startsWith("mac os x");
    }
    
}
