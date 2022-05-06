package partialRenderers;

import lwjglutils.OGLTexture2D;
import transforms.Vec3D;
import utils.Collidable;
import utils.PhysicalObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class Building extends Collidable implements PhysicalObject, IRenderable{

    private float textureCoef = 1;

    private static ArrayList<OGLTexture2D> buildingTextures;

    private OGLTexture2D buildingTexture;

    private static void initTextures(){
        if (buildingTextures == null){
            buildingTextures = new ArrayList<>();
            try
            {
                buildingTextures.add(new OGLTexture2D("textures/houseSide.jpg"));
                buildingTextures.add(new OGLTexture2D("textures/buildingPart.jpg"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private OGLTexture2D getRandomTexture(){
        return buildingTextures.get(new Random().nextInt(buildingTextures.size()));
    }

    public Building(Vec3D origin, int size, int height){
        initTextures();

        this.buildingTexture = getRandomTexture();

        this.width = size;
        this.height = height;
        this.origin = origin;

        this.textureCoef = height / 30;

        if (textureCoef == 0) textureCoef = 1;
    }

    public void setBuildingTexture(String path) {
        if (buildingTexture != null) return;

        try
        {
            this.buildingTexture = new OGLTexture2D(path);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int getHeight(){
        return this.height;
    }

    @Override
    public void Render() {
        glPushMatrix();

        glEnable(GL_TEXTURE_2D);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        buildingTexture.bind(); //-x  (left)
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, textureCoef);
        glVertex3d(-width + origin.getX(),0 + origin.getY(), -width + origin.getZ());
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-width + origin.getX(), height + origin.getY(), -width + origin.getZ());
        glTexCoord2f(textureCoef, 0.0f);
        glVertex3d(-width + origin.getX(), height + origin.getY(), width + origin.getZ());
        glTexCoord2f(textureCoef, textureCoef);
        glVertex3d(-width + origin.getX(), 0 + origin.getY(), width + origin.getZ());
        glEnd();

        buildingTexture.bind();//+x  (right)
        glBegin(GL_QUADS);
        glTexCoord2f(textureCoef, textureCoef);
        glVertex3d(width + origin.getX(), 0 + origin.getY(), -width + origin.getZ());
        glTexCoord2f(0.0f, textureCoef);
        glVertex3d(width + origin.getX(), 0 + origin.getY(), width + origin.getZ());
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(width + origin.getX(), height + origin.getY(), width + origin.getZ());
        glTexCoord2f(textureCoef, 0.0f);
        glVertex3d(width + origin.getX(), height + origin.getY(), -width + origin.getZ());
        glEnd();


        buildingTexture.bind(); //+y  top
        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-width + origin.getX(), height + origin.getY(), -width + origin.getZ());
        glTexCoord2f(textureCoef, 0.0f);
        glVertex3d(width + origin.getX(), height + origin.getY(), -width + origin.getZ());
        glTexCoord2f(textureCoef, textureCoef);
        glVertex3d(width + origin.getX(), height + origin.getY(), width + origin.getZ());
        glTexCoord2f(0.0f, textureCoef);
        glVertex3d(-width + origin.getX(), height + origin.getY(), width + origin.getZ());


        glEnd();

        buildingTexture.bind(); //-z
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, textureCoef);
        glVertex3d(width + origin.getX(), 0 + origin.getY(), -width + origin.getZ());
        glTexCoord2f(textureCoef, textureCoef);
        glVertex3d(-width + origin.getX(), 0 + origin.getY(), -width + origin.getZ());
        glTexCoord2f(textureCoef, 0.0f);
        glVertex3d(-width + origin.getX(), height + origin.getY(), -width + origin.getZ());
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(width + origin.getX(), height + origin.getY(), -width + origin.getZ());
        glEnd();

        buildingTexture.bind(); //+z
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-width + origin.getX(), height + origin.getY(), width + origin.getZ());
        glTexCoord2f(0.0f, textureCoef);
        glVertex3d(-width + origin.getX(), 0 + origin.getY(), width + origin.getZ());
        glTexCoord2f(textureCoef, textureCoef);
        glVertex3d(width + origin.getX(), 0 + origin.getY(), width + origin.getZ());
        glTexCoord2f(textureCoef, 0.0f);
        glVertex3d(width + origin.getX(), height + origin.getY(), width + origin.getZ());
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();
    }
}
