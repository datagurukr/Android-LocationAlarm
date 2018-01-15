package service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.OnMapReadyCallback;

import kr.dataguru.locationalarm.RunActivity;

/**
 * Created by dataguru on 2017. 10. 17..
 */

public class LocationAlarmService extends Service {
    private static final String TAG = LocationAlarmService.class.getSimpleName();

    //private static final long MIN_TIME_RADAR_REQUEST = 1000 * 25 * 1;
    private static long TIME_REQUEST = 1000 * 0 * 0;

    private Context context;
    private SharedPreferences session;

    private double lat = 0;
    private double lng = 0;

    private double now_lat = 0;
    private double now_lng = 0;

    private double distance = 0;
    private Boolean distance_flag = true;

    private int period = 0;
    private String location_msg = "";

    // private
    private RequestThread requestThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.v(TAG, "onDestroy:onStart");

        context = getApplicationContext();
        session = getSharedPreferences("session", MODE_PRIVATE);

        period = session.getInt("period",0);

        if ( period == 1 ) {
            TIME_REQUEST = 1000 * 180 * 1;
        } else if ( period == 2 ) {
            TIME_REQUEST = 1000 * 360 * 1;
        } else if ( period == 3 ) {
            TIME_REQUEST = 1000 * 720 * 1;
        }

        if ( TIME_REQUEST != 0 && session.getBoolean("logged_in", false) ) {
            if ((ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
                    ||
                    (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(LocationAlarmService.this.getApplicationContext(),"서비스를 실행하려면 위치정보를 실행해 주세요.",Toast.LENGTH_SHORT).show();
            } else {
                final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, EventLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, EventLocationListener);

                final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

                requestThread = new RequestThread();
                requestThread.setDaemon(false);
                requestThread.start();
            }
        } else {
            if ( session.getBoolean("logged_in", false) ) {
                Toast.makeText(LocationAlarmService.this.getApplicationContext(),"설정에서 보호자 연락처를 입력해 주세요.",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LocationAlarmService.this.getApplicationContext(),"설정에서 전송 주기 선택해 주세요.",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        Log.v(TAG, "onDestroy:stop");
    }

    public class RequestThread extends Thread {
        public void run() {
            while (true) {
                if (session.getBoolean("logged_in", false)) {
                    if( 0 < lat && 0 < lng ) {
                        location_msg = "http://maps.googleapis.com/maps/api/staticmap?center="+lat+","+lng+"&zoom=15&size=600x300&maptype=roadmap&markers=color%3aorange%7Clabel%3aA%7C"+lat+","+lng+"&sensor=false";
                        final SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(session.getString("tel",""), null, location_msg, null, null);

                        // 같은 자리 위치 초기화
                        distance_flag = true;
                    }
                }
                try {
                    Thread.sleep(TIME_REQUEST);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private final LocationListener EventLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            Log.d(TAG, "onLocationChanged, location:" + location);
            /*
            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            */

            now_lat = location.getLatitude();
            now_lng = location.getLongitude();

            if ( session.getBoolean("fixation_sensor", false) ) {
                if (0 < lng && 0 < lng) {
                    distance = getDistance(lat, lng, lat, lng);
                    if (distance == 0 && distance_flag) {
                        distance_flag = false;
                        if ( 0 < lat && 0 < lng ) {
                            String msg = "같은 자리에 머물고 있습니다."+"("+location_msg+")";
                            Log.v(TAG, "같은 자리에 머물고 있다!! 메시지 보내기");
                            final SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(session.getString("tel", ""), null, msg, null, null);
                        }
                    }
                    Log.v(TAG, "distance ::: " + distance);
                }
            }

            lat = location.getLatitude();
            lng = location.getLongitude();

            if ( 0 < lat && 0 < lng ) {
                SharedPreferences.Editor sessionEditor = session.edit();
                sessionEditor.putString("lat", lat+"");
                sessionEditor.putString("lng", lng+"");
                sessionEditor.commit();
            }

            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            /*
            tv.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n고도 : " + altitude + "\n정확도 : "  + accuracy);
            */
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

    public double getDistance(double lat1 , double lng1 , double lat2 , double lng2 ){
        double distance;

        Location locationA = new Location("point A");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lng1);

        Location locationB = new Location("point B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lng2);

        distance = locationA.distanceTo(locationB);

        return distance;
    }

}
