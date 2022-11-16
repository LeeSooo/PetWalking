package com.example.petwalking;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DiaryUpdate1Activity extends AppCompatActivity {
    private TextView tv_date, tv_time;
    private EditText et_content, et_title;
    private Button mBtnOk, mUploadBtn;
    private ImageView iv_photo;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();                              // 파이어베이스 데이터베이스 연동
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef = mFirebaseDB.getInstance().getReference();
    private FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();                           // 방금 로그인 성공한 유저의 정보를 가져오는 객체
    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference("Image");
    private final StorageReference reference = FirebaseStorage.getInstance().getReference();
    private ArrayList<DiaryItem> diaryArrayList;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_update);
        et_title = findViewById(R.id.et_title);
        tv_date = findViewById(R.id.tv_date);
        tv_time = findViewById(R.id.tv_time);
        iv_photo = findViewById(R.id.iv_photo);
        et_content = findViewById(R.id.et_content);
        mBtnOk = findViewById(R.id.btn_insert);
        mUploadBtn = findViewById(R.id.mUploadBtn);

        Intent intent = getIntent(); /*데이터 수신*/
        String content = intent.getExtras().getString("content");
        String title = intent.getExtras().getString("title");
        String date = intent.getExtras().getString("date");
        String time = intent.getExtras().getString("time");
        String photo = intent.getExtras().getString("photo");

        if(photo != null) {
            iv_photo.setImageURI(Uri.parse(photo));
            imageUri = Uri.parse(photo);
        }

        final DiaryItem[] diaryItems = {new DiaryItem()};
        //데이터 읽기
        mDatabaseRef.child("Diary").child(firebaseUser.getUid()).child(date+time).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
              et_title.setText("");
              tv_date.setText("");
              tv_time.setText("");
              et_content.setText("");
              Log.d("참조에러1", "DiaryUpdate1Activity.java 여기 지나감!");
          }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                  diaryItems[0] = snapshot.getValue(DiaryItem.class);
                  if (diaryItems[0] == null || diaryItems[0].equals(null)) {
                      et_title.setText("");
                      tv_date.setText("");
                      tv_time.setText("");
                      et_content.setText("");
                      Log.d("참조에러2", "DiaryUpdate1Activity.java 여기 지나감!");
                  } else {
                      et_title.setText(diaryItems[0].getTitle());
                      tv_date.setText(diaryItems[0].getWriteDate());
                      tv_time.setText(diaryItems[0].getWriteTime());
                      iv_photo.setImageURI(Uri.parse(diaryItems[0].getPhoto()));
                      et_content.setText(diaryItems[0].getContent());
                  }
            }
        });

        mUploadBtn = findViewById(R.id.mUploadBtn);
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/");
                activityResult.launch(galleryIntent);
            }
        });

        mBtnOk = findViewById(R.id.btn_insert);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DiaryItem diaryItem = new DiaryItem();
                String title = et_title.getText().toString();
                String content = et_content.getText().toString();
                // 사진업로드
                if (imageUri != null || !title.equals("") || !content.equals("")) {
                    //uploadToFirebase(imageUri);
                    diaryItem.setTitle(title);
                    diaryItem.setWriteDate(date);
                    diaryItem.setContent(content);
                    diaryItem.setWriteTime(time);
                    diaryItem.setPhoto(String.valueOf(imageUri));

                    Map<String, Object> taskMap1 = new HashMap<String, Object>();
                    taskMap1.put("title", title);
                    mDatabaseRef.child("Diary").child(firebaseUser.getUid()).child(date+time).updateChildren(taskMap1);
                    Map<String, Object> taskMap2 = new HashMap<String, Object>();
                    taskMap2.put("content", content);
                    mDatabaseRef.child("Diary").child(firebaseUser.getUid()).child(date+time).updateChildren(taskMap2);
                    Map<String, Object> taskMap3 = new HashMap<String, Object>();
                    taskMap3.put("photo", String.valueOf(imageUri));
                    mDatabaseRef.child("Diary").child(firebaseUser.getUid()).child(date+time).updateChildren(taskMap3);
                    Toast toast = Toast.makeText(DiaryUpdate1Activity.this, "다이어리가 수정되었습니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                } else {
                    Toast toast = Toast.makeText(DiaryUpdate1Activity.this, "입력하신 정보를 다시 확인해주세요.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
    // 사진 가져오기
    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        iv_photo.setImageURI(imageUri);
                    }
                }
            }
    );

    /*// 파이어베이스 이미지 업로드
    private void uploadToFirebase(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String photomime = mime.getExtensionFromMimeType(cr.getType(uri));
        StorageReference fileRef = reference.child(System.currentTimeMillis()+"."+photomime);
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // 이미지 모델에 담기
                        Model model = new Model(uri.toString());

                        // 키로 아이디 생성
                        String modelid = root.push().getKey();

                        // 데이터 넣기
                        root.child(modelid).setValue(model);
                        Toast.makeText(DiaryAdd1Activity.this, "사진업로드에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DiaryAdd1Activity.this, "사진업로드에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    // 파일타입 가져오기
    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        Log.d("이미지 파일타입", mime.getExtensionFromMimeType(cr.getType(uri)));
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }*/
}
