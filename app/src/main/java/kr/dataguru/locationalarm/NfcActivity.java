package kr.dataguru.locationalarm;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * Created by dataguru on 2017. 10. 17..
 */

public class NfcActivity  extends AppCompatActivity {
    private static final String TAG = NfcActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST_SMS_PHONE = 0x100;

    private Context context;
    private SharedPreferences session;
    private Intent intent;

    private boolean backPressed = false;

    private Button btnSubmit, btnNfc;
    private EditText editTaxiNumber, editTaxiDriverName, editTaxiGetOnTime, editTaxiGetOffTime;

    NfcAdapter mNfcAdapter; // NFC 어댑터
    PendingIntent mPendingIntent; // 수신받은 데이터가 저장된 인텐트
    IntentFilter[] mIntentFilters; // 인텐트 필터
    String[][] mNFCTechLists;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        context = getApplicationContext();
        session = getSharedPreferences("session", MODE_PRIVATE);

        editTaxiNumber = (EditText) findViewById(R.id.editTaxiNumber);
        editTaxiDriverName = (EditText) findViewById(R.id.editTaxiDriverName);
        editTaxiGetOnTime = (EditText) findViewById(R.id.editTaxiGetOnTime);
        editTaxiGetOffTime = (EditText) findViewById(R.id.editTaxiGetOffTime);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnNfc = (Button) findViewById(R.id.btnNfc);

