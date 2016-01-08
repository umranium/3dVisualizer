/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Umran
 */
public class TimeScale {
    
    private int initTimeUnitsCount;
    private Map<Integer,List<RenderableComponent>> timeComponents;
    private List<RenderableComponent> allTimeComponent;
    private int minTime = Integer.MAX_VALUE;
    private int maxTime = Integer.MIN_VALUE;

    public TimeScale(int initTimeUnitsCount) {
        this.initTimeUnitsCount = initTimeUnitsCount;
        timeComponents = new HashMap<>(initTimeUnitsCount);
        allTimeComponent = new ArrayList<>();
    }
    
    public void clearAll() {
        timeComponents.clear();
    }
    
    private void register(int time, RenderableComponent c) {
        if (!timeComponents.containsKey(time)) {
            timeComponents.put(time, new ArrayList<RenderableComponent>());
            
            if (time<minTime) {
                minTime = time;
            }
            if (time>maxTime) {
                maxTime = time;
            }
        }
        timeComponents.get(time).add(c);
    }
    
    private void registerAllTime(RenderableComponent c) {
        allTimeComponent.add(c);
    }
    
    private void register(Set<Long> registered, RenderableComponent c) {
        long id = c.getId();
        
        if (registered.contains(id)) {
            return;
        }
        
        registered.add(id);
        
        Set<Integer> t = c.getTimeLocs();
        if (t.isEmpty()) {
            registerAllTime(c);
        } else {
            for (Integer i:t) {
                register(i, c);
            }
        }
        if (c.isGroup()) {
            for (RenderableComponent rc:c.getChildren()) {
                register(registered, rc);
            }
        }
    }
    
    public void register(RenderableComponent c) {
        register(new HashSet<Long>(initTimeUnitsCount), c);
    }
    
    public List<RenderableComponent> getAt(int time) {
        return timeComponents.get(time);
    }

    public int getMinTime() {
        return minTime;
    }

    public int getMaxTime() {
        return maxTime;
    }
    
    public int getTimeRange() {
        return maxTime - minTime + 1;
    }
    
    public List<RenderableComponent> getList(int startTime, int endTime) {
//        System.out.println("getting all components from "+startTime+" to "+endTime);
        List<RenderableComponent> comp = new ArrayList<>();
        for (Map.Entry<Integer,List<RenderableComponent>> entry:timeComponents.entrySet()) {
            int time = entry.getKey();
            if (time>=startTime && time<=endTime) {
                comp.addAll(entry.getValue());
            }
        }
//        System.out.println("\t"+comp.size()+" items found");
        for (RenderableComponent c:allTimeComponent) {
            comp.add(c);
        }
//        System.out.println("\t"+allTimeComponent.size()+" all time items added");
        return comp;
    }
    
    
}
