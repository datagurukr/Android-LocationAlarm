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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import service.LocationAlarmService;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = SettingActivity.class.getSimpleName();

    private Context context;
    private SharedPreferences session;

    private boolean backPressed = false;

    private EditText editTel;
    private Spinner spinnerPeriod;
    private Switch switchRunoutSensor, switchFixationSensor, switchSmsSend;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        context = getApplicationContext();
        session = getSharedPreferences("session", MODE_PRIVATE);

        editTel = (EditText) findViewById(R.id.editTel);
        spinnerPeriod = (Spinner) findViewById(R.id.spinnerPeriod);
        switchRunoutSensor = (Switch) findViewById(R.id.switchRunoutSensor);
        switchFixationSensor = (Switch) findViewById(R.id.switchFixationSensor);
        switchSmsSend = (Switch) findViewById(R.id.switchSmsSend);
        btnSave = (Button) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(EventHandler);

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
        editTel.setText(session.getString("tel",""));
        spinnerPeriod.setSelection(session.getInt("period",0));
        switchRunoutSensor.setChecked(session.getBoolean("runout_sensor",false));
        switchFixationSensor.setChecked(session.getBoolean("fixation_sensor",false));
        switchSmsSend.setChecked(session.getBoolean("sms_send",false));
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
        SetToolbarTitle(context.getString(R.string.lang_activity_setting));
    }

    private void Save () {
        SharedPreferences.Editor sessionEditor = session.edit();
        if ( editTel.getText().toString().length() != 0 ) {
            sessionEditor.putBoolean("logged_in", true);
        } else {
            sessionEditor.putBoolean("logged_in", false);
        }
        sessionEditor.putString("tel", editTel.getText().toString());
        sessionEditor.putInt("period", spinnerPeriod.getSelectedItemPosition());
        sessionEditor.putBoolean("runout_sensor", switchRunoutSensor.isChecked());
        sessionEditor.putBoolean("fixation_sensor", switchFixationSensor.isChecked());
        sessionEditor.putBoolean("sms_send", switchSmsSend.isChecked());
        sessionEditor.commit();
        Toast.makeText(context,"저장되었습니다.\n서비스를 종료 후 재실행 해주세요.",Toast.LENGTH_SHORT).show();
    }

    View.OnClickListener EventHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final View view = v;
            switch (v.getId()) {
                case R.id.btnSave:
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            SettingActivity.this);

                    // AlertDialog 셋팅
                    alertDialogBuilder
                            .setMessage("저장하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("저장",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            Save();
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
            }
        }
    };

}
