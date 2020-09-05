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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText edt_Ad, edt_Email, edt_Sifre;
    Button btn_Kaydol;
    TextView txt_girisSayfasina_git;
    ProgressDialog pd;

    private FirebaseAuth yetki;
    private DatabaseReference yol;
    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edt_Ad = (EditText) findViewById(R.id.edt_Ad);
        edt_Email = (EditText) findViewById(R.id.edt_email);
        edt_Sifre = (EditText) findViewById(R.id.edt_sifre);

        btn_Kaydol= findViewById(R.id.btn_kaydol_activity);

        txt_girisSayfasina_git= findViewById(R.id.txt_girisSayfasina_git);

        yetki = FirebaseAuth.getInstance();

        txt_girisSayfasina_git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        btn_Kaydol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pd =new ProgressDialog(RegisterActivity.this);
                pd.setMessage("Lütfen Bekleyin..");
                pd.show();

                final String str_Ad = edt_Ad.getText().toString();
                final String str_Email = edt_Email.getText().toString();
                final String str_Sifre = edt_Sifre.getText().toString();

                            if (TextUtils.isEmpty(str_Ad)||TextUtils.isEmpty(str_Email)||TextUtils.isEmpty(str_Sifre))
                            {
                                pd.dismiss();
                                Toast.makeText(RegisterActivity.this, "Lütfen bütün alanları doldurun!", Toast.LENGTH_LONG).show();
                            } else if (str_Sifre.length()<6)
                            {
                                pd.dismiss();
                                Toast.makeText(RegisterActivity.this, "Şifreniz min. 6 karakter olmalıdır!", Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                //Yeni Kullanıcı kaydetmet kodlarını çağır
                                kaydet(str_Ad, str_Email, str_Sifre);

                            }

                        pd.dismiss();
                    }
        });
    }

    private void kaydet (final String ad, String email, String sifre)
    {
        //Yeni Kullanıcı kaydet
        yetki.createUserWithEmailAndPassword(email,sifre)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseKullanici = yetki.getCurrentUser();

                            String kullaniciId = firebaseKullanici.getUid();

                            yol = FirebaseDatabase.getInstance().getReference().child("Kullanıcılar").child(kullaniciId);
                            HashMap<String , Object> hashMap  = new HashMap<>();
                            hashMap.put("id", kullaniciId);
                            hashMap.put("ad", ad);
                            hashMap.put("resimurl","https://firebasestorage.googleapis.com/v0/b/vaveyla-b2ca8.appspot.com/o/placeholder.jpg?alt=media&token=8085a983-5920-4e9e-ba89-d6b2b0752a24");
                            yol.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful())
                                    {
                                        pd.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        Toast.makeText(RegisterActivity.this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            });
                        } else {
                            pd.dismiss();
                        }
                    }
                });
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