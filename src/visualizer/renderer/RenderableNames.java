/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.io.File;
import java.util.List;

/**
 *
 * @author Umran
 */
public class RenderableNames {
    
    public final RenderableComponent component;
    public final String name;

    public RenderableNames(RenderableComponent component, String name) {
        this.component = component;
        this.name = name;
    }
    
    public static String toNameString(List<RenderableNames> list) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (RenderableNames name:list) {
            if (first) {
                first = false;
            } else {
                builder.append(File.separator);
            }
            builder.append(name);
        }
        return builder.toString();
    }
    
}
