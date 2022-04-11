package com.example.vvuexampermitapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class BluetoothReaderTestActivity extends AppCompatActivity{

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothReader  mBluetoothReader =null;
    private String btAddress="";
    private boolean bAutoLink=true;
    private View view;

    private TextView mTitle;
    private ListView mConversationView;
    private ImageView fingerprintImage;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private FirebaseAuth mAuth;

    public byte mRefData[]=new byte[512];
    public int mRefSize=0;
    public byte mMatData[]=new byte[512];
    public int mMatSize=0;

    public byte mCardSn[]=new byte[4];
    public byte mCardData[]=new byte[4096];
    public int mCardSize=0;
    public byte mBat[]=new byte[2];
    public byte mVersion[]=new byte[8];
    public long mTimeStart,mTimeEnd;

    public byte mFlashData[]=new byte[32768];

    private TextInputLayout editText1;

    private Timer linkTimer;
    private TimerTask linkTask;
    private ImageView logout;
    Handler linkHandler;

    private DatabaseReference reference;

    private StudentClass studentClass;

    private DialogFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_reader_test);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        btAddress =sp.getString("device","");
        //InitViews();
        mAuth = FirebaseAuth.getInstance();
        logout = (ImageView) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(BluetoothReaderTestActivity.this,Login.class);
                startActivity(intent);

            }
        });

        view = (LinearLayout)findViewById(R.id.lin);

       // inserting into database
        reference = FirebaseDatabase.getInstance().getReference("student");
        String key = reference.push().getKey();

        //dummy data delete later
        //studentClass = new StudentClass("Richard","Owusu","4375","01/01/1990","Male","Bachelor of Science","Computer Science","4.00","Ghanaian"," ",false,key);
        //studentClass = new StudentClass("Mavis","Narh","1365","01/02/1993","Female","Bachelor of Science","Nursing","0.00","Ghanaian"," ",false,key);
        //studentClass = new StudentClass("Patrick","Dzah","5733","01/05/1997","Male","Bachelor of Science","Information Technology","500.00","Ghanaian"," ",false,key);
        //studentClass = new StudentClass("Rose","Ofosu","7478","01/09/1996","Female","Bachelor of Arts","Sociology","1400.00","Ghanaian"," ",false,key);
        //studentClass = new StudentClass("Desmond","Tutu","1140","01/11/1998","Male","Bachelor of Science","Computer Science","0.00","Ghanaian"," ",false,key);


