package com.example.uploadimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button btnChoose, btnUpload;
    private ImageView imgPrev;
    private EditText etImageName, etSortImage;
    private LinearLayout list;
    private ImageButton ib_search;
    private final int REQUEST_CODE_PICK_IMAGE = 100;
    private Uri filePath;

    //Firebase Storage
    private FirebaseStorage storage;
    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imgPrev = findViewById(R.id.imgPrev);
        list = findViewById(R.id.linearScroll);
        etImageName = findViewById(R.id.etImageName);
        etSortImage = findViewById(R.id.etSortImage);
        ib_search = findViewById(R.id.ib_search);

        // Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        ib_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadList();
            }
        });
        DownloadList();

    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a Pictures"), REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgPrev.setImageBitmap(bitmap);
                imgPrev.setBackgroundColor(Color.parseColor("#37131313"));
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (filePath != null){
            String fileName = etImageName.getText().toString().trim();

            if(fileName.isEmpty() ){
                Toast.makeText(this, "Missing name", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            SimpleDateFormat sdf = new SimpleDateFormat("dd|MM|yy--HH:mm:ss->", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            StorageReference ref = storageReference.child("images/" + currentDateandTime + fileName);
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    etImageName.setText("");
                    imgPrev.setImageResource(R.drawable.image_search_logo);
                    imgPrev.setBackgroundColor(Color.parseColor("#37E4E4E4"));
                    filePath = null;
                    Toast.makeText(MainActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    int progress = (int) (100 * (float) snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Upload:\t" + progress + " %");
                }
            });
        } else{
            Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show();
        }
    }

    public void DownloadList(){
        list.removeAllViews();
        StorageReference ref = storageReference.child("images/");
            ref.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                String sortUserInput = etSortImage.getText().toString().trim();
                for (StorageReference item: listResult.getItems()){
                    item.getBytes(5000000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            if (item.getName().substring((item.getName().indexOf("->"))+2).indexOf(sortUserInput) != -1){

                            TextView tv = new TextView(MainActivity.this);
                            tv.setText(item.getName().substring((item.getName().indexOf("->"))+2));
                            tv.setGravity(Gravity.CENTER);
                            tv.setTextSize(20);
                            list.addView(tv);

                            ImageView iv = new ImageView(MainActivity.this);
                            iv.setImageBitmap(bitmap);
                            list.addView(iv);

                            TextView tvSpace = new TextView(MainActivity.this);
                            tvSpace.setText("\n\n");
                            list.addView(tvSpace);

                            Toast.makeText(MainActivity.this, "sort successfully", Toast.LENGTH_SHORT).show();

                        }
                        }
                    });
                };
            }
        });

    }
    }