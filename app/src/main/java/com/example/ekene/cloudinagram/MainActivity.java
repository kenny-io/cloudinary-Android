package com.example.ekene.cloudinagram;

import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.Uploader;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.payload.FileNotFoundException;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int INTENT_REQUEST_CODE = 100;

    public static final String URL = "http://127.0.0.1:8000/images/upload/";

    private ImageButton imgBtn;
    private ProgressBar progressBar;
    private String mImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

    }

    private void initViews() {
        imgBtn = findViewById(R.id.imageBtn);
        progressBar = findViewById(R.id.progress_bar);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");

                try {
                    startActivityForResult(intent, INTENT_REQUEST_CODE);

                } catch (ActivityNotFoundException e) {

                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INTENT_REQUEST_CODE && data.getData()!= null) {

            if (resultCode == RESULT_OK ) {

                try {

                    InputStream is = getContentResolver().openInputStream(data.getData());

                    uploadImage(getBytes(is));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            Toast.makeText(this, "No data selected", Toast.LENGTH_SHORT).show();
        }
    }

    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();

        int buffSize = 1024;
        byte[] buff = new byte[buffSize];

        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }

        return byteBuff.toByteArray();
    }


    private void uploadImage(byte[] imageBytes) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);
        Call<ResponseBody> call = retrofitInterface.uploadImage(body);
        progressBar.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {

                    ResponseBody responseBody = response.body();
                    //Response responseBody = response.body();
                    //mImageUrl = URL + responseBody.getPath();
                    mImageUrl = URL + responseBody.source();
                    //Toast.makeText(MainActivity.this, "RESPONSE "+responseBody.getMessage(), Toast.LENGTH_SHORT).show();

                } else {

                    ResponseBody errorBody = response.errorBody();
                    Gson gson = new Gson();
                    try {
                        Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
                        Toast.makeText(MainActivity.this, "GSON " +errorResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "onFailure() is called", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " +t.getLocalizedMessage());
            }
        });
    }
}


//    private void initViews() {
//
//        progressBar = findViewById(R.id.progress_bar);
//        imgBtn = findViewById(R.id.imageBtn);
//
//        imgBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/jpeg");
//
//                try {
//                    startActivityForResult(intent, INTENT_REQUEST_CODE);
//
//                } catch (ActivityNotFoundException e) {
//
//                    e.printStackTrace();
//                }
//            }
//
//        });
//
//    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (requestCode == INTENT_REQUEST_CODE) {
//
//            if (resultCode == RESULT_OK) {
//
//                InputStream is = null;
//                try {
//                    is = getContentResolver().openInputStream(data.getData());
//                    uploadImage(getBytes(is));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, "" +e, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//            }
//        }
//
//
//    public byte[] getBytes(InputStream is) throws IOException {
//        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
//
//        int buffSize = 1024;
//        byte[] buff = new byte[buffSize];
//
//        int len = 0;
//        while ((len = is.read(buff)) != -1) {
//            byteBuff.write(buff, 0, len);
//        }
//
//        return byteBuff.toByteArray();
//    }
//
//    private void uploadImage(byte[] imageBytes) {
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
//        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);
//        Call<Response> call = retrofitInterface.uploadImage(body);
//        progressBar.setVisibility(View.VISIBLE);
//        call.enqueue(new Callback<Response>() {
//            @Override
//            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//
//                progressBar.setVisibility(View.GONE);
//
//                if (response.isSuccessful()) {
//
//                    Toast.makeText(MainActivity.this, "onResponse() is called "+response, Toast.LENGTH_SHORT).show();
//                    Response responseBody = response.body();
//                    mBtImageShow.setVisibility(View.VISIBLE);
//                    mImageUrl = URL + responseBody.getPath();
//
//                } else {
//
//                    Toast.makeText(MainActivity.this, "!response", Toast.LENGTH_SHORT).show();
//                    ResponseBody errorBody = response.errorBody();
//                    Gson gson = new Gson();
//
//                    try {
//
//                        Response errorResponse = gson.fromJson(String.valueOf(response), Response.class);
//                        Toast.makeText(MainActivity.this, "ERROR: "+ errorResponse.getMessage(), Toast.LENGTH_SHORT).show();
//
//                    } catch (IllegalStateException | JsonSyntaxException exception) {
//                        exception.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Response> call, Throwable t) {
//
//                progressBar.setVisibility(View.GONE);
//                Toast.makeText(MainActivity.this, "onFailure: "+t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "onFailure: "+t.getLocalizedMessage());
//            }
//        });
//    }

