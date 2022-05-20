package com.example.tannae.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tannae.R;
import com.example.tannae.network.Network;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// << Sign Up Activity >>
public class SignUpActivity extends AppCompatActivity {
    private Button btnCheckID, btnSignUp;
    private RadioGroup rgGender;
    private EditText etID, etPW, etPWR, etName, etRRN, etPhone, etEmail;
    private TextView tvCheckId, tvCheckPW;
    private Toolbar toolbar;
    private boolean availableID = false, checkedID = false, availablePW = false, availablePWR = false, genderType = true, availableEmail = false, availablePhone = false;

    // < onCreate >
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // Setting
        setViews();
        setEventListeners();
    }

    // < Register views >
    private void setViews() {
        btnCheckID = findViewById(R.id.btn_checkID_sign_up);
        btnSignUp = findViewById(R.id.btn_sign_up);
        rgGender = findViewById(R.id.rg_gender_sign_up);
        etID = findViewById(R.id.et_id_sign_up);
        etPW = findViewById(R.id.et_pw_sign_up);
        etPWR = findViewById(R.id.et_checkpw_sign_up);
        etName = findViewById(R.id.et_name_sign_up);
        etRRN = findViewById(R.id.et_rrn_sign_up);
        etPhone = findViewById(R.id.et_phone_sign_up);
        etEmail = findViewById(R.id.et_email_sign_up);

        tvCheckId = findViewById(R.id.tv_checkID_sign_up);
        tvCheckPW = findViewById(R.id.tv_retrypw_sign_up);
        toolbar = findViewById(R.id.topAppBar_sign_up);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("type", false);
                startActivity(intent);
            }
        });
    }

    // < Register event listeners >
    private void setEventListeners() {
        // Check if ID type is available
        etID.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String id = etID.getText().toString();
                if (id.length() == 0) {
                    tvCheckId.setTextColor(0xAA000000);
                    tvCheckId.setText("영문 혹은 숫자를 사용하여 6자리 이상 작성하세요."); // 이부분 다크모드로 구현하기가 애매. xml의 원본 텍스트랑 같은 형식 사용하고 싶음. 비밀번호도
                    availableID = false;
                } else if (id.length() >= 6 && (id.matches(".*[a-zA-Z].*") || id.matches(".*[0-9].*"))
                        && !id.matches(".*[가-힣].*") && !id.matches(".*[\\W].*")) {
                    tvCheckId.setTextColor(0xAA0000FF);
                    tvCheckId.setText("사용 가능한 ID 형식입니다.");
                    availableID = true;
                } else {
                    tvCheckId.setTextColor(0xAAFF0000);
                    tvCheckId.setText("사용 불가능한 ID 형식입니다.");
                    availableID = false;
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
        });

        // Check if PW type is available
        etPW.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pw = etPW.getText().toString();
                if (etPW.getText().toString().length() == 0) {
                    tvCheckPW.setTextColor(0xAA000000);
                    tvCheckPW.setText("영문, 숫자를 조합하여 8자리 이상으로 작성하세요.");
                    availablePW = false;
                } else if (pw.length() >= 8 && pw.matches(".*[a-zA-Z].*") && pw.matches(".*[0-9].*")
                        && !pw.matches(".*[가-힣].*") && !pw.matches(".*[\\W].*")) {
                    tvCheckPW.setTextColor(0xAA0000FF);
                    tvCheckPW.setText("사용 가능한 PW 형식입니다.");
                    availablePW = true;
                } else {
                    tvCheckPW.setTextColor(0xAAFF0000);
                    tvCheckPW.setText("사용 불가능한 PW 형식입니다..");
                    availablePW = false;
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
        });

        // Check if PWR is identical with PW
        etPWR.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pwr = etPWR.getText().toString();
                if (!availablePW) {
                    tvCheckPW.setTextColor(0xAAFF0000);
                    tvCheckPW.setText("사용 불가능한 PW 형식입니다.");
                    availablePWR = false;
                } else {
                    if(etPW.getText().toString().equals(pwr)) {
                        tvCheckPW.setTextColor(0xAA0000FF);
                        tvCheckPW.setText("비밀번호가 일치합니다.");
                        availablePWR = true;
                    } else {
                        tvCheckPW.setTextColor(0xAAFF0000);
                        tvCheckPW.setText("비밀번호가 일치하지 않습니다.");
                        availablePWR = false;
                    }
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
        });

        // Change gender type by user input
        rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                genderType = (checkedId == R.id.rb_man_sign_up) ? true : false;
            }
        });

        // Store RRN by user input
        etRRN.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String rrn = etRRN.getText().toString();
                if (rrn.length() == 7 && rrn.charAt(rrn.length() - 1) != '-') {
                    etRRN.setText(new StringBuffer(rrn).insert(6, '-').toString());
                    etRRN.setSelection(rrn.length() + 1);
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
        });

        // Check if id entered is available [RETROFIT]
        btnCheckID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if ID type is available
                if (!availableID) {
                    Toast.makeText(getApplicationContext(), "지원되지 않는 ID 형식입니다. \n다른 ID를 사용해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Request if ID is not user [RETROFIT]
                Network.service.checkID(etID.getText().toString()).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        String message = response.body();
                        System.out.println(message);

                        if (message.equals("OK")) {
                            checkedID = true;
                            Toast.makeText(getApplicationContext(), "사용 가능한 ID 입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            checkedID = false;
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                        Log.e("Error", t.getMessage());
                    }
                });
            }
        });

        // Sing Up [RETROFIT]
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Check if entered info's are available
                    if (!availableID || !availablePW)
                        Toast.makeText(getApplicationContext(), "허용되지 않은 ID or PW 형식입니다.", Toast.LENGTH_SHORT).show();
                    else if (!checkedID)
                        Toast.makeText(getApplicationContext(), "ID 중복을 확인하세요.", Toast.LENGTH_SHORT).show();
                    else if (!availablePWR)
                        Toast.makeText(getApplicationContext(), "PW가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    else if (etName.getText().toString().length() == 0)
                        Toast.makeText(getApplicationContext(), "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                    else if (etRRN.getText().toString().length() != 14)
                        Toast.makeText(getApplicationContext(), "주민등록번호를 정확하게 입력하세요.", Toast.LENGTH_SHORT).show();
                    else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches())
                        Toast.makeText(getApplicationContext(), "Email 을 정확하게 작성하세요.", Toast.LENGTH_SHORT).show();
                    else if (!Patterns.PHONE.matcher(etPhone.getText().toString()).matches())
                        Toast.makeText(getApplicationContext(), "전화번호를 정확하게 작성하세요.", Toast.LENGTH_SHORT).show();
                    // If available request sign up [RETROFIT]
                    else {
                        // Create User JSON
                        JSONObject reqObj = new JSONObject();
                        reqObj.put("id", etID.getText().toString());
                        reqObj.put("pw", etPW.getText().toString());
                        reqObj.put("uname", etName.getText().toString());
                        reqObj.put("rrn", etRRN.getText().toString());
                        reqObj.put("gender", genderType);
                        reqObj.put("phone", etPhone.getText().toString());
                        reqObj.put("email", etEmail.getText().toString());

                        // Request sign up [RETROFIT]
                        Network.service.signup(reqObj).enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                Toast.makeText(getApplicationContext(), "가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                Log.e("Error", t.getMessage());
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
