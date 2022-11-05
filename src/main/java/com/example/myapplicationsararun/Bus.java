package com.example.myapplicationsararun;

import static com.example.myapplicationsararun.GameView.screenRatioX;
import static com.example.myapplicationsararun.GameView.screenRatioY;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Bus {
    int toShoot = 0;
    boolean isGoingUp = false;
    int x, y, width, height, wingCounter = 0, shootCounter = 1;
    Bitmap bus1, bus2, shoot1, shoot2, shoot3, shoot4, shoot5, dead;
    private GameView gameView;

    Bus (GameView gameView, int screenY,  Resources res) {

        this.gameView = gameView;

        bus1 = BitmapFactory.decodeResource(res, R.drawable.fly1);
        bus2 = BitmapFactory.decodeResource(res, R.drawable.fly2);

        width = bus1.getWidth();
        height = bus1.getHeight();

        width /= 5;
        height /= 5;// TO FIX THE SIZE OF THE BUS

        width = (int) (width * screenRatioX);
        height = (int) (height * screenRatioY);

        bus1 = Bitmap.createScaledBitmap(bus1, width, height, false);
        bus2 = Bitmap.createScaledBitmap(bus2, width, height, false);

        shoot1 = BitmapFactory.decodeResource(res, R.drawable.shoot1);
        shoot2 = BitmapFactory.decodeResource(res, R.drawable.shoot2);
        shoot3 = BitmapFactory.decodeResource(res, R.drawable.shoot3);
        shoot4 = BitmapFactory.decodeResource(res, R.drawable.shoot4);
        shoot5 = BitmapFactory.decodeResource(res, R.drawable.shoot5);

        shoot1 = Bitmap.createScaledBitmap(shoot1, width, height, false);
        shoot2 = Bitmap.createScaledBitmap(shoot2, width, height, false);
        shoot3 = Bitmap.createScaledBitmap(shoot3, width, height, false);
        shoot4 = Bitmap.createScaledBitmap(shoot4, width, height, false);
        shoot5 = Bitmap.createScaledBitmap(shoot5, width, height, false);

        dead = BitmapFactory.decodeResource(res, R.drawable.dead);
        dead = Bitmap.createScaledBitmap(dead, width, height, false);

        y = screenY / 2;
        x = (int) (64 * screenRatioX);
dead = BitmapFactory.decodeResource(res,R.drawable.dead);
        dead = Bitmap.createScaledBitmap(dead, width, height, false);
    }

    Bitmap getBus () {

        if (toShoot != 0) {

            if (shootCounter == 1) {
                shootCounter++;
                return shoot1;
            }

            if (shootCounter == 2) {
                shootCounter++;
                return shoot2;
            }

            if (shootCounter == 3) {
                shootCounter++;
                return shoot3;
            }

            if (shootCounter == 4) {
                shootCounter++;
                return shoot4;
            }

            shootCounter = 1;
            toShoot--;
            gameView.newBullet();

            return shoot5;
        }

        if (wingCounter == 0) {
            wingCounter++;
            return bus1;
        }
        wingCounter--;

        return bus2;
    }

    Rect getCollisionShape () {
        return new Rect(x, y, width, height);
    }

    Bitmap getDead () {
        return dead;
    }
}
