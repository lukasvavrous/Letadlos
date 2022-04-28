import partialRenderers.Building;
import partialRenderers.IRenderable;
import partialRenderers.SkyBox;
import transforms.Vec3D;

import java.util.ArrayList;

public class GameScene implements IRenderable {
    public ArrayList<Building> buildings;
    private SkyBox skyBox;

    public GameScene(){
        buildings = new ArrayList<>();
        skyBox = new SkyBox();

        generateBuildings();
    }

    public void generateBuildings(){
        Vec3D origin = new Vec3D(140,0,-20);

        buildings.add(new Building(origin, 30));
        buildings.add(new Building(origin.add(new Vec3D(61,0, 0 )), 30));
    }

    @Override
    public void Render() {
        skyBox.Render();
        buildings.forEach(Building::Render);
    }
}
