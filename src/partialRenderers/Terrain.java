package partialRenderers;

import lwjglutils.OGLTexture2D;
import transforms.Vec3D;
import utils.BuildingGenerator;

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


    int _maxHeight = 70;
    int _minHeight = 10;

    int _maxSize = 70;
    int _minSize = 10;

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

/*

    public void generateBuildings(){
        int maxHeight = 70;
        int minHeight = 10;

        int maxSize = 70;
        int minSize = 10;

        Random random = new Random();

        int number = 20;

        for (int i = 0; i <= number; i++){
            switch (i % 4){
                case 0:
                    l_d();
                break;
                case 1:
                    l_t();
                    break;
                case 2:
                    r_d();
                    break;
                case 3:
                    r_t();
                    break;
                default:
                    System.out.println("Chbyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
                    break;
            }

        }

        BuildingGenerator generator = new BuildingGenerator();

            Building _building = new Building(new Vec3D(x, 0, z), size, height);

            if(buildings.stream().allMatch(_building::isNotOverlaping)){

            buildings.add(_building);

            // Is not overlaying with others
            while(!buildings.stream().allMatch(_building::isNotOverlaping)){
                int x = random.nextInt(availableSize);
                int z = random.nextInt(availableSize);

                _building = new Building(new Vec3D(x, 0, z), size, height);

                isNotOverlaying(_building){
                    bre
                }

                boolean allNotOverlaped = buildings.stream().allMatch(_building::isNotOverlaping);
            }
            if (allNotOverlaped){
                buildings.add(_building);
            }
            else {
                for (int n = x; x < availableSize; i++){
                    _building = new Building(new Vec3D(n, 0, z), size, height);

                    allNotOverlaped = buildings.stream().allMatch(_building::isNotOverlaping);

                    if(allNotOverlaped) {
                        buildings.add(_building);

                        break;
                    }
                }

                if ()
            }
         */

    public void generateBuildings(){
        int maxHeight = 100;
        int minHeight = 10;

        int maxSize = 80;
        int minSize = 10;

        Random random = new Random();

        //Top right
        for(int i = 0; i <= 10; i++){
            int height = random.nextInt(maxHeight - minHeight) + minHeight;
            int size = random.nextInt(maxSize - minSize) + minSize;

            int availableSize = terrainSize - size;

            int x = random.nextInt(availableSize);
            int z = random.nextInt(availableSize);

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
