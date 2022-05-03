package partialRenderers;

import lwjglutils.OGLTexture2D;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class SkyBox implements IRenderable{
    private static OGLTexture2D[] textureCube;

    public SkyBox(){
        loadTextures();
    }

    private void loadTextures(){

        if (textureCube != null) return;
        textureCube = new OGLTexture2D[6];

        try {
            textureCube[0] = new OGLTexture2D("textures/px.png");
            textureCube[1] = new OGLTexture2D("textures/nx.png");
            textureCube[2] = new OGLTexture2D("textures/py.png");
            textureCube[3] = new OGLTexture2D("textures/ny.png");
            textureCube[4] = new OGLTexture2D("textures/pz.png");
            textureCube[5] = new OGLTexture2D("textures/nz.png");
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
        int size = 10;

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
