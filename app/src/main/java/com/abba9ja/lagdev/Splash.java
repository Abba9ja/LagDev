package com.abba9ja.lagdev;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splash extends AppCompatActivity {
    MainActivity mainActivity = new MainActivity();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent openStartingPoint = new Intent("com.abba9ja.lagdev.MAINACTIVITY");
                    startActivity(openStartingPoint);
                }
            }
        };
        timer.start();

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }


}
