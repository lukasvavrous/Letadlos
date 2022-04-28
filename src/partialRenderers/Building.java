package partialRenderers;

import lwjglutils.OGLTexture2D;
import transforms.Vec3D;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class Building implements IRenderable{
    private OGLTexture2D buildingTexture;
    private int size;
    private Vec3D origin;

    public Building(Vec3D origin, int size){
        setBuildingTexture("textures/houseSide.jpg");

        this.size = size;
        this.origin = origin;
    }

    public void setBuildingTexture(String path) {
        try
        {
            this.buildingTexture = new OGLTexture2D(path);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size + origin.getX(),0 + origin.getY(), -size + origin.getZ());
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size + origin.getX(), size + origin.getY(), -size + origin.getZ());
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size + origin.getX(), size + origin.getY(), size + origin.getZ());
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size + origin.getX(), 0 + origin.getY(), size + origin.getZ());
        glEnd();

        buildingTexture.bind();//+x  (right)
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


        buildingTexture.bind(); //+y  top
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

        buildingTexture.bind(); //-z
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

        buildingTexture.bind(); //+z
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
    }
}
