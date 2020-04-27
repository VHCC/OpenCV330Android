package acl.siot.opencvwpc20191007noc;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static acl.siot.opencvwpc20191007noc.DetectActivity.df;
import static acl.siot.opencvwpc20191007noc.DetectActivity.faceImageBitmap;
import static acl.siot.opencvwpc20191007noc.DetectActivity.person_temp;

/**
 * Created by IChen.Chu on 2020/4/23
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class DisplayActivity extends Activity {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private ImageButton backDetectBtn;
    private ImageView resultImage_demo;
    private TextView status;
    private TextView temps;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display);
        backDetectBtn = findViewById(R.id.backDetectBtn);
        resultImage_demo = findViewById(R.id.resultImage_demo);
        status = findViewById(R.id.status);
        temps = findViewById(R.id.temps);
        backDetectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DetectActivity.class);
                startActivity(i);
            }
        });
        resultImage_demo.setImageBitmap(faceImageBitmap);
        if (person_temp >= 37.5) {
            status.setTextColor(Color.rgb(255, 0, 0));
            status.setText("Abnormal");
        } else {
            status.setTextColor(Color.rgb(0, 255, 0));
            status.setText("Normal");
        }
        temps.setText(df.format(person_temp));
        temps.setTextColor(Color.rgb(0, 255, 0));
    }

    @Override
    protected void onResume() {
        mLog.d(TAG, " * onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
