import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import com.raylib.Raylib.Camera3D;
import com.raylib.Raylib.Model;
import com.raylib.Raylib.Ray;
import com.raylib.Raylib.Rectangle;
import com.raylib.Raylib.Vector3;

public class App {

    static boolean simulating = true;
    static boolean waspButtonEnabled=true;  
    static float minDistance = 2.0f;
    static float maxDistance = 40.0f;
    static float rotationSpeed = 2.0f;
    static float zoomSpeed = 8.0f;
    
    public static void main(String[] args) throws Exception {
        InitWindow(1280, 720, "Bee Simulator");
        SetTargetFPS(60);

        //--- this is all camera setup releated stuff
        //the current setup is a orbiting camera that is controlled with the left and right arrow keys and a zoom with up and down
        //idk if we should add a way to change the pitch but its not nessesary for it to work
        Vector3 CameraOrigin = new Vector3();
        
        Camera3D camera = new Camera3D()
                .target(new Vector3())
                .up(new Vector3().x(0).y(1).z(0))
                .fovy(45).projection(CAMERA_PERSPECTIVE);
        
        
        float distance = 10.0f;
        float yaw = 0.0f;
        float pitch = 0.2f;
        //---
        
        // this area im mainly using for initalising the scene so ex. the field, hives and models

        Field flowers = new Field(CameraOrigin, 9f);
        for(int i=0;i<200;i++){flowers.SpawnFlower();}//temp flower spawner

        Hive hive = new Hive(new Vector3().x(0.4f).y(2.1f).z(-1f), flowers);
        for(int i=0;i<50;i++){hive.CreateBee();}//temp start bee spawner
        //hive.CreateQueen();

        Object Selected = null;
        Model treeModel = LoadModel("Assets/beetree.obj");

        //---

        Rectangle AddBeeButton  = new Rectangle();
                AddBeeButton.height(40).width(100);
                AddBeeButton.x(20).y(660);
        
        Rectangle PauseButtonrRectangle  = new Rectangle();
                PauseButtonrRectangle.height(40).width(100);
                PauseButtonrRectangle.x(130).y(660);

        Rectangle WaspButton  = new Rectangle();
                WaspButton.height(40).width(100);
                WaspButton.x(20).y(610);    //assuming this spot is ok?

        while (!WindowShouldClose()) {
            float deltaTime = GetFrameTime();
            Ray MouseRay = GetScreenToWorldRay(GetMousePosition(), camera);

            //--- this section is for processing the scene camera controls
            if (IsKeyDown(KEY_LEFT))  yaw -= rotationSpeed * deltaTime;
            if (IsKeyDown(KEY_RIGHT)) yaw += rotationSpeed * deltaTime;
            if (IsKeyDown(KEY_UP))   distance -= zoomSpeed * deltaTime;
            if (IsKeyDown(KEY_DOWN)) distance += zoomSpeed * deltaTime;

            distance = Math.max(minDistance, Math.min(maxDistance, distance));

            float x = CameraOrigin.x() + distance * (float) (Math.cos(pitch) * Math.sin(yaw));
            float y = CameraOrigin.y() + distance * (float) Math.sin(pitch);
            float z = CameraOrigin.z() + distance * (float) (Math.cos(pitch) * Math.cos(yaw));

            camera._position(new Vector3().x(x).y(y).z(z));
            //---

            //This is where all the drawing is done
            BeginDrawing();
                ClearBackground(BLUE);
                
                //This is where the 3d is drawn
                BeginMode3D(camera);

                    //this model is the whole tree and grass plain
                    DrawModel(treeModel, CameraOrigin, 1.0f, RAYWHITE);

                    //this is the flowerfield update stuff
                    if(flowers!=null){
                        flowers.fieldCheck();
                        for(Flower flower : flowers.flowerfield){
                            flower.DrawFlower(camera);
                            

                            if(GetRayCollisionBox(MouseRay, flower.Collider).hit() && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)){
                                Selected = flower;
                            }
                        }
                    }

                    //this is the bee update stuff
                    if(hive!=null){
                        hive.DrawHive();

                        if(GetRayCollisionBox(MouseRay, hive.Collider).hit() && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)){
                            Selected = hive;
                        }
                        Hive.BeeCheck();    //remove dead bees
                        for(Bee bee: hive.Bees){
                            bee.Draw(camera);
                            if(simulating){
                                bee.update(deltaTime);
                            }
                            

                            if(GetRayCollisionBox(MouseRay, bee.Collider).hit() && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)){
                                Selected = bee;
                            }
                        }
                        //we don't need to do this because we can downcast the queen bee into the other bees and it will know its a bee
                        /*                         
                            for(QueenBee queen: hive.Queens){
                            queen.Draw(camera);
                            queen.update(deltaTime);

                            if(GetRayCollisionBox(MouseRay, queen.Collider).hit() && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)){
                                Selected = queen;
                            }

                        } */
                       
                    }
                    //wasp stuff
                        if(flowers!=null){
                            //flowers.waspCheck();
                            for(EnemyWasp Wasp: flowers.wasps){
                                Wasp.Draw(camera);
                                if(simulating){ 
                                    Wasp.update(deltaTime);
                                }
                            

                                if(GetRayCollisionBox(MouseRay, Wasp.Collider).hit() && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)){
                                    Selected = Wasp;
                                }
                            }
                        }
                    

                EndMode3D();

                if(Selected != null) DrawStats(Selected);
                //this is a example button that spawns a bee
                if(GuiButton(AddBeeButton,"Spawn Bee")==1){
                    hive.CreateBee();
                }

                if(waspButtonEnabled){
                    if(GuiButton(WaspButton,"Spawn Wasp")==1){
                        try{flowers.spawnWasp();
                        }   //wasps can't spawn if there are no bees
                        catch(RuntimeException e){
                            waspButtonEnabled=false;   //if there's no bees, the button is disabled
                            GuiSetState(STATE_DISABLED);
                        }
                    }
                }
                else{
                    GuiSetState(STATE_DISABLED);
                    GuiButton(WaspButton, "Spawn Wasp");
                    GuiSetState(STATE_NORMAL);
                }
                if(Hive.Bees!= null&&!Hive.Bees.isEmpty()){
                    waspButtonEnabled = true;
                }
                else{
                    waspButtonEnabled = false;
                }

                String c_playing = "pause";
                if(simulating){
                    c_playing = "Pause";
                }else{
                    c_playing = "Play";
                }
                
                if(GuiButton(PauseButtonrRectangle,c_playing)==1){
                    if(simulating){
                        simulating = false;
                    }
                    else{
                        simulating = true;
                    }
                }

            EndDrawing();
        }
    }

    void DrawButtons(){
        
    }



    //the text can be made with DrawText
    //its probably good to have the full string of data layed out and then drawing it with one call
    static void DrawStats(Object Item){
        String status = "";

        switch (Item) {
            case BeeWorker bee:
                status = "Object: " + bee.toString() + "\n" + "Name: " + bee.Name + "\n"
                            + "Nector Count: " + Float.toString(bee.getNectorCount())+ "\n"+"Health: "+ Double.toString(bee.getHealth());
                break;

            case Location place:
                status = "Object: " + place.toString() + "\n" + "Name: " + place.Name;
                switch (place) {
                    case Hive hive:
                        status = status + "\n" + "Honey Count: " + Integer.toString(hive.HoneyCapacity) + "\n" + "Bee Count: " + Integer.toString(hive.Bees.size());
                        break;
                    case Flower flower:
                        status = status + "\n" + "Pollen Count: " + Float.toString(flower.Pollen);
                        break;
                    default:
                        break;
                }
                break;
            case Enemy enemy:
                status="Object: "+enemy.toString()+"\n"+"Name: "+enemy.name;
                switch (enemy){
                    case EnemyWasp wasp:
                    status=status + "\n"+"Bees Killed: "+Integer.toString(wasp.getBeesKilled())
                                +"\n"+"Honey Stolen: "+Integer.toString(wasp.getHoneyStolen())+"\n"+"Health: "+ Double.toString(wasp.getHealth());
                    break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        DrawText(status, 20, 20, 20, BLACK);
    }   
}
