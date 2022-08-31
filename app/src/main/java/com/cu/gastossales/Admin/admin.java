package com.cu.gastossales.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.cu.gastossales.R;
import com.cu.gastossales.databinding.ActivityAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class admin extends AppCompatActivity {

    ActivityAdminBinding binding;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    DatabaseReference reference;
    List<String> list_asm=new ArrayList<>();
    List<String> tl_list=new ArrayList<>();
    int c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference= FirebaseDatabase.getInstance().getReference().child("/Merchant_data/BDSales");
        ArrayAdapter<String> myadapter=new ArrayAdapter<String>(admin.this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.itemselect));
        myadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.select.setAdapter(myadapter);
        binding.select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    c=0;
                    binding.asmName.setVisibility(View.GONE);
                    binding.tlName.setVisibility(View.GONE);
                }
                else if(i==1){
                    c=1;
                    binding.tlName.setVisibility(View.GONE);
                    binding.asmName.setVisibility(View.VISIBLE);
                }
                else{
                    c=2;
                    binding.asmName.setVisibility(View.VISIBLE);
                    binding.tlName.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        mDateSetListener = (datePicker, year, month, day) -> {

            String d=String.valueOf(day);
            String m=String.valueOf(month+1);
            Log.e("month",m+"");
            month = month + 1;
            Log.e("month",month+"");
            if(String.valueOf(day).length()==1)
                d="0"+ day;
            if(String.valueOf(month).length()==1)
                m="0"+ month;
            String date = d + "/" + m + "/" + year;
            binding.rmDate.setText(date);
        };

        binding.rmDate.setOnClickListener(v->{
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    admin.this,
                    mDateSetListener,
                    year,month,day);
            dialog.show();
        });

        binding.imageView4.setOnClickListener(v->{
            finish();
        });

        binding.progressBar.setVisibility(View.GONE);

        binding.submitTxt1.setOnClickListener(v->{
            String sales_code=binding.salesCode.getText().toString().trim();
            String sales_name=binding.salesName.getText().toString().trim();
            String sales_phone=binding.salesPhone.getText().toString().trim();
            String date_of_joining=binding.rmDate.getText().toString().trim();
            if(!sales_code.equals("")) {
                if(!sales_name.equals("")) {
                    if (!sales_phone.equals("")) {
                        if (!date_of_joining.equals("")) {
                            if (c == 0) {
                                reference.child(sales_code).child("name").setValue(sales_name);
                                reference.child(sales_code).child("phone").setValue("+91"+sales_phone);
                                reference.child(sales_code).child("date_of_joining").setValue(date_of_joining);
                                reference.child("hierarchy").child(sales_code).child("name").setValue(sales_name);
                                binding.progressBar.setVisibility(View.VISIBLE);
                                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(admin.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                                        binding.progressBar.setVisibility(View.GONE);
                                    }
                                },1000);
                            } else if (c == 1) {
                                if (!binding.asmName.getText().toString().trim().equals("")) {
                                    reference.child(sales_code).child("name").setValue(sales_name);
                                    reference.child(sales_code).child("phone").setValue("+91"+sales_phone);
                                    reference.child(sales_code).child("date_of_joining").setValue(date_of_joining);
                                    reference.child("hierarchy").child(binding.asmName.getText().toString().trim()).child(sales_code).child("name").setValue(sales_name);
                                    binding.progressBar.setVisibility(View.VISIBLE);
                                    new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(admin.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                                            binding.progressBar.setVisibility(View.GONE);
                                        }
                                    },1000);
                                } else {
                                    Toast.makeText(this, "Please Enter Code of ASM", Toast.LENGTH_SHORT).show();
                                }
                            } else if (c == 2) {
                                if (!binding.asmName.getText().toString().trim().equals("") && !binding.tlName.getText().toString().trim().equals("")) {
                                    reference.child(sales_code).child("name").setValue(sales_name);
                                    reference.child(sales_code).child("phone").setValue("+91"+sales_phone);
                                    reference.child(sales_code).child("date_of_joining").setValue(date_of_joining);
                                    reference.child("hierarchy").child(binding.asmName.getText().toString().trim()).child(binding.tlName.getText().toString().trim()).child(sales_code).child("name").setValue(sales_name);
                                    binding.progressBar.setVisibility(View.VISIBLE);
                                    new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(admin.this, "Added Successfully", Toast.LENGTH_SHORT).show();
                                            binding.progressBar.setVisibility(View.GONE);
                                        }
                                    },1000);
                                } else {
                                    Toast.makeText(this, "Please Enter Code of ASM and TL", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else
                            binding.rmDate.setError("Empty");
                    }
                    else
                        binding.salesPhone.setError("Empty");
                }
                else
                    binding.salesName.setError("Empty");
            }
            else
                binding.salesCode.setError("Empty");
        });
        get_asm();
        binding.asmName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                get_tl(editable.toString());
            }
        });
    }

    private void get_tl(String code) {
        reference.child("hierarchy").child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    tl_list.add(ds.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>
                        (admin.this, android.R.layout.select_dialog_item, tl_list);
                //Getting the instance of AutoCompleteTextView
                binding.tlName.setThreshold(1);//will start working from first character
                binding.tlName.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                binding.tlName.setTextColor(Color.RED);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_asm(){
        reference.child("hierarchy").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    list_asm.add(ds.getKey());
                }
                //Creating the instance of ArrayAdapter containing list of language names
                ArrayAdapter<String> adapter = new ArrayAdapter<String>
                        (admin.this, android.R.layout.select_dialog_item, list_asm);
                //Getting the instance of AutoCompleteTextView
                binding.asmName.setThreshold(1);//will start working from first character
                binding.asmName.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                binding.asmName.setTextColor(Color.RED);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}