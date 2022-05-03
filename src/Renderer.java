import global.AbstractRenderer;
import global.GLCamera;
import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import partialRenderers.Building;
import partialRenderers.Plane;
import partialRenderers.SkyBox;
import partialRenderers.Terrain;

import transforms.Vec3D;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

import static global.GluUtils.gluPerspective;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL33.*;


public class Renderer extends AbstractRenderer {
    private float dx, dy, ox, oy;
    private float zenit, azimut;

    private float power = 0;

    private float trans, deltaTrans = 0;

    private boolean mouseButton1 = false;

    private long lastFrame = 0;

    private OGLTexture2D concreteTexture;
    private GLCamera camera;
    public int frameNum;

    private double zfar = 10000;

    private Plane plane;
    private Terrain terrain;
    private SkyBox skyBox;
    public ArrayList<Building> buildings;

    private boolean firstPerson = false;
    private boolean debug = false;

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
                        case GLFW_KEY_R:
                            init();
                            break;

                        case GLFW_KEY_P:
                            firstPerson = !firstPerson;
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
                        plane.left();
                        break;

                    case GLFW_KEY_D:
                        plane.right();
                        break;

                    case GLFW_KEY_UP:
                        if (power < 10)
                            power += 0.02f;
                        break;
                    case GLFW_KEY_DOWN:
                        if (power >= 0.02f)
                            power -= 0.02f;
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

                    float zenitDiff = dy / width * 180;
                    float azimutDiff = dx / height * 180;

                    zenit -= zenitDiff;

                    if (zenit > 90)
                        zenit = 90;
                    if (zenit <= -90)
                        zenit = -90;

                    azimut += azimutDiff;

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

    private void initVariables(){
        power = 0;

        deltaTrans = 0;

        lastFrame = 0;

        zfar = 10000;
    }

    @Override
    public void init() {
        super.init();
        initVariables();
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
        camera.setPosition(new Vec3D(0, 10, 0));
        camera.setFirstPerson(true);
        //camera.setRadius(15);

        terrain = new Terrain();
        buildings = new ArrayList<>();
        skyBox = new SkyBox();
        plane = new Plane();
    }

    @Override
    public void display() {
        frameNum++;

        camera.forward(power);

        if(terrain.isCollision(camera.getPosition())){
            int a = 20;
        }

        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        String text = "Reset: [ R ] | Power: " + power;

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
            terrain.Render();
        glPopMatrix();

        if (firstPerson){
            //Cockpit
        }
        else {
            glPushMatrix();
            glMatrixMode(GL_MODELVIEW);
            plane.preset();
            glPopMatrix();

            glPushMatrix();
            plane.Render(camera);
            glPopMatrix();
        }

        textRenderer.addStr2D(3, 20, text);
        textRenderer.addStr2D(3, 40, "P " + (firstPerson ? "First person" : "Third person"));

        if (debug){
            String textInfo = "position " + camera.getPosition().toString();
            textInfo += String.format(" azimuth %3.1f, zenith %3.1f", azimut, zenit);

            textRenderer.addStr2D(3, 60, textInfo);

            textRenderer.addStr2D(3, 80, "Frame Count: " + frameNum + ", FPS: " + getFPS());
        }

        textRenderer.addStr2D(width - 150, height - 3, "PGRF @ UHK  LETADLOS");
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