//        reference.child(key).setValue(studentClass).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void unused) {
//                // if successful
//                //do nothing
//
//            }
//        });



    }

    public void LinkStart(){
        linkTimer = new Timer();
        linkHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                linkStop();

                if(btAddress.length()>=12){
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btAddress);
                    mBluetoothReader.connect(device);
                }

                super.handleMessage(msg);
            }
        };
        linkTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                linkHandler.sendMessage(message);
            }
        };
        linkTimer.schedule(linkTask, 1000, 1000);
    }

    public void linkStop(){
        if (linkTimer!=null){
            linkTimer.cancel();
            linkTimer = null;
            linkTask.cancel();
            linkTask=null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothDeviceListActivity.REQUEST_ENABLE_BT);
        }else{
            if(mBluetoothReader==null){
                InitViews();
                mBluetoothReader = new BluetoothReader(this, mHandler);
                //mBluetoothReader.InitMatch();
                AddStatusList(btAddress);
                if(bAutoLink){
                    LinkStart();
                }
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mBluetoothReader != null) {
            if (mBluetoothReader.getState() == mBluetoothReader.STATE_NONE) {
                mBluetoothReader.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothReader != null)
            mBluetoothReader.stop();
    }

    public void LinkBluetoothReader(){
        Intent serverIntent = new Intent(BluetoothReaderTestActivity.this, BluetoothDeviceListActivity.class);
        startActivityForResult(serverIntent, BluetoothDeviceListActivity.REQUEST_CONNECT_DEVICE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BluetoothDeviceListActivity.REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mBluetoothReader.connect(device);

                    btAddress = address;

                    SharedPreferences sp;
                    sp = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("device", btAddress);
                    edit.commit();
                }
                break;
            case BluetoothDeviceListActivity.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    if (mBluetoothReader == null)
                        InitViews();
                    mBluetoothReader = new BluetoothReader(this, mHandler);
                    //mBluetoothReader.InitMatch();

                    if (bAutoLink) {
                        if (btAddress.length() >= 12) {
                            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btAddress);
                            mBluetoothReader.connect(device);
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    void InitViews(){
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        fingerprintImage=(ImageView)findViewById(R.id.imageView1);
        mTitle=(TextView)findViewById(R.id.textView1);
        editText1=(TextInputLayout)findViewById(R.id.editText1);
        editText1.getEditText().setText("1");

        final ImageView btnLinkDevice = (ImageView) findViewById(R.id.btnLinkDevice);
        btnLinkDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinkBluetoothReader();
            }
        });

//        final Button btnEnrolToHost = (Button) findViewById(R.id.btnEnrolToHost);
//        btnEnrolToHost.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mBluetoothReader.EnrolToHost();
//                AddStatusList("Place Finger (Twice) ...");
//            }
//        });

//        final Button btnCaptureToHost = (Button) findViewById(R.id.btnCaptureToHost);
//        btnCaptureToHost.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                AddStatusList("Place Finger ...");
//                mBluetoothReader.CaptureToHost();
//            }
//        });

//        final Button btbMatcTemplate = (Button) findViewById(R.id.btbMatcTemplate);
//        btbMatcTemplate.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                //Match In Device
//                //mBluetoothReader.MatchInDevice(mRefData,mMatData);
//                //Match In System
//                int score=mBluetoothReader.MatchTemplate(mRefData,mMatData);
//                AddStatusList("Match Score:"+String.valueOf(score));
//            }
//        });

//        final Button btnGetImage = (Button) findViewById(R.id.btnGetImage);
//        btnGetImage.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                AddStatusList("Place Finger ...");
//                mBluetoothReader.GetImageAndDate();
//                mTimeStart= SystemClock.uptimeMillis();
//            }
//        });

        final Button btnEnrolInModule = (Button) findViewById(R.id.btnEnrolInModule);
        btnEnrolInModule.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int id=Integer.parseInt(editText1.getEditText().getText().toString());
                if((id>0)&&(id<=1000)){
                    AddStatusList("Place Finger (Twice) ...");
                    mBluetoothReader.EnrolInModule(id);
                }
            }
        });

        final Button btnVerifyInModule = (Button) findViewById(R.id.btnVerifyInModule);
        btnVerifyInModule.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int id=Integer.parseInt(editText1.getEditText().getText().toString());
                if((id>0)&&(id<=1000)){
                    AddStatusList("Place Finger ...");
                    mBluetoothReader.VerifyInModule(id);
                }
            }
        });

        final Button btnIdentifyInModule = (Button) findViewById(R.id.btnIdentifyInModule);
        btnIdentifyInModule.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AddStatusList("Place Finger ...");
                mBluetoothReader.SearchInModule();
            }
        });


        final Button btnDeleteInModule = (Button) findViewById(R.id.btnDeleteInModule);
        btnDeleteInModule.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int id=Integer.parseInt(editText1.getEditText().getText().toString());
                if((id>0)&&(id<=1000)){
                    mBluetoothReader.DeleteInModule(id);
                }
            }
        });

//        final Button btnClearInModule = (Button) findViewById(R.id.btnClearInModule);
//        btnClearInModule.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mBluetoothReader.ClearModule();
//            }
//        });

//        final Button btnGetSN = (Button) findViewById(R.id.btnGetSN);
//        btnGetSN.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mBluetoothReader.GetDeviceSn();
//            }
//        });

//        final Button btnGetBatVal = (Button) findViewById(R.id.btnGetBatVal);
//        btnGetBatVal.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mBluetoothReader.GetBatVal();
//            }
//        });

//        final Button btnCloseDevice = (Button) findViewById(R.id.btnCloseDevice);
//        btnCloseDevice.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mBluetoothReader.CloseDevvice();
//            }
//        });

//        final Button btnGetVersion = (Button) findViewById(R.id.btnGetVersion);
//        btnGetVersion.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mBluetoothReader.GetDeviceVer();
//            }
//        });

