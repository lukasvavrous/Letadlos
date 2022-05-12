package utils;

import partialRenderers.Building;
import transforms.Vec3D;

import java.util.ArrayList;
import java.util.Random;

public class BuildingGenerator {
    protected int maxHeight = 150;
    protected int minHeight = 10;

    protected int maxSize = 70;
    protected int minSize = 10;

    private int borders;

    private Random random;

    private ArrayList<Building> buildings;

    public BuildingGenerator(int borders, ArrayList<Building> buildings) {
        this.borders = borders;
        this.buildings = buildings;

        random = new Random();
    }

    public void generateAmount(int number){
        while (number >= 0){
            generateWithoutOverlap();
            number--;
        }
    }

    public Building getNewBuilding(){
        int height = random.nextInt(maxHeight - minHeight) + minHeight;
        int size = random.nextInt(maxSize - minSize) + minSize;

        int availableSize = borders - size;

        int x = random.nextInt(availableSize);
        int z = random.nextInt(availableSize);

        Vec3D origin = new Vec3D(x, 0, z);

        switch (random.nextInt(4)) {
            case 0:
                origin = new Vec3D(x, 0, z);
                break;
            case 1:
                origin = new Vec3D(-x, 0, z);
                break;
            case 2:
                origin = new Vec3D(x, 0, -z);
                break;
            case 3:
                origin = new Vec3D(-x, 0, -z);
                break;
        }

        return new Building(origin, (int) (height/2.5), height);
    }

    public void generateWithoutOverlap(){
        Building generatedBuilding = getNewBuilding();

        // Generate building without overlap but try max 50 times (in case of no free space)
        for (int i = 0; i < 50; i++){
            if(Collidable.withoutOverlap(buildings, generatedBuilding)){
                buildings.add(generatedBuilding);
                break;
            }
        }
    }
}
