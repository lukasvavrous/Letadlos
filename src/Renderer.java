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
import utils.FpsHelper;

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

    public int frameNum;
    public long delta = 0;



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
                        // Restart
                        case GLFW_KEY_R:
                            init();
                            break;

                        case GLFW_KEY_P:
                            firstPerson = !firstPerson;

                            if(firstPerson){
                                camera.setFirstPerson(true);
                            }
                            else{
                                camera.setFirstPerson(false);
                                camera.setRadius(10);
                            }
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


                    // Movement
                    case GLFW_KEY_W:
                        plane.up();
                        break;

                    case GLFW_KEY_A:
                        plane.left();
                        break;

                    case GLFW_KEY_S:
                        plane.down();
                        break;

                    case GLFW_KEY_D:
                        plane.right();
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

                double radAzimuth = Math.toRadians(azimut);
                double radZenith = Math.toRadians(zenit);

                System.out.println("azimut: " + azimut + "radAzimuth: " + radAzimuth);
                System.out.println("zenit: " + zenit + "radZenith: " + radZenith);

                camera.setAzimuth(radAzimuth);
                camera.setZenith(radZenith);

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

    private void initVariables(){
        deltaTrans = 0;

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
        frameNum++;

        String text = "R reset G-regenerate B-generate";

        Vec3D camPos = camera.getPosition();

        var eye = camera.getEye();

        collision = terrain.isCollision(eye);

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
            plane.setVar(scroll);
            plane.Render();
            glPopMatrix();
        }

        textRenderer.addStr2D(3, 20, text);
        textRenderer.addStr2D(3, 40, "P " + (firstPerson ? "First person" : "Third person"));
        textRenderer.addStr2D(3, 100, "ActualSpeed: " + plane.getActualSpeed());
        textRenderer.addStr2D(3, 120, "Speed: " + plane.getSpeed());
        textRenderer.addStr2D(3, 140, String.format("Fps %d", FpsHelper.getInstance().getFps()));

        if (debug){
            String textInfo = "position " + camera.getPosition().toString();
            textInfo += String.format(" azimuth %3.1f, zenith %3.1f", azimut, zenit);

            textRenderer.addStr2D(3, 60, textInfo);
        }

        if (collision){
            textRenderer.addStr2D(width / 2, height /2, "Collision");
        }

        textRenderer.addStr2D(width - 150, height - 3, "PGRF @ UHK  LETADLOS");
    }
}
