package partialRenderers;

import lwjglutils.OGLTexture2D;
import transforms.Vec3D;
import utils.BuildingGenerator;
import utils.Collidable;
import utils.ColliderType;
import utils.PhysicalObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

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

    private Collidable runway;
    private Collidable terrain;


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

        runway = new RunwayColider(200, 25, new Vec3D(0, 0, 0));
        terrain = new TerrainColider(1000, 1000, new Vec3D(0, 0, 0));
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

    public ArrayList<Collidable> getColidables(){
        ArrayList<Collidable> collidables = new ArrayList<>();

        var othersCollidables = new ArrayList<Collidable>();

        // Keep runway at first or it´s position will colide with terrain -> Will not return runway type but terrain
        othersCollidables.add(runway);
        othersCollidables.add(terrain);

        Stream.of(buildings, othersCollidables).forEach(collidables::addAll);

        return collidables;
    }

    public ColliderType getCollider(Vec3D pos){
        ColliderType result = null;

        var collidables= getColidables();

        for (Collidable collidable : collidables){
            if(collidable.isCollision(pos)){
                result = collidable.getType();
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

    private class RunwayColider extends Collidable
    {
        RunwayColider(int height, int width, Vec3D origin){
            this.height = height;
            this.width = width;
            this.origin = origin;
        }

        @Override
        public boolean isCollision(Vec3D p){
            float margin = 0.7f;

            boolean x = p.getX() <= origin.getX() + height && p.getX() >= origin.getX() - height;
            boolean y = p.getY() <= origin.getY() + margin && p.getY() >= origin.getY() - margin;
            boolean z = p.getZ() <= origin.getZ() + width && p.getZ() >= origin.getZ() - width;

            return x && y && z;
        }

        @Override
        public ColliderType getType() {
            return ColliderType.RUNWAY;
        }
    }

    private class TerrainColider extends Collidable
    {
        TerrainColider(int height, int width, Vec3D origin){
            this.height = height;
            this.width = width;
            this.origin = origin;
        }

        @Override
        public boolean isCollision(Vec3D p){
            float margin = 0.33f;

            boolean x = p.getX() <= origin.getX() + width && p.getX() >= origin.getX() - width;
            boolean y = p.getY() <= origin.getY() + margin && p.getY() >= origin.getY() - margin;
            boolean z = p.getZ() <= origin.getZ() + width && p.getZ() >= origin.getZ() - width;

            return x && y && z;
        }

        @Override
        public ColliderType getType() {
            return ColliderType.TERRAIN;
        }
    }
}
