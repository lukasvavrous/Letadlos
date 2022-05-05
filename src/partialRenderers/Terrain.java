package partialRenderers;

import lwjglutils.OGLTexture2D;
import transforms.Vec3D;
import utils.BuildingGenerator;
import utils.Collidable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class Terrain implements IRenderable{
    private OGLTexture2D terrainTexture;
    private OGLTexture2D roadTexture;

    ArrayList<Building> buildings;

    ArrayList<Collidable> collidables;

    int runwayWidth = 25;
    int runwayLength = 200;

    int terrainSize = 1000;

    int buildingNumber = 15;

    private BuildingGenerator buildingGenerator;

    public Terrain(){
        collidables = new ArrayList<Collidable>();
        buildings = new ArrayList<>();
        buildingGenerator = new BuildingGenerator(terrainSize, buildings);

        buildingGenerator.generateAmount(buildingNumber);

        loadTextures();
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

        //At first check ground hit
        if (pos.getX() >= -terrainSize &&
            pos.getX() <= terrainSize &&
            pos.getY() < 1 &&
            pos.getY() > -1 &&
            pos.getZ() >= -terrainSize &&
            pos.getZ() <= terrainSize){
            result = true;
        }

        if (!result) return result;

        for (Building building : buildings){
            if(building.isCollision(pos)){
                building.wasHit = true;

                result = true;
            }
        }

        return result;
    }

    public void generateBuildings(){
        buildingGenerator.generateAmount(buildingNumber);

        if (buildings == null || buildings.size() == 0) return;

        Optional<Building> highestBuilding = buildings.stream().max(Comparator.comparing(Building::getHeight));

        System.out.println("Nejvyšší: " + (!highestBuilding.isPresent() ? "Bez objektu" : Integer.toString(highestBuilding.get().getHeight())));

    }

    public void regenerateBuilding(){
        this.buildings.clear();
        buildingGenerator.generateAmount(buildingNumber);
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
