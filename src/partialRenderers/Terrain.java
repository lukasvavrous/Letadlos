package partialRenderers;

import lwjglutils.OGLTexture2D;
import transforms.Vec3D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class Terrain implements IRenderable{
    private OGLTexture2D terrainTexture;
    private OGLTexture2D roadTexture;

    ArrayList<Building> buildings;

    int runwayWidth = 25;
    int runwayLength = 200;

    int terrainSize = 1000;

    public Terrain(){
        buildings = new ArrayList<>();
        loadTextures();

        generateBuildings();
    }

    public void loadTextures() {
        try
        {
            this.terrainTexture = new OGLTexture2D("textures/grass.jpg");
            this.roadTexture = new OGLTexture2D("textures/road.jpg");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isCollision(Vec3D pos){
        boolean result = false;

        for (Building building : buildings){
            if(building.isCollision(pos)){
                building.wasHit = true;

                result = true;
            }
        }

        return result;
    }
    public void regenerateBuilding(){
        this.buildings.clear();
        generateBuildings();
    }

    public void generateBuildings(){
        int maxHeight = 70;
        int minHeight = 10;

        int maxSize = 70;
        int minSize = 10;

        Random random = new Random();

        //Top right
        for(int i = 0; i <= 10; i++){
            int height = random.nextInt(maxHeight - minHeight) + minHeight;
            int size = random.nextInt(maxSize - minSize) + minSize;

            int x = random.nextInt(terrainSize - size);
            int z = random.nextInt(terrainSize - size);

            Vec3D origin = new Vec3D(x, 0, z);

            buildings.add(new Building(origin, size, height));
        }

        //Bottom right
        for(int i = 0; i <= 10; i++){
            int height = random.nextInt(maxHeight - minHeight) + minHeight;
            int size = random.nextInt(maxSize - minSize) + minSize;

            int x = random.nextInt(terrainSize - size);
            int z = random.nextInt(terrainSize - size);

            Vec3D origin = new Vec3D(-x, 0, z);

            buildings.add(new Building(origin, size, height));
        }

        //top left
        for(int i = 0; i <= 10; i++){
            int height = random.nextInt(maxHeight - minHeight) + minHeight;
            int size = random.nextInt(maxSize - minSize) + minSize;

            int x = random.nextInt(terrainSize - size);
            int z = random.nextInt(terrainSize - size);

            Vec3D origin = new Vec3D(x, 0, -z);

            buildings.add(new Building(origin, size, height));
        }

        //top right
        for(int i = 0; i <= 10; i++){
            int height = random.nextInt(maxHeight - minHeight) + minHeight;
            int size = random.nextInt(maxSize - minSize) + minSize;

            int x = random.nextInt(terrainSize - size);
            int z = random.nextInt(terrainSize - size);

            Vec3D origin = new Vec3D(-x, 0, -z);

            buildings.add(new Building(origin, size, height));
        }
    }

    public void Render(){
        buildings.forEach(Building::Render);

        glPushMatrix();

            glEnable(GL_TEXTURE_2D);
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

            glTexCoord2f(0.0f, 1.0f);
            glVertex3d(-runwayLength, 0.1, -runwayWidth);

            glTexCoord2f(1.0f, 1.0f);
            glVertex3d(runwayLength, 0.1, -runwayWidth);

            glTexCoord2f(1.0f, 0.0f);
            glVertex3d(runwayLength, 0.1, runwayWidth);

            glTexCoord2f(0.0f, 0.0f);
            glVertex3d(-runwayLength, 0.1, runwayWidth);
            glEnd();

            glDisable(GL_TEXTURE_2D);
        glPopMatrix();
    }
}
