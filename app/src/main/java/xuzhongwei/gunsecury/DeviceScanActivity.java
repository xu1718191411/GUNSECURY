package xuzhongwei.gunsecury;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class DeviceScanActivity extends AppCompatActivity {
    private ProgressBar mProgressBar = null;
    private Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan);
        mProgressBar = (ProgressBar) findViewById(R.id.connectingProgress);
        mActivity = this;
    }


    public void startScan(View view){
        ((LinearLayout) findViewById(R.id.scanResult)).setVisibility(View.VISIBLE);
    }

    public void connnect(View view){
            mProgressBar.setVisibility(View.VISIBLE);
            Timer timer = new Timer();

            switch (view.getId()){
                case R.id.c1:
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(mActivity.getResources().getString(R.string.scaning_complete_string));
                                    goToDeviceDetail();
                                }
                            });

                        }
                    },3000);
                    break;
            }

        showToast(getResources().getString(R.string.scaning_string));
    }

    private void goToDeviceDetail(){
        mProgressBar.setVisibility(View.GONE);
        Intent intent = new Intent(mActivity,DeviceDetailActivity.class);
        mActivity.startActivity(intent);
    }

    private void showToast(String str){
        Toast toast = Toast.makeText(mActivity,str,Toast.LENGTH_LONG);
        toast.show();
    }

}
