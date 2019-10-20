package com.example.textrecognition;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView mImageView;
    EditText mTextView;
    Button b1, b2, b3,b4;
    Context mContext;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;
    Uri imguri;
    DatabaseReference mDatabaseReference;
    DatabaseReference reff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.i);
        mTextView = (EditText) findViewById(R.id.t1);
        b1 = (Button) findViewById(R.id.b1);
        b2 = (Button) findViewById(R.id.b2);
        b3 = (Button) findViewById(R.id.b3);
        b4=(Button)findViewById(R.id.b4);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        reff=FirebaseDatabase.getInstance().getReference();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
load1();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    detecttxt();
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load();
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result1();
            }
        });
    }
    private void result1()
    {
        reff= FirebaseDatabase.getInstance().getReference();
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String result = dataSnapshot.child("result").getValue().toString();
                String output = dataSnapshot.child("output").getValue().toString();
                String correctOutput = "OUTPUT: " + output;
                Intent i = new Intent(MainActivity.this, results.class);
                i.putExtra("result", result);
                i.putExtra("output", correctOutput);
                startActivity(i);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }

        });
    }

            private void load()
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openfile();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9); //ask for permission
        }
    }
    private void load1() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.CAMERA}, 10); //ask for permission
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openfile();
        } else  if(requestCode==10&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            dispatchTakePictureIntent();

        } else{
            Toast.makeText(MainActivity.this, "Please Provide Permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void openfile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 10&& resultCode == RESULT_OK && data != null) {
            imguri = data.getData();
//            Bundle extras=data.getExtras();
            try {
                imageBitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imguri);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            Picasso.get().load(imguri).into(imageView);
            mImageView.setImageBitmap(imageBitmap);
//            mImageView.setImageURI(imguri);        //Another Way to display image on a ImageView
        }else
//            if(requestCode == 9&&resultCode==RESULT_OK&&data!=null)
        {
//             Toast.makeText(MainActivity.this,"IDHAR",Toast.LENGTH_SHORT).show();
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
//            Picasso.get().load().into(mImageView);
            mImageView.setImageBitmap(imageBitmap);
        }
    }
   String one,two;
    private void detecttxt()  {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);//instance of firebase instance image
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processtxt(firebaseVisionText);
//                reff.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        one=dataSnapshot.child("text").getValue().toString();
//                        two=mTextView.getText().toString();
//                        if(!two.equals(one))
//                            mDatabaseReference.child("text").setValue(two);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void processtxt(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blocks = firebaseVisionText.getBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No Text", Toast.LENGTH_SHORT).show();
            return;
        }
        String txt="";
        for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
            for(FirebaseVisionText.Line line:block.getLines())
            {
                 txt+= line.getText();
            }
                txt+="\n";
            mTextView.setTextSize(15);
            mTextView.setText(txt);
            mDatabaseReference.child("text").setValue(txt);
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
