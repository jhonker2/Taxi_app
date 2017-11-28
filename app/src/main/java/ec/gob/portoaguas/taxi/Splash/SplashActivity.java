package ec.gob.portoaguas.taxi.Splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import ec.gob.portoaguas.taxi.R;
import ec.gob.portoaguas.taxi.login.LoginActivity;


public class SplashActivity extends AppCompatActivity {
    private static  int SPLASH_TIME_OUT = 4000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent Homeintent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(Homeintent);
                finish();
               // overridePendingTransition(R.anim.zoom_back_in,R.anim.zoom_back_out);

            }
        },SPLASH_TIME_OUT);
    }
}
