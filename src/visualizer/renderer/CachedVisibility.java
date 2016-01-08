/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Umran
 */
public class CachedVisibility {
    
    private CachedVisibility parent;
    private List<CachedVisibility> children;
    private boolean visible;
    private boolean hierarchyVisible;

    public CachedVisibility() {
        children = new ArrayList<>();
        hierarchyVisible = visible = true;
    }

    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible, boolean propagateUp) {
//        System.out.println("set to "+visible);
        this.visible = visible;
        if (visible && propagateUp) {
          propagateVisibilityUp(visible);
        } else {
          propagateHierarchyVisibilityDown();
        }
    }
    
    public void setTreeVisible(boolean visible) {
        propagateVisibilityDown(visible);
    }

    public CachedVisibility getParent() {
        return parent;
    }

    public void setParent(CachedVisibility parent) {
        if (this.parent!=null) {
            this.parent.children.remove(this);
        }
        this.parent = parent;
        propagateHierarchyVisibilityDown();
        this.parent.children.add(this);
    }

    public boolean isHierarchyVisible() {
        return hierarchyVisible;
    }
    
    private void propagateHierarchyVisibilityDown() {
        this.hierarchyVisible = visible && (parent==null || parent.isHierarchyVisible());
//        System.out.println("propagate down "+hierarchyVisible);
        for (CachedVisibility child:children) {
            child.propagateHierarchyVisibilityDown();
        }
    }
    
    private void propagateVisibilityDown(boolean visible) {
        this.visible = visible;
        this.hierarchyVisible = visible && (parent==null || parent.isHierarchyVisible());
//        System.out.println("propagate down "+hierarchyVisible);
        for (CachedVisibility child:children) {
            child.propagateVisibilityDown(visible);
        }
    }
    
    private void propagateVisibilityUp(boolean visible) {
        this.visible = visible;
        if (parent!=null) {
          parent.propagateVisibilityUp(visible);
        } else {
          propagateHierarchyVisibilityDown();
        }
    }
}
