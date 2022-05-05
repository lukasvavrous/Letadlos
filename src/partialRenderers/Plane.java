package partialRenderers;

import global.GLCamera;
import lwjglutils.OGLModelOBJ;
import lwjglutils.OGLTexture2D;
import transforms.Mat4;
import transforms.Vec3D;
import utils.Converters;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;


public class Plane {
    private OGLTexture2D planeTexture;

    private long lastFrame;

    private int vaoId, vboId, iboId, vaoIdOBJ;
    OGLModelOBJ model;

    float speed = 0;
    float steps = 0.1f;

    float actualSpeed = 0;

    private float variable = 0;

    private float planeAngle = 0;
    GLCamera camera;

    public Plane(GLCamera camera){
        this.camera = camera;
        loadTextures();
        init();
    }

    public void setVar(float var){
        this.variable = var;
    }

    public void faster(){
        speed ++;
    }

    public float getActualSpeed(){
        return actualSpeed;
    }
    public float getSpeed(){
        return speed;
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    private int getDeltaTime(){
        long time = System.currentTimeMillis();
        int delta = (int) (lastFrame - time);

        lastFrame = time;
        return delta;
    }

    public void slower(){
        speed --;
    }

    public void loadTextures() {
        if (planeTexture != null) return;
        try {
            planeTexture = new OGLTexture2D("textures/Plane_diffuse.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void up(){
        camera.up(speed);
    }

    public void down(){
        camera.down(speed);
    }

    public void right(){
        planeAngle++;
    }

    public void left(){
        planeAngle--;
    }

    public void preset(){
        glCallList(3);
    }

    private void init(){
        glNewList(3, GL_COMPILE);

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glVertexPointer(3, GL_FLOAT, 6 * 4, 0);
        glColorPointer(3, GL_FLOAT, 6 * 4, 3 * 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        iboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboId);

        model= new OGLModelOBJ("/obj/plane.obj");

        vaoIdOBJ = glGenVertexArrays();
        glBindVertexArray(vaoIdOBJ);

        FloatBuffer fb = model.getVerticesBuffer();
        if (fb != null) {
            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            fb.rewind();
            glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
            glVertexPointer(4, GL_FLOAT, 4 * 4, 0);
        }
        fb = model.getNormalsBuffer();
        if (fb != null) {
            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            fb.rewind();
            glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
            glColorPointer(3, GL_FLOAT, 3 * 4, 0);
            glNormalPointer(GL_FLOAT, 3 * 4, 0);
        }
        fb = model.getTexCoordsBuffer();
        if (fb != null) {
            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            fb.rewind();
            glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
            glTexCoordPointer(2, GL_FLOAT, 2 * 4, 0);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        System.out.println("Loading textures...");

        glEndList();
    }

    private void updateSpeed(){
        if (speed == 0){
            actualSpeed = 0;
            return;
        }

        if(actualSpeed == speed) return;

        float deltaStep = (steps * ( getDeltaTime())) / 10;

        if (actualSpeed < speed)
        {
            actualSpeed += deltaStep;
        }
        else
        {
            actualSpeed -= deltaStep;
        }
    }

    public void Render()
    {
        updateSpeed();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        camera.setMatrix();

        camera.backward(actualSpeed);

        float[] array = new float[16];


        Vec3D camPos = camera.getPosition();

        glGetFloatv(GL_MODELVIEW_MATRIX, array);
        double[] dArr = Converters.convertFloatsToDoubles(array);
        Mat4 mat = new Mat4(dArr);
        //System.out.println(mat);

        // WTF


        // Render full model
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glBindVertexArray(vaoIdOBJ);

        glEnable(GL_TEXTURE_2D);
        planeTexture.bind();
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_VERTEX_ARRAY);

        glEnableClientState(GL_COLOR_ARRAY);

        glDrawArrays(GL_TRIANGLES, 0, model.getVerticesBuffer().limit());
        glDisableClientState(GL_COLOR_ARRAY);

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glBindVertexArray(0);

        glDisable(GL_VERTEX_ARRAY);
        glDisable(GL_COLOR_ARRAY);
        glDisable(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }
}
