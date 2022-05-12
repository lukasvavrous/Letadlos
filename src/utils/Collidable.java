package utils;
import transforms.Vec3D;

import java.util.ArrayList;

public abstract class Collidable implements PhysicalObject {

    protected int height, width;
    protected Vec3D origin;

    public boolean wasHit = false;

    public boolean isCollision(Vec3D p){
        boolean x = p.getX() <= origin.getX() + width && p.getX() >= origin.getX() - width;
        boolean y = p.getY() <= origin.getY() + height && p.getY() >= 0;
        boolean z = p.getZ() <= origin.getZ() + width && p.getZ() >= origin.getZ() - width;

        return x && y && z;
    }

    public boolean isOverlaping(PhysicalObject b){
        // Left -> top | down
        boolean l_t = isCollision(new Vec3D(b.getOrigin().getX() - b.getWidth(), 0, b.getOrigin().getZ() + b.getWidth()));
        boolean l_d = isCollision(new Vec3D(b.getOrigin().getX() - b.getWidth(), 0, b.getOrigin().getZ() - b.getWidth()));

        // Right -> top | down
        boolean r_t = isCollision(new Vec3D(b.getOrigin().getX() + b.getWidth(), 0, b.getOrigin().getZ() + b.getWidth()));
        boolean r_d = isCollision(new Vec3D(b.getOrigin().getX() + b.getWidth(), 0, b.getOrigin().getZ() - b.getWidth()));

        return ( l_t || l_d || r_t || r_d);
    }

    public static ColliderType checkColision(ArrayList<? extends PhysicalObject> collidables, Vec3D pos){
        ColliderType collision = null;

        for (PhysicalObject collidable: collidables) {
            if(collidable.isCollision(pos)) {
                collision = collidable.getType();
                break;
            }
        }

        return collision;
    }

    public static boolean withoutOverlap(ArrayList<? extends PhysicalObject> collidables, PhysicalObject toCheck){
        boolean overlap = false;

        for (PhysicalObject collidable: collidables) {
            if(collidable.isOverlaping(toCheck))
                overlap = true;
        }

        return !overlap;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public Vec3D getOrigin() {
        return origin;
    }
}
