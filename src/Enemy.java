import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import com.raylib.Raylib.Camera3D;
import com.raylib.Raylib.Texture;
import com.raylib.Raylib.Vector3;
import Components.CollisionBox;


public class Enemy implements CollisionBox {
    String name;
    float speed = 2f; //same speed as bees?
    float scale= 0.5f;
    
    enum EnemyState {
        RESTING,
        ATTACKING;
    }

    EnemyState state = EnemyState.RESTING;

    //I also took this from the bee class
    //if its in the base class then it doesn't need to be specified here
    public boolean EnabledCollider = true;
    protected Vector3 Position = new Vector3();
    public Texture VisualTexture;

    //i dont think this one is needed?
    /*public Enemy(String name, Vector3 spawnPos,String sprite){
        this.name=name;
        Position=spawnPos;
        VisualTexture=LoadTexture(sprite);
        
    }*/

    public Enemy(String name, Vector3 spawnPos, Texture texture){
        this.name = name;
        Position=spawnPos;
        this.VisualTexture = texture;
    }

    public Bee pickTarget(){ //same logic as Bee picking flower
       if((Hive.Bees==null||Hive.Bees.isEmpty())){
        throw new NoAvailableBeeTarget("no bees");
       }
       return Hive.pickRandom(Hive.Bees);
    }


    public void update(float deltaTime){
    }

    public void Draw(Camera3D camera){
        DrawBillboard(camera, VisualTexture, Position, scale, RAYWHITE);
        if(EnabledCollider){
            CreateUniformCollider(Position, scale);
        }
    }

    public class NoAvailableBeeTarget extends RuntimeException{ //stolen from Hive
        public NoAvailableBeeTarget(String message){
            super(message);
        }
    }

}
