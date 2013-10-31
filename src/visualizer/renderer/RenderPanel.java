/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.util.concurrent.ScheduledExecutorService;
import javax.swing.JPanel;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.JFrame;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import javax.swing.JScrollPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import visualizer.OsCommon;
import visualizer.renderer.ViewFrustrumHelper.FrustrumFaces;
import visualizer.utils.Vector3f;

/**
 *
 * @author Umran
 */
public class RenderPanel extends JPanel {

  static {
    GLProfile.initSingleton();
  }

  public static final float FOVY = 45.0f;
  public static final float Z_NEAR = 0.01f;
  public static final float Z_FAR = 20.0f;
  public static final float[] EYE_UP = {0.0f, 1.0f, 0.0f};

  public static final double MIN_LOOKAT_DIST = 0.1;
  public static final double MAX_LOOKAT_DIST = Z_FAR / 2.0;

  public static final int KEY_RESET_VALUES = KeyEvent.VK_F9;
  public static final int KEY_INCR_PITCH = KeyEvent.VK_UP;
  public static final int KEY_DECR_PITCH = KeyEvent.VK_DOWN;
  public static final int KEY_DECR_BEARING = KeyEvent.VK_LEFT;
  public static final int KEY_INCR_BEARING = KeyEvent.VK_RIGHT;
  public static final int KEY_DECR_DISTANCE = KeyEvent.VK_PAGE_UP;
  public static final int KEY_INCR_DISTANCE = KeyEvent.VK_PAGE_DOWN;
  public static final int KEY_DECR_LOC_Z = KeyEvent.VK_Q;
  public static final int KEY_DECR_LOC_Y = KeyEvent.VK_W;
  public static final int KEY_INCR_LOC_Z = KeyEvent.VK_E;
  public static final int KEY_DECR_LOC_X = KeyEvent.VK_A;
  public static final int KEY_INCR_LOC_Y = KeyEvent.VK_S;
  public static final int KEY_INCR_LOC_X = KeyEvent.VK_D;

  public static final int[] ALL_KEYS = new int[]{
    KEY_RESET_VALUES,
    KEY_INCR_PITCH,
    KEY_DECR_PITCH,
    KEY_INCR_BEARING,
    KEY_DECR_BEARING,
    KEY_INCR_DISTANCE,
    KEY_DECR_DISTANCE,
    KEY_INCR_LOC_X,
    KEY_DECR_LOC_X,
    KEY_INCR_LOC_Y,
    KEY_DECR_LOC_Y,
    KEY_INCR_LOC_Z,
    KEY_DECR_LOC_Z,};

  public interface LookAtChangedEventHandler {

    void onLookAtChange();
  }

  public interface LookAtItemChangedEventHandler {

    void itemAt(int locX, int locY, List<RenderableSelectionInfo> items);
  }

  private TimeScale timeScale;
  private RenderableGroup rootGroup;
  private List<RenderableComponent> allComponents;
  private int startTime;
  private int endTime;
  private RenderableComponent[] renderingArr;
  private int renderingArrLen;
  private Color clearColor;

  private GLU glu;                         // get GL Utilities
  private GLUT glut;                         // get GL Utility Toolkit
  private double[] lookAtLocation;
  private double lookAtDistance;
  private double lookAtPitch;
  private double lookAtBearing;
  private float[] eyeLoc;
  private RenderableAxi axi;
  private ViewFrustrumHelper.FrustrumFaces frustrumFaces = null;

  private GLProfile glprofile;
  private GLCapabilities glcapabilities;
  private GLCanvas glcanvas;
  private long lastSceneDrawTime;
  private boolean pendingUpdate;
  private GlTaskQueue glTaskQueue = new GlTaskQueue();
  private ScheduledExecutorService scheduledExecutorService;

  private LookAtChangedEventHandler lookAtChangedEventHandler;
  private LookAtItemChangedEventHandler lookAtItemChangedEventHandler;

  private MouseHandler mouseHandler = new MouseHandler(this);

  private class GlTaskQueue {

    final List<Runnable> tasks;

    public GlTaskQueue() {
      tasks = new ArrayList<>();
    }

    public void add(Runnable task) {
      synchronized (tasks) {
        tasks.add(task);
      }
    }

