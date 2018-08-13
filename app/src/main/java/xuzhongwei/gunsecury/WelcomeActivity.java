package xuzhongwei.gunsecury;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            Log.d("FIGHTING","1111111");
        }else{
            Log.d("FIGHTING",savedInstanceState.toString());
        }

        setContentView(R.layout.welcome);
        Button welcomeButton = (Button) findViewById(R.id.welcomeButton);
        welcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScanActivity();
            }
        });

    }


    private void goToScanActivity(){
        Intent intent = new Intent(this,DeviceScanActivity.class);
        startActivity(intent);
    }
}

