/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visualizer.renderer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import visualizer.utils.Vector3f;

/**
 *
 * @author abd01c
 */
public class RenderUnit {
    
    private static final int NORMAL_SIZE = 3;
    
    private static interface Buffer {
        public int getSize();
        public FloatBuffer getBuffer();
    }
    
    private static class DirectBuffer implements Buffer {
        int size;
        FloatBuffer buffer;

        public DirectBuffer(int size, int count, float[] values) {
            this.size = size;
            this.buffer = com.jogamp.common.nio.Buffers.newDirectFloatBuffer(values,
                    0, count*size);
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public FloatBuffer getBuffer() {
            return buffer;
        }
        
    }
    
    protected int verticeCount;
    protected Buffer coordsBuffer;
    protected Buffer normalBuffer;
    protected Buffer colorBuffer;
    protected float[] ambientColor;
    protected float[] diffuseColor;
    protected float[] specularColor;
    protected float[] emissionColor;

    /**
     * 
     * @param verticeCoords Array containing coordinates values of vertices
     * @param verticeColors Array containing diffuseColor values of vertices
     * @param verticeCount Number of vertices
     * @param vertexCoordSize Number of elements in the vertex coordinate array, per vertex
     * @param vertexColorSize Number of elements in the vertex diffuseColor array, per vertex
     */
    private RenderUnit(int verticeCount, Buffer coordsBuffer, Buffer normalBuffer, Buffer colorBuffer) {
        this.verticeCount = verticeCount;
        this.coordsBuffer = coordsBuffer;
        this.normalBuffer = normalBuffer;
        this.colorBuffer = colorBuffer;
        this.ambientColor = new float[] {0.1f, 0.1f, 0.1f, 1.0f};
        this.diffuseColor = new float[] {0.5f, 0.5f, 0.5f, 1.0f};
        this.specularColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
        this.emissionColor = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
    }

    public float[] getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(float[] ambientColor) {
        this.ambientColor = ambientColor;
    }
    
    public float[] getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(float[] diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public float[] getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(float[] specularColor) {
        this.specularColor = specularColor;
    }

    public float[] getEmissionColor() {
        return emissionColor;
    }

    public void setEmissionColor(float[] emissionColor) {
        this.emissionColor = emissionColor;
    }
    
    public void render(GL2 gl, int mode) {
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        if (colorBuffer!=null)
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        if (normalBuffer!=null)
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        
        gl.glVertexPointer(coordsBuffer.getSize(), GL2.GL_FLOAT, 0, coordsBuffer.getBuffer());
        if (colorBuffer!=null)
            gl.glColorPointer(colorBuffer.getSize(), GL2.GL_FLOAT, 0, colorBuffer.getBuffer());
        if (normalBuffer!=null)
            gl.glNormalPointer(GL2.GL_FLOAT, 0, normalBuffer.getBuffer());
        
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambientColor, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuseColor, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specularColor, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emissionColor, 0);
        gl.glDrawArrays(mode, 0, verticeCount);
        
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        if (colorBuffer!=null)
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        if (normalBuffer!=null)
            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
    }
    
    public static class Builder {
        int vertexCoordSize;
        List<float[]> vertices;
        boolean useNormals;
        List<float[]> normals;
        boolean useColors;
        int vertexColorSize;
        List<float[]> colors;

        public Builder(int initCapacity, int vertexCoordSize, boolean useNormals, boolean useColors, int vertexColorSize) {
            this.vertexCoordSize = vertexCoordSize;
            this.vertices = new ArrayList<>(initCapacity);
            this.useNormals = useNormals;
            this.useColors = useColors;
            this.vertexColorSize = vertexColorSize;
            if (useNormals)
                this.normals = new ArrayList<>(initCapacity);
            if (useColors)
                this.colors = new ArrayList<>(initCapacity);
        }
        
        public void add(float[] vertex, float[] color, Vector3f normal) {
            if (vertex.length!=vertexCoordSize) {
                throw new RuntimeException("Vertex coordinates arrays have to be "+vertexCoordSize+" long");
            }
            if (useColors && color.length!=vertexColorSize) {
                throw new RuntimeException("Vertex color arrays have to be "+vertexColorSize+" long");
            }
            
            this.vertices.add(Arrays.copyOf(vertex, vertexCoordSize));
            if (useColors) {
                this.colors.add(Arrays.copyOf(color, vertexColorSize));
            }
            if (useNormals)
                this.normals.add(new float[]{normal.x,normal.y,normal.z});
        }
        
        public void add(Vector3f vertex, float[] color, Vector3f normal) {
            if (vertexCoordSize!=3) {
                throw new RuntimeException("Vertex coordinates arrays have to be "+vertexCoordSize+" long");
            }
            if (useColors && color.length!=vertexColorSize) {
                throw new RuntimeException("Vertex color arrays have to be "+vertexColorSize+" long");
            }
            
            this.vertices.add(new float[]{vertex.x,vertex.y,vertex.z});
            if (useColors)
                this.colors.add(Arrays.copyOf(color, vertexColorSize));
            if (useNormals)
                this.normals.add(new float[]{normal.x,normal.y,normal.z});
        }
        
        public RenderUnit compile() {
            float[] coordArray = new float[vertices.size()*vertexCoordSize];
            float[] colorArray = null;
            float[] normalArray = null;
            if (useColors) {
                colorArray = new float[vertices.size()*vertexColorSize];
            }
            if (useNormals)
                normalArray = new float[vertices.size()*NORMAL_SIZE];
            
            for (int l=vertices.size(), i=0; i<l; ++i) {
                float[] coords = vertices.get(i);
                for (int j=0; j<vertexCoordSize; ++j)
                    coordArray[i*vertexCoordSize+j] = coords[j];
                if (useColors) {
                    float[] color = colors.get(i);
                    for (int j=0; j<vertexColorSize; ++j)
                        colorArray[i*vertexColorSize+j] = color[j];
                }
                if (useNormals) {
                    float[] normal = normals.get(i);
                    for (int j=0; j<NORMAL_SIZE; ++j)
                        normalArray[i*NORMAL_SIZE+j] = normal[j];
                }
            }
            
            Buffer coordBuffer = new DirectBuffer(vertexCoordSize, vertices.size(), coordArray);
            Buffer normalsBuffer = null;
            Buffer colorBuffer = null;
            if (useNormals)
                normalsBuffer = new DirectBuffer(NORMAL_SIZE, vertices.size(), normalArray);
            if (useColors)
                colorBuffer = new DirectBuffer(vertexColorSize, vertices.size(), colorArray);
            
            return new RenderUnit(
                    vertices.size(),
                    coordBuffer,
                    normalsBuffer,
                    colorBuffer
                    );
        }
    }
}
