import static com.raylib.Raylib.Vector3Distance;
import static com.raylib.Raylib.Vector3Lerp;

import java.util.Random;

import com.raylib.Raylib.Texture;
import com.raylib.Raylib.Vector3;

public class EnemyExterminator extends Enemy{
    private int beesKilled=0;
    private int waspsKilled=0;
    private boolean spraying;
    public Texture VisualTexture;
    private Object target;
    public static final double SPRAY_RADIUS = 2.2;
    private double health;
    public boolean isAlive;

    public EnemyExterminator(String name, Vector3 spawnPos, Texture texture){
        super(name, spawnPos, texture);
        texture=VisualTexture;
    }

    public boolean isSpraying() { return spraying; }
    public int getBeesKilled() { return beesKilled; }
    public int getWaspsKilled() { return waspsKilled; }
    public double getHealth(){return health;}
    public double distanceTo(Vector3 other) {
        return Math.sqrt(Math.pow(this.Position.x() - other.x(), 2) + Math.pow(this.Position.y() - other.y(), 2)
                + Math.pow(this.Position.z() - other.z(), 2));
    }
    private boolean inSpray(Object object) {
        double dx = getPosition().x() - object.getPosition().x();
        double dz = getPosition().z() - object.getPosition().z();
        return Math.hypot(dx, dz) <= SPRAY_RADIUS;
    }

    protected final Bee nearestBee() {
        Bee target = null;
        double distance = Double.POSITIVE_INFINITY;
        for (Bee bee : Hive.Bees) {
            if (!bee.isAlive) continue;  //if bee is inactive/dead
            double candidate = distanceTo(bee.getPosition());
            if (candidate < distance) { distance = candidate; target = bee; }
        }
        return target;
    }

    protected final EnemyWasp nearestWasp() {
        EnemyWasp target = null;
        double distance = Double.POSITIVE_INFINITY;
        for (EnemyWasp wasp : Field.wasps) {
            if (!wasp.isAlive) continue;  //if bee is inactive/dead
            double candidate = distanceTo(wasp.Position);
            if (candidate < distance) { distance = candidate; target = wasp; }
        }
        return target;
    }

    public void update(float deltaTime){
        target=nearestWasp();   //try attack wasp
        if (target == null) target = nearestBee();  //if there are no wasps, attack bee
        Vector3 offsetPos = new Vector3().y (0.8f).x(target.getPosition.x()).z(target.getPosition.z());
        Position = Vector3Lerp(Position, offsetPos, deltaTime * speed);
        spraying=false;
        for (Bee bee : Hive.Bees) {     //spray all bees in spraying range
            if (bee.isAlive && inSpray(bee)) {
                spraying = true;
                bee.takeDamage(32 * deltaTime);
                if (!bee.isAlive) beesKilled++;
            }
        }
        for (EnemyWasp wasp : Field.wasps) {        //spray all wasps in spraying range
            if (wasp.isAlive && inSpray(wasp)) {
                spraying = true;
                wasp.takeDamage(32 * deltaTime);
                if (!wasp.isAlive) waspsKilled++;
            }
        }
    }

    public final void takeDamage(double damage) {
        isAlive = health > 0;
        health = Math.max(0, health - damage);
    }

}