    public List<Runnable> fetch() {
      synchronized (tasks) {
        if (tasks.isEmpty()) {
          return Collections.EMPTY_LIST;
        } else {
          List<Runnable> tasksCopy = new ArrayList<>(tasks);
          tasks.clear();
          return tasksCopy;
        }
      }
    }
  }

  public RenderPanel() {
    this.lookAtLocation = new double[]{0.0, 0.0, 0.0};
    this.axi = new RenderableAxi();
    this.eyeLoc = new float[3];
    this.setFocusable(false);

    this.timeScale = null;
    this.rootGroup = null;
    this.startTime = Integer.MIN_VALUE;
    this.endTime = Integer.MAX_VALUE;
    this.renderingArr = new RenderableComponent[0];
    this.renderingArrLen = 0;
    this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    this.clearColor = Color.BLACK;

    resetLookAtValues(true);

    this.addHierarchyListener(new HierarchyListener() {
      @Override
      public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0
                && RenderPanel.this.isShowing()) {
          initGl();
        }
      }
    });
  }
  
  public void destroy() {
    this.scheduledExecutorService.shutdown();
  }

  private void resetLookAtValues(boolean resetLocation) {
    if (resetLocation) {
      this.lookAtLocation[0] = 0.0f;
      this.lookAtLocation[1] = 0.0f;
      this.lookAtLocation[2] = 0.0f;
    }
    this.lookAtDistance = 6.0f;
    this.lookAtBearing = 0.0f;
    this.lookAtPitch = 0.0f;

    updateCamera();
  }

  public static void calcEyeLoc(float[] res, double[] lookAtLoc, double bearing,
          double pitch, double dist) {
    double bearingRadians = Math.toRadians(bearing);
    double pitchRadians = Math.toRadians(pitch);
    double cosLookAtBearing = Math.cos(bearingRadians);
    double sinLookAtBearing = Math.sin(bearingRadians);
    double cosLookAtPitch = Math.cos(pitchRadians);
    double sinLookAtPitch = Math.sin(pitchRadians);
    res[0] = (float) (dist * sinLookAtBearing * cosLookAtPitch + lookAtLoc[0]);
    res[1] = (float) (dist * sinLookAtPitch + lookAtLoc[1]);
    res[2] = (float) (dist * cosLookAtBearing * cosLookAtPitch + lookAtLoc[2]);
  }

  private FrustrumFaces computeFrustrum() {
    if (glcanvas == null) {
      return null;
    }

    float width = glcanvas.getWidth();
    float height = glcanvas.getHeight();
    if (height == 0) {
      height = 1;   // prevent divide by zero
    }
    float aspect = (float) width / height;
    return ViewFrustrumHelper.computeFrustrumFaces(aspect, eyeLoc, lookAtLocation);
  }

  private void updateCamera() {
    calcEyeLoc(eyeLoc, lookAtLocation, lookAtBearing, lookAtPitch, lookAtDistance);
    frustrumFaces = null;//computeFrustrum();
    if (lookAtChangedEventHandler != null) {
      lookAtChangedEventHandler.onLookAtChange();
    }
  }

  private void initGl() {
    glprofile = GLProfile.getDefault();
    glcapabilities = new GLCapabilities(glprofile);
    glcanvas = new GLCanvas(glcapabilities);
    glcanvas.addGLEventListener(new GLEventListener() {
      @Override
      public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
        RenderPanel.this.reshape(glautodrawable, width, height);
      }

      @Override
      public void init(GLAutoDrawable glautodrawable) {
        RenderPanel.this.init(glautodrawable);
      }

      @Override
      public void dispose(GLAutoDrawable glautodrawable) {
        RenderPanel.this.dispose(glautodrawable);
      }

      @Override
      public void display(GLAutoDrawable glautodrawable) {
        RenderPanel.this.render(glautodrawable);
      }
    });
    glcanvas.setFocusable(true);
    glcanvas.setEnabled(true);
    this.setFocusable(true);
    this.setLayout(new BorderLayout());
    this.add(glcanvas, BorderLayout.CENTER);

    glcanvas.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        double dist = getLookAtDistance();
        dist += e.getPreciseWheelRotation();
        setLookAtDistance(dist);
        refreshScene();
      }
    });
    glcanvas.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        //System.out.println("mouseMoved:"+e.getPoint());
        if (lookAtItemChangedEventHandler != null) {
          findItemAtLoc(e.getX(), e.getY(),
                  new RenderPanel.ItemAtLocCallback() {
                    @Override
                    public void itemAt(int locX, int locY, ViewInfo viewInfo,
                            List<RenderableSelectionInfo> items) {
                      if (lookAtItemChangedEventHandler != null) {
                        lookAtItemChangedEventHandler.itemAt(
                                locX, locY, items);
                      }
                    }
                  });
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        //System.out.println("mouseDragged:"+e.getPoint());
        mouseHandler.mouseDragged(e);
      }

    });

    glcanvas.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        //System.out.println("mouseClicked");
        mouseHandler.mouseClicked(e);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        //System.out.println("mousePressed: "+e.getPoint());
        mouseHandler.mousePressed(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        //System.out.println("mouseReleased: "+e.getPoint());
        mouseHandler.mouseReleased(e);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        //System.out.println("mouseEntered");
      }

      @Override
      public void mouseExited(MouseEvent e) {
        //System.out.println("mouseExited");
      }
    });

    glcanvas.revalidate();
  }

  public void setTimeScale(TimeScale timeScale) {
    this.timeScale = timeScale;
    doRenderableTimeScaleRegistration();
  }

  public void setRootGroup(RenderableGroup rootGroup) {
    this.rootGroup = rootGroup;
    this.allComponents = this.rootGroup.getAllHierarchyNodes(false);
    doRenderableTimeScaleRegistration();
  }

  private void doRenderableTimeScaleRegistration() {
    if (timeScale != null) {
      timeScale.clearAll();
      if (rootGroup != null) {
        timeScale.register(rootGroup);

        extractRenderableArr();
      }
    }
  }

  public void setVisibleTime(int startTime, int endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
    extractRenderableArr();
  }

  private void extractRenderableArr() {
    List<RenderableComponent> renderingList = timeScale.getList(startTime, endTime);
    if (renderingArr.length < renderingList.size()) {
      renderingArr = new RenderableComponent[renderingList.size()];
    }
    renderingList.toArray(renderingArr);
    renderingArrLen = renderingList.size();
  }

  public GLU getGLU() {
    return glu;
  }

  public GLUT getGLUT() {
    return glut;
  }

  public Vector3f getLookAtLocation() {
    return new Vector3f(lookAtLocation);
  }

  public double getLookAtBearing() {
    return lookAtBearing;
  }

  public double getLookAtPitch() {
    return lookAtPitch;
  }

  public double getLookAtDistance() {
    return lookAtDistance;
  }

  public void setLookAtLocation(double x, double y, double z) {
    if (lookAtLocation[0] == x && lookAtLocation[1] == y
            && lookAtLocation[2] == z) {
      return;
    }

    lookAtLocation[0] = x;
    lookAtLocation[1] = y;
    lookAtLocation[2] = z;
    updateCamera();
  }

  public void setLookAtPitch(double pitch) {
    setPitchBearingDistance(pitch, Double.NaN, Double.NaN);
  }

  public void setLookAtBearing(double bearing) {
    setPitchBearingDistance(Double.NaN, bearing, Double.NaN);
  }

  public void setLookAtDistance(double distance) {
    setPitchBearingDistance(Double.NaN, Double.NaN, distance);
  }

  public void setPitchBearingDistance(double pitch, double bearing, double distance) {
    boolean needsUpdate = false;
    if (!Double.isNaN(pitch) && pitch != lookAtPitch) {
      lookAtPitch = clip(pitch, -90.0, 90.0);
      needsUpdate = true;
    }
    if (!Double.isNaN(bearing) && bearing != lookAtBearing) {
      lookAtBearing = bearing;
      needsUpdate = true;
    }
    if (!Double.isNaN(distance) && distance != lookAtDistance) {
      lookAtDistance = Math.min(MAX_LOOKAT_DIST,
              Math.max(MIN_LOOKAT_DIST, distance));
      needsUpdate = true;
    }
    if (needsUpdate) {
      updateCamera();
    }
  }

  private double clip(double value, double min, double max) {
    if (value < min) {
      value = min;
    }
    if (value > max) {
      value = max;
    }
    return value;
  }

  public String getLookAtDescription() {
    return String.format(
            "Looking at [%.4f,%.4f,%.4f] distance=%.4f, bearing=%.4f, pitch=%.4f",
            lookAtLocation[0], lookAtLocation[1], lookAtLocation[2],
            lookAtDistance,
            lookAtBearing,
            lookAtPitch);
  }

  public RenderableAxi getAxi() {
    return axi;
  }

  public GLCanvas getGLCanvas() {
    return glcanvas;
  }

  public LookAtChangedEventHandler getLookAtChangedEventHandler() {
    return lookAtChangedEventHandler;
  }

  public void setLookAtChangedEventHandler(LookAtChangedEventHandler lookAtChangedEventHandler) {
    this.lookAtChangedEventHandler = lookAtChangedEventHandler;
  }

  public LookAtItemChangedEventHandler getLookAtItemChangedEventHandler() {
    return lookAtItemChangedEventHandler;
  }

  public void setLookAtItemChangedEventHandler(LookAtItemChangedEventHandler lookAtItemChangedEventHandler) {
    this.lookAtItemChangedEventHandler = lookAtItemChangedEventHandler;
  }

  public FrustrumFaces getFrustrumFaces() {
    if (frustrumFaces == null) {
      frustrumFaces = computeFrustrum();
    }
    return frustrumFaces;
  }

  public Color getClearColor() {
    return clearColor;
  }

  public void setClearColor(Color clearColor) {
    this.clearColor = clearColor;
  }

  protected void init(GLAutoDrawable glautodrawable) {
    glu = new GLU();
    glut = new GLUT();

    GL2 gl = glautodrawable.getGL().getGL2();

    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();

    // coordinate system origin at lower left with width and height same as the window
    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
    gl.glClearDepth(1.0f);      // set clear depth value to farthest
    gl.glEnable(GL_DEPTH_TEST); // enables depth testing
    gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
    gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
    gl.glShadeModel(GL_SMOOTH);
    gl.glEnable(GL_POINT_SMOOTH);
    gl.glEnable(GL_LINE_SMOOTH);
    gl.glEnable(GL_CULL_FACE);

//        float mat_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
    float mat_shininess[] = {50.0f};
    float light_position[] = {1.0f, 1.0f, 1.0f, 1.0f};
    float light_ambient[] = {0.5f, 0.5f, 0.5f, 1.0f};
    float light_diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};

