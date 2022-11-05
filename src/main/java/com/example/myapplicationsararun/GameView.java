package com.example.myapplicationsararun;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable{
    private Thread thread;
    private boolean isPlaying,isGameOver = false;
    private Background background1,background2;

    private int screenX,screenY;
    int score=0;
    private Paint paint;

    private Bus bus;

    private List<Bullet> bullets;

    private Bird[] birds;
    private Random random;
    private SharedPreferences prefs;

    private GameActivity activity;

    private SoundPool soundPool;
    int sound;
    public static float screenRatioX,screenRatioY;// To match all screen size
    //Here we will be showing surface view insted of layout (used to change content on screen very quickly)

    public GameView(GameActivity activity,int screenX, int screenY) {
        super(activity);
        this.activity = activity;
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);
//        creating soundPool functinality--------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        sound = soundPool.load(activity, R.raw.shoot, 1);



        this.screenX = screenX;
        this.screenY = screenY;

        screenRatioX = 2040f / screenX;
        screenRatioY = 1080f / screenY;

        background1= new Background(screenX,screenY,getResources());
        background2= new Background(screenX,screenY,getResources());



        bus = new Bus(this,screenY, getResources());
        bullets = new ArrayList<>();
        background2.x = screenX;

        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        birds = new Bird[4];


        for (int i = 0;i < 4;i++) {

            Bird bird = new Bird(getResources());
            birds[i] = bird;

        }
        random = new Random();

    }

    @Override
    public void run() {
        while(isPlaying){
            update();
            draw();
            sleep();
        }
    }
    private void update(){

        background1.x -= 10 * screenRatioX;
        background2.x -= 10 * screenRatioX;

        if (background1.x + background1.background.getWidth() < 0) {// when thew background is off the screen
            background1.x = screenX;
        }

        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }

        if (bus.isGoingUp)
            bus.y -= 30 * screenRatioY;
        else
            bus.y += 30 * screenRatioY;

        if (bus.y < 0)
            bus.y = 0;

        if (bus.y >= screenY - bus.height)
            bus.y = screenY - bus.height;

        List<Bullet> trash = new ArrayList<>();

        for (Bullet bullet : bullets) {

            if (bullet.x > screenX)
                trash.add(bullet);

            bullet.x += 50 * screenRatioX;

            for (Bird bird : birds) {

                if (Rect.intersects(bird.getCollisionShape(),
                        bullet.getCollisionShape())) {

                    score++;
                    bird.x = -500;
                    bullet.x = screenX + 500;
                    bird.wasShot = true;

                }

            }

        }

        for (Bullet bullet : trash)
            bullets.remove(bullet);

        for (Bird bird : birds) {

            bird.x -= bird.speed;

            if (bird.x + bird.width < 0) {

                if (!bird.wasShot) {
                    isGameOver = true;
                    return;
                }

                int bound = (int) (30 * screenRatioX);
                bird.speed = random.nextInt(bound);

                if (bird.speed < 10 * screenRatioX)
                    bird.speed = (int) (10 * screenRatioX);

                bird.x = screenX;
                bird.y = random.nextInt(screenY - bird.height);

                bird.wasShot = false;
            }

            if (Rect.intersects(bird.getCollisionShape(), bus.getCollisionShape())) {
                //getCollisionShape will create a rectangle around the bird

                isGameOver = true;
                return;
            }

        }

    }
    private void draw(){
        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            for (Bird bird : birds)
                canvas.drawBitmap(bird.getBird(), bird.x, bird.y, paint);

            canvas.drawText(score + "", screenX / 2f, 164, paint);

            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(bus.getDead(), bus.x, bus.y, paint);
                getHolder().unlockCanvasAndPost(canvas); // to draw canvas on screen
                saveIfHighScore();
                waitBeforeExiting ();
                return;
            }

            canvas.drawBitmap(bus.getBus(), bus.x, bus.y, paint);

            for (Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);

            getHolder().unlockCanvasAndPost(canvas);

        }

    }

    private void waitBeforeExiting() {
        try {
            Thread.sleep(2000);
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveIfHighScore() {
        if (prefs.getInt("highscore", 0) < score) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

    private void sleep  (){
        try {
            Thread.sleep(17);// for 60 fps
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  void  resume(){
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }
    public void pause(){
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
                e.printStackTrace();
        }
    }
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < screenX / 2) { // will work when the user touches  left half of the screen
                    bus.isGoingUp = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                bus.isGoingUp = false;
                if (event.getX() > screenX / 2)
                    bus.toShoot++;
                break;
        }

        return true;
    }

    public void newBullet() {
        if (!prefs.getBoolean("isMute", false))
            soundPool.play(sound, 1, 1, 0, 0, 1);

        Bullet bullet = new Bullet(getResources());
        bullet.x = bus.x + bus.width;
        bullet.y = bus.y + (bus.height / 2);
        bullets.add(bullet);
    }
}
