package jp.techacademy.watanabe.shouta.autoslideshowapp;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Timer mTimer;

    Handler mHandler = new Handler();

    Button mNextButton;
    Button mBackButton;
    Button mPlayStopButton;
    ImageView mImageView;
    Cursor cursor;

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private boolean isAutoTimer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNextButton = findViewById(R.id.next_button);
        mBackButton = findViewById(R.id.back_button);
        mPlayStopButton = findViewById(R.id.playstop_button);
        mImageView =  findViewById(R.id.imageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            getContentsInfo();

        }
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextInfo();
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPreviousInfo();
            }
        });
        mPlayStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //再生時
                if (mTimer == null) {
                    mTimer = new Timer();
                    //「進む」と「戻る」をタップ不可
                    mNextButton.setEnabled(false);
                    mBackButton.setEnabled(false);
                    mPlayStopButton.setText("停止");
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getNextInfo();
                                }
                            });
                        }
                        //2秒毎にスライド
                    }, 2000, 2000);
                } else {
                    //停止時
                    mTimer.cancel();
                    mTimer = null;
                    mNextButton.setEnabled(true);
                    mBackButton.setEnabled(true);
                    mPlayStopButton.setText("再生");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }else{
                    Toast.makeText(this, "パーミッションが無許可です", Toast.LENGTH_SHORT).show();
                    mNextButton.setEnabled(false);
                    mBackButton.setEnabled(false);
                    mPlayStopButton.setEnabled(false);
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }

    private void getNextInfo() {
        //表示画像のループ
        if (!cursor.moveToNext()) {
            cursor.moveToFirst();
        }

        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }

    private void getPreviousInfo() {
        //表示画像のループ
        if (!cursor.moveToPrevious()) {
            cursor.moveToLast();
        }

        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }
}
