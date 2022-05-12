package partialRenderers;

import lwjglutils.OGLTexture2D;
import transforms.Vec3D;
import utils.BuildingGenerator;
import utils.Collidable;
import utils.ColliderType;
import utils.PhysicalObject;

import java.io.IOException;
import java.util.ArrayList;
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

    int grassCoeficient = terrainSize / 25;

    private BuildingGenerator buildingGenerator;

    public PhysicalObject floorObject;

    public Terrain(){
        collidables = new ArrayList<Collidable>();
        buildings = new ArrayList<>();
        buildingGenerator = new BuildingGenerator(terrainSize, buildings);

        buildingGenerator.generateAmount(buildingNumber);

        floorObject = new Collidable() {
            @Override
            public ColliderType getType() {
                return ColliderType.TERRAIN;
            }
        };

        loadTextures();
    }

    public void loadTextures() {
        try
        {
            this.terrainTexture = new OGLTexture2D("textures/seamless-grass.jpg");
            this.roadTexture = new OGLTexture2D("textures/road.jpg");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isCollision(Vec3D pos){
        return getCollider(pos) != null;
    }

    public ColliderType getCollider(Vec3D pos){
        ColliderType result = null;

        //At first check runway hit
        if (pos.getX() >= -runwayLength &&
                pos.getX() <= runwayLength &&
                pos.getY() < 1 &&
                pos.getY() > -1 &&
                pos.getZ() >= -runwayWidth &&
                pos.getZ() <= runwayWidth)
            return ColliderType.RUNWAY;

        //Ground hit
        if (pos.getX() >= -terrainSize &&
                pos.getX() <= terrainSize &&
                pos.getY() < 1 &&
                pos.getY() > -1 &&
                pos.getZ() >= -terrainSize &&
                pos.getZ() <= terrainSize)
            return ColliderType.TERRAIN;


        //Then check hit with other buildings
        for (Building building : buildings){
            if(building.isCollision(pos)){
                result = ColliderType.BUILDING;
            }
        }

        return result;
    }

    public void generateBuildings(){
        buildingGenerator.generateAmount(buildingNumber);

        if (buildings == null || buildings.size() == 0) return;
    }

    public void regenerateBuilding(){
        this.buildings.clear();
        buildingGenerator.generateAmount(buildingNumber);
    }

    public void Render(){
        buildings.forEach(Building::Render);

        glPushMatrix();

            glEnable(GL_TEXTURE_2D);
        terrainTexture.bind();


        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);

            renderGround();

            renderRunway();

            glDisable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    private void renderGround(){

        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, grassCoeficient);
        glVertex3d(-terrainSize, 0, -terrainSize);

        glTexCoord2f(grassCoeficient, grassCoeficient);
        glVertex3d(terrainSize, 0, -terrainSize);

        glTexCoord2f(grassCoeficient, 0.0f);
        glVertex3d(terrainSize, 0, terrainSize);

        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-terrainSize, 0, terrainSize);
        glEnd();
    }

    private void renderRunway(){
        roadTexture.bind();
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
    }
}
