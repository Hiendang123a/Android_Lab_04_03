package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    Button btnChoose, btnUpload;
    ImageView imageViewChoose, imageViewUpload;
    EditText editTextUserName;
    TextView textViewUsername;
    private Uri mUri;
    private ProgressDialog mProgressDialog;
    public static final int MY_REQUEST_CODE=100;
    public static final String TAG = MainActivity. class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AnhXa();
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Please wait upload...");
        btnUpload.setOnClickListener(v->{
            if(mUri!=null)
                UploadImage1();
        });


        btnChoose.setOnClickListener(v->{
            CheckPermission();
        });
    }

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storge_permissions_33;
        } else {
            p = storge_permissions;
        }
        return p;
    }
    private void CheckPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "Không cần cấp quyền trên Android phiên bản thấp");
            openGallery();
            return;
        }

        // Nếu đã có quyền, mở gallery ngay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Quyền đã được cấp (Android 13+)");
                openGallery();
            } else {
                Log.d(TAG, "Yêu cầu quyền READ_MEDIA_IMAGES cho Android 13+");
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, MY_REQUEST_CODE);
            }
        }
    }

    public static String[] storge_permissions = {
            android. Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android. Manifest.permission. READ_EXTERNAL_STORAGE

    };
    @RequiresApi(api = Build.VERSION_CODES. TIRAMISU)
    public static String[] storge_permissions_33 = {
            android. Manifest.permission. READ_MEDIA_IMAGES,
            android. Manifest.permission.READ_MEDIA_AUDIO,
            android. Manifest.permission.READ_MEDIA_VIDEO

    };

    //Hàm xử Lý Lấy ảnh
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        mActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //request code
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageViewChoose.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void AnhXa() {
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imageViewUpload = findViewById(R.id.imgMultipart);
        editTextUserName = findViewById(R.id.editUserName);
        textViewUsername = findViewById(R.id.tvUsername);
        imageViewChoose = findViewById(R.id.imgChoose);

    }


    public void UploadImage1() {
        mProgressDialog.show();
        GoogleDriveHelper googleDriveHelper = new GoogleDriveHelper(this);
        googleDriveHelper.uploadFileToSociety(mUri, "image/jpg", () -> {
            // Đảm bảo đoạn mã này chạy trên main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (googleDriveHelper.getFileID() != null) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("imageID", googleDriveHelper.getFileID());
                        mProgressDialog.dismiss();  // Đóng progress dialog
                        startActivity(intent);  // Mở activity mới
                    }
                }
            });
        });
    }
}