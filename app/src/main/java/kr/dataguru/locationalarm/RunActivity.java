package kr.dataguru.locationalarm;

/**
 * Created by dataguru on 2017. 10. 12..
 */

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import service.LocationAlarmService;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = RunActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST_SMS_PHONE = 0x100;
    public static final int PERMISSION_REQUEST_LOCATION_SMS_PHONE = 0x101;
    public static final int PERMISSION_REQUEST_LOCATION = 0x102;

    private Context context;
    private SharedPreferences session;
    private Intent intent;

    private double lat = 0;
    private double lng = 0;

    private boolean backPressed = false;

    private SupportMapFragment map;
    private Button btnSms, btnLocationSend, btnNfc, btnServiceClose, btnSiren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        context = getApplicationContext();
        session = getSharedPreferences("session", MODE_PRIVATE);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        btnSms = (Button) findViewById(R.id.btnSms);
        btnLocationSend = (Button) findViewById(R.id.btnLocationSend);
        btnNfc = (Button) findViewById(R.id.btnNfc);
        btnServiceClose = (Button) findViewById(R.id.btnServiceClose);
        btnSiren = (Button) findViewById(R.id.btnSiren);

        btnSms.setOnClickListener(EventHandler);
        btnLocationSend.setOnClickListener(EventHandler);
        btnNfc.setOnClickListener(EventHandler);
        btnServiceClose.setOnClickListener(EventHandler);
        btnSiren.setOnClickListener(EventHandler);

        SetToolbar();
        Initialization();
    }

    /*
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
    */

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if ( backPressed ) {
            //overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.gc();
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
    }

    /**
     * func
     */
    public void Initialization() {
        map.getMapAsync(this);
        LoadMyLocation();
        if ( !IsServiceRunning("service.LocationAlarmService") ) {
            Log.v(TAG,"startService");
            startService(new Intent(RunActivity.this, LocationAlarmService.class));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(37.545965, 126.985421);
        if ( 0 < lat && 0 < lng ) {
            sydney = new LatLng(lat, lng);
        }
        googleMap.addMarker(new MarkerOptions().position(sydney).title("현재위치치"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16));

    }

    private void SetToolbarTitle (String title ) {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(title);
    }

    private void SetToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageButton toolbarPrevBtn = (ImageButton) toolbar.findViewById(R.id.btnPrev);
        toolbarPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        SetToolbarTitle(context.getString(R.string.lang_activity_run));
    }

    private void LoadMyLocation () {
        if ((ContextCompat.checkSelfPermission(RunActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(RunActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(RunActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, EventLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, EventLocationListener);
        }
    }

    private void OnRequestPermission (View view) {
        switch (view.getId()) {
            case R.id.btnSms:
                if ((ContextCompat.checkSelfPermission(RunActivity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.RECEIVE_SMS)
                                != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.READ_PHONE_STATE)
                                != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(RunActivity.this,
                            new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_SMS_PHONE);
                } else {

                }
                break;
            case R.id.btnLocationSend:
                if ((ContextCompat.checkSelfPermission(RunActivity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.RECEIVE_SMS)
                                != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.READ_PHONE_STATE)
                                != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(RunActivity.this,
                            new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_LOCATION_SMS_PHONE);
                } else {

                }
                break;
        }
    }

    private void OnPhoneLocationSms () {
        Uri uri = Uri.parse("smsto:"+session.getString("tel","112")); //sms 문자와 관련된 Data는 'smsto:'로 시작. 이후는 문자를 받는 사람의 전화번호
        intent = new Intent(Intent.ACTION_SENDTO,uri); //시스템 액티비티인 SMS문자보내기 Activity의 action값
        String msg = "http://maps.googleapis.com/maps/api/staticmap?center="+lat+","+lng+"&zoom=15&size=600x300&maptype=roadmap&markers=color%3aorange%7Clabel%3aA%7C"+lat+","+lng+"&sensor=false";
        intent.putExtra("sms_body", msg);  //보낼 문자내용을 추가로 전송, key값은 반드시 'sms_body'
        try {
            startActivity(intent);//액티비티 실행
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void OnPhoneSms () {
        Uri uri = Uri.parse("smsto:112"); //sms 문자와 관련된 Data는 'smsto:'로 시작. 이후는 문자를 받는 사람의 전화번호
        intent = new Intent(Intent.ACTION_SENDTO,uri); //시스템 액티비티인 SMS문자보내기 Activity의 action값
        String msg = "http://maps.googleapis.com/maps/api/staticmap?center="+lat+","+lng+"&zoom=15&size=600x300&maptype=roadmap&markers=color%3aorange%7Clabel%3aA%7C"+lat+","+lng+"&sensor=false";
        intent.putExtra("sms_body", msg);  //보낼 문자내용을 추가로 전송, key값은 반드시 'sms_body'
        try {
            startActivity(intent);//액티비티 실행
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    View.OnClickListener EventHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final View view = v;
            switch (v.getId()) {
                case R.id.btnSiren:
                    try {
                        final MediaPlayer selectDropButtonMedia = MediaPlayer.create(RunActivity.this, R.raw.siren);
                        selectDropButtonMedia.start();
                        selectDropButtonMedia.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                try{
                                    Log.v(TAG,"MediaPlayer Exception e :r elease");
                                    mediaPlayer.release();
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        //selectDropButtonMedia.release();
                    } catch ( Exception e ) {
                        Log.v(TAG,"MediaPlayer Exception e : "+e);
                    }
                    break;
                case R.id.btnSms:
                    if ( 0 < lat && 0 < lng ) {
                        if ((ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.SEND_SMS)
                                != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(RunActivity.this,
                                        Manifest.permission.RECEIVE_SMS)
                                        != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(RunActivity.this,
                                        Manifest.permission.READ_PHONE_STATE)
                                        != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(RunActivity.this,
                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(RunActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED)) {
                            OnRequestPermission(view);
                        } else {
                            OnPhoneSms();
                        }
                    } else {
                        Toast.makeText(context,"위치정보를 가져올 수 없습니다.",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnLocationSend:

                    if ( 0 < lat && 0 < lng && session.getBoolean("logged_in",false) ) {
                        if ((ContextCompat.checkSelfPermission(RunActivity.this,
                                Manifest.permission.SEND_SMS)
                                != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(RunActivity.this,
                                        Manifest.permission.RECEIVE_SMS)
                                        != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(RunActivity.this,
                                        Manifest.permission.READ_PHONE_STATE)
                                        != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(RunActivity.this,
                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(RunActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED)) {
                            OnRequestPermission(view);
                        } else {
                            OnPhoneLocationSms();
                        }
                    } else {
                        if ( lat == 0 && lng == 0 ) {
                            Toast.makeText(context,"위치정보를 가져올 수 없습니다.",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context,"설정에서 보호자 연락처를 입력해 주세요.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case R.id.btnNfc:
                    intent = new Intent(context, NfcActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btnServiceClose:
                    Toast.makeText(context,"서비스를 종료합니다.",Toast.LENGTH_SHORT).show();
                    if ( IsServiceRunning("service.LocationAlarmService") ) {
                        Log.v(TAG,"stopService");
                        stopService(new Intent(RunActivity.this, LocationAlarmService.class));
                    }
                    onBackPressed();
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_SMS_PHONE:
                if ((grantResults.length > 0)
                        && (
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[2] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[3] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[4] == PackageManager.PERMISSION_GRANTED
                )) {
                } else {
                    Log.d("TAG", "Call Permission Not Granted 2");
                }
                break;
            case PERMISSION_REQUEST_LOCATION:
                if ((grantResults.length > 0)
                        && (
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED
                )) {
                } else {
                    Log.d("TAG", "Call Permission Not Granted 2");
                }
                break;
            case PERMISSION_REQUEST_LOCATION_SMS_PHONE:
                if ((grantResults.length > 0)
                        && (
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[2] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[3] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[4] == PackageManager.PERMISSION_GRANTED
                )) {
                } else {
                    Log.d("TAG", "Call Permission Not Granted 2");
                }
                break;
            default:
                break;
        }
    }

    private final LocationListener EventLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            /*
            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            */
            lat = location.getLatitude();
            lng = location.getLongitude();
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            /*
            tv.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n고도 : " + altitude + "\n정확도 : "  + accuracy);
            */
            map.getMapAsync(RunActivity.this);

            final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(EventLocationListener);
        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.v(TAG, "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.v(TAG, "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.v(TAG, "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

    public Boolean IsServiceRunning ( String serviceName ) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo :
                activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}