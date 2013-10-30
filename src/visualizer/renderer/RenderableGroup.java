/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.awt.Color;
import java.util.List;
import javax.media.opengl.GLAutoDrawable;

/**
 *
 * @author abd01c
 */
public class RenderableGroup extends RenderableComponent {
    
    private List<RenderableComponent> components;

    public RenderableGroup(String groupName, List<? extends RenderableComponent> components) {
        this.name = groupName;
        this.components = (List<RenderableComponent>)components;
        
        setAllChildren();
    }
    
    @Override
    public boolean isGroup() {
        return true;
    }
    
    @Override
    public List<RenderableComponent> getChildren() {
        return components;
    }
    
    private void setAllChildren() {
        for (RenderableComponent comp:components) {
            comp.setParentComponent(this);
        }
    }
    
    @Override
    public void init(RenderPanel renderTool, GLAutoDrawable glautodrawable) {
        for (RenderableComponent cp:components) {
            cp.init(renderTool, glautodrawable);
        }
    }
    
    @Override
    public void render(RenderPanel renderTool, GLAutoDrawable glautodrawable, RenderedSelection selection) {
        if (!visibility.isHierarchyVisible()) {
            return;
        }
        
//        System.out.println("rendering group "+name);
        
//        for (RenderableComponent cp:components) {
//            cp.render(renderTool, glautodrawable, selection);
//        }
    }
    
    @Override
    public void destroy(RenderPanel renderTool, GLAutoDrawable glautodrawable) {
        for (RenderableComponent cp:components) {
            cp.destroy(renderTool, glautodrawable);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        for (RenderableComponent cp:components) {
            cp.setSelected(selected);
        }
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        for (RenderableComponent r:components) {
            r.setColor(color);
        }
    }

    
    
    
}
