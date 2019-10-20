package com.example.textrecognition;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class results extends AppCompatActivity {
 DatabaseReference reff;
 TextView mTextView;
 RelativeLayout mLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        mTextView=(TextView)findViewById(R.id.t1);
        mLayout=(RelativeLayout)findViewById(R.id.relative);
        String result=getIntent().getStringExtra("result");
        String output=getIntent().getStringExtra("output");
       if(result.equals("0")) {
           mLayout.setBackgroundResource(R.drawable.ic_sentiment_satisfied_black_24dp);
          mTextView.setText(output);
       }else
       {
           mLayout.setBackgroundResource(R.drawable.ic_sentiment_dissatisfied_black_24dp);
       }

    }
}
