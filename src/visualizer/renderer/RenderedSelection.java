/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.util.HashMap;
import java.util.Map;
import javax.media.opengl.GL2;
import visualizer.renderer.ViewInfo;

/**
 *
 * @author abd01c
 */
public class RenderedSelection {
    
    private int nameIds;
    private Map<Integer,RenderableSelectionInfo> itemMapping;
    private int mouseX, mouseY;
    private ViewInfo viewInfo;

    public RenderedSelection(int mouseX, int mouseY, ViewInfo viewInfo) {
        nameIds = 1;
        itemMapping = new HashMap<>();
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.viewInfo = viewInfo;
    }

    public int getIssuedIdCount() {
        return nameIds-1;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
    
    public ViewInfo getViewInfo() {
        return viewInfo;
    }
    
    public boolean hasId(int id) {
        return itemMapping.containsKey(id);
    }
    
    synchronized
    private int getId(RenderableSelectionInfo extra) {
        int id = nameIds;
        itemMapping.put(id, extra);
        ++nameIds;
        return id;
    }
    
    public RenderableSelectionInfo getItem(int id) {
        return itemMapping.get(id);
    }
    
    /**
     * Registers the extra information, and returns the ID issued.
     * @param gl OpenGL instance to use
     * @param extra Extra information to register
     * @return ID issued.
     */
    public int register(GL2 gl, RenderableSelectionInfo extra) {
        int val = getId(extra);
        gl.glLoadName(val);
        return val;
    }
    
    /**
     * Registers the ID
     */
    public void register(GL2 gl, int id) {
        gl.glLoadName(id);
    }
    
}
