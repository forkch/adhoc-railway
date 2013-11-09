package ch.fork.adhocrailway.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class AdHocRailwayAndroidActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.srcpd);
        
        Toast.makeText(this, "HELLO", Toast.LENGTH_LONG);
    }
}