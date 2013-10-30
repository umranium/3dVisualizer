/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

/**
 *
 * @author Umran
 */
public interface Renderable {
    public void init(RenderPanel renderTool, GLAutoDrawable glautodrawable);
    public void render(RenderPanel renderTool, GLAutoDrawable glautodrawable, RenderedSelection selection);
    public void destroy(RenderPanel renderTool, GLAutoDrawable glautodrawable);
    
}
