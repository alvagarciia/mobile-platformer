package com.example.game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import java.lang.StringBuilder;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.nio.file.Files;


public class MainActivity extends AppCompatActivity
{
    Model model;
    GameView view;
    GameController controller;


@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        model = new Model(this);
        view = new GameView(this, model);
        controller = new GameController(model, view, this);
        setContentView(view);

        Json loadObject = Json.loadAndroid(this, "map.json");
        model.unmarshal(this, loadObject);
        System.out.println("Loaded file!");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        controller.resume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        controller.pause();
    }



    static class Model
    {
        Luigi luigi;
        private ArrayList<Sprite> sprites; // array list to handle Bricks


        // Model constructor
        public Model(Context context)
        {
            // Sprites in Game
            luigi = new Luigi(context,450, 650);
            sprites = new ArrayList<Sprite>();
            sprites.add(luigi);
        }

        void update()
        {
            Iterator<Sprite> iter = sprites.iterator();
            while(iter.hasNext()) {
                Sprite s = iter.next();

                // returns isValid boolean, where it is not valid if the sprite gets removed/killed/consumed
                if (!s.update()) {
                    iter.remove();      //remove sprite
                    //continue;
                }
                else for(Sprite t: sprites)
                {
                    if(s != t && s.isThereACollision(t))
                    {
                        if(s.isLuigi() && t.isBrick())
                        {
                            s.getOutOfSprite(t);
                        }
                        else if(s.isLuigi() && t.isMushroom())
                        {
                            t.setUnvalid();     // mushroom is eaten
                            ((Luigi)s).changeSize();
                        }
                        else if(s.isLuigi() && t.isDryBones())
                        {
                            ((DryBones)t).isDown();     // Drybones goes down
                        }
                        else if(s.isGoomba() && t.isBrick())
                        {
                            s.getOutOfSprite(t);
                        }
                        else if(s.isDryBones() && t.isBrick())
                        {
                            s.getOutOfSprite(t);
                        }
                        else if(s.isMushroom() && t.isBrick())
                        {
                            s.getOutOfSprite(t);
                        }
                        else if(s.isGoomba() && t.isFireball())
                        {
                            if(!((Goomba)s).isOnFire())
                                t.setUnvalid();
                        }
                        else if(s.isDryBones() && t.isFireball())
                        {
                            if(!((DryBones)s).isDown())
                                t.setUnvalid();
                        }
                    }
                }
            }
        }

        // Sprites unmarshal: clear current sprite list and load saved file
        public void unmarshal(Context context, Json ob)
        {
            sprites.clear();
            sprites.add(luigi);
            try{
                Json tmpListBricks = ob.get("bricks");
                for(int i = 0; i < tmpListBricks.size(); i++)
                    sprites.add(new Brick(context, tmpListBricks.get(i)));
            }
            catch(Exception e)
            {
                System.out.println("There are no bricks saved.");
            }
            try{
                Json tmpListGoombas = ob.get("goombas");
                for(int i = 0; i < tmpListGoombas.size(); i++)
                    sprites.add(new Goomba(context, tmpListGoombas.get(i)));
            }
            catch(Exception e)
            {
                System.out.println("There are no goombas saved.");
            }
            try{
                Json tmpListDryBones = ob.get("drybones");
                for(int i = 0; i < tmpListDryBones.size(); i++)
                    sprites.add(new DryBones(context, tmpListDryBones.get(i)));
            }
            catch(Exception e)
            {
                System.out.println("There are no drybones saved.");
            }
            try{
                Json tmpListMushrooms = ob.get("mushrooms");
                for(int i = 0; i < tmpListMushrooms.size(); i++)
                    sprites.add(new Mushroom(context, tmpListMushrooms.get(i)));
            }
            catch(Exception e)
            {
                System.out.println("There are no mushrooms saved.");
            }
        }

        ////////// To deal with other Sprites externally (getters and clear) //////////
        public int getSpritesTotal()
        {
            return sprites.size();
        }
        public Sprite getSprite(int i)
        {
            return sprites.get(i);
        }


        // Shoot fireball
        public void fireballShot(Context context)
        {
            int x = luigi.getX() + luigi.getW();    // X is luigi's right side
            int y = (luigi.getY()) + (luigi.getH()/2); // Y is luigi's center
            sprites.add(new Fireball(context, x, y));
        }

        ////////// To deal with Luigi externally (getters and others)  //////////
        public int getLuigiX()
        {
            return luigi.getX();
        }
        public int getLuigiJumpCounter()
        {
            return luigi.getJumpCounter();
        }
        public void saveLuigiPrev()
        {
            luigi.savePrev();
        }
        public void moveLuigiRight()
        {
            luigi.moveRight();
        }
        public void moveLuigiLeft()
        {
            luigi.moveLeft();
        }
        public void jumpLuigi()
        {
            luigi.jump();
        }
    }




    static class GameView extends SurfaceView
    {
        SurfaceHolder ourHolder; // to lock and unlock canvas for fast and safe drawing
        Canvas canvas;
        Paint paint;
        Model model;
        GameController controller;
        Bitmap ground_image;

        // For Arrow Controllers
        Bitmap leftArrow;
        static Rect leftArrowRect;
        Bitmap rightArrow;
        static Rect rightArrowRect;
        Bitmap upArrow;
        static Rect upArrowRect;
        Bitmap downArrow;
        static Rect downArrowRect;


        // To deal with scrolling
        int scrollX;

        public GameView(Context context, Model m)
        {
            super(context);
            model = m;
            scrollX = getScrollX2();	// sets difference between Luigi's X and view's left margin

            // Initialize ourHolder and paint objects
            ourHolder = getHolder();
            paint = new Paint();


            // Load the images
            ground_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.ground);

            // Load movement arrows + set up their rectangles
            rightArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.right_arrow);
            rightArrowRect = new Rect(2050, 800, 2250, 1000);
            leftArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.left_arrow);
            leftArrowRect = new Rect(1150, 800, 1350, 1000);
            upArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.up_arrow);
            upArrowRect = new Rect(1750, 800, 1950, 1000);
            downArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.down_arrow);
            downArrowRect = new Rect(1450, 800, 1650, 1000);
        }

        // For Arrows
        public void paintArrows()
        {
            // Create transparent paint for background of arrows
            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(Color.WHITE);
            backgroundPaint.setAlpha(180); // transparency

            // Draw the white semi-transparent rectangle (your button area)
            canvas.drawRect(leftArrowRect, backgroundPaint);
            canvas.drawRect(rightArrowRect, backgroundPaint);
            canvas.drawRect(upArrowRect, backgroundPaint);
            canvas.drawRect(downArrowRect, backgroundPaint);

//            // 3. Create Paint for the arrow
//            Paint arrowPaint = new Paint();
//            arrowPaint.setAlpha(220); // almost fully visible, just slightly transparent

            //Draw the arrows inside the rectangles
            canvas.drawBitmap(leftArrow, null, leftArrowRect, paint);
            canvas.drawBitmap(rightArrow, null, rightArrowRect, paint);
            canvas.drawBitmap(upArrow, null, upArrowRect, paint);
            canvas.drawBitmap(downArrow, null, downArrowRect, paint);
        }

        // For scrolling
        public int getScrollX2()
        {
            scrollX = (model.getLuigiX() - 300); 	// scrolling based on Luigi's X position
            return scrollX;
        }

        void setController(GameController c)
        {
            controller = c;
        }

        public void update()
        {
            if (!ourHolder.getSurface().isValid())
                return;
            canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(Color.rgb(0, 0, 0));

            // Draw Sprites
            for(int i = 0; i < model.getSpritesTotal(); i++)
            {
                Sprite spriteA = model.getSprite(i);
                spriteA.drawYourself(canvas, paint, this.getScrollX2());
            }

            // Draw Ground (ground.jpg's adjacent to each other)
            for (int i = 0; i < 40; i++)
            {
                Rect groundDstUp = new Rect((i*225) - this.getScrollX2(), 950, ((i*225) - this.getScrollX2()) + 300, 950 + 225);
                Rect groundDstDown = new Rect((i*225) - this.getScrollX2(), 1100, ((i*225) - this.getScrollX2()) + 300, 1100 + 225);
                canvas.drawBitmap(ground_image, null, groundDstUp, paint);
                canvas.drawBitmap(ground_image, null, groundDstDown, paint);
            }

            // Draw Movement Arrows
            this.paintArrows();

            ourHolder.unlockCanvasAndPost(canvas);
        }

        // The SurfaceView class (which GameView extends) already
        // implements onTouchListener, so we override this method
        // and pass the event to the controller.
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent)
        {
            controller.onTouchEvent(motionEvent);
            return true;
        }
    }




    static class GameController implements Runnable
    {
        volatile boolean playing;
        Thread gameThread = null;
        Model model;
        GameView view;

        Context context;

        // For arrow Keys
        private boolean keyLeft;
        private boolean keyRight;
        private boolean keyJump = false;
        private boolean keyDown = false;


        GameController(Model m, GameView v, Context c)
        {
            model = m;
            view = v;
            view.setController(this);
            playing = true;
            context = c;
        }

        void update()
        {
            model.saveLuigiPrev();		// save previous Luigi position, for collisions
            if(keyRight)		// move Luigi to right
                model.moveLuigiRight();
            if(keyLeft)		// move Luigi to left
                model.moveLuigiLeft();
            if(keyJump && (model.getLuigiJumpCounter() < 9)) // conditions for Luigi to jump
                model.jumpLuigi();
        }

        @Override
        public void run()
        {
            while(playing)
            {
                //long time = System.currentTimeMillis();
                this.update();
                model.update();
                view.update();

                try {
                    Thread.sleep(20);
                } catch(Exception e) {
                    Log.e("Error:", "sleeping");
                    System.exit(1);
                }
            }
        }

        void onTouchEvent(MotionEvent motionEvent)
        {
            float x = motionEvent.getX();
            float y = motionEvent.getY();

            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: // Player touched the screen
                    if(GameView.rightArrowRect.contains((int)x, (int)y))
                        keyRight = true;
                    if(GameView.leftArrowRect.contains((int)x, (int)y))
                        keyLeft = true;
                    if(GameView.upArrowRect.contains((int)x, (int)y))
                        keyJump = true;
                    if(GameView.downArrowRect.contains((int)x, (int)y))
                    {
                        if (!keyDown)
                            model.fireballShot(context);
                        keyDown = true;
                    }
                    break;

                case MotionEvent.ACTION_UP: // Player withdrew finger
                    keyRight = false;
                    keyLeft = false;
                    keyJump = false;
                    keyDown = false;
                    // releasing any touch will stop Luigi from moving
                    // can be expanded to assign different presses with different IDs
                    // for a future version of this project
                    break;
            }
        }

        // Shut down the game thread.
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
                System.exit(1);
            }

        }

        // Restart the game thread.
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }



    /////////// OTHER CLASSES (Sprites) ///////////
    static public abstract class Sprite
    {
        protected int x, y, w, h;
        protected int x_prev, y_prev;   // for collisions
        protected boolean isValid;      // for update
        protected double vertVelocity;    // for gravity
        protected int xdir;         // for direction (collisions)

        // Abstract methods
        public abstract String toString();
        public abstract boolean update();
        public abstract void drawYourself(Canvas c, Paint p, int scroll);



        //////// FOR EDIT MODE ////////
        // To remove Sprites
        public void setUnvalid()
        {
            isValid = false;
        }


        //////// FOR COLLISIONS ////////
        //To detect sprite collisions
        public boolean isThereACollision(Sprite s)
        {
            // THIS sprite's collision coordinates
            int thisRight = this.getX() + this.getW() - 6;   // 1's added to fix some pixel scenarios
            int thisLeft = this.getX() + 3;
            int thisHead = this.getY() + 3;
            int thisBottom = this.getY() + this.getH();

            // Sprite s collision coordinates
            int sRight = s.getX() + s.getW() - 3;
            int sLeft = s.getX();
            int sTop = s.getY() + 1;
            int sBottom = s.getY() + s.getH();

            // if a 'no-collision' case is detected, skip to next brick
            if(thisRight < sLeft)
                return false;
            if(thisLeft > sRight)
                return false;
            if(thisBottom < sTop) // assumes bigger is downward
                return false;
            if(thisHead > sBottom) // assumes bigger is downward
                return false;
            return true; // collision detected
        }
        // Get out of a sprite
        public void getOutOfSprite(Sprite A)
        {
            if((y_prev >= A.getY() + A.getH()) && (y + h >= A.getY())) //jumping from below
            {
                this.y = A.getY() + A.getH();
                vertVelocity = 2;
            }
            else if((y_prev + h <= A.getY()) && (y <= A.getY() + A.getH())) //falling from above
            {
//                System.out.println("inside collision");
                this.y = A.getY() - h;
                vertVelocity = -2.4;
            }
            if((x_prev >= A.getX() + A.getW()) && (x <= A.getX() + A.getW())) //moving left
            {
                this.x = A.getX() + A.getW();
                changeDirection();
            }
            else if((x_prev + w <= A.getX()) && (x + w >= A.getX())) //moving right
            {
                this.x = A.getX() - this.w;
                changeDirection();
            }
        }

        public void changeDirection()
        {
            xdir *= -1;
        }

        public void savePrev()
        {
            x_prev = x;
            y_prev = y;
        }


        // Sprite Types
        public boolean isLuigi()
        {
            return false;
        }
        public boolean isBrick()
        {
            return false;
        }
        public boolean isGoomba()
        {
            return false;
        }
        public boolean isDryBones()
        {
            return false;
        }
        public boolean isMushroom()
        {
            return false;
        }
        public boolean isFireball()
        {
            return false;
        }

        // Getters
        public int getX()
        {
            return x;
        }
        public int getY()
        {
            return y;
        }
        public int getW()
        {
            return w;
        }
        public int getH()
        {
            return h;
        }
    }


    static public class Luigi extends Sprite
    {
        private static Bitmap[] luigiImages;
        private Bitmap currentImage;
        private int state;      // to deal with image animations
        private int luigiSpeed;     // speed on x-axis
        private int jumpCounter;    // to deal with higher jumps
        private boolean onGround;   // determines if Luigi is on ground


        // Luigi Constructor
        public Luigi(Context context, int x, int y)
        {
            this.x = x;
            this.y = y;
            w = 150;
            h = 300;
            luigiSpeed = 30;
            jumpCounter = 0;
            onGround = true;
            state = 0;
            isValid = true;
            if(luigiImages == null)
            {
                luigiImages = new Bitmap[5];
                luigiImages[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.luigi1);
                luigiImages[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.luigi2);
                luigiImages[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.luigi3);
                luigiImages[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.luigi4);
                luigiImages[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.luigi5);
            }
        }


        // Luigi Update
        public boolean update()
        {
            //System.out.println(this.toString());
            if(onGround)
                jumpCounter = 0;    //Jump counter set to 0 when Luigi is on ground
            else{
                vertVelocity += 2.4; // Due to acceleration of Gravity
                jumpCounter++;      // Jump counter increases after a jump
            }
            y += vertVelocity;

            if(y + h > 950)         // Ground stops Luigi from falling
            {
                vertVelocity = 0.0;
                y = 950 - h;    // snap back to the ground
                onGround = true;    // luigi can jump here
                jumpCounter = 0;   // jump counter is 0 on ground
            }
            return true;
        }


        // Draws Luigi
        public void drawYourself(Canvas c, Paint p, int scroll)
        {

            currentImage = luigiImages[state];

            Rect dst = new Rect(this.x - scroll, this.y, (this.x - scroll) + this.w, this.y + this.h);
            c.drawBitmap(currentImage, null, dst, p);
        }


        ///////// Luigi Methods /////////
        public void moveRight()
        {
            state = (state + 1) % 5; // for animation
            x += luigiSpeed;    // move Luigi
            currentImage = luigiImages[state];
        }
        public void moveLeft()
        {
            state = (state + 1) % 5;
            x -= luigiSpeed;    // move Luigi
            currentImage = luigiImages[state];
        }
        public void jump()
        {
            switch(jumpCounter){
                case(0):
                case(1):
                case(2):
                case(3):
                case(4):
                case(5):
                case(6):
                case(7):
                case(8):
                    vertVelocity = -34;
            }
            onGround = false;   // no longer on Ground
        }
        public void changeSize()
        {
            if(this.h == 300)
            {
                this.h = 150;
                onGround = false;
            }
            else if(this.h == 150)
            {
                this.y -= 150;
                this.h = 300;
            }
        }

        // Gets Luigi out of brick
        @Override
        public void getOutOfSprite(Sprite A)
        {
            //System.out.println("inside getout");
            if((y_prev >= A.getY() + A.getH()) && (y + h >= A.getY())) //jumping from below
            {
                this.y = A.getY() + A.getH();
                vertVelocity = 2;
            }
            else if((y_prev + h <= A.getY()) && (y <= A.getY() + A.getH())) //falling from above
            {
                //System.out.println("collision toe");
                this.y = A.getY() - h;
                vertVelocity = -2.4;
                jumpCounter = 0;
            }
            if((x_prev >= A.getX() + A.getW()) && (x <= A.getX() + A.getW())) //moving left
                this.x = A.getX() + A.getW();
            else if((x_prev + w <= A.getX()) && (x + w >= A.getX())) //moving right
                this.x = A.getX() - this.w;     // -1 to account for an error
        }

        // Luigi-specific Getter
        public int getJumpCounter()
        {
            return jumpCounter;
        }


        // For Polymorphism
        @Override
        public boolean isLuigi()
        {
            return true;
        }


        // returns objects parameters as values, as opposed to addresses
        @Override
        public String toString()
        {
            return "Luigi (x,y) = (" + x + ", " + y + "), w = " + w + ", h = " + h;
        }
    }


    static public class Brick extends Sprite
    {
        private Bitmap brick_image;


        // Update Brick
        public boolean update()
        {
            return isValid;
        }

        // Draws brick
        public void drawYourself(Canvas c, Paint p, int scroll)
        {
            Rect dst = new Rect(this.x - scroll, this.y, (this.x - scroll) + this.w, this.y + this.h);
            c.drawBitmap(brick_image, null, dst, p);
        }

        public void getOutOfSprite(Sprite A){}

        // Default constructor
        public Brick(Context context, int x, int y)
        {
            this.x = x;
            this.y = y;
            isValid = true;
            w = 150;
            h = 150;
            if(brick_image == null)
                brick_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.brick);
        }

        // Json parameterized constructor
        public Brick(Context context, Json ob)
        {
            isValid = true;
            this.x = (int)ob.getLong("x");
            this.y = (int)ob.getLong("y");
            this.w = 150; //(int)ob.getLong("w");
            this.h = 150; //(int)ob.getLong("h");
            brick_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.brick);
        }

        // Brick Getters
        public int getX()
        {
            return x;
        }
        public int getY()
        {
            return y;
        }
        public int getW()
        {
            return w;
        }
        public int getH()
        {
            return h;
        }

        // For Polymorphism
        @Override
        public boolean isBrick()
        {
            return true;
        }

        // Json marshal: to save a file as a json
