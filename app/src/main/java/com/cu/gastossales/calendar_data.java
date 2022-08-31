package com.cu.gastossales;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;

import com.cu.gastossales.Adapter.sales_provider_Adapter;
import com.cu.gastossales.databinding.ActivityCalendarDataBinding;
import com.cu.gastossales.model.sales_code_data;
import com.cu.gastossales.model.sales_provider_data;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class calendar_data extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    DatabaseReference reference,ref_hierachy;
    List<String> gcode_list=new ArrayList<>();
    List<String> ecode_list=new ArrayList<>();
    List<String> tcode_list=new ArrayList<>();
    String salescode;
    ActivityCalendarDataBinding binding;
    List<sales_provider_data> list=new ArrayList<>();
    sales_provider_Adapter sales_provider_adapter;
    String specific;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCalendarDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales");
        ref_hierachy= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales").child("hierarchy");

        salescode=getIntent().getStringExtra("sales-code");
        specific=getIntent().getStringExtra("specific-tl");
        Log.e("saleee",salescode+"");
        LinearLayoutManager mManager = new LinearLayoutManager(calendar_data.this);
        binding.recyclerView.setItemViewCacheSize(500);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.recyclerView.setLayoutManager(mManager);
        if(specific==null) {
            if (salescode.charAt(0) == 'G') {
                Log.e("sed", salescode + "=====");
                get_today_asm(today());
            } else if (salescode.charAt(0) == 'T') {
                Log.e("sed", salescode + "=====");
                get_today_t(today());
            } else if (salescode.charAt(0) == 'E') {
                Log.e("sed", salescode + "=====");
                get_today_e(today());
            } else {
                show_data(today());
            }
        }
        else{
            if (salescode.charAt(0) == 'G') {
                Log.e("sed", salescode + "=====");
                get_today_asm_specific(today());
            } else if (salescode.charAt(0) == 'T') {
                Log.e("specific", salescode + "=====");
                get_today_t_specific(today());
            }
        }
        binding.calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // add code here
                try {
                    String date = dayOfMonth + "/" + (month+1) + "/" + year;
                    Log.e("code here", dayOfMonth + "-" + month + "-" + year);
                    SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat f1 = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                    Date date1 = f.parse(date);
                    String str = f1.format(Objects.requireNonNull(date1));
                    if(specific==null) {
                        if (salescode.charAt(0) == 'G') {
                            Log.e("sed", salescode + "=====");
                            get_today_asm(str);
                        } else if (salescode.charAt(0) == 'T') {
                            Log.e("sed", salescode + "=====");
                            get_today_t(str);
                        } else if (salescode.charAt(0) == 'E') {
                            Log.e("sed", salescode + "=====");
                            get_today_e(str);
                        } else {
                            show_data(str);
                        }
                    }
                    else{
                        if (salescode.charAt(0) == 'G') {
                            Log.e("sed", salescode + "=====");
                            get_today_asm_specific(str);
                        } else if (salescode.charAt(0) == 'T') {
                            Log.e("specific", salescode + "=====");
                            get_today_t_specific(str);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void get_today_asm_specific(String str) {
        binding.mSwipeRefreshLayout.setRefreshing(true);
        reference.child(salescode).child("Providers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total=0;
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()) {
                    long sales_total = 0;
                    if (snapshot.child(ds.getKey()).child("date_time").exists()) {
                        if (database_format_date(Objects.requireNonNull(snapshot.child(ds.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                            String date_time = snapshot.child(ds.getKey()).child("date_time").getValue(String.class);
                            String uid = snapshot.child(ds.getKey()).child("uid").getValue(String.class);
                            if (snapshot.child(ds.getKey()).child("package_info").exists()) {
                                for (DataSnapshot ps : snapshot.child(ds.getKey()).child("package_info").getChildren()) {
                                    String pacakge = snapshot.child(ds.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                    total = Long.parseLong(pacakge) + total;
                                    Log.e("uid ", ds.getKey());
                                    sales_total = Long.parseLong(pacakge) + sales_total;
                                }
                            }

                            sales_provider_data sales_provider_data = new sales_provider_data(ds.getKey(), date_time, uid, String.valueOf(sales_total));
                            list.add(sales_provider_data);
                            sales_total=0;
                        }
                    }
                }

                binding.mSwipeRefreshLayout.setRefreshing(false);
                binding.textView5.setText(String.valueOf(list.size()));
                binding.textView7.setText(String.valueOf(total));
                Collections.sort(list, new sortCompare());
                sales_provider_adapter=new sales_provider_Adapter(list,calendar_data.this);
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_today_t_specific(String str) {
        binding.mSwipeRefreshLayout.setRefreshing(true);
        reference.child(salescode).child("Providers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total=0;
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()) {
                    long sales_total = 0;
                    if (snapshot.child(ds.getKey()).child("date_time").exists()) {
                        if (database_format_date(Objects.requireNonNull(snapshot.child(ds.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                            String date_time = snapshot.child(ds.getKey()).child("date_time").getValue(String.class);
                            String uid = snapshot.child(ds.getKey()).child("uid").getValue(String.class);
                            if (snapshot.child(ds.getKey()).child("package_info").exists()) {
                                for (DataSnapshot ps : snapshot.child(ds.getKey()).child("package_info").getChildren()) {
                                    String pacakge = snapshot.child(ds.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                    total = Long.parseLong(pacakge) + total;
                                    Log.e("uid ", ds.getKey());
                                    sales_total = Long.parseLong(pacakge) + sales_total;
                                }
                            }

                            sales_provider_data sales_provider_data = new sales_provider_data(ds.getKey(), date_time, uid, String.valueOf(sales_total));
                            list.add(sales_provider_data);
                            sales_total=0;
                        }
                    }
                }

                binding.mSwipeRefreshLayout.setRefreshing(false);
                binding.textView5.setText(String.valueOf(list.size()));
                binding.textView7.setText(String.valueOf(total));
                Collections.sort(list, new sortCompare());
                sales_provider_adapter=new sales_provider_Adapter(list,calendar_data.this);
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_today_e(String str) {
        binding.mSwipeRefreshLayout.setRefreshing(true);
        reference.child(salescode).child("Providers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total=0;
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()) {
                    long sales_total = 0;
                    if (snapshot.child(ds.getKey()).child("date_time").exists()) {
                        if (database_format_date(Objects.requireNonNull(snapshot.child(ds.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                            String date_time = snapshot.child(ds.getKey()).child("date_time").getValue(String.class);
                            String uid = snapshot.child(ds.getKey()).child("uid").getValue(String.class);
                            if (snapshot.child(ds.getKey()).child("package_info").exists()) {
                                for (DataSnapshot ps : snapshot.child(ds.getKey()).child("package_info").getChildren()) {
                                    String pacakge = snapshot.child(ds.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                    total = Long.parseLong(pacakge) + total;
                                    Log.e("uid ", ds.getKey());
                                    sales_total = Long.parseLong(pacakge) + sales_total;
                                }
                            }

                            sales_provider_data sales_provider_data = new sales_provider_data(ds.getKey(), date_time, uid, String.valueOf(sales_total));
                            list.add(sales_provider_data);
                            sales_total=0;
                        }
                    }
                }

                binding.mSwipeRefreshLayout.setRefreshing(false);
                binding.textView5.setText(String.valueOf(list.size()));
                binding.textView7.setText(String.valueOf(total));
                Collections.sort(list, new sortCompare());
                sales_provider_adapter=new sales_provider_Adapter(list,calendar_data.this);
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_today_t(String str) {
        tcode_list.clear();
        gcode_list.clear();
        ecode_list.clear();
        binding.mSwipeRefreshLayout.setRefreshing(true);
        ref_hierachy.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds_g:snapshot.getChildren()){
                    if(snapshot.child(ds_g.getKey()).child(salescode).exists()) {
                        for (DataSnapshot ds_e : snapshot.child(ds_g.getKey()).child(salescode).getChildren()) {
                            ecode_list.add(ds_e.getKey());
                        }
                    }
                }
                get_today_provider_t(str);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_today_provider_t(String str) {
        Log.e("tcode",tcode_list+"");
        Log.e("gcode",gcode_list+"");
        Log.e("ecode",ecode_list+"");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total = 0;
                long sales_total = 0;
                list.clear();
                for (DataSnapshot ds_phone : snapshot.child(salescode).child("Providers").getChildren()) {
                    if(snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                        if (database_format_date(Objects.requireNonNull(snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                            String date_time = snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class);
                            String uid = snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("uid").getValue(String.class);
                            if (snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                for (DataSnapshot ps : snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").getChildren()) {
                                    String pacakge = snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                    total = Long.parseLong(pacakge) + total;
                                    sales_total = Long.parseLong(pacakge) + sales_total;
                                }
                            }
                            sales_provider_data sales_provider_data = new sales_provider_data(ds_phone.getKey(), date_time, uid, String.valueOf(sales_total));
                            list.add(sales_provider_data);
                            sales_total=0;
                        }
                    }
                }
                for(int i=0;i<ecode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(ecode_list.get(i)).child("Providers").getChildren()) {
                        if(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                                String date_time = snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class);
                                String uid = snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("uid").getValue(String.class);
                                if (snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (DataSnapshot ps : snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildren()) {
                                        String pacakge = snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                        total = Long.parseLong(pacakge) + total;
                                        sales_total = Long.parseLong(pacakge) + sales_total;
                                    }
                                }

                                sales_provider_data sales_provider_data = new sales_provider_data(ds_phone.getKey(), date_time, uid, String.valueOf(sales_total));
                                list.add(sales_provider_data);
                                sales_total=0;
                            }
                        }
                    }
                }
                binding.mSwipeRefreshLayout.setRefreshing(false);
                Collections.sort(list, new sortCompare());
                binding.textView5.setText(String.valueOf(list.size()));
                binding.textView7.setText(String.valueOf(total));
                sales_provider_adapter=new sales_provider_Adapter(list,calendar_data.this);
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_today_asm(String str) {
        tcode_list.clear();
        ecode_list.clear();
        gcode_list.clear();
        binding.mSwipeRefreshLayout.setRefreshing(true);
        ref_hierachy.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds_t:snapshot.child(salescode).getChildren()){
                    tcode_list.add(ds_t.getKey());
                    for(DataSnapshot ds_e:snapshot.child(salescode).child(ds_t.getKey()).getChildren()){
                        ecode_list.add(ds_e.getKey());
                    }
                }
                get_today_provider_asm(str);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void get_today_provider_asm(String str) {
        Log.e("tcode",tcode_list+"");
        Log.e("ecode",ecode_list+"");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total=0;
                long sales_total=0;
                list.clear();
                for (DataSnapshot ds_phone : snapshot.child(salescode).child("Providers").getChildren()) {
                    if (snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                        if (database_format_date(Objects.requireNonNull(snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                            Log.e("asd", str);
                            Log.e("11 ", database_format_date(Objects.requireNonNull(snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))) + "");
                            String date_time = snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class);
                            String uid = snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("uid").getValue(String.class);
                            if (snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                for (DataSnapshot ps : snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").getChildren()) {
                                    String pacakge = snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                    total = Long.parseLong(pacakge) + total;
                                    sales_total = Long.parseLong(pacakge) + sales_total;
                                }
                            }

                            sales_provider_data sales_provider_data = new sales_provider_data(ds_phone.getKey(), date_time, uid, String.valueOf(sales_total));
                            list.add(sales_provider_data);
                            sales_total=0;
                        }

                    }
                }
                for(int i=0;i<tcode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(tcode_list.get(i)).child("Providers").getChildren()) {
                        if(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                                Log.e("asd", str);
                                Log.e("11 ", database_format_date(Objects.requireNonNull(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))) + "");
                                String date_time = snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class);
                                String uid = snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("uid").getValue(String.class);
                                if (snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (DataSnapshot ps : snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildren()) {
                                        String pacakge = snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                        total = Long.parseLong(pacakge) + total;
                                        sales_total = Long.parseLong(pacakge) + sales_total;
                                    }
                                }

                                sales_provider_data sales_provider_data = new sales_provider_data(ds_phone.getKey(), date_time, uid, String.valueOf(sales_total));
                                list.add(sales_provider_data);
                                sales_total=0;
                            }
                        }
                    }
                }
                for(int i=0;i<ecode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(ecode_list.get(i)).child("Providers").getChildren()) {
                        if(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                                Log.e("asd", str);
                                Log.e("11 ", database_format_date(Objects.requireNonNull(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))) + "");
                                String date_time = snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class);
                                String uid = snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("uid").getValue(String.class);
                                if (snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (DataSnapshot ps : snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildren()) {
                                        String pacakge = snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                        total = Long.parseLong(pacakge) + total;
                                        sales_total = Long.parseLong(pacakge) + sales_total;
                                    }
                                }

                                sales_provider_data sales_provider_data = new sales_provider_data(ds_phone.getKey(), date_time, uid, String.valueOf(sales_total));
                                list.add(sales_provider_data);
                                sales_total=0;
                            }
                        }
                    }
                }
                binding.mSwipeRefreshLayout.setRefreshing(false);
                Collections.sort(list, new sortCompare());
                binding.textView5.setText(String.valueOf(list.size()));
                binding.textView7.setText(String.valueOf(total));
                Log.e("lii",list+"");
                sales_provider_adapter=new sales_provider_Adapter(list,calendar_data.this);
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void show_data(String str) {
        binding.mSwipeRefreshLayout.setRefreshing(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total=0;
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()) {
                    long sales_total=0;
                    for (DataSnapshot ds_phone : snapshot.child(ds.getKey()).child("Providers").getChildren()) {
                        if (snapshot.child(ds.getKey()).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(ds.getKey()).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(str)) {
                                String date_time=snapshot.child(ds.getKey()).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class);
                                String uid=snapshot.child(ds.getKey()).child("Providers").child(ds_phone.getKey()).child("uid").getValue(String.class);
                                if(snapshot.child(ds.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (DataSnapshot ps : snapshot.child(ds.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").getChildren()) {
                                        String pacakge = snapshot.child(ds.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                        total = Long.parseLong(pacakge) + total;
                                        sales_total=Long.parseLong(pacakge)+sales_total;
                                    }
                                }

                                sales_provider_data sales_provider_data=new sales_provider_data(ds_phone.getKey(),date_time,uid,String.valueOf(sales_total));
                                list.add(sales_provider_data);
                                sales_total=0;
                            }
                        }
                    }
                }
                binding.mSwipeRefreshLayout.setRefreshing(false);
                Collections.sort(list, new sortCompare());
                binding.textView5.setText(String.valueOf(list.size()));
                binding.textView7.setText(String.valueOf(total));
                sales_provider_adapter=new sales_provider_Adapter(list,calendar_data.this);
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private static String today() {
        Format f = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        String str_time = f.format(new Date());
        //
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String str_date = formatter.format(new Date());
        String date=str_date + " - " + str_time;
        Log.e("date_time",date);
        for(int i=0;i<date.length();i++){
            if(date.charAt(i)=='-'){
                String formatted_date=date.substring(0,i);
                return formatted_date.trim();
            }
        }
        return "";
    }
    private static String database_format_date(String date){
        for(int i=0;i<date.length();i++){
            if(date.charAt(i)=='-'){
                String formatted_date=date.substring(0,i);
                return formatted_date.trim();
            }
        }
        return "";
    }

    static class sortCompare implements Comparator<sales_provider_data>
    {
        // Method of this class
        @Override
        public int compare(sales_provider_data sales_provider_data, sales_provider_data t1) {
            if(sales_provider_data.getDate()!=null && t1.getDate()!=null)
                return convert_format(sales_provider_data.getDate()).compareTo(convert_format(t1.getDate()));
            return 0;
        }
    }
    private static String convert_format(String date1){
        //Date/time pattern of input date
        DateFormat df = new SimpleDateFormat("dd MMMM yyyy - hh:mm:ss aa",Locale.getDefault());
        //Date/time pattern of desired output date
        DateFormat outputformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss aa",Locale.getDefault());
        Date date;
        String output;
        try{
            //Conversion of input String to date
            date= df.parse(date1);
            //old date format to new date format
            output = outputformat.format(date);
            return output;
        }catch(ParseException pe){
            pe.printStackTrace();
        }
        return "";
    }
}