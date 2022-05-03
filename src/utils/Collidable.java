package utils;

import transforms.Vec3D;

public abstract class Collidable {
    protected int height, width;
    protected Vec3D origin;

    public boolean wasHit = false;

    private boolean isXCollision(Vec3D point) {
        boolean l = point.getX() <= origin.getX() + width;
        boolean r = point.getX() >= origin.getX() - width;
        return ( l && r);
    }

    private boolean isYCollision(Vec3D point) {
        return (point.getY() <= origin.getY() + height && point.getY() >= 0);
    }

    private boolean isZCollision(Vec3D point) {
        boolean l = point.getZ() <= origin.getZ() + width;
        boolean r = point.getZ() >= origin.getZ() - width ;
        return ( l && r);
    }

    public boolean isCollision(Vec3D p){
        boolean x = isXCollision(p);
        boolean y = isYCollision(p);
        boolean z = isZCollision(p);

        return x && y && z;
    }
}
