package utils;

import partialRenderers.Building;
import transforms.Vec3D;

public abstract class Collidable {
    protected int height, width;
    protected Vec3D origin;

    public boolean wasHit = false;

    private boolean isXCollision(Vec3D point) {
        return ( point.getX() <= origin.getX() + width && point.getX() >= origin.getX() - width );
    }

    private boolean isYCollision(Vec3D point) {
        return (point.getY() <= origin.getY() + height && point.getY() >= 0);
    }

    private boolean isZCollision(Vec3D point) {
        return ( point.getZ() <= origin.getZ() + width && point.getZ() >= origin.getZ() - width );
    }

    public boolean isCollision(Vec3D p){
        return isXCollision(p) && isYCollision(p) && isZCollision(p);
    }

    public boolean isOverlaping(Building b){
        // Left top | down
        boolean l_t = isCollision(new Vec3D(b.origin.getX() - b.width, 0, b.origin.getZ() + b.width));
        boolean l_d =isCollision(new Vec3D(b.origin.getX() - b.width, 0, b.origin.getZ() - b.width));

        // Right top | down
        boolean r_t = isCollision(new Vec3D(b.origin.getX() + b.width, 0, b.origin.getZ() + b.width));
        boolean r_d = isCollision(new Vec3D(b.origin.getX() + b.width, 0, b.origin.getZ() - b.width));

        return ( l_t || l_d || r_t || r_d);
    }
}
