package utils;

import partialRenderers.Building;
import transforms.Vec3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatCodePointException;
import java.util.Random;

public class BuildingGenerator {
    protected int maxHeight = 500;
    protected int minHeight = 10;

    protected int maxSize = 70;
    protected int minSize = 10;

    Random random = new Random();

    private int borders;

    ArrayList<Building> buildings;

    public BuildingGenerator(int borders, ArrayList<Building> buildings) {
        this.borders = borders;
        this.buildings = buildings;
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

        return new Building(origin, size, height);
    }

    public void generateWithoutOverlap(){
        Building generatedBuilding = getNewBuilding();

        if(Collidable.withoutColision(buildings, generatedBuilding))
            buildings.add(generatedBuilding);
    }
}
