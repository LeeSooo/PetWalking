package com.example.petwalking;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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

public class ActivityDogInfo extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();                              // 파이어베이스 데이터베이스 연동
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseRef = mFirebaseDB.getInstance().getReference();
    private FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();                           // 방금 로그인 성공한 유저의 정보를 가져오는 객체

    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference("Image");
    private final StorageReference reference = FirebaseStorage.getInstance().getReference();

    private EditText mEtdogName, mEtdogBreed, mEtweight;
    private ImageView iv_photo;
    private static String strBirth = "2022-11-11";
    private Spinner mSpGender;
    private Button mBtnOk, mUploadBtn;
    private Uri imageUri;

    private String photo, name, bread, gender, birth, weight;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_info);

        // 파이어베이스 데이터베이스 연동
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("DogInfo");      // 파이어베이스 DB에 저장시킬 상위 주소위치

        // 반려동물 입력필드 초기화
        mEtdogName = findViewById(R.id.et_dogName);
        mEtdogBreed = findViewById(R.id.et_dogBreed);
        mEtweight = findViewById(R.id.et_weight);
        iv_photo = findViewById(R.id.iv_photo);

        // 반려동물 생년월일
        DatePicker datePicker = findViewById(R.id.dataPicker);

        final DogInfo[] dogInfo = {new DogInfo()};
        //데이터 읽기
        mDatabaseRef.child("DogInfo").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                name = "";
                bread = "";
                gender = "";
                birth = "";
                weight="";

                datePicker.init(2022, 01, 01, new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        strBirth = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                    }
                });

                Log.d("반려동물 정보수정 클래스 에러1", "파이어베이스 참조 실패");
            }
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dogInfo[0] = snapshot.getValue(DogInfo.class);
                if (dogInfo[0] == null || dogInfo[0].equals(null)) {
                    name = "";
                    bread = "";
                    gender = "";
                    birth = "";
                    weight="";

                    datePicker.init(2022, 01, 01, new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            strBirth = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                        }
                    });
                    Log.d("반려동물 정보수정 클래스 에러2", "파이어베이스 정보 불러오는 거 실패");
                } else {
                    Log.d("일단 여기는 지나갑니당", "여기 지나감!");
                    name = dogInfo[0].getDogName();
                    bread = dogInfo[0].getDogBreed();
                    gender = dogInfo[0].getDogGender();
                    birth = dogInfo[0].getDogBirth();
                    weight = dogInfo[0].getDogWeight();

                    if(dogInfo[0].getDogImg() != null || dogInfo[0].getDogImg().equals("") ) {
                        photo = dogInfo[0].getDogImg();
                        iv_photo.setImageURI(Uri.parse(photo));
                        imageUri = Uri.parse(photo);
                    }

                    Log.d("파이어베이스로부터 받은 생일", birth);
                    mEtdogName.setText(name);
                    mEtdogBreed.setText(bread);
                    mEtweight.setText(weight);

                    String[] birthArr = birth.split("-");
                    for(int i = 0; i < 3; i++){
                        Log.d("일단 여기는 지나갑니당", String.valueOf(Integer.parseInt(birthArr[i])));
                    }
                    datePicker.init(Integer.parseInt(birthArr[0]), Integer.parseInt(birthArr[1])-1, Integer.parseInt(birthArr[2]), new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            strBirth = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                            Log.d("생일", strBirth);
                        }
                    });
                }
            }
        });
        if(photo != null) {
            iv_photo.setImageURI(Uri.parse(photo));
        }
        // 반려동물 성별
        mSpGender = findViewById(R.id.sp_gender);
        ArrayAdapter adapterGender = ArrayAdapter.createFromResource(this,
                R.array.arr_gender, android.R.layout.simple_spinner_item);
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpGender.setAdapter(adapterGender);

        // 반려견 사진 업로드 버튼
        mUploadBtn = findViewById(R.id.btn_upload);
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/");
                activityResult.launch(galleryIntent);
            }
        });

        // 반려견 정보 입력 버튼
        mBtnOk = findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 반려견 정보 입력 처리
                String strDogName = mEtdogName.getText().toString();
                String strDogBread = mEtdogBreed.getText().toString();
                String strDogGender = mSpGender.getSelectedItem().toString();
                String strDogWeight = mEtweight.getText().toString();
                String strDogImage = String.valueOf(imageUri);

                Log.d("정보",strDogName);
                Log.d("정보",strDogBread);
                Log.d("정보",strDogGender);
                Log.d("정보",strDogWeight);
                Log.d("정보",strDogImage);
                Log.d("생일",strBirth);

                // 사진업로드
                if (strDogImage.equals("") || strDogName.equals("")  || strDogBread.equals("") || strDogGender.equals("")  || strDogWeight.equals("") ) {
                    Toast.makeText(ActivityDogInfo.this, "입력하신 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // 방금 로그인 성공한 유저의 정보를 가져오는 객체
                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

                    // 반려견 정보 수정
                    DogInfo dogInfo = new DogInfo();
                    dogInfo.setIdToken(firebaseUser.getUid());
                    dogInfo.setDogImg(strDogImage);
                    dogInfo.setDogName(strDogName);
                    dogInfo.setDogBreed(strDogBread);
                    dogInfo.setDogGender(strDogGender);
                    dogInfo.setDogBirth(strBirth);
                    dogInfo.setDogWeight(strDogWeight);
                    Log.d("생일",strBirth);

                    // setValue : DB 하위주소(UserAccount)에 정보를 삽입함. (2022-10-21 이수)
                    mDatabaseRef.child("DogInfo").child(firebaseUser.getUid()).setValue(dogInfo);

                    Toast.makeText(ActivityDogInfo.this, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
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
                        Log.d("이미지", String.valueOf(imageUri));
                        iv_photo.setImageURI(imageUri);
                    }
                }
            }
    );

    /*// 이름 변경을 위한 메소드
    private String read() {

        final UserAccount[] userInfo = {new UserAccount()};
        //데이터 읽기
        mDatabaseRef.child("DogInfo").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) { //참조에 액세스 할 수 없을 때 호출
                weight = 0;
                height = 0;
                Log.d("정보1", "333333");
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfo[0] = snapshot.getValue(UserAccount.class);
                if (userInfo[0] == null || userInfo[0].equals(null)) {
                    weight = 0;
                    height = 0;
                    Log.d("정보1", "2222222");
                } else {
                    weight = userInfo[0].getWeight();
                    height = userInfo[0].getHeight();
                    Log.d("정보1", "11111111111111111111111111111111323123123131232");
                    Log.d("키1", String.valueOf(weight));
                    Log.d("키2", String.valueOf(height));
                }
            }
        });
    }*/

}