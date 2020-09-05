package com.sedadurmus.weatherapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sedadurmus.weatherapp.model.Users;

public class LoginActivity extends AppCompatActivity {

    EditText edt_email_Giris, edt_sifre_Giris, editText;
    Button btn_giris_yap;
    TextView txt_kayitSayfasina_git;

    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private DatabaseReference yol;
    public static Users users;
    String mail, sifre;

    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        edt_email_Giris = findViewById(R.id.emailEt);
        edt_sifre_Giris = findViewById(R.id.passwordEt);
        btn_giris_yap = findViewById(R.id.login_btn);
        txt_kayitSayfasina_git = findViewById(R.id.nothave_accountTv);

        if (firebaseUser != null){
            MainActivity.mevcutKullanici = firebaseUser;

        final DatabaseReference yolGiris = FirebaseDatabase.getInstance().getReference().child("Kullanıcılar")
                .child(mAuth.getCurrentUser().getUid());

        yolGiris.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LoginActivity.users = dataSnapshot.getValue(Users.class);
                if (LoginActivity.users == null) return;
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                 }
            });
        }else {
            Toast.makeText(this, "Lütfen giriş yapın veya kaydolun", Toast.LENGTH_SHORT).show();
        }

        txt_kayitSayfasina_git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btn_giris_yap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Giriş Yapılıyor..");
                progressDialog.show();
                final String str_emailGiris = edt_email_Giris.getText().toString();
                final String str_sifreGiris = edt_sifre_Giris.getText().toString();
                if (TextUtils.isEmpty(str_emailGiris) || TextUtils.isEmpty(str_sifreGiris)) {
                    Toast.makeText(LoginActivity.this, "Lütfen bütün alanları doldurun..", Toast.LENGTH_SHORT).show();
                } else {
                    //Giriş yapma kodları
                    mAuth.signInWithEmailAndPassword(str_emailGiris, str_sifreGiris)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        final DatabaseReference yolGiris = FirebaseDatabase.getInstance().getReference().child("Kullanıcılar")
                                                .child(mAuth.getCurrentUser().getUid());

                                        yolGiris.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                progressDialog.dismiss();

                                                users = dataSnapshot.getValue(Users.class);
                                                if (users == null) return;
                                                users.setMail(str_emailGiris);
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                progressDialog.dismiss();
                                            }
                                        });
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Giriş Başarısız!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

    }





    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            backToast.cancel();
        } else {
            backToast = Toast.makeText(getBaseContext(), "Çıkmak için iki kere basınız", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();

    }



}