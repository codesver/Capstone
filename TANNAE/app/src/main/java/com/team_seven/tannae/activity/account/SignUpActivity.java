package com.team_seven.tannae.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.team_seven.tannae.R;
import com.team_seven.tannae.network.Network;
import com.team_seven.tannae.sub.Toaster;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// << Sign Up Activity >>
public class SignUpActivity extends AppCompatActivity {
    private Button btnCheckID, btnSignUp, btnCheckUser;
    private EditText etID, etPW, etPWR, etName, etRRN, etPhone, etEmail;
    private TextView tvCheckId, tvCheckPW;
    private Toolbar toolbar;
    private boolean availableID = false, checkedID = false, checkedUser = false, availablePW = false, availablePWR = false, genderType = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setViews();
        setEventListeners();
    }

    private void setViews() {
        btnCheckID = findViewById(R.id.btn_checkID_sign_up);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnCheckUser = findViewById(R.id.btn_check_user_sign_up);

        etID = findViewById(R.id.et_id_sign_up);
        etPW = findViewById(R.id.et_pw_sign_up);
        etPWR = findViewById(R.id.et_checkpw_sign_up);
        etName = findViewById(R.id.et_name_sign_up);
        etRRN = findViewById(R.id.et_rrn_sign_up);
        etPhone = findViewById(R.id.et_phone_sign_up);
        etEmail = findViewById(R.id.et_email_sign_up);

        tvCheckId = findViewById(R.id.tv_checkID_sign_up);
        tvCheckPW = findViewById(R.id.tv_retrypw_sign_up);

        ((RadioGroup) findViewById(R.id.rg_gender_sign_up)).setOnCheckedChangeListener((group, checkedId) -> genderType = (checkedId == R.id.rb_man_sign_up) ? true : false);

        setSupportActionBar(toolbar = findViewById(R.id.topAppBar_sign_up));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("?????? ??????");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
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
                    tvCheckId.setText("?????? ?????? ????????? ???????????? 6?????? ?????? ???????????????.");
                    availableID = false;
                } else if (id.length() >= 6 && (id.matches(".*[a-zA-Z].*") || id.matches(".*[0-9].*"))
                        && !id.matches(".*[???-???].*") && !id.matches(".*[\\W].*")) {
                    tvCheckId.setTextColor(0xAA0000FF);
                    tvCheckId.setText("?????? ????????? ID ???????????????.");
                    availableID = true;
                } else {
                    tvCheckId.setTextColor(0xAAFF0000);
                    tvCheckId.setText("?????? ???????????? ID ???????????????.");
                    availableID = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Check if PW type is available
        etPW.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pw = etPW.getText().toString();
                if (etPW.getText().toString().length() == 0) {
                    tvCheckPW.setTextColor(0xAA000000);
                    tvCheckPW.setText("??????, ????????? ???????????? 8?????? ???????????? ???????????????.");
                    availablePW = false;
                } else if (pw.length() >= 8 && pw.matches(".*[a-zA-Z].*") && pw.matches(".*[0-9].*")
                        && !pw.matches(".*[???-???].*") && !pw.matches(".*[\\W].*")) {
                    tvCheckPW.setTextColor(0xAA0000FF);
                    tvCheckPW.setText("?????? ????????? PW ???????????????.");
                    availablePW = true;
                } else {
                    tvCheckPW.setTextColor(0xAAFF0000);
                    tvCheckPW.setText("?????? ???????????? PW ???????????????..");
                    availablePW = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Check if PWR is identical with PW
        etPWR.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pwr = etPWR.getText().toString();
                if (!availablePW) {
                    tvCheckPW.setTextColor(0xAAFF0000);
                    tvCheckPW.setText("?????? ???????????? PW ???????????????.");
                    availablePWR = false;
                } else {
                    if (etPW.getText().toString().equals(pwr)) {
                        tvCheckPW.setTextColor(0xAA0000FF);
                        tvCheckPW.setText("??????????????? ???????????????.");
                        availablePWR = true;
                    } else {
                        tvCheckPW.setTextColor(0xAAFF0000);
                        tvCheckPW.setText("??????????????? ???????????? ????????????.");
                        availablePWR = false;
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Change checkedUser by name input
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkedUser = false;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
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
                checkedUser = false;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Check if id entered is available [RETROFIT]
        btnCheckID.setOnClickListener(v -> {
            // Check if ID type is available
            if (!availableID) {
                Toaster.show(getApplicationContext(), "???????????? ?????? ID ???????????????. \n?????? ID??? ??????????????????.");
                return;
            }

            // Request if ID is not user [RETROFIT]
            Network.service.checkID(etID.getText().toString()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    String message = response.body();
                    checkedID = message.equals("OK");
                    Toaster.show(getApplicationContext(), message.equals("OK") ? "?????? ????????? ID ?????????." : message);
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toaster.show(getApplicationContext(), "Error");
                    Log.e("Error", t.getMessage());
                }
            });
        });

        btnCheckUser.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String rrn = etRRN.getText().toString();
            if (name.length() == 0 && rrn.length() != 14)
                Toaster.show(getApplicationContext(), "????????? ????????????????????? ??????????????????.");
            else if (name.length() == 0)
                Toaster.show(getApplicationContext(), "????????? ??????????????????.");
            else if (rrn.length() != 14)
                Toaster.show(getApplicationContext(), "????????????????????? ????????? ??????????????????.");
            else {
                Network.service.checkUser(name, rrn).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        String message = response.body();
                        checkedUser = message.equals("OK");
                        Toaster.show(getApplicationContext(), message.equals("OK") ? "??????????????? ?????? ???????????????." : message);
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toaster.show(getApplicationContext(), "Error");
                        Log.e("Error", t.getMessage());
                    }
                });
            }
        });

        // Sing Up [RETROFIT]
        btnSignUp.setOnClickListener(v -> {
            try {
                // Check if entered info's are available
                if (!availableID || !availablePW)
                    Toaster.show(getApplicationContext(), "???????????? ?????? ID or PW ???????????????.");
                else if (!checkedID)
                    Toaster.show(getApplicationContext(), "ID ????????? ???????????????");
                else if (!availablePWR)
                    Toaster.show(getApplicationContext(), "PW??? ???????????? ????????????.");
                else if (etName.getText().toString().length() == 0)
                    Toaster.show(getApplicationContext(), "????????? ???????????????.");
                else if (etRRN.getText().toString().length() != 14)
                    Toaster.show(getApplicationContext(), "????????????????????? ???????????? ???????????????.");
                else if (!checkedUser)
                    Toaster.show(getApplicationContext(), "??????????????? ????????????.");
                else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches())
                    Toaster.show(getApplicationContext(), "Email ??? ???????????? ???????????????.");
                else if (!Patterns.PHONE.matcher(etPhone.getText().toString()).matches())
                    Toaster.show(getApplicationContext(), "??????????????? ???????????? ???????????????.");
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
                            Toaster.show(getApplicationContext(), "????????? ?????????????????????.");
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            Toaster.show(getApplicationContext(), "Error");
                            Log.e("Error", t.getMessage());
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
}