//        public Json marshal()
//        {
//            Json ob = Json.newObject();
//            ob.add("x", x);
//            ob.add("y", y);
//            ob.add("w", w);
//            ob.add("h", h);
//            return ob;
//        }


        // returns objects parameters as values, as opposed to addresses
        @Override
        public String toString()
        {
            return "Brick (x,y) = (" + x + ", " + y + "), w = " + w + ", h = " + h;
        }
    }

    static public class Goomba extends Sprite
    {
        private static Bitmap[] goombaImages;
        private Bitmap currentImage;
        private int state;      // to deal with image animations
        private int speed;
        private int onFireCounter;  // to have Goomba on the screen before it disappears

        // Default constructor
        public Goomba(Context context, int x, int y)
        {
            this.x = x;
            this.y = y;
            w = 150;
            h = 150;
            state = 0;
            speed = 15;
            xdir = -1;
            onFireCounter = 0;
            isValid = true;
            if(goombaImages == null)
            {
                goombaImages = new Bitmap[5];
                goombaImages[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.goomba);
                goombaImages[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.goomba_fire);

            }
        }

        // Json parameterized constructor
        public Goomba(Context context, Json ob)
        {
            isValid = true;
            this.x = (int)ob.getLong("x");
            this.y = (int)ob.getLong("y");
            this.w = 150; //(int)ob.getLong("w");
            this.h = 150; //(int)ob.getLong("h");
            this.xdir = (int)ob.getLong("xdir");
            this.speed = 15; //(int)ob.getLong("v");
            this.state = (int)ob.getLong("state");
            this.onFireCounter = (int)ob.getLong("oFC");
            if(goombaImages == null)
            {
                goombaImages = new Bitmap[5];
                goombaImages[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.goomba);
                goombaImages[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.goomba_fire);

            }
        }

        // Update Brick
        public boolean update()
        {
            this.savePrev();
            vertVelocity += 2.4; // Due to acceleration of Gravity
            y += vertVelocity;
            x += speed * xdir;
            if(state == 1)
            {
                speed = 0;
                onFireCounter += 1;
            }
            if(y + h > 950)         // Ground stops Goomba from falling
            {
                vertVelocity = 0.0;
                y = 950 - h;    // snap back to the ground
            }
            if(onFireCounter > 50)  // removes goomba after 50 update calls
                isValid = false;
            return isValid;
        }

        // Draws brick
        public void drawYourself(Canvas c, Paint p, int scroll)
        {
            currentImage = goombaImages[state];

            Rect dst = new Rect(this.x - scroll, this.y, (this.x - scroll) + this.w, this.y + this.h);
            c.drawBitmap(currentImage, null, dst, p);
        }


        public boolean isOnFire()
        {
            if(state == 1)
                return true;    // returns true if goomba already on fire
            state = 1;      // else, makes it on fire
            return false;
        }

        // For Polymorphism
        @Override
        public boolean isGoomba()
        {
            return true;
        }


        // returns objects parameters as values, as opposed to addresses
        @Override
        public String toString()
        {
            return "Goomba (x,y) = (" + x + ", " + y + "), w = " + w + ", h = " + h;
        }
    }

    static public class DryBones extends Sprite
    {
        private static Bitmap[] drybonesImages;
        private Bitmap currentImage;
        private int state;      // to deal with image animations
        private int speed;     // speed on x-axis
        private int downCounter;    // to deal with higher jumps


        // Drybones Constructor
        public DryBones(Context context, int x, int y)
        {
            this.x = x;
            this.y = y;
            w = 150;
            h = 300;
            state = 0;
            speed = 2;
            xdir = 1;       // for direction change after collisions
            downCounter = 0;
            isValid = true;
            if(drybonesImages == null)
            {
                drybonesImages = new Bitmap[9];
                drybonesImages[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones1);
                drybonesImages[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones2);
                drybonesImages[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones3);
                drybonesImages[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones4);
                drybonesImages[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones5);
                drybonesImages[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones6);
                drybonesImages[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones7);
                drybonesImages[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones8);
                drybonesImages[8] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones11);

            }
        }

        // Json parameterized constructor
        public DryBones(Context context, Json ob)
        {
            isValid = true;
            this.x = (int)ob.getLong("x");
            this.y = (int)ob.getLong("y");
            this.w = 150; //(int)ob.getLong("w");
            this.h = (int)ob.getLong("h");
            this.xdir = (int)ob.getLong("xdir");
            this.speed = (int)ob.getLong("v");
            this.state = (int)ob.getLong("state");
            this.downCounter = (int)ob.getLong("dC");
            if(drybonesImages == null)
            {
                drybonesImages = new Bitmap[9];
                drybonesImages[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones1);
                drybonesImages[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones2);
                drybonesImages[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones3);
                drybonesImages[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones4);
                drybonesImages[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones5);
                drybonesImages[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones6);
                drybonesImages[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones7);
                drybonesImages[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones8);
                drybonesImages[8] = BitmapFactory.decodeResource(context.getResources(), R.drawable.drybones11);

            }
        }

        // Drybones Update
        public boolean update()
        {
            this.savePrev();
            vertVelocity += 2.4; // Due to acceleration of Gravity
            y += vertVelocity;
            if(state != 8)
            {
                x += speed * xdir;
                state = (state + 1) % 8;
            }
            if(y + h > 950)         // Ground stops DryBones from falling
            {
                vertVelocity = 0.0;
                y = 950 - h;    // snap back to the ground
            }
            if(state == 8)      // while invalid, stay on the ground and increase downCounter
            {
                this.w = 300;
                this.h = 150;
                speed = 0;
                downCounter += 1;
            }
            if(downCounter > 100)  // drybones stands up after 100 update calls
            {
                state = 0;
                this.w = 150;
                this.h = 300;
                this.y -= 150;
                speed = 6 * xdir;
                downCounter = 0;    //dC is 0 so it can move again
            }
            return isValid;
        }


        // Draws Drybones
        public void drawYourself(Canvas c, Paint p, int scroll)
        {
            currentImage = drybonesImages[state];

            Rect dst = new Rect(this.x - scroll, this.y, (this.x - scroll) + this.w, this.y + this.h);
            c.drawBitmap(currentImage, null, dst, p);
        }

        public boolean isDown()
        {
            if(state == 8)
                return true;    // returns true if drybones already down
            state = 8;  // else, set drybones down
            return false;
        }


        // For Polymorphism
        @Override
        public boolean isDryBones()
        {
            return true;
        }


        // returns objects parameters as values, as opposed to addresses
        @Override
        public String toString()
        {
            return "DryBones (x,y) = (" + x + ", " + y + "), w = " + w + ", h = " + h;
        }
    }

    static public class Mushroom extends Sprite
    {
        private Bitmap mushroom_image;

        // Default constructor
        public Mushroom(Context context, int x, int y)
        {
            this.x = x;
            this.y = y;
            isValid = true;
            w = 150;
            h = 150;
            if(mushroom_image == null)
                mushroom_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.mushroom);
        }

        // Json parameterized constructor
        public Mushroom(Context context, Json ob)
        {
            isValid = true;
            this.x = (int)ob.getLong("x");
            this.y = (int)ob.getLong("y");
            this.w = 150; //(int)ob.getLong("w");
            this.h = 150; //(int)ob.getLong("h");
            if(mushroom_image == null)
                mushroom_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.mushroom);
        }

        // Draws mushroom
        public void drawYourself(Canvas c, Paint p, int scroll)
        {
            Rect dst = new Rect(this.x - scroll, this.y, (this.x - scroll) + this.w, this.y + this.h);
            c.drawBitmap(mushroom_image, null, dst, p);
        }

        // Update Mushroom
        public boolean update()
        {
            this.savePrev();
            vertVelocity += 2.4; // Due to acceleration of Gravity
            y += vertVelocity;
            if(y + h > 950)         // Ground stops Mushroom from falling
            {
                vertVelocity = 0.0;
                y = 950 - h;    // snap back to the ground
            }
            return isValid;
        }


        // For Polymorphism
        @Override
        public boolean isMushroom()
        {
            return true;
        }


        // returns objects parameters as values, as opposed to addresses
        @Override
        public String toString()
        {
            return "Mushroom (x,y) = (" + x + ", " + y + "), w = " + w + ", h = " + h;
        }
    }

    static public class Fireball extends Sprite
    {
        private Bitmap fireball_image;
        private int speed;
        int x_init = 0;


        // Default constructor
        public Fireball(Context context, int x, int y)
        {
            this.x = x;
            this.y = y;
            w = 75;
            h = 75;
            speed = 36;
            isValid = true;
            if(fireball_image == null)
                fireball_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.fireball);
        }

        // Json parameterized constructor
        public Fireball(Context context, Json ob)
        {
            isValid = true;
            this.w = 75;
            this.h = 75;
            speed = 36;
            this.x = (int)ob.getLong("x");
            this.y = (int)ob.getLong("y");
            vertVelocity = (double)ob.getDouble("vY");
            if(fireball_image == null)
                fireball_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.fireball);
        }

        // Draws fireball
        public void drawYourself(Canvas c, Paint p, int scroll)
        {
            Rect dst = new Rect(this.x - scroll, this.y, (this.x - scroll) + this.w, this.y + this.h);
            c.drawBitmap(fireball_image, null, dst, p);
        }

        // Update fireball
        public boolean update()
        {
            vertVelocity += 2.4;    // Due to acceleration of Gravity
            y += vertVelocity;
            x += speed;
            x_init += speed;
            if(y + h > 950)         // Fireball bounces off ground
            {
                vertVelocity *= -1;
                y = 950 - h;    // snap back to the ground
                if(vertVelocity > -45)    // avoid balls from stay at ground level
                    vertVelocity = -45;
            }
            if(this.x_init > 4500)     // fireball removed from arraylist
                isValid = false;
            return isValid;
        }

        public void getOutOfSprite(Sprite A){}


        // For Polymorphism
        @Override
        public boolean isFireball()
        {
            return true;
        }


        // returns objects parameters as values, as opposed to addresses
        @Override
        public String toString()
        {
            return "Fireball (x,y) = (" + x + ", " + y + "), w = " + w + ", h = " + h;
        }
    }

    /////////// Json (to load sprites) ///////////
    public static abstract class Json
    {
        abstract void write(StringBuilder sb);

        public static Json newObject()
        {
            return new JObject();
        }

        public static Json newList()
        {
            return new JList();
        }

        public static Json parseNode(StringParser p)
        {
            p.skipWhitespace();
            if(p.remaining() == 0)
                throw new RuntimeException("Unexpected end of JSON file");
            char c = p.peek();
            if(c == '"')
                return new JString(JString.parseString(p));
            else if(c == '{')
                return JObject.parseObject(p);
            else if(c == '[')
                return JList.parseList(p);
            else if(c == 't')
            {
                p.expect("true");
                return new JBool(true);
            }
            else if(c == 'f')
            {
                p.expect("false");
                return new JBool(false);
            }
            else if(c == 'n')
            {
                p.expect("null");
                return new JNull();
            }
            else if((c >= '0' && c <= '9') || c == '-')
                return JDouble.parseNumber(p);
            else
                throw new RuntimeException("Unexpected token at " + p.str.substring(p.pos, Math.min(p.remaining(), 50)));
        }

        public int size()
        {
            return this.asList().size();
        }

        public Json get(String name)
        {
            return this.asObject().field(name);
        }

        public Json get(int index)
        {
            return this.asList().get(index);
        }

        public boolean getBool(String name)
        {
            return get(name).asBool();
        }

        public boolean getBool(int index)
        {
            return get(index).asBool();
        }

        public long getLong(String name)
        {
            return get(name).asLong();
        }

        public long getLong(int index)
        {
            return get(index).asLong();
        }

        public double getDouble(String name)
        {
            return get(name).asDouble();
        }

        public double getDouble(int index)
        {
            return get(index).asDouble();
        }

        public String getString(String name)
        {
            return get(name).asString();
        }

        public String getString(int index)
        {
            return get(index).asString();
        }

        public void add(String name, Json tmpListBricks)
        {
            this.asObject().add(name, tmpListBricks);
        }

        public void add(String name, boolean val)
        {
            this.asObject().add(name, new Json.JBool(val));
        }

        public void add(String name, long val)
        {
            this.asObject().add(name, new Json.JLong(val));
        }

        public void add(String name, double val)
        {
            this.asObject().add(name, new Json.JDouble(val));
        }

        public void add(String name, String val)
        {
            this.asObject().add(name, new Json.JString(val));
        }

        public void add(Json item)
        {
            this.asList().add(item);
        }

        public void add(boolean val)
        {
            this.asList().add(new Json.JBool(val));
        }

        public void add(long val)
        {
            this.asList().add(new Json.JLong(val));
        }

        public void add(double val)
        {
            this.asList().add(new Json.JDouble(val));
        }

        public void add(String val)
        {
            this.asList().add(new Json.JString(val));
        }

        public boolean asBool()
        {
            return ((JBool)this).value;
        }

        public long asLong()
        {
            return ((JLong)this).value;
        }

        public double asDouble()
        {
            if(this instanceof JDouble)
                return ((JDouble)this).value;
            else
                return (double)((JLong)this).value;
        }

        public String asString()
        {
            return ((JString)this).value;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            write(sb);
            return sb.toString();
        }

        private JObject asObject()
        {
            return (JObject)this;
        }

        private JList asList()
        {
            return (JList)this;
        }

        public void save(String filename)
        {
            try
            {
                BufferedWriter out = new BufferedWriter(new FileWriter(filename));
                out.write(toString());
                out.close();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        public static Json parse(String s)
        {
            StringParser p = new StringParser(s);
            return Json.parseNode(p);
        }

//        public static Json load(String filename)
//        {
//            String contents;
//            try
//            {
//                contents = new String(Files.readAllBytes(Paths.get(filename)));
//            }
//            catch(Exception e)
//            {
//                throw new RuntimeException(e);
//            }
//            return parse(contents);
//        }

        public static Json loadAndroid(Context context, String filename)
        {
            String contents;
            try {
                AssetManager assetManager = context.getAssets();
                InputStream inputStream = assetManager.open(filename);

                int size = inputStream.available();
                byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();

                contents = new String(buffer, "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return parse(contents);

        }

        public static class StringParser
        {
            String str;
            int pos;

            StringParser(String s)
            {
                str = s;
                pos = 0;
            }

            int remaining()
            {
                return str.length() - pos;
            }

            char peek()
            {
                return str.charAt(pos);
            }

            void advance(int n)
            {
                pos += n;
            }

            void skipWhitespace()
            {
                while(pos < str.length() && str.charAt(pos) <= ' ')
                    pos++;
            }

            void expect(String s)
            {
                if(!str.substring(pos, Math.min(str.length(), pos + s.length())).equals(s))
                    throw new RuntimeException("Expected \"" + s + "\", Got \"" + str.substring(pos, Math.min(str.length(), pos + s.length())) + "\"");
                pos += s.length();
            }

            String until(char c)
            {
                int i = pos;
                while(i < str.length() && str.charAt(i) != c)
                    i++;
                String s = str.substring(pos, i);
                pos = i;
                return s;
            }

            String until(char a, char b)
            {
                int i = pos;
                while(i < str.length() && str.charAt(i) != a && str.charAt(i) != b)
                    i++;
                String s = str.substring(pos, i);
                pos = i;
                return s;
            }

            String untilWhitespace()
            {
                int i = pos;
                while(i < str.length() && str.charAt(i) > ' ')
                    i++;
                String s = str.substring(pos, i);
                pos = i;
                return s;
            }

            String untilQuoteSensitive(char a, char b)
            {
                if(peek() == '"')
                {
                    advance(1);
                    String s = "\"" + until('"') + "\"";
                    advance(1);
                    until(a, b);
                    return s;
                }
                else
                    return until(a, b);
            }

            String whileReal()
            {
                int i = pos;
                while(i < str.length())
                {
                    char c = str.charAt(i);
                    if((c >= '0' && c <= '9') ||
                            c == '-' ||
                            c == '+' ||
                            c == '.' ||
                            c == 'e' ||
                            c == 'E')
                        i++;
                    else
                        break;
                }
                String s = str.substring(pos, i);
                pos = i;
                return s;
            }
        }

        private static class NameVal
        {
            String name;
            Json value;

            NameVal(String nam, Json val)
            {
                if(nam == null)
                    throw new IllegalArgumentException("The name cannot be null");
                if(val == null)
                    val = new JNull();
                name = nam;
                value = val;
            }
        }

        private static class JObject extends Json
        {
            ArrayList<NameVal> fields;

            JObject()
            {
                fields = new ArrayList<NameVal>();
            }

            public void add(String name, Json val)
            {
                fields.add(new NameVal(name, val));
            }

            Json fieldIfExists(String name)
            {
                for(NameVal nv : fields)
                {
                    if(nv.name.equals(name))
                        return nv.value;
                }
                return null;
            }

            Json field(String name)
            {
                Json n = fieldIfExists(name);
                if(n == null)
                    throw new RuntimeException("No field named \"" + name + "\" found.");
                return n;
            }

            void write(StringBuilder sb)
            {
                sb.append("{");
                for(int i = 0; i < fields.size(); i++)
                {
                    if(i > 0)
                        sb.append(",");
                    NameVal nv = fields.get(i);
                    JString.write(sb, nv.name);
                    sb.append(":");
                    nv.value.write(sb);
                }
                sb.append("}");
            }

            static JObject parseObject(StringParser p)
            {
                p.expect("{");
                JObject newOb = new JObject();
                boolean readyForField = true;
                while(p.remaining() > 0)
                {
                    char c = p.peek();
                    if(c <= ' ')
                    {
                        p.advance(1);
                    }
                    else if(c == '}')
                    {
                        p.advance(1);
                        return newOb;
                    }
                    else if(c == ',')
                    {
                        if(readyForField)
                            throw new RuntimeException("Unexpected ','");
                        p.advance(1);
                        readyForField = true;
                    }
                    else if(c == '\"')
                    {
                        if(!readyForField)
                            throw new RuntimeException("Expected a ',' before the next field in JSON file");
                        p.skipWhitespace();
                        String name = JString.parseString(p);
                        p.skipWhitespace();
                        p.expect(":");
                        Json value = Json.parseNode(p);
                        newOb.add(name, value);
                        readyForField = false;
                    }
                    else
                        throw new RuntimeException("Expected a '}' or a '\"'. Got " + p.str.substring(p.pos, p.pos + 10));
                }
                throw new RuntimeException("Expected a matching '}' in JSON file");
            }
        }

        private static class JList extends Json
        {
            ArrayList<Json> list;

            JList()
            {
                list = new ArrayList<Json>();
            }

            public void add(Json item)
            {
                if(item == null)
                    item = new JNull();
                list.add(item);
            }

            public int size()
            {
                return list.size();
            }

            public Json get(int index)
            {
                return list.get(index);
            }

            void write(StringBuilder sb)
            {
                sb.append("[");
                for(int i = 0; i < list.size(); i++)
                {
                    if(i > 0)
                        sb.append(",");
                    list.get(i).write(sb);
                }
                sb.append("]");
            }

            static JList parseList(StringParser p)
            {
                p.expect("[");
                JList newList = new JList();
                boolean readyForValue = true;
                while(p.remaining() > 0)
                {
                    p.skipWhitespace();
                    char c = p.peek();
                    if(c == ']')
                    {
                        p.advance(1);
                        return newList;
                    }
                    else if(c == ',')
                    {
                        if(readyForValue)
                            throw new RuntimeException("Unexpected ',' in JSON file");
                        p.advance(1);
                        readyForValue = true;
                    }
                    else
                    {
                        if(!readyForValue)
                            throw new RuntimeException("Expected a ',' or ']' in JSON file");
                        newList.list.add(Json.parseNode(p));
                        readyForValue = false;
                    }
                }
                throw new RuntimeException("Expected a matching ']' in JSON file");
            }
        }

        private static class JBool extends Json
        {
            boolean value;

            JBool(boolean val)
            {
                value = val;
            }

            void write(StringBuilder sb)
            {
                sb.append(value ? "true" : "false");
            }
        }

        private static class JLong extends Json
        {
            long value;

            JLong(long val)
            {
                value = val;
            }

            void write(StringBuilder sb)
            {
                sb.append(value);
            }
        }

        private static class JDouble extends Json
        {
            double value;

            JDouble(double val)
            {
                value = val;
            }

            void write(StringBuilder sb)
            {
                sb.append(value);
            }

            static Json parseNumber(StringParser p)
            {
                String s = p.whileReal();
                if(s.indexOf('.') >= 0)
                    return new JDouble(Double.parseDouble(s));
                else
                    return new JLong(Long.parseLong(s));
            }
        }

        private static class JString extends Json
        {
            String value;

            JString(String val)
            {
                value = val;
            }

            static void write(StringBuilder sb, String value)
            {
                sb.append('"');
                for(int i = 0; i < value.length(); i++)
                {
                    char c = value.charAt(i);
                    if(c < ' ')
                    {
                        switch(c)
                        {
                            case '\b': sb.append("\\b"); break;
                            case '\f': sb.append("\\f"); break;
                            case '\n': sb.append("\\n"); break;
                            case '\r': sb.append("\\r"); break;
                            case '\t': sb.append("\\t"); break;
                            default:
                                sb.append(c);
                        }
                    }
                    else if(c == '\\')
                        sb.append("\\\\");
                    else if(c == '"')
                        sb.append("\\\"");
                    else
                        sb.append(c);
                }
                sb.append('"');
            }

            void write(StringBuilder sb)
            {
                write(sb, value);
            }

            static String parseString(StringParser p)
            {
                StringBuilder sb = new StringBuilder();
                p.expect("\"");
                while(p.remaining() > 0)
                {
                    char c = p.peek();
                    if(c == '\"')
                    {
                        p.advance(1);
                        return sb.toString();
                    }
                    else if(c == '\\')
                    {
                        p.advance(1);
                        c = p.peek();
                        p.advance(1);
                        switch(c)
                        {
                            case '"': sb.append('"'); break;
                            case '\\': sb.append('\\'); break;
                            case '/': sb.append('/'); break;
                            case 'b': sb.append('\b'); break;
                            case 'f': sb.append('\f'); break;
                            case 'n': sb.append('\n'); break;
                            case 'r': sb.append('\r'); break;
                            case 't': sb.append('\t'); break;
                            case 'u': throw new RuntimeException("Sorry, unicode characters are not yet supported");
                            default: throw new RuntimeException("Unrecognized escape sequence");
                        }
                    }
                    else
                    {
                        sb.append(c);
                        p.advance(1);
                    }
                }
                throw new RuntimeException("No closing \"");
            }
        }

        private static class JNull extends Json
        {
            JNull()
            {
            }

            void write(StringBuilder sb)
            {
                sb.append("null");
            }
        }
    }


}