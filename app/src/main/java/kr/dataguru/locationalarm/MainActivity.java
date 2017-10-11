package kr.dataguru.locationalarm;

/**
 * Created by dataguru on 2017. 10. 12..
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST_CALL_PHONE = 0x99;
    public static final int PERMISSION_REQUEST_SMS_PHONE = 0x100;

    private Context context;
    private SharedPreferences session;
    private Intent intent;

    private boolean backPressed = false;

    private Button btnRun, btnCall, btnSms, btnSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        session = getSharedPreferences("session", MODE_PRIVATE);

        btnRun = (Button) findViewById(R.id.btnRun);
        btnCall = (Button) findViewById(R.id.btnCall);
        btnSms = (Button) findViewById(R.id.btnSms);
        btnSetting = (Button) findViewById(R.id.btnSetting);

        btnRun.setOnClickListener(EventHandler);
        btnCall.setOnClickListener(EventHandler);
        btnSms.setOnClickListener(EventHandler);
        btnSetting.setOnClickListener(EventHandler);
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
    }

    private void OnRequestPermission (View view) {
        switch (view.getId()) {
            case R.id.btnCall:
                if ((ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_CALL_PHONE);
                } else {
                }
                break;
            case R.id.btnSms:
                if ((ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.RECEIVE_SMS)
                                != PackageManager.PERMISSION_GRANTED)
                        ||
                        (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_PHONE_STATE)
                                != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE},
                            PERMISSION_REQUEST_SMS_PHONE);
                } else {

                }
                break;
        }
    }

    private void OnPhoneCall () {
        Uri uri = Uri.parse("tel:112"); //전화와 관련된 Data는 'Tel:'으로 시작. 이후는 전화번호
        intent = new Intent(Intent.ACTION_CALL,uri); //시스템 액티비티인 Dial Activity의 action값
        try {
            startActivity(intent);//액티비티 실행
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void OnPhoneSms () {
        Uri uri = Uri.parse("smsto:112"); //sms 문자와 관련된 Data는 'smsto:'로 시작. 이후는 문자를 받는 사람의 전화번호
        intent = new Intent(Intent.ACTION_SENDTO,uri); //시스템 액티비티인 SMS문자보내기 Activity의 action값
        intent.putExtra("sms_body", "Hello...");  //보낼 문자내용을 추가로 전송, key값은 반드시 'sms_body'
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
                case R.id.btnRun:
                    intent = new Intent(context, RunActivity.class);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                    break;
                case R.id.btnCall:
                case R.id.btnSms:
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            MainActivity.this);

                    String dialogMessageText = "";
                    String dialogConfirmText = "";

                    // 제목셋팅
                    if ( R.id.btnCall == view.getId() ) {
                        alertDialogBuilder.setTitle("112에 전화를 거시겠습니까?");
                        dialogConfirmText = "전화 걸기";
                        dialogMessageText = "112에 전화 걸기 화면으로 이동합니다.";
                    } else {
                        alertDialogBuilder.setTitle("112에 문자를 전송하시겠습니까?");
                        dialogConfirmText = "문자 보내기";
                        dialogMessageText = "112에 문자 보내기 화면으로 이동합니다.";
                    }

                    // AlertDialog 셋팅
                    alertDialogBuilder
                            .setMessage(dialogMessageText)
                            .setCancelable(false)
                            .setPositiveButton(dialogConfirmText,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            if ( R.id.btnCall == view.getId() ) {
                                                if ((ContextCompat.checkSelfPermission(MainActivity.this,
                                                        Manifest.permission.CALL_PHONE)
                                                        != PackageManager.PERMISSION_GRANTED)) {
                                                    OnRequestPermission(view);
                                                } else {
                                                    OnPhoneCall();
                                                }
                                            } else {
                                                if ((ContextCompat.checkSelfPermission(MainActivity.this,
                                                        Manifest.permission.SEND_SMS)
                                                        != PackageManager.PERMISSION_GRANTED)
                                                        ||
                                                        (ContextCompat.checkSelfPermission(MainActivity.this,
                                                                Manifest.permission.RECEIVE_SMS)
                                                                != PackageManager.PERMISSION_GRANTED)
                                                        ||
                                                        (ContextCompat.checkSelfPermission(MainActivity.this,
                                                                Manifest.permission.READ_PHONE_STATE)
                                                                != PackageManager.PERMISSION_GRANTED)) {
                                                    OnRequestPermission(view);
                                                } else {
                                                    OnPhoneSms();
                                                }
                                            }
                                        }
                                    })
                            .setNegativeButton("취소",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // 다이얼로그 생성
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // 다이얼로그 보여주기
                    alertDialog.show();
                    break;
                case R.id.btnSetting:
                    intent = new Intent(context, SettingActivity.class);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CALL_PHONE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    OnPhoneCall();
                } else {
                    Log.d("TAG", "Call Permission Not Granted 1");
                }
                break;
            case PERMISSION_REQUEST_SMS_PHONE:
                if ((grantResults.length > 0)
                        && (
                                grantResults[0] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED
                                &&
                                grantResults[2] == PackageManager.PERMISSION_GRANTED
                        )) {
                } else {
                    Log.d("TAG", "Call Permission Not Granted 2");
                }
                break;
            default:
                break;
        }
    }
}