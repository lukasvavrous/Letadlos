import global.AbstractRenderer;
import global.GLCamera;
import lwjglutils.OGLModelOBJ;
import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import partialRenderers.Building;
import partialRenderers.SkyBox;
import transforms.Vec3D;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static global.GluUtils.gluPerspective;
import static global.GlutUtils.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL33.*;


public class Renderer extends AbstractRenderer {
    private float dx, dy, ox, oy;
    private float zenit, azimut;

    private float trans, deltaTrans = 0;

    private float uhel = 0;

    private boolean mouseButton1 = false;
    private boolean per = true, move = false;

    private long lastFrame = 0;
    private int passedFrames = 0;

    private OGLTexture2D planeTexture;
    private OGLTexture2D terrainTexture;
    private OGLTexture2D roadTexture;
    private OGLTexture2D concreteTexture;
    private OGLTexture2D houseSideTexture;
    private GLCamera camera;
    public int frameNum;

    public ArrayList<Building> buildings;
    private double zfar = 10000;

    private int vaoId, vboId, iboId, vaoIdOBJ;
    OGLModelOBJ model;

    private SkyBox skyBox;

    public Renderer() {
        super();

        /*used default glfwWindowSizeCallback see AbstractRenderer*/

        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    // We will detect this in our rendering loop
                    glfwSetWindowShouldClose(window, true);
                if (action == GLFW_RELEASE) {
                    trans = 0;
                    deltaTrans = 0;
                }

                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_R:
                            relocateObjects();
                            break;
                        case GLFW_KEY_M:
                            move = !move;
                            break;
                        case GLFW_KEY_K:
                            zfar -= 10;

                            System.out.println(camera.getPosition());
                            break;
                        case GLFW_KEY_W:
                        case GLFW_KEY_S:
                        case GLFW_KEY_A:
                        case GLFW_KEY_D:
                            deltaTrans = 0.001f;
                            break;
                    }
                }
                switch (key) {
                    case GLFW_KEY_W:
                        camera.forward(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_S:
                        camera.backward(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_A:
                        camera.left(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_D:
                        camera.right(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;
                }
            }
        };

        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);

                mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;

                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                    ox = (float) x;
                    oy = (float) y;
                }
            }

        };

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mouseButton1) {
                    dx = (float) x - ox;
                    dy = (float) y - oy;
                    ox = (float) x;
                    oy = (float) y;
                    zenit -= dy / width * 180;
                    if (zenit > 90)
                        zenit = 90;
                    if (zenit <= -90)
                        zenit = -90;
                    azimut += dx / height * 180;
                    azimut = azimut % 360;
                    camera.setAzimuth(Math.toRadians(azimut));
                    camera.setZenith(Math.toRadians(zenit));
                    dx = 0;
                    dy = 0;
                }
            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                camera.forward(dy * 2);
            }
        };
    }

    @Override
    public void init() {
        super.init();
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_FILL);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        System.out.println("Loading textures...");
        try {
            terrainTexture = new OGLTexture2D("textures/grass.jpg");
            houseSideTexture = new OGLTexture2D("textures/houseSide.jpg");
            roadTexture = new OGLTexture2D("textures/road.jpg");

            concreteTexture = new OGLTexture2D("textures/bricks.jpg");

        } catch (IOException e) {
            e.printStackTrace();
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        camera = new GLCamera();
        camera.setPosition(new Vec3D(-50, 20, 0));
        camera.setFirstPerson(true);
        //camera.setRadius(15);

        Vec3D origin = new Vec3D(140,0,-20);

        buildings = new ArrayList<>();
        skyBox = new SkyBox();
        buildings.add(new Building(origin, 30));
        buildings.add(new Building(origin.add(new Vec3D(61,0, 0 )), 30));


        //scene();

        terrain();
        //house();
        plane();
    }

    private void relocateObjects(){

    }

    private void renderBuildings(){
        buildings.forEach(Building::Render);
    }

    private void scene() {
        glNewList(1, GL_COMPILE);
        glPushMatrix();
        glTranslatef(100, 0, 0);

        glColor3f(1, 0, 0);
        for (int i = 0; i < 10; i++) {
            glTranslatef(-10, 0, 0);
            glutSolidSphere(5, 30, 30);
        }
        glColor3f(0.5f, 0, 0);
        for (int i = 0; i < 10; i++) {
            glTranslatef(-10, 0, 0);
            glutSolidSphere(5, 30, 30);
        }

        glPopMatrix();

        glPushMatrix();
        glTranslatef(0, 100, 0);

        glColor3f(0, 1, 0);
        for (int i = 0; i < 10; i++) {
            glTranslatef(0, -10, 0);
            glutSolidSphere(5, 30, 30);
        }
        glColor3f(0, 0.5f, 0);
        for (int i = 0; i < 10; i++) {
            glTranslatef(0, -10, 0);
            glutSolidSphere(5, 30, 30);
        }
        glPopMatrix();

        glPushMatrix();
        glTranslatef(0, 0, 0);
        glColor3f(0, 0, 1);
        for (int i = 0; i < 10; i++) {
            glTranslatef(0, 0, -10);
            glutSolidSphere(5, 30, 30);
        }
        glColor3f(0, 0, 0.5f);
        for (int i = 0; i < 10; i++) {
            glTranslatef(0, 0, -10);
            glutSolidSphere(5, 30, 30);
        }
        glPopMatrix();

        glEndList();
    }

    private void plane(){
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
        //model= new OGLModelOBJ("/obj/TexturedCube.obj");

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
        try {
            planeTexture = new OGLTexture2D("textures/Plane_diffuse.png"); // vzhledem k adresari res v projektu
        } catch (IOException e) {
            e.printStackTrace();
        }

        glEndList();
    }

    private void terrain() {

        glNewList(4, GL_COMPILE);
        glPushMatrix();

        glEnable(GL_TEXTURE_2D);

        int terrainSize = 750;

        terrainTexture.bind(); //-y bottom

        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);

        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, 10.0f);
        glVertex3d(-terrainSize, 0, -terrainSize);

        glTexCoord2f(10.0f, 10.0f);
        glVertex3d(terrainSize, 0, -terrainSize);

        glTexCoord2f(10.0f, 0.0f);
        glVertex3d(terrainSize, 0, terrainSize);

        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-terrainSize, 0, terrainSize);
        glEnd();

        roadTexture.bind(); //-y bottom
        glBegin(GL_QUADS);

        int runwayWidth = 25;
        int runwayLength = 200;



        glTexCoord2f(0.0f, 2.0f);
        glVertex3d(-runwayLength, 0.1, -runwayWidth);

        glTexCoord2f(1.0f, 2.0f);
        glVertex3d(runwayLength, 0.1, -runwayWidth);

        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(runwayLength, 0.1, runwayWidth);

        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-runwayLength, 0.1, runwayWidth);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        glEndList();
    }
/*
    private void house(){
        glNewList(5, GL_COMPILE);
        glPushMatrix();

        //default
        int size = 30;

        glEnable(GL_TEXTURE_2D);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        Vec3D origin = new Vec3D(140,0,-20);



        houseSideTexture.bind(); //-x  (left)
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size + origin.getX(),0 + origin.getY(), -size + origin.getZ());
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size + origin.getX(), size + origin.getY(), -size + origin.getZ());
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size + origin.getX(), size + origin.getY(), size + origin.getZ());
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size + origin.getX(), 0 + origin.getY(), size + origin.getZ());
        glEnd();

        houseSideTexture.bind();//+x  (right)
        glBegin(GL_QUADS);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size + origin.getX(), 0 + origin.getY(), -size + origin.getZ());
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(size + origin.getX(), 0 + origin.getY(), size + origin.getZ());
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(size + origin.getX(), size + origin.getY(), size + origin.getZ());
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size + origin.getX(), size + origin.getY(), -size + origin.getZ());
        glEnd();


        concreteTexture.bind(); //+y  top
        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size + origin.getX(), size + origin.getY(), -size + origin.getZ());
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size + origin.getX(), size + origin.getY(), -size + origin.getZ());
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size + origin.getX(), size + origin.getY(), size + origin.getZ());
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size + origin.getX(), size + origin.getY(), size + origin.getZ());


        glEnd();

        houseSideTexture.bind(); //-z
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(size + origin.getX(), 0 + origin.getY(), -size + origin.getZ());
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size + origin.getX(), 0 + origin.getY(), -size + origin.getZ());
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size + origin.getX(), size + origin.getY(), -size + origin.getZ());
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(size + origin.getX(), size + origin.getY(), -size + origin.getZ());
        glEnd();

        houseSideTexture.bind(); //+z
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size + origin.getX(), size + origin.getY(), size + origin.getZ());
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size + origin.getX(), 0 + origin.getY(), size + origin.getZ());
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size + origin.getX(), 0 + origin.getY(), size + origin.getZ());
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size + origin.getX(), size + origin.getY(), size + origin.getZ());
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        glEndList();
    }


 */
    @Override
    public void display() {
        frameNum++;
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        String text = this.getClass().getName() + ": [lmb] move";

        trans += deltaTrans;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        if (per)
            //gluPerspective(45, width / (float) height, 0.1f, 500.0f);
            gluPerspective(45, width / (float) height, 0.1f, zfar);
        else
            glOrtho(-20 * width / (float) height,
                    20 * width / (float) height,
                    -20, 20, 0.1f, 500.0f);

        if (move) {
            uhel++;
        }

        GLCamera cameraSky = new GLCamera(camera);
        cameraSky.setPosition(new Vec3D());

        glPushMatrix();
            cameraSky.setMatrix();
            skyBox.Render();
        glPopMatrix();


        glPushMatrix();
            camera.setMatrix();
            glCallList(4);
        glPopMatrix();

        //buildings
        glPushMatrix();
            camera.setMatrix();

            renderBuildings();
        glPopMatrix();

        glPushMatrix();
            glMatrixMode(GL_MODELVIEW);
            glCallList(3);
        glPopMatrix();

        glPushMatrix();
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            camera.setMatrix();

            // Render full model
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glBindVertexArray(vaoIdOBJ);

            glEnable(GL_TEXTURE_2D);
            planeTexture.bind();
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glEnableClientState(GL_VERTEX_ARRAY);


            glRotatef(90, 0,1,0);

            glScalef(10f, 10f, 10f);

            glTranslatef(0,1.5f,0);

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

        glPopMatrix();




        /*

        System.out.println("renderTime " + renderTime);

        if(renderTime == 0){
            renderTime = System.currentTimeMillis()-100;
        }

        System.out.println("renderTime " + renderTime);


        long timeDiff = System.currentTimeMillis() - renderTime;

        System.out.println("Time diff " + timeDiff);

        renderTime += timeDiff;
        passedFrames++;

*/


        if (per)
            text += ", [P]ersp ";
        else
            text += ", [p]ersp ";

        if (move)
            text += ", Ani[M] ";
        else
            text += ", Ani[m] ";

        //System.out.println(passedFrames + " + " + renderTime)


        String textInfo = "position " + camera.getPosition().toString();
        textInfo += String.format(" azimuth %3.1f, zenith %3.1f", azimut, zenit);

        textRenderer.addStr2D(3, 20, text);
        textRenderer.addStr2D(3, 40, textInfo);

        textRenderer.addStr2D(3, 60, "Frame Count: " + frameNum + ", FPS: " + getFPS());
        textRenderer.addStr2D(width - 150, height - 3, "PGRF@UHK  LETADLOS");
    }

    private long getFPS(){
        return 1000 / getDeltaTime();
    }

    private int getDeltaTime(){
        long time = System.currentTimeMillis();
        int delta = (int) (time - lastFrame);

        lastFrame = time;
        return delta;
    }
}