//        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, mat_specular, 0);
    gl.glMaterialfv(GL_FRONT, GL_SHININESS, mat_shininess, 0);
    gl.glLightfv(GL_LIGHT0, GL_POSITION, light_position, 0);
    gl.glLightfv(GL_LIGHT0, GL_AMBIENT, light_ambient, 0);
    gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diffuse, 0);
    gl.glEnable(GL_LIGHT0);

    axi.init(this, glautodrawable);
    for (RenderableComponent r : allComponents) {
      r.init(this, glautodrawable);
    }

  }

  protected void reshape(GLAutoDrawable glautodrawable, int width, int height) {
    GL2 gl = glautodrawable.getGL().getGL2();

    if (height == 0) {
      height = 1;   // prevent divide by zero
    }
    float aspect = (float) width / height;

    // Set the view port (display area) to cover the entire window
    gl.glViewport(0, 0, width, height);

    // Setup perspective projection, with aspect ratio matches viewport
    gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
    gl.glLoadIdentity();             // reset projection matrix
    glu.gluPerspective(FOVY, aspect, Z_NEAR, Z_FAR); // fovy, aspect, zNear, zFar

    // Enable the model-view transform
    gl.glMatrixMode(GL_MODELVIEW);
    gl.glLoadIdentity(); // reset

        //System.out.println("width="+width+", height="+height);
    frustrumFaces = null;//computeFrustrum();

    render(glautodrawable);
  }

  protected void dispose(GLAutoDrawable glautodrawable) {
    axi.destroy(this, glautodrawable);
    for (Renderable r : allComponents) {
      r.destroy(this, glautodrawable);
    }

    glu.destroy();
    glu = null;

    glut = null;
  }

  protected void render(GLAutoDrawable glautodrawable) {
    List<Runnable> tasks = glTaskQueue.fetch();
    if (!tasks.isEmpty()) {
      for (Runnable r : tasks) {
        r.run();
      }
    }

    render(glautodrawable, null);
  }

  synchronized protected void render(GLAutoDrawable glautodrawable, RenderedSelection selection) {
    GL2 gl = glautodrawable.getGL().getGL2();

    gl.glMatrixMode(GL_MODELVIEW);
    gl.glLoadIdentity();

    gl.glClearColor(clearColor.getRed() / 255.0f,
            clearColor.getGreen() / 255.0f,
            clearColor.getBlue() / 255.0f,
            clearColor.getAlpha() / 255.0f);

    if (selection == null) {
      gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    glu.gluLookAt(
            eyeLoc[0],
            eyeLoc[1],
            eyeLoc[2],
            lookAtLocation[0],
            lookAtLocation[1],
            lookAtLocation[2],
            RenderPanel.EYE_UP[0], RenderPanel.EYE_UP[1], RenderPanel.EYE_UP[2]);

    if (axi.isVisible()) {
      if (selection == null) {
        gl.glDisable(GL_LIGHTING);
      }
      axi.render(this, glautodrawable, selection);
    }

    if (selection == null) {
      gl.glEnable(GL_LIGHTING);
    }

    int total = 0;
    int rendered = 0;
//    long renderStartTime = System.currentTimeMillis();
    for (int i = 0; i < renderingArrLen; ++i) {
      RenderableComponent r = renderingArr[i];

      ++total;
      if (r.isHierachyVisible()) {
        ++rendered;
        r.render(this, glautodrawable, selection);
      }
    }
//    long renderEndTime = System.currentTimeMillis();
        //System.out.println("rendered "+rendered+" ("+((renderEndTime-renderStartTime)/1000.0)+" s rendering)");
    
    if (axi.isVisible()) {
      if (selection == null) {
        gl.glDisable(GL_LIGHTING);
      }
      axi.renderText(this, glautodrawable, selection);
      if (selection == null) {
        gl.glEnable(GL_LIGHTING);
      }
    }

    if (selection == null) {
      lastSceneDrawTime = System.currentTimeMillis();
    }
  }

  public boolean processKeys(int keyCode, int modifiers) {
    boolean shiftDown = (modifiers & KeyEvent.SHIFT_MASK) > 0;
    int ctrlMask = KeyEvent.CTRL_MASK;
    if (OsCommon.isMacOs) {
      ctrlMask = KeyEvent.META_MASK;
    }

    boolean ctrlDown = (modifiers & ctrlMask) > 0;

    float amountScale = 1.0f;
    if (ctrlDown && shiftDown) {
      amountScale = 25.0f;
    } else {
      if (shiftDown) {
        amountScale = 0.25f;
      } else {
        if (ctrlDown) {
          amountScale = 5.0f;
        }
      }
    }

    boolean received = false;

    switch (keyCode) {
      case KEY_DECR_BEARING: {
        lookAtBearing -= 0.1f * amountScale; // up
        while (lookAtBearing < 0.0) {
          lookAtBearing += 360.0;
        }
        received = true;
        break;
      }
      case KEY_INCR_BEARING: {
        lookAtBearing += 0.1f * amountScale; // up
        while (lookAtBearing > 360.0) {
          lookAtBearing -= 360.0;
        }
        received = true;
        break;
      }
      case KEY_DECR_PITCH: {
        lookAtPitch -= 0.1f * amountScale; // down
        lookAtPitch = Math.max(-90.0f, lookAtPitch);
        received = true;
        break;
      }
      case KEY_INCR_PITCH: {
        lookAtPitch += 0.1f * amountScale; // up
        lookAtPitch = Math.min(90.0f, lookAtPitch);
        received = true;
        break;
      }
      case KEY_DECR_DISTANCE: {
        lookAtDistance -= 0.1f * amountScale; // out
        lookAtDistance = Math.max(MIN_LOOKAT_DIST, lookAtDistance);
        received = true;
        break;
      }
      case KEY_INCR_DISTANCE: {
        lookAtDistance += 0.1f * amountScale; // in
        lookAtDistance = Math.min(MAX_LOOKAT_DIST, lookAtDistance);
        received = true;
        break;
      }
      case KEY_RESET_VALUES: {
        if (ctrlDown) {
          resetLookAtValues(true);
        } else {
          resetLookAtValues(false);
        }
        received = true;
        break;
      }
//            case KeyEvent.VK_END:
//            {
//                lookAtLocation[0] += 0.1f*amountScale; // right
//                received = true;
//                break;
//            }
      case KEY_DECR_LOC_X: {
        lookAtLocation[0] -= 0.1f * amountScale; // left
        received = true;
        break;
      }
      case KEY_INCR_LOC_X: {
        lookAtLocation[0] += 0.1f * amountScale; // right
        received = true;
        break;
      }
      case KEY_DECR_LOC_Y: {
        lookAtLocation[1] -= 0.1f * amountScale; // left
        received = true;
        break;
      }
      case KEY_INCR_LOC_Y: {
        lookAtLocation[1] += 0.1f * amountScale; // right
        received = true;
        break;
      }
      case KEY_DECR_LOC_Z: {
        lookAtLocation[2] -= 0.1f * amountScale; // left
        received = true;
        break;
      }
      case KEY_INCR_LOC_Z: {
        lookAtLocation[2] += 0.1f * amountScale; // right
        received = true;
        break;
      }
    }

    if (received) {
      updateCamera();
      refreshScene();
    }

    return received;
  }

  public void refreshScene(boolean scheduled) {
    long currentTime = System.currentTimeMillis();
    //  avoid fast updates
    if (currentTime - lastSceneDrawTime > 100L) {
      glcanvas.display();
      pendingUpdate = false;
    } else {
      if (!scheduled) {
        pendingUpdate = true;
        final long t = System.currentTimeMillis();
        scheduledExecutorService.schedule(new Runnable() {
          @Override
          public void run() {
            if (pendingUpdate) {
              refreshScene(true);
            }
          }
        }, 200L, TimeUnit.MILLISECONDS);
      }
    }
  }
  
  synchronized public void refreshScene() {
    refreshScene(false);
  }

  public interface ItemAtLocCallback {

    /**
     * Returns the item found by the RenderPanel#findItemAtLoc function
     *
     * @param locX X location requested relative to panel.
     * @param locY Y location requested relative to panel.
     * @param itemInfo The item found at that location, closest to the screen, null if none-found
     */
    public void itemAt(int locX, int locY, ViewInfo info, List<RenderableSelectionInfo> itemInfo);
  }

  public void findItemAtLoc(final int locX, final int locY, final ItemAtLocCallback itemAtLocCallback) {
    //System.out.println("\tfindItemAtLoc "+locX+","+locY);
    glTaskQueue.add(new Runnable() {
      @Override
      public void run() {
        internalFindItemAtLoc(locX, locY, itemAtLocCallback);
      }
    });
    glcanvas.display();
  }

  private final int SELECT_BUFFER_SIZE = 512;
  private final IntBuffer DIRECT_SELECT_BUFFER = com.jogamp.common.nio.Buffers.newDirectIntBuffer(SELECT_BUFFER_SIZE);

  synchronized private void internalFindItemAtLoc(int mouseX, int mouseY, ItemAtLocCallback itemAtLocCallback) {
        //System.out.println("searching for item on mouse "+mouseX+", "+mouseY);

    GL2 gl = glcanvas.getGL().getGL2();

    int[] viewport = new int[]{0, 0, glcanvas.getWidth(), glcanvas.getHeight()};

    //System.out.println("selecting direct buffer");
    gl.glSelectBuffer(SELECT_BUFFER_SIZE, DIRECT_SELECT_BUFFER);

    //System.out.println("preparing for rendering");
    gl.glRenderMode(GL_SELECT);

    //System.out.println("initialize name buffer");
    gl.glInitNames();
    gl.glPushName(0);

        //System.out.println("rendering");
    //  adjust projection matrix to render only the location under the mouse pointer
    gl.glMatrixMode(GL_PROJECTION);
    gl.glPushMatrix();  // save current matrix
    gl.glLoadIdentity();
    glu.gluPickMatrix((float) mouseX, (float) (viewport[3] - mouseY), 1.0f, 1.0f, viewport, 0);
    glu.gluPerspective(FOVY, (float) (viewport[2] - viewport[0]) / (float) (viewport[3] - viewport[1]), Z_NEAR, Z_FAR);

    //  return back to model view matrix for rendering
    gl.glMatrixMode(GL_MODELVIEW);

    // render
    double[] projectionMatrix = new double[16];
    double[] modelViewMatrix = new double[16];
    gl.glGetDoublev(GL_PROJECTION_MATRIX, projectionMatrix, 0);
    gl.glGetDoublev(GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
    ViewInfo viewInfo = new ViewInfo(viewport, projectionMatrix, modelViewMatrix);
    RenderedSelection selection = new RenderedSelection(mouseX, mouseY, viewInfo);
    render(glcanvas, selection);
        //System.out.println(selection.getIssuedIdCount()+" issued IDs");

    //  revert projection matrix to previous matrix, and return to model view
    gl.glMatrixMode(GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(GL_MODELVIEW);

    int hits = gl.glRenderMode(GL_RENDER);
        //System.out.println("\t"+hits+" hits");

    if (hits > 0) {
      long minMinDepth = -1;
      long minMaxDepth = -1;
      List<RenderableSelectionInfo> nearestSelection = new ArrayList<>();

      int locPtr = 0;
      for (int i = 0; i < hits; ++i) {
        int nameCount = DIRECT_SELECT_BUFFER.get(locPtr++);
        long minHitDepth = DIRECT_SELECT_BUFFER.get(locPtr++) & 0xFFFFFFFFL;
        long maxHitDepth = DIRECT_SELECT_BUFFER.get(locPtr++) & 0xFFFFFFFFL;

        if (minMaxDepth < 0 || minMaxDepth < maxHitDepth) {
          minMaxDepth = maxHitDepth;
        }

        if (minMinDepth < 0 || minHitDepth < minMinDepth) {
          double depth = (double) minHitDepth / (double) 0xFFFFFFFFL;
          nearestSelection.clear();
          for (int j = 0; j < nameCount; ++j) {
            int name = DIRECT_SELECT_BUFFER.get(locPtr++);
            if (selection.hasId(name)) {
              RenderableSelectionInfo selInfo = selection.getItem(name);
              selInfo.setDepth(depth);
              nearestSelection.add(selInfo);
            }
          }
          minMinDepth = minHitDepth;
        } else {
          locPtr += nameCount;
        }

      }

//            System.out.println("minMinDepth = " + minMinDepth);
//            System.out.println("minMaxDepth = " + minMaxDepth);
//            System.out.println("\tselected minDepth="+minMinDepth+" item-count="+nearestSelection.size());
      itemAtLocCallback.itemAt(mouseX, mouseY, viewInfo, nearestSelection);
    } else {
      itemAtLocCallback.itemAt(mouseX, mouseY, viewInfo, Collections.EMPTY_LIST);
    }
  }

  public static void main(String[] args) throws UnsupportedEncodingException, IOException {
    RenderablePoint point1 = new RenderablePoint(new float[]{0.0f, 0.0f, 0.0f});
    RenderablePoint point2 = new RenderablePoint(new float[]{1.0f, 0.0f, 0.0f});
    RenderablePoint point3 = new RenderablePoint(new float[]{0.0f, 1.0f, 0.0f});
    RenderablePoint point4 = new RenderablePoint(new float[]{0.0f, 0.0f, 1.0f});

    RenderableVector vec2 = new RenderableVector(new float[]{-2.0f, 0.0f, 0.0f}, new float[]{-3.0f, 0.0f, 0.0f});
    RenderableVector vec3 = new RenderableVector(new float[]{0.0f, -2.0f, 0.0f}, new float[]{0.0f, -3.0f, 0.0f});
    RenderableVector vec4 = new RenderableVector(new float[]{0.0f, 0.0f, -2.0f}, new float[]{0.0f, 0.0f, -3.0f});

    point1.setSize(0.1f);
    point2.setSize(0.1f);
    point3.setSize(0.1f);
    point4.setSize(0.1f);
    vec2.setSize(0.1f);
    vec3.setSize(0.1f);
    vec4.setSize(0.1f);

    final RenderPanel renderTool = new RenderPanel();
    //renderTool.initGl();

    final JFrame jframe = new JFrame("RenderTool");
    jframe.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent windowevent) {
        jframe.dispose();
      }
    });
    jframe.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyPressed(KeyEvent e) {
        renderTool.processKeys(e.getKeyCode(), e.getModifiers());
      }

      @Override
      public void keyReleased(KeyEvent e) {
      }
    });

    jframe.getContentPane().add(renderTool, BorderLayout.CENTER);

    JPanel panel = new JPanel();
    panel.setPreferredSize(new Dimension(100, 100));
    jframe.getContentPane().add(panel, BorderLayout.SOUTH);
    jframe.setSize(800, 600);
    System.out.println("jframe.setVisible(true);");
    jframe.setVisible(true);

    renderTool.setRootGroup(new RenderableGroup("root", Arrays.asList(point1, point2, point3, point4, vec2, vec3, vec4)));
  }

}
