package acl.siot.opencvwpc20191007noc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import acl.siot.opencvwpc20191007noc.util.MLog;
import androidx.annotation.Nullable;

/**
 * Created by IChen.Chu on 2020/4/23
 */
public class DisplayActivity extends Activity {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private ImageButton backDetectBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mLog.d(TAG, " * onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display);
        backDetectBtn = findViewById(R.id.backDetectBtn);
        backDetectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DetectActivity.class);
                startActivity(i);
            }
        });
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
