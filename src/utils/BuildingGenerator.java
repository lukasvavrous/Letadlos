package utils;

import partialRenderers.Building;
import transforms.Vec3D;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class BuildingGenerator {
    protected int maxHeight = 70;
    protected int minHeight = 10;

    protected int maxSize = 70;
    protected int minSize = 10;

    Random random = new Random();

    private int borders;

    ArrayList<Building> buildings;

    public BuildingGenerator(int borders, ArrayList<Building> buildings) {

        this.borders = borders;
        this.buildings = buildings;

        generate();
    }

    public void generate(){
        switch (random.nextInt(4)) {
            case 0:
                topRight();
                break;
            case 1:
                buttomRight();
                break;
            case 2:
                topLeft();
                break;
            case 3:
                buttomLeft();
                break;

            case 4:
                System.out.println("delete me");

                break;
            default:
                System.out.println("Chbyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
                break;
        }
    }

    private void topRight(){
        int height = random.nextInt(maxHeight - minHeight) + minHeight;
        int size = random.nextInt(maxSize - minSize) + minSize;

        int availableSize = borders - size;

        int x = random.nextInt(availableSize);
        int z = random.nextInt(availableSize);

        Vec3D origin = new Vec3D(x, 0, z);

        buildings.add(new Building(origin, size, height));

    }

    private void buttomRight(){
        int height = random.nextInt(maxHeight - minHeight) + minHeight;
        int size = random.nextInt(maxSize - minSize) + minSize;

        int availableSize = borders - size;

        int x = random.nextInt(availableSize);
        int z = random.nextInt(availableSize);

        Vec3D origin = new Vec3D(-x, 0, z);

        buildings.add(new Building(origin, size, height));
    }

    private void buttomLeft() {

    }

    private void topLeft(){

    }
}
