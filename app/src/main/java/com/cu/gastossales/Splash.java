package com.cu.gastossales;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cu.gastossales.databinding.ActivitySplashBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Splash extends AppCompatActivity {

    ActivitySplashBinding binding;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    CountDownTimer countDownTimer;
    private PhoneAuthProvider.ForceResendingToken resendOTPtoken;
    private String verificationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth=FirebaseAuth.getInstance();
        binding.progressBar.setVisibility(View.GONE);
        binding.signIn.setOnClickListener(v->{
            if(binding.edtEmail.getGetTextValue().trim().length()==10){
                String phone = "+91" + binding.edtEmail.getGetTextValue();
                binding.progressBar.setVisibility(View.VISIBLE);
                check_admin(phone,binding.salesCode.getGetTextValue().trim().toUpperCase());
            }
            else{
                Toast.makeText(Splash.this, "Enter 10 digit mobile number.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.textView14.setOnClickListener(v->{
            if(binding.textView14.getText().toString().equals("RESEND NEW CODE")) {
                String phone = "+91" + binding.edtEmail.getGetTextValue();
                resendVerificationCode(phone, resendOTPtoken);
                countTimer();
            }
        });

        binding.pBack.setOnClickListener(v->{
            binding.textView13.setVisibility(View.GONE);
            binding.textView14.setVisibility(View.GONE);
            onAnimate(binding.edtEmail);
            binding.pinView.setText("");
            binding.pBack.setVisibility(View.GONE);
            binding.textView23.setText("Send OTP");
            binding.salesCode.setVisibility(View.VISIBLE);
            offanimate(binding.pinView);
            countDownTimer.cancel();
        });

        binding.pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ch=s+"";
                if(ch.length()==6){
                    String otp_text= Objects.requireNonNull(binding.pinView.getText()).toString().trim();
                    Log.e("pinView","==========");
                    verifyCode(otp_text);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void check_admin(String phone, String toUpperCase) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("/Merchant_data/BDSales");
        reference.child("Admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int c=0;
                for(DataSnapshot ds_a:snapshot.getChildren()){
                    Log.e("codd",ds_a.getValue(String.class));
                    Log.e("cooo",phone.substring(3));
                    if(phone.substring(3).equals(ds_a.getValue(String.class))){
                        c=1;
                        getSharedPreferences("saving_code",MODE_PRIVATE).edit()
                                .putString("the_code_is","admin").apply();
                        binding.progressBar.setVisibility(View.GONE);
                        offanimate(binding.edtEmail);
                        binding.textView13.setVisibility(View.VISIBLE);
                        binding.textView14.setVisibility(View.VISIBLE);
                        binding.pBack.setVisibility(View.VISIBLE);
                        binding.salesCode.setVisibility(View.GONE);
                        binding.textView23.setText("Verify");
                        onAnimate(binding.pinView);
                        binding.pinView.setVisibility(View.VISIBLE);
                        sendVerificationCode(phone);
                        countTimer();
                        break;
                    }
                }
                if(c==0) {
                    if (!binding.salesCode.getGetTextValue().trim().equals(""))
                        check_validity(phone, binding.salesCode.getGetTextValue().trim().toUpperCase());
                    else
                        Toast.makeText(Splash.this, "Enter your code.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void check_validity(String phone, String getTextValue) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("/Merchant_data/BDSales");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(getTextValue).exists()){
                    String phone_d=snapshot.child(getTextValue).child("phone").getValue(String.class);
                    if(phone!=null) {
                        if (phone.equals(phone_d)) {
                            getSharedPreferences("saving_code", MODE_PRIVATE).edit()
                                    .putString("the_code_is", getTextValue).apply();
                            binding.progressBar.setVisibility(View.GONE);
                            offanimate(binding.edtEmail);
                            binding.textView13.setVisibility(View.VISIBLE);
                            binding.textView14.setVisibility(View.VISIBLE);
                            binding.pBack.setVisibility(View.VISIBLE);
                            binding.salesCode.setVisibility(View.GONE);
                            binding.textView23.setText("Verify");
                            onAnimate(binding.pinView);
                            binding.pinView.setVisibility(View.VISIBLE);
                            sendVerificationCode(phone);
                            countTimer();
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(Splash.this, "Wrong Phone number", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(Splash.this, "Phone number not in database.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    int c=0;
                    for(DataSnapshot ds_t:snapshot.getChildren()){
                        if(ds_t.child(getTextValue).exists()){
                            c=1;
                            String phone_d=snapshot.child(ds_t.getKey()).child(getTextValue).child("phone").getValue(String.class);
                            if(phone!=null) {
                                if (phone.equals(phone_d)) {
                                    getSharedPreferences("saving_code", MODE_PRIVATE).edit()
                                            .putString("the_code_is", getTextValue).apply();
                                    binding.progressBar.setVisibility(View.GONE);
                                    offanimate(binding.edtEmail);
                                    binding.textView13.setVisibility(View.VISIBLE);
                                    binding.textView14.setVisibility(View.VISIBLE);
                                    binding.pBack.setVisibility(View.VISIBLE);
                                    binding.salesCode.setVisibility(View.GONE);
                                    binding.textView23.setText("Verify");
                                    onAnimate(binding.pinView);
                                    binding.pinView.setVisibility(View.VISIBLE);
                                    sendVerificationCode(phone);
                                    countTimer();
                                    break;
                                }
                                else{
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Splash.this, "Wrong Phone number", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else{
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(Splash.this, "Phone number not in database.", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                    if(c!=1){
                        for(DataSnapshot ds_t:snapshot.getChildren()){
                            for(DataSnapshot ds_e:snapshot.child(ds_t.getKey()).getChildren()){
                                if(ds_e.child(getTextValue).exists()){
                                    String phone_d=snapshot.child(ds_t.getKey()).child(ds_e.getKey()).child(getTextValue).child("phone").getValue(String.class);
                                    if(phone!=null) {
                                        if (phone.equals(phone_d)) {
                                            getSharedPreferences("saving_code", MODE_PRIVATE).edit()
                                                    .putString("the_code_is", getTextValue).apply();
                                            binding.progressBar.setVisibility(View.GONE);
                                            offanimate(binding.edtEmail);
                                            binding.textView13.setVisibility(View.VISIBLE);
                                            binding.textView14.setVisibility(View.VISIBLE);
                                            binding.pBack.setVisibility(View.VISIBLE);
                                            binding.salesCode.setVisibility(View.GONE);
                                            binding.textView23.setText("Verify");
                                            onAnimate(binding.pinView);
                                            binding.pinView.setVisibility(View.VISIBLE);
                                            sendVerificationCode(phone);
                                            countTimer();
                                            break;
                                        } else {
                                            binding.progressBar.setVisibility(View.GONE);
                                            Toast.makeText(Splash.this, "Wrong Phone number", Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }else{
                                        binding.progressBar.setVisibility(View.GONE);
                                        Toast.makeText(Splash.this, "Phone number not in database.", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                                else{
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Splash.this, "No code exists, please contact admin.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // callback method is called on Phone auth provider.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            verificationId = s;
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.
                binding.pinView.setText(code);
                Log.e("inside code block","==========");
                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                verifyCode(code);
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Log.e("error",e+"");
        }
    };
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallBack)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void sendVerificationCode(String number) {
        // this method is used for getting
        // OTP on user phone number.
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    // below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        // below line is used for getting getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }
    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
                            Log.e("task successfull","Success");
                            update_ui();
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            Log.e("task result",task.getException().getMessage());
                        }
                    }
                });
    }

    private void update_ui() {
        String code=getSharedPreferences("saving_code",MODE_PRIVATE)
                .getString("the_code_is","");
        if(code.charAt(0)=='G'){
            Intent intent=new Intent(this,ASM.class);
            intent.putExtra("sales-code",code);
            intent.putExtra("hide back?","hide");
            startActivity(intent);
            finish();
        }
        else if(code.charAt(0)=='T'){
            Intent intent=new Intent(this,TL.class);
            intent.putExtra("sales-code",code);
            intent.putExtra("hide back?","hide");
            startActivity(intent);
            finish();
        }
        else if(code.equals("admin")){
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if(code.charAt(0)=='E'){
            Intent intent=new Intent(this,executive_home.class);
            intent.putExtra("sales-code",code);
            intent.putExtra("hide back?","hide");
            startActivity(intent);
            finish();
        }
    }

    private void countTimer()
    {
        countDownTimer=new CountDownTimer(25000, 1000)
        {
            public void onTick(long millisUntilFinished) {

                NumberFormat f = new DecimalFormat("00");
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                binding.textView14.setText("Retry after - "+f.format(min) + ":" + f.format(sec));
            }
            // When the task is over it will print 00:00:00 there
            public void onFinish() {
                binding.textView14.setText("RESEND NEW CODE");
                binding.textView14.setVisibility(View.VISIBLE);
                // btnVerify.setEnabled(true);
            }
        };
        countDownTimer.start();
    }
    void offanimate(View view){
        ObjectAnimator move=ObjectAnimator.ofFloat(view, "translationX",-800f);
        move.setDuration(1000);
        ObjectAnimator alpha2= ObjectAnimator.ofFloat(view, "alpha",0);
        alpha2.setDuration(500);
        AnimatorSet animset=new AnimatorSet();
        animset.play(alpha2).with(move);
        animset.start();
    }
    void onAnimate(View view){
        ObjectAnimator move=ObjectAnimator.ofFloat(view, "translationX",0f);
        move.setDuration(1000);
        ObjectAnimator alpha2= ObjectAnimator.ofFloat(view, "alpha",100);
        alpha2.setDuration(500);
        AnimatorSet animset=new AnimatorSet();
        animset.play(alpha2).with(move);
        animset.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        user=mAuth.getCurrentUser();
        if(user!=null){
            String code=getSharedPreferences("saving_code",MODE_PRIVATE)
                    .getString("the_code_is"," ");
            if(code.charAt(0)=='G'){
                Intent intent=new Intent(this,ASM.class);
                intent.putExtra("sales-code",code);
                intent.putExtra("hide back?","hide");
                startActivity(intent);
                finish();
            }
            else if(code.charAt(0)=='T'){
                Intent intent=new Intent(this,TL.class);
                intent.putExtra("sales-code",code);
                intent.putExtra("hide back?","hide");
                startActivity(intent);
                finish();
            }
            else if(code.equals("admin")){
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
            }
            else if(code.charAt(0)=='E'){
                Intent intent=new Intent(this,executive_home.class);
                intent.putExtra("sales-code",code);
                intent.putExtra("hide back?","hide");
                startActivity(intent);
                finish();
            }
        }
    }
}