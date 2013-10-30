/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

/**
 *
 * @author Umran
 */
public abstract class RenderableComponent implements Renderable {
    
    private static java.util.concurrent.atomic.AtomicLong id_counter = new AtomicLong(0);
    
    public static final float[] SELECTED_EMISSION_COL = new float[] {0.5f,0.5f,0.5f,1.0f};
    public static final float[] UNSELECTED_EMISSION_COL = new float[] {0.0f,0.0f,0.0f,1.0f};
    
    protected long id;
    protected RenderableComponent parentComponent;
    protected float size;
    protected Color color;
    protected CachedVisibility visibility;
    protected boolean selected;
    protected String name;
    protected Set<Integer> timeLocs;

    public RenderableComponent(float size, Color color) {
        this.id = id_counter.getAndIncrement();
        this.parentComponent = null;
        this.size = size;
        this.color = color;
        this.visibility = new CachedVisibility();
        this.name = null;
        this.timeLocs = new HashSet<>();
    }
    
    public RenderableComponent() {
        this(1.0f, Color.WHITE);
    }

    public long getId() {
        return id;
    }
    
    public RenderableComponent getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(RenderableComponent parentComponent) {
        this.parentComponent = parentComponent;
        this.visibility.setParent(parentComponent.visibility);
    }
    
    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
    
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    public boolean isHierachyVisible() {
        return visibility.isHierarchyVisible();
    }

    public boolean isVisible() {
        return visibility.isVisible();
    }

    public void setVisible(boolean visible) {
        visibility.setVisible(visible);
    }
    
    public void setTreeVisible(boolean visible) {
        visibility.setTreeVisible(visible);
    }
    
    public String getDescription() {
        String descr = "";
        if (parentComponent!=null) {
            descr += parentComponent.getDescription() + "(" + this.name + ")";
        } else {
            descr += this.name;
        }
        return descr;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public List<RenderableNames> getNameHierarchy() {
        List<RenderableNames> list;
        if (parentComponent!=null) {
            list = parentComponent.getNameHierarchy();
        } else {
            list = new ArrayList<>();
        }
        list.add(new RenderableNames(this, name));
        return list;
    }
    
    public boolean isGroup() {
        return false;
    }
    
    public List<RenderableComponent> getChildren() {
        return Collections.EMPTY_LIST;
    }

    public void setTimeLocs(Set<Integer> timeLocs) {
        this.timeLocs = timeLocs;
    }
    
    public Set<Integer> getTimeLocs() {
        return timeLocs;
    }
    
    protected void fillHierarchyNodes(Set<Long> filled, List<RenderableComponent> list, boolean leafsOnly) {
        if (filled.contains(id)) {
            return;
        }
        
        if (this.isGroup()) {
            if (leafsOnly) {
                filled.add(id);
                list.add(this);
            }
            List<RenderableComponent> children = getChildren();
            for (RenderableComponent c:children) {
                c.fillHierarchyNodes(filled, list, leafsOnly);
            }
        } else {
            filled.add(id);
            list.add(this);
        }
    }
    
    public List<RenderableComponent> getAllHierarchyNodes(boolean leafsOnly) {
        Set<Long> filled = new TreeSet<>();
        List<RenderableComponent> list = new ArrayList<>();
        fillHierarchyNodes(filled, list, leafsOnly);
        return list;
    }
    
    @Override
    public void init(RenderPanel renderTool, GLAutoDrawable glautodrawable) {
    }

    @Override
    public void render(RenderPanel renderTool, GLAutoDrawable glautodrawable, RenderedSelection selection) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void destroy(RenderPanel renderTool, GLAutoDrawable glautodrawable) {
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
    
    
    
    
}