//        final Button btnCardTest = (Button) findViewById(R.id.btnCardTest);
//        btnCardTest.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if(mBluetoothReader.mDeviceInfo.IsSupportCard()){
//                    mBluetoothReader.CardGetSN();
//                }else
//                    AddStatusList("No Support Smart Card");
//            }
//        });

//        final Button btnFlashTest = (Button) findViewById(R.id.btnFlashTest);
//        btnFlashTest.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if(mBluetoothReader.mDeviceInfo.IsSupportFlash()){
//                    mBluetoothReader.GetFlashID();
//                    //mBluetoothReader.ReadFlashData(0,2048);
//            		/*
//            		for(int i=0;i<2048;i++)
//            			mFlashData[i]=0x50;
//            		mBluetoothReader.WriteFlashData(0,2048,mFlashData);
//            		*/
//                }else
//                    AddStatusList("No Support Flash Memory");
//            }
//        });
    }

    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothReader.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothReader.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothReader.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothReader.STATE_LISTEN:
                        case BluetoothReader.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case BluetoothReader.MESSAGE_WRITE:
                    //byte[] writeBuf = (byte[]) msg.obj;
                    //AddStatusListHex(writeBuf,writeBuf.length);
                    break;
                case BluetoothReader.MESSAGE_READ:
                    //byte[] readBuf = (byte[]) msg.obj;
                    //AddStatusList("Read Len="+Integer.toString(msg.arg1));
                    //AddStatusListHex(readBuf,msg.arg1);
                    //AddLogListHex(readBuf,msg.arg1);
                    break;
                case BluetoothReader.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothReader.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothReader.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothReader.TOAST),Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothReader.CMD_GETDEVTYPE:
                    if(msg.arg1==1){
                        //AddStatusList("Device Type:"+Integer.toHexString(msg.arg2));
                        AddStatusList("Device Type:"+String.format("%08X",msg.arg2));
                    }else
                        AddStatusList("Device Type:Read Fail");
                    break;

                case BluetoothReader.CMD_GETDEVINFO:
                    if(msg.arg1==1){
                        AddStatusList("Device Type:"+String.format("%08X",mBluetoothReader.mDeviceInfo.DeviceType));

                        //String sn = "";
                        //for(int i=0;i<8;i++)
                        //	sn=sn+String.format("%02X",mBluetoothReader.mDeviceInfo.DeviceSN[i]);
                        //AddStatusList("Device SN:"+sn);
                        String sn=new String(mBluetoothReader.mDeviceInfo.DeviceSN);
                        AddStatusList("Device SN:"+sn);

                        AddStatusList("Device Version:"+String.valueOf(mBluetoothReader.mDeviceInfo.Version));
                        AddStatusList("Device Sensor:"+String.valueOf(mBluetoothReader.mDeviceInfo.SensorType));

                    }else
                        AddStatusList("Get Device Info Fail");
                    break;

                case BluetoothReader.CMD_GETSTDIMAGE:
                    if(msg.arg1==1){
                        byte[] bmpdata = null;
                        switch(msg.arg2){
                            case BluetoothReader.IMAGESIZE_152_200:
                                bmpdata=mBluetoothReader.getFingerprintImage((byte[]) msg.obj,152,200,0);
                                break;
                            case BluetoothReader.IMAGESIZE_256_288:
                                bmpdata=mBluetoothReader.getFingerprintImage((byte[]) msg.obj,256,288,0);
                                break;
                            case BluetoothReader.IMAGESIZE_256_360:
                                bmpdata=mBluetoothReader.getFingerprintImage((byte[]) msg.obj,256,360,0);
                                break;
                        }
                        Bitmap image = BitmapFactory.decodeByteArray(bmpdata, 0,bmpdata.length);
                        fingerprintImage.setImageBitmap(image);
                        mTimeEnd=SystemClock.uptimeMillis();
                        AddStatusList("Display Image:"+String.valueOf((mTimeEnd-mTimeStart))+"ms");
                    }else{
                    }
                    break;
                case BluetoothReader.CMD_GETRESIMAGE:
                    if(msg.arg1==1){
                        byte[] bmpdata = null;
                        switch(msg.arg2){
                            case BluetoothReader.IMAGESIZE_152_200:
                                bmpdata=mBluetoothReader.getFingerprintImage((byte[]) msg.obj,152,200,0);
                                break;
                            case BluetoothReader.IMAGESIZE_256_288:
                                bmpdata=mBluetoothReader.getFingerprintImage((byte[]) msg.obj,256,288,0);
                                break;
                            case BluetoothReader.IMAGESIZE_256_360:
                                bmpdata=mBluetoothReader.getFingerprintImage((byte[]) msg.obj,256,360,0);
                                break;
                        }
                        Bitmap image = BitmapFactory.decodeByteArray(bmpdata, 0,bmpdata.length);
                        fingerprintImage.setImageBitmap(image);
                        mTimeEnd=SystemClock.uptimeMillis();
                        AddStatusList("Display Image:"+String.valueOf((mTimeEnd-mTimeStart))+"ms");
                    }else{
                    }
                    break;
                case BluetoothReader.CMD_ENROLID:
                    if(msg.arg1==1) {
                        AddStatusList("Enrol Succeed:"+String.valueOf(msg.arg2));
                    }else
                        AddStatusList("Enrol Fail");
                    break;
                case BluetoothReader.CMD_VERIFY:
                    if(msg.arg1==1){
                        AddStatusList("Verify Succeed");
                        //where we add the database code
                        Query query = FirebaseDatabase.getInstance().getReference("student").orderByChild("studentid").equalTo(editText1.getEditText().getText().toString());
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren() ){
                                    studentClass = ds.getValue(StudentClass.class);
                                }

                                if (studentClass!=null){
                                    //calling dialogfragment
                                    Bundle bundle = new Bundle();
                                    bundle.putString("fname",studentClass.getFirstname());
                                    bundle.putString("lname",studentClass.getLastname());
                                    bundle.putString("id",studentClass.getStudentid());
                                    bundle.putString("dob",studentClass.getDob());
                                    bundle.putString("gender",studentClass.getGender());
                                    bundle.putString("degree",studentClass.getDegree());
                                    bundle.putString("major",studentClass.getMajor());
                                    bundle.putString("fee",studentClass.getFeeBalance());
                                    bundle.putString("nationality",studentClass.getNationality());
                                    bundle.putString("seatnum",studentClass.getSeatnumber());
                                    bundle.putString("keyid",studentClass.getKeyid());

                                    dialogFragment = StudentBSD.newInstance();
                                    dialogFragment.setArguments(bundle);
                                    dialogFragment.show(getSupportFragmentManager(),"tagg");

                                }else {
                                    Snackbar.make(view,"No user for"+editText1.getEditText().getText().toString(),Snackbar.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    } else
                        AddStatusList("Verify Fail");
                    break;
                case BluetoothReader.CMD_IDENTIFY:
                    if(msg.arg1==1)
                        AddStatusList("Search Result:"+String.valueOf(msg.arg2));
                    else
                        AddStatusList("Search Fail");
                    break;
                case BluetoothReader.CMD_DELETEID:
                    if(msg.arg1==1)
                        AddStatusList("Delete Succeed");
                    else
                        AddStatusList("Delete Fail");
                    break;
                case BluetoothReader.CMD_CLEARID:
                    if(msg.arg1==1)
                        AddStatusList("Clear Succeed");
                    else
                        AddStatusList("Clear Fail");
                    break;
                case BluetoothReader.CMD_ENROLHOST:
                    if(msg.arg1==1){
                        //byte[] readBuf = (byte[]) msg.obj;
                        mRefSize=msg.arg2;
                        mBluetoothReader.memcpy(mRefData,0, (byte[])msg.obj,0,msg.arg2);
                        AddStatusList("Enrol Succeed");
                        AddStatusListHex(mRefData,512);
                    }else
                        AddStatusList("Enrol Fail");
                    break;
                case BluetoothReader.CMD_CAPTUREHOST:
                    if(msg.arg1==1){
                        //byte[] readBuf = (byte[]) msg.obj;
                        mMatSize=msg.arg2;
                        mBluetoothReader.memcpy(mMatData,0, (byte[]) msg.obj,0,msg.arg2);
                        AddStatusList("Capture Succeed");
                        AddStatusListHex(mMatData,256);
                    }else
                        AddStatusList("Capture Fail");
                    break;
                case BluetoothReader.CMD_MATCH:
                    if(msg.arg1==1)
                        AddStatusList("Match Succeed:"+String.valueOf(msg.arg2));
                    else
                        AddStatusList("Match Fail");
                    break;
                case BluetoothReader.CMD_CARD_GETSN:
                    if(msg.arg1==1){
                        mBluetoothReader.memcpy(mCardSn,0,(byte[]) msg.obj,0,msg.arg2);
                        AddStatusList("Read Card SN Succeed:"+Integer.toHexString(mCardSn[0]&0xFF)+Integer.toHexString(mCardSn[1]&0xFF)+Integer.toHexString(mCardSn[2]&0xFF)+Integer.toHexString(mCardSn[3]&0xFF));
                    }else
                        AddStatusList("Read Card SN Fail");
                    break;
                case BluetoothReader.CMD_GETSN:
                    if(msg.arg1==1){
                        String sn = "";
                        for(int i=0;i<msg.arg2;i++)
                            sn=sn+String.format("%02X",((byte[])msg.obj)[i]);
                        AddStatusList("SN:"+sn);
                    }else
                        AddStatusList("Get SN Fail");
                    break;
                case BluetoothReader.CMD_GETBAT:
                    if(msg.arg1==1){
                        mBluetoothReader.memcpy(mBat,0,(byte[]) msg.obj,0,msg.arg2);
                        AddStatusList("Battery Value:"+Integer.toString(mBat[0]/10)+"."+Integer.toString(mBat[0]%10)+"V");
                    }else
                        AddStatusList("Get Battery Value Fail");
                    break;
                case BluetoothReader.CMD_GETVERSION: {
                    if(msg.arg1==1){
                        mBluetoothReader.memcpy(mVersion,0,(byte[]) msg.obj,0,msg.arg2);
                        AddStatusList("Ver:"+String.valueOf(mVersion[0]));
                    } else
                        AddStatusList("Fail");
                }
                break;
                case BluetoothReader.CMD_SHUTDOWNDEVICE: {
                    if(msg.arg1==1){
                        AddStatusList("Shutdown Device Succeed");
                    } else
                        AddStatusList("Shutdown Device  Fail");
                }
                break;
                case BluetoothReader.CMD_GETSTDCHAR:
                case BluetoothReader.CMD_GETRESCHAR:
                    if(msg.arg1==1){
                        mBluetoothReader.memcpy(mMatData,0,(byte[]) msg.obj,0,msg.arg2);
                        mMatSize=msg.arg2;
                        AddStatusList("Len="+String.valueOf(mMatSize));
                        AddStatusList("Get Data Succeed");
                        AddStatusListHex(mMatData,mMatSize);
                    }
                    else
                        AddStatusList("Get Data Fail");
                    break;
                case BluetoothReader.CMD_FLASHGETID:
                    if(msg.arg1==1){
                        byte[] fid=(byte[])(byte[]) msg.obj;
                        String sn = "";
                        for(int i=0;i<msg.arg2;i++)
                            sn=sn+String.format("%02X",fid[i]);
                        AddStatusList("Flash ID:"+sn);
                    }else
                        AddStatusList("Flash ID:Read Fail");
                    break;
                case BluetoothReader.CMD_FLASHREAD:{
                    mBluetoothReader.memcpy(mFlashData,0,(byte[]) msg.obj,0,msg.arg2);
                    String sn = "["+String.valueOf(msg.arg2)+"]";
                    //for(int i=0;i<msg.arg2;i++)
                    //	sn=sn+String.format("%02X",mFlashData[i]);
                    AddStatusList("Data:"+sn);
                    AddStatusList("Read Flash OK");
                }
                break;
                case BluetoothReader.CMD_FLASHWRITE:{
                    AddStatusList("Write Flash OK");
                }
                break;
            }
        }
    };

    private void AddStatusList(String text) {
        mConversationArrayAdapter.add(text);
    }

    private void AddStatusListHex(byte[] data,int size) {
        String text="";
        for(int i=0;i<size;i++) {
            text=text+","+Integer.toHexString(data[i]&0xFF).toUpperCase();
        }
        mConversationArrayAdapter.add(text);
    }

}