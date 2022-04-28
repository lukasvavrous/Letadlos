package partialRenderers;

import lwjglutils.OGLTexture2D;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class SkyBox implements IRenderable{
    private OGLTexture2D[] textureCube;

    public SkyBox(){
        textureCube = new OGLTexture2D[6];

        try {
            textureCube[0] = new OGLTexture2D("textures/snow_positive_x.jpg");
            textureCube[1] = new OGLTexture2D("textures/snow_negative_x.jpg");
            textureCube[2] = new OGLTexture2D("textures/snow_positive_y.jpg");
            textureCube[3] = new OGLTexture2D("textures/skyBox_bottom.jpg");
            textureCube[4] = new OGLTexture2D("textures/snow_positive_z.jpg");
            textureCube[5] = new OGLTexture2D("textures/snow_negative_z.jpg");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void Render() {
        glPushMatrix();
        glDepthMask(false);
        glColor3d(0.5, 0.5, 0.5);
        int size = 250;

        glEnable(GL_TEXTURE_2D);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);

        textureCube[1].bind(); //-x  (left)
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, -size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size, size, size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size, -size, size);
        glEnd();

        textureCube[0].bind();//+x  (right)
        glBegin(GL_QUADS);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, -size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(size, -size, size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(size, size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, -size);
        glEnd();

        textureCube[3].bind(); //-y bottom
        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, -size);

        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, -size);

        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, -size, size);

        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, -size, size);

        glEnd();

        textureCube[2].bind(); //+y  top
        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, size, size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, size, size);


        glEnd();

        textureCube[5].bind(); //-z
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(size, -size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(-size, -size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(-size, size, -size);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(size, size, -size);
        glEnd();

        textureCube[4].bind(); //+z
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-size, size, size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3d(-size, -size, size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3d(size, -size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3d(size, size, size);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glDepthMask(true);
        glPopMatrix();
    }
}