        btnSubmit.setOnClickListener(EventHandler);
        btnNfc.setOnClickListener(EventHandler);

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
        readNfc();
    }

    public void readNfc () {
        // 앱이 실행될때 NFC 어댑터를 활성화 한다
        if( mNfcAdapter != null ) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);
        }

        // NFC 태그 스캔으로 앱이 자동 실행되었을때
        if( NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()) ) {
            // 인텐트에 포함된 정보를 분석해서 화면에 표시
            onNewIntent(getIntent());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();

        // 앱이 종료될때 NFC 어댑터를 비활성화 한다
        if( mNfcAdapter != null )
            mNfcAdapter.disableForegroundDispatch(this);

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
        // NFC 어댑터를 구한다
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // NFC 어댑터가 null 이라면 칩이 존재하지 않는 것으로 간주
        if( mNfcAdapter == null ) {
            // NFC 를 지원하지 않는경우
            Toast.makeText(context, "This phone is not NFC enable.", Toast.LENGTH_SHORT).show();
            return;
        }

        editTaxiDriverName.setText("Scan a NFC tag");

        // NFC 데이터 활성화에 필요한 인텐트를 생성
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // NFC 데이터 활성화에 필요한 인텐트 필터를 생성
        IntentFilter iFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            iFilter.addDataType("*/*");
            mIntentFilters = new IntentFilter[] { iFilter };
        } catch (Exception e) {
            editTaxiDriverName.setText("Make IntentFilter error");
        }
        mNFCTechLists = new String[][] { new String[] { NfcF.class.getName() } };
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
        SetToolbarTitle(context.getString(R.string.lang_activity_nfc));
    }

    private void OnRequestPermission (View view) {
        if ((ContextCompat.checkSelfPermission(NfcActivity.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(NfcActivity.this,
                        Manifest.permission.RECEIVE_SMS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(NfcActivity.this,
                        Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(NfcActivity.this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_REQUEST_SMS_PHONE);
        } else {

        }
    }

    private void OnPhoneSms () {
        if ( session.getBoolean("logged_in",false) ) {
            Uri uri = Uri.parse("smsto:"+session.getString("tel","112")); //sms 문자와 관련된 Data는 'smsto:'로 시작. 이후는 문자를 받는 사람의 전화번호
            intent = new Intent(Intent.ACTION_SENDTO,uri); //시스템 액티비티인 SMS문자보내기 Activity의 action값


            String msg = "";
            msg = "택시 번호 : "+editTaxiNumber.getText().toString()+",택시 기사님 성함 : "+editTaxiDriverName.getText().toString()+",택시 탑승 시각 : "+editTaxiGetOnTime.getText().toString()+",택시 하차 시각 : "+editTaxiGetOffTime;

            intent.putExtra("sms_body", msg);  //보낼 문자내용을 추가로 전송, key값은 반드시 'sms_body'
            try {
                startActivity(intent);//액티비티 실행
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context,"설정에서 보호자 연락처를 입력해 주세요.",Toast.LENGTH_SHORT).show();
        }
    }

    View.OnClickListener EventHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final View view = v;
            switch (v.getId()) {
                case R.id.btnSubmit:
                    if ( !editTaxiNumber.getText().toString().equals("") ) {
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                NfcActivity.this);

                        String dialogMessageText = "";
                        String dialogConfirmText = "";

                        alertDialogBuilder.setTitle("보호자에게 문자를 전송하시겠습니까?");
                        dialogConfirmText = "문자 보내기";
                        dialogMessageText = "보보자에게 문자 보내기 화면으로 이동합니다.";

                        // AlertDialog 셋팅
                        alertDialogBuilder
                                .setMessage(dialogMessageText)
                                .setCancelable(false)
                                .setPositiveButton(dialogConfirmText,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                DialogInterface dialog, int id) {
                                                if ((ContextCompat.checkSelfPermission(NfcActivity.this,
                                                        Manifest.permission.SEND_SMS)
                                                        != PackageManager.PERMISSION_GRANTED)
                                                        ||
                                                        (ContextCompat.checkSelfPermission(NfcActivity.this,
                                                                Manifest.permission.RECEIVE_SMS)
                                                                != PackageManager.PERMISSION_GRANTED)
                                                        ||
                                                        (ContextCompat.checkSelfPermission(NfcActivity.this,
                                                                Manifest.permission.READ_PHONE_STATE)
                                                                != PackageManager.PERMISSION_GRANTED)) {
                                                    OnRequestPermission(view);
                                                } else {
                                                    OnPhoneSms();
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

                    } else {
                        Toast.makeText(context,"NFC태그를 스캔해 주세요.",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnNfc:
                    readNfc();

                    editTaxiNumber.setText("서울 31 사 9345");
                    editTaxiDriverName.setText("황민관");
                    editTaxiGetOnTime.setText("06:33");
                    editTaxiGetOffTime.setText("06:49");

                    break;
            }
        }
    };

    // NFC 태그 정보 수신 함수. 인텐트에 포함된 정보를 분석해서 화면에 표시
    @Override
    public void onNewIntent(Intent intent) {
        // 인텐트에서 액션을 추출
        String action = intent.getAction();
        // 인텐트에서 태그 정보 추출
        String tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG).toString();
        String strMsg = action + "\n\n" + tag;
        // 액션 정보와 태그 정보를 화면에 출력
        editTaxiDriverName.setText(strMsg);

        // 인텐트에서 NDEF 메시지 배열을 구한다
        Parcelable[] messages = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(messages == null) return;

        for(int i=0; i < messages.length; i++)
            // NDEF 메시지를 화면에 출력
            showMsg((NdefMessage)messages[i]);
    }

    // NDEF 메시지를 화면에 출력
    public void showMsg(NdefMessage mMessage) {
        String strMsg = "", strRec="";
        // NDEF 메시지에서 NDEF 레코드 배열을 구한다
        NdefRecord[] recs = mMessage.getRecords();
        for (int i = 0; i < recs.length; i++) {
            // 개별 레코드 데이터를 구한다
            NdefRecord record = recs[i];
            byte[] payload = record.getPayload();
            // 레코드 데이터 종류가 텍스트 일때
            if( Arrays.equals(record.getType(), NdefRecord.RTD_TEXT) ) {
                // 버퍼 데이터를 인코딩 변환
                strRec = byteDecoding(payload);
                strRec = "Text: " + strRec;
            }
            // 레코드 데이터 종류가 URI 일때
            else if( Arrays.equals(record.getType(), NdefRecord.RTD_URI) ) {
                strRec = new String(payload, 0, payload.length);
                strRec = "URI: " + strRec;
            }
            strMsg += ("\n\nNdefRecord[" + i + "]:\n" + strRec);
        }

        editTaxiNumber.append(strMsg);
        editTaxiDriverName.append("OK");
    }

    // 버퍼 데이터를 디코딩해서 String 으로 변환
    public String byteDecoding(byte[] buf) {
        String strText="";
        String textEncoding = ((buf[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
        int langCodeLen = buf[0] & 0077;

        try {
            strText = new String(buf, langCodeLen + 1,
                    buf.length - langCodeLen - 1, textEncoding);
        } catch(Exception e) {
            Log.d("tag1", e.toString());
        }
        return strText;
    }

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