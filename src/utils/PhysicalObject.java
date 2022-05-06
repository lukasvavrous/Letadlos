package utils;

import transforms.Vec3D;

public interface PhysicalObject {
    boolean isOverlaping(PhysicalObject obj);
    boolean isCollision(Vec3D p);
    Vec3D getOrigin();
    int getWidth();
    int getHeight();
}
