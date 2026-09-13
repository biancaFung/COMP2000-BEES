import static com.raylib.Raylib.Vector3Distance;
import static com.raylib.Raylib.Vector3Lerp;

import java.util.Random;

import com.raylib.Raylib.Texture;
import com.raylib.Raylib.Vector3;

public class EnemyWasp extends Enemy {
    //attributes based off UML
    private int beesKilled=0;
    private int honeyStolen=0;

    public Texture VisualTexture;
    boolean isAlive;
    private double health=100;
    Bee target;
    float huntTimer=0f; //time since last target pos update
    float huntInterval = 5f;    //update target pos after 5s
    
    //used when resting
    Vector3 randPos;
    float fieldSize = Field.fieldSize *5;
    float restSpeed = 0.8f;

    public static final double attack_range = 2.2;    //attack against exterminator

    public EnemyWasp(String name, Vector3 spawnPos, Texture texture) {
        super(name, spawnPos, texture);
        texture=VisualTexture;
        target=pickTarget();        
    }
    
    private void Sting(){
        //check if bee is already dead, or in the hive
        if(!target.isAlive ||target.InHive||target==null){
            state=EnemyState.RESTING;
        }
        else{   //bee is alive --> sting == kill?
            target.takeDamage(30);
            honeyStolen+=((BeeWorker)target).getNectorCount();
            beesKilled++;
            state=EnemyState.RESTING;
        }
    }

    /*private void Sting(){
        //check if exterminator is already dead
        if(!target.isAlive||target==null){
            state=EnemyState.RESTING;
        }
        else{   //bee is alive --> sting == kill?
            target.takeDamage(15);
        }
    }*/

    private Vector3 getNewRandPos(){
        float randX = new Random().nextFloat(-fieldSize,fieldSize);
        float randY = new Random().nextFloat(-fieldSize/5,fieldSize/5);
        float randZ = new Random().nextFloat(-fieldSize,fieldSize);
        return new Vector3().y(randY).x(randX).z(randZ);
    }

    @Override
    public void update (float deltaTime) {
        switch(state){
            case EnemyState.RESTING:
                if (randPos==null){
                    randPos=getNewRandPos();
                }
                //go towards random position
                Position = Vector3Lerp(Position, randPos, deltaTime * restSpeed);
                if (Vector3Distance(Position, randPos) < 0.5f) {    //once the wasp gets close, choose a new randPos
                    randPos = getNewRandPos();
                }
                if(new Random().nextFloat(0, 100) < 50){    //chance to attack
                    state=EnemyState.ATTACKING;
                }
                break;
            case EnemyState.ATTACKING:
                if (target==null||target.InHive) {
                    //target=pickTarget();
                    target=null;
                    try{
                        target=pickTarget();
                    }
                    catch(NoAvailableBeeTarget e){
                        state=EnemyState.RESTING;
                        randPos=getNewRandPos();
                        target=null;
                        break;
                    }
                /*for(EnemyExterminator exterminator:Field.exterminators){
                    if(exterminator.isAlive && inRange(exterminator)){
                        exterminator.takeDamage(32 * deltaTime);
                    }
                }
                break;*/
            }
        //update target position - assuming it doesn't update automatically
                Vector3 offsetPos = new Vector3().y (0.8f).x(target.getPosition().x()).z(target.getPosition().z());
                huntTimer+=deltaTime;
                if (huntTimer>=huntInterval) {
            //update target pos after 5s
                    offsetPos = new Vector3().y(0.8f).x(target.getPosition().x()).z(target.getPosition().z());
                    huntTimer=0f;
                }
                Position = Vector3Lerp(Position, offsetPos, deltaTime * speed);
        // if wasp is close enough to bee, sting it
                if (Vector3Distance (Position, offsetPos) < 0.1 / speed){
                    Sting();
                    if(state==EnemyState.RESTING){
                        target=null;
                        randPos=getNewRandPos();
                   }
                }
            }
    
        }
    public int getBeesKilled(){
        return this.beesKilled;
    }

    public int getHoneyStolen(){
        return this.honeyStolen;
    }
    public Vector3 getPosition(){
        return Position;
    }
    public final double getHealth() { return health; } 
    public final void takeDamage(double damage) {
        isAlive = health > 0;
        health = Math.max(0, health - damage);
    }

     private boolean inRange(Object object) {
        double dx = getPosition().x() - object.getPosition().x();
        double dz = getPosition().z() - object.getPosition().z();
        return Math.hypot(dx, dz) <= attack_range;
    }
}
