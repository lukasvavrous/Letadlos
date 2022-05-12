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
import utils.DeathSentencer;
import utils.FpsHelper;

import java.awt.*;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;

import static global.GluUtils.gluPerspective;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL33.*;


public class Renderer extends AbstractRenderer {
    private float dx, dy, ox, oy;

    private float zenit, azimut;

    private float trans, deltaTrans = 0;

    private boolean mouseButton1 = false;

    private long lastFrame = 0;

    private OGLTexture2D concreteTexture;
    private GLCamera camera;

    private double zfar = 10000;

    private float scroll;

    private Plane plane;
    private Terrain terrain;
    private SkyBox skyBox;
    public ArrayList<Building> buildings;

    private boolean firstPerson = true;
    private boolean debug = false;
    private boolean collision = false;
    private boolean cursor = false;

    public int frameNum;
    public long delta = 0;

    public Renderer() {
        super();

        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
                    glfwSetWindowShouldClose(window, true);
                }

                if (action == GLFW_RELEASE) {
                    trans = 0;
                    deltaTrans = 0;
                }

                if (action == GLFW_PRESS) {
                    switch (key) {
                        // Restart
                        case GLFW_KEY_R:
                            init();
                            break;

                        case GLFW_KEY_P:
                            firstPerson = !firstPerson;

                            if(firstPerson)
                                setFirstPerson();
                            else
                                setThirdPerson();

                            break;

                        case GLFW_KEY_C:
                            cursor = !cursor;

                            if (cursor)
                                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                            else
                                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

                            break;
                    }
                }
                switch (key) {
                    // Utils
                    case GLFW_KEY_G:
                        terrain.regenerateBuilding();
                        break;

                    case GLFW_KEY_B:
                        terrain.generateBuildings();
                        break;

                    // Speed
                    case GLFW_KEY_UP:
                        plane.faster();
                        break;
                    case GLFW_KEY_DOWN:
                        plane.slower();
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
                    plane.fire();
                }

                if(ox == Float.MAX_VALUE && oy == Float.MAX_VALUE){
                    ox = (float) x;
                    oy = (float) y;

                    return;
                }

                dx = (float) x - ox;
                dy = (float) y - oy;
                ox = (float) x;
                oy = (float) y;

                float zenitDiff = dy / width * 180;
                float azimutDiff = dx / height * 180;

                zenit -= zenitDiff;
                azimut += azimutDiff;
                azimut = azimut % 360;

                if (zenit > 90)
                    zenit = 90;
                if (zenit <= -90)
                    zenit = -90;

                plane.azimut = azimut;
                plane.zenit = zenit;

                dx = 0;
                dy = 0;
            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                camera.forward(dy * 10);

                scroll += dy;
            }
        };
    }

    private void onDeath(){
        glPushMatrix();
        glColor3f(0,0,0);

        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-100, 0.1, -100);

        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(100, 0.1, -100);

        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(100, 0.1, 100);

        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-100, 0.1, 100);
        glEnd();

    }

    private void initVariables(){
        deltaTrans = 0;

        zfar = 10000;

        ox = Float.MAX_VALUE;
        oy = Float.MAX_VALUE;
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
        camera.setRadius(10);
        camera.setPosition(new Vec3D(0, 15, 0));

        if (firstPerson){
            setFirstPerson();
        }
        else {
            setThirdPerson();
        }

        terrain = new Terrain();
        buildings = new ArrayList<>();
        skyBox = new SkyBox();
        plane = new Plane(camera);

    }

    private void setFirstPerson(){
        camera.setFirstPerson(true);
    }

    private void setThirdPerson(){

        camera.setFirstPerson(false);
        camera.setRadius(10);
    }

    @Override
    public void display() {
        Vec3D eye = camera.getEye();

        collision = terrain.isCollision(eye);

        if(collision){
            textRenderer.xCenterAddStr2D(height / 2, DeathSentencer.getTextForCollisionType(terrain.getCollider(eye)), 32, Color.red);
            textRenderer.xCenterAddStr2D((height / 2) + 50, "Stiskni R pro restart", 26, Color.WHITE);

            return;
        }
        frameNum++;

        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

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
            plane.renderFirstPerson();
        }
        else {
            glPushMatrix();
            glMatrixMode(GL_MODELVIEW);
            plane.preset();
            glPopMatrix();

            glPushMatrix();
            plane.Render();
            glPopMatrix();
        }

        textRenderer.customAddStr2D(3, 20, "R- Reset");
        textRenderer.customAddStr2D(3, 40, "G- Regenerage buildings");
        textRenderer.customAddStr2D(3, 60, "B- Add buildings");
        textRenderer.customAddStr2D(3, 80, "R- Reset");
        textRenderer.customAddStr2D(3, 100, "C- On/Off Cursor");
        textRenderer.customAddStr2D(3, 120, "P " + (firstPerson ? "First person" : "Third person"));

        textRenderer.addStr2D(width - 100, 20, "ActualSpeed: " + plane.getActualSpeed());
        textRenderer.addStr2D(width - 100, 40, "Speed: " + plane.getSpeed());
        textRenderer.addStr2D(width - 100, 60, String.format("Fps %d", FpsHelper.getInstance().getFps()));

        if (debug){
            String textInfo = "position " + camera.getPosition().toString();
            textInfo += String.format(" az: %3.1f, zen: %3.1f", azimut, zenit);

            textRenderer.addStr2D(3, 60, textInfo);
        }

        textRenderer.xCenterAddStr2D(height - 3, "LETADLOS_LUKAS_VAVROUS_PGRF@UHK", 10, Color.white);
    }
}