//    private ImageButton imgBtn;
//    private ImageView imgview;
//    private Button postBtn;
//    private static final int GALLERY_REQUEST_CODE = 2;
//    private String imagePath;
//    private Uri selectedImage;
//    private EditText postEdit;
//    private FirebaseDatabase database;
//    private DatabaseReference DBRef;
//    private ProgressBar progressBar;
//    private List<Cloudinagram>cloudinagramList;
//    private String mImageUrl="";
//    public static final String URL = "http://127.0.0.1:8000/images/upload/";
//    private static final int INTENT_REQUEST_CODE = 100;
//    public static final String TAG = MainActivity.class.getSimpleName();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        progressBar = findViewById(R.id.progress_bar);
//
//        MediaManager.init(this);
//        postEdit = findViewById(R.id.postMessage);
//        postBtn = findViewById(R.id.postBtn);
//        imgBtn = findViewById(R.id.imageBtn);
//        imgview = findViewById(R.id.imgview);
//        database = FirebaseDatabase.getInstance();
//        //cloudinagramList = new ArrayList<>();
//        DBRef = database.getReference();
//
//        imgBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/jpeg");
//                try {
//                    startActivityForResult(intent, INTENT_REQUEST_CODE);
//
//                } catch (ActivityNotFoundException e) {
//
//                    e.printStackTrace();
//                }
////
////                Intent galleryIntent = new Intent();
////                galleryIntent.setType("image/*");
////                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
////                startActivityForResult(Intent.createChooser(galleryIntent,
////                        "select image"), GALLERY_REQUEST_CODE);
//            }
//        });
//
//    }
//
// //       postBtn.setOnClickListener(new View.OnClickListener() {
// //           @Override
// //           public void onClick(View view) {
////                MediaManager.get()
////                        .upload(selectedImage)
////                        .option("resource_type", "image")
////                        .callback(new UploadCallback() {
////                            @Override
////                            public void onStart(String requestId) {
////                              final String  msg  = postEdit.getText().toString().trim();
////                                if (!TextUtils.isEmpty(msg)){
////
////                                    final DatabaseReference post_message = DBRef.push();
////
////                                    post_message.child("Post_Message").setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
////                                        @Override
////                                        public void onComplete(@NonNull final Task<Void> task) {
////                                            Toast.makeText(MainActivity.this, "Text Posted!!", Toast.LENGTH_SHORT).show();
////
////
////                                        }
////                                    });
////
////                                }
////
////                                progressBar.setVisibility(View.VISIBLE);
////                                Toast.makeText(MainActivity.this, "Uploading...", Toast.LENGTH_LONG).show();
////                            }
////
////                            @Override
////                            public void onProgress(String requestId, long bytes, long totalBytes) {
////
////                            }
////
////                            @Override
////                            public void onSuccess(String requestId, Map resultData) {
////                                progressBar.setVisibility(View.INVISIBLE);
////                                Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_LONG).show();
////                                imagePath = MediaManager.get().url().generate(resultData.get("public_id").toString().concat(".jpg"));
////                                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
////                                startActivity(intent);
////
////                            }
////
////                            @Override
////                            public void onError(String requestId, ErrorInfo error) {
////                                Toast.makeText(MainActivity.this, error.getDescription(), Toast.LENGTH_SHORT).show();
////                            }
////
////                            @Override
////                            public void onReschedule(String requestId, ErrorInfo error) {
////                                Toast.makeText(MainActivity.this, error.getDescription(), Toast.LENGTH_SHORT).show();
////                            }
////                        }).dispatch();
////
////            }
////        });
////
//// }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == INTENT_REQUEST_CODE && resultCode == RESULT_OK) {
//
//            if (data != null && data.getData() != null) {
//                selectedImage = data.getData();
//
//               String localImagePath = getRealPathFromURI(selectedImage);
//
//                Bitmap bitmap;
//                try {
//                    InputStream is = getContentResolver().openInputStream(data.getData());
//                    uploadImage(getBytes(is));
//                    //InputStream stream = getContentResolver().openInputStream(selectedImage);
//                    bitmap = BitmapFactory.decodeStream(is);
//                    imgBtn.setImageBitmap(bitmap);
//                    Uri uri = data.getData();
//                    Picasso.with(this).load(uri).into(imgview);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//                    }
//            else {
//                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
//            }
//
//        }
//
//    private String getRealPathFromURI(Uri contentUri) {
//
//        Cursor cursor = null;
//        try {
//            String[] projection = { MediaStore.Images.Media.DATA };
//            cursor = getContentResolver().query(contentUri,  projection, null, null, null);
//            @SuppressWarnings("ConstantConditions")
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            return  cursor.getString(column_index);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//
//    }
//    public byte[] getBytes(InputStream is) throws IOException {
//        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
//
//        int buffSize = 1024;
//        byte[] buff = new byte[buffSize];
//
//        int len = 0;
//        while ((len = is.read(buff)) != -1) {
//            byteBuff.write(buff, 0, len);
//        }
//
//        return byteBuff.toByteArray();
//    }
//
//
//    private void uploadImage(byte[] imageBytes) {
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);
//        Call<Response> call = retrofitInterface.uploadImage(body);
//        progressBar.setVisibility(View.VISIBLE);
//
//        call.enqueue(new Callback<Response>() {
//            @Override
//            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//
//                progressBar.setVisibility(View.GONE);
//                Toast.makeText(MainActivity.this, "onResponse ", Toast.LENGTH_SHORT).show();
//
//                if (response.isSuccessful()) {
//
//                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
//                    Response responseBody = response.body();
//                    //mBtImageShow.setVisibility(View.VISIBLE);
//                    mImageUrl = URL + responseBody.getPath();
//                    //Snackbar.make(findViewById(R.id.content), responseBody.getMessage(),Snackbar.LENGTH_SHORT).show();
//                    Toast.makeText(MainActivity.this, "MSG: " + responseBody.getMessage(), Toast.LENGTH_SHORT).show();
//
//                } else {
//
//                    ResponseBody errorBody = response.errorBody();
//                    Toast.makeText(MainActivity.this, "Not Successful", Toast.LENGTH_SHORT).show();
//                    Gson gson = new Gson();
//
//                    try {
//                        Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
//                        Toast.makeText(MainActivity.this, "MSG: "+ errorResponse.getMessage(), Toast.LENGTH_SHORT).show();
//                        //Snackbar.make(findViewById(R.id.content), errorResponse.getMessage(),Snackbar.LENGTH_SHORT).show();
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Response> call, Throwable t) {
//                progressBar.setVisibility(View.GONE);
//                Log.d(TAG, "onFailure: "+t.getLocalizedMessage());
//                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//

//}
