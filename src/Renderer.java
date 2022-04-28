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
import transforms.Mat4;
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

    private boolean mouseButton1 = false;

    private long lastFrame = 0;

    private OGLTexture2D planeTexture;
    private OGLTexture2D terrainTexture;
    private OGLTexture2D roadTexture;
    private OGLTexture2D concreteTexture;
    private GLCamera camera;
    public int frameNum;

    private float planeAngle = 0;

    private double zfar = 10000;

    private int vaoId, vboId, iboId, vaoIdOBJ;
    OGLModelOBJ model;

    private SkyBox skyBox;
    public ArrayList<Building> buildings;

    public Renderer() {
        super();

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
                        planeAngle--;
                        break;

                    case GLFW_KEY_D:
                        planeAngle++;
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
                camera.forward(dy * 10);
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
        camera.setPosition(new Vec3D(0, 30, 0));
        camera.setFirstPerson(true);
        //camera.setRadius(15);

        Vec3D origin = new Vec3D(140,0,-20);

        buildings = new ArrayList<>();
        skyBox = new SkyBox();
        buildings.add(new Building(origin, 30, 60));
        buildings.add(new Building(origin.add(new Vec3D(-61,0, 0 )), 30, 50));
        buildings.add(new Building(origin.add(new Vec3D(61,0, -30 )), 20, 30));

        terrain();
        plane();
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

    @Override
    public void display() {
        frameNum++;
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        String text = "Info:";

        trans += deltaTrans;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        gluPerspective(45, width / (float) height, 0.1f, zfar);


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

            float[] array = new float[16];

            camera.setMatrix();

            Vec3D camPos = camera.getPosition();

            glGetFloatv(GL_MODELVIEW_MATRIX, array);
            double[] dArr = convertFloatsToDoubles(array);
            Mat4 mat = new Mat4(dArr);
            System.out.println(mat);

            // WTF
            glTranslated(camPos.getX(),camPos.getY() - 12, camPos.getZ() - 70);
            glRotatef(180,0,1,0);
            glScalef(10f, 10f, 10f);

            glRotatef(planeAngle,0, 0, 1);

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

        glPopMatrix();

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

    public static double[] convertFloatsToDoubles(float[] input)
    {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
    }
}
