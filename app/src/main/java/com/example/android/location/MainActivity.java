package com.example.android.location;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hqb.android.location.Location;
import com.hqb.android.location.LocationErrorType;
import com.hqb.android.location.LocationListener;
import com.hqb.android.location.LocationManager;
import com.hqb.android.location.LocationType;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private RadioGroup radioGroup;
    private TextView tvResultView;
    private ImageView imgView;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                RadioButton btn = (RadioButton) group.findViewById(checkedId);
                int index = group.indexOfChild(btn);
                switch (index) {
                    case 0:
                        LocationManager.getInstance(MainActivity.this).setLocationType(LocationType.AMAP);
                        break;
                    case 1:
                        LocationManager.getInstance(MainActivity.this).setLocationType(LocationType.BAIDU);
                        break;
                    case 2:
                        LocationManager.getInstance(MainActivity.this).setLocationType(LocationType.TENCENT);
                        break;
                    default:
                        throw new RuntimeException("Unsupported location type " + index);
                }
            }
        });
        radioGroup.check(radioGroup.getChildAt(0).getId());

        tvResultView = (TextView) findViewById(R.id.tv_result);
        imgView = (ImageView) findViewById(R.id.img);
    }

    // 单次定位
    public void clickOnLocationByOnceBtn(View view) {
        resetViewState(LocationManager.getInstance(this).getLocationType());
        LocationManager.getInstance(MainApplication.getContext()).requestLocationOnce(listener);
    }

    // 持续定位
    public void clickOnLocationByContinuousBtn(View view) {
        tvResultView.setText(String.format("正在使用[%s]持续定位...", LocationManager.getInstance(this).getLocationType().getDesc()));
        imgView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        LocationManager.getInstance(this).requestLocationContinuous();
    }

    // 快速定位
    public void clickOnLocationByQuickBtn(View view) {
        resetViewState(LocationManager.getInstance(this).getLocationType());
        LocationManager.getInstance(MainApplication.getContext()).requestLocation(listener);
    }

    private void resetViewState(LocationType locationType) {
        if (locationType == null) {
            tvResultView.setText("All In One ...");
        } else {
            tvResultView.setText(String.format("正在使用[%s]定位...", locationType.getDesc()));
        }
        imgView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void displayLocation(LocationType locationType, Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append(locationType.getDesc());
        sb.append("\n").append("经度：").append(location.getLongitude());
        sb.append("\n").append("纬度：").append(location.getLatitude());
        sb.append("\n").append("地址：").append(location.getAddress());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss sss");
        sb.append("\n").append("时间：").append(sdf.format(new Date(location.getTime())));

        tvResultView.setText(sb.toString());

        // 显示图片
        String url = String.format("http://restapi.amap.com/v3/staticmap?key=40601a1363f910ccebb1e1f291248969&markers=large,0xffa500,A:%s,%s&zoom=17&size=1000*1000", location.getLongitude(), location.getLatitude());
        Glide.with(this).load(url).centerCrop().crossFade().into(imgView);
    }

    LocationListener listener = new LocationListener() {

        @Override
        public void onReceiveLocation(@NonNull final LocationType locationType, @NonNull final Location location) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    displayLocation(locationType, location);
                }
            });
        }

        @Override
        public void onError(@NonNull LocationType locationType, @NonNull LocationErrorType errorType, @Nullable String reason) {
            final StringBuilder sb = new StringBuilder(locationType.getDesc());
            sb.append("\n");
            sb.append(errorType.name());
            sb.append("\n");
            if (reason != null) {
                sb.append(reason);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tvResultView.setText(sb.toString());
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationManager.getInstance(this).stopLocation();
    }
}
