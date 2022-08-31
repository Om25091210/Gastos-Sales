package com.cu.gastossales;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cu.gastossales.Adapter.Esales_Adapter;
import com.cu.gastossales.Adapter.sales_provider_Adapter;
import com.cu.gastossales.databinding.ActivityExecutiveHomeBinding;
import com.cu.gastossales.databinding.ActivityTlBinding;
import com.cu.gastossales.model.sales_code_data;
import com.cu.gastossales.model.sales_provider_data;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImageTranscoderType;
import com.facebook.imagepipeline.core.MemoryChunkType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class executive_home extends AppCompatActivity {

    List<String> gcode_list=new ArrayList<>();
    List<String> ecode_list=new ArrayList<>();
    List<String> tcode_list=new ArrayList<>();
    List<String> data_tcode_list=new ArrayList<>();
    List<String> today_asm_provier=new ArrayList<>();
    List<String> today_asm_name=new ArrayList<>();
    List<sales_provider_data> list=new ArrayList<>();
    List<String> today_asm_revenue=new ArrayList<>();
    String salescode;
    FirebaseAuth auth;
    DatabaseReference reference,reference_ref,ref_hierachy;
    ActivityExecutiveHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityExecutiveHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data");
        ref_hierachy= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales").child("hierarchy");
        Fresco.initialize(
                executive_home.this,
                ImagePipelineConfig.newBuilder(executive_home.this)
                        .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                        .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                        .experiment().setNativeCodeDisabled(true)
                        .build());
        salescode=getIntent().getStringExtra("sales-code");
        if(salescode==null){
            salescode=getSharedPreferences("saving_code",MODE_PRIVATE)
                    .getString("the_code_is","");
        }
        reference_ref= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales").child(salescode).child("Providers");
        get_data();
        get_today_provider();
        get_today_revenue();
        auth=FirebaseAuth.getInstance();
        LinearLayoutManager mManager = new LinearLayoutManager(executive_home.this);
        binding.recyclerView.setItemViewCacheSize(500);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.recyclerView.setLayoutManager(mManager);
        set_data_asm();
        binding.mSwipeRefreshLayout.setOnRefreshListener(() -> {
            get_data();
            get_today_revenue();
            get_today_provider();
            set_data_asm();
        });
        binding.logout.setVisibility(View.GONE);
        String hide_or_not=getIntent().getStringExtra("hide back?");
        if(hide_or_not!=null) {
            if (hide_or_not.equals("hide")) {
                binding.imageView4.setVisibility(View.GONE);
                binding.logout.setVisibility(View.VISIBLE);
            }
        }binding.imageView4.setOnClickListener(v->{
            finish();
        });
        binding.logout.setOnClickListener(v->{
            auth.signOut();
            startActivity(new Intent(executive_home.this , Splash.class));
            finish();
        });
        binding.textView81.setOnClickListener(v->{
            Intent intent=new Intent(executive_home.this,calendar_data.class);
            intent.putExtra("sales-code",salescode);
            startActivity(intent);
        });
    }
    private void set_data_asm() {
        today_asm_provier.clear();
        today_asm_name.clear();
        today_asm_revenue.clear();
        data_tcode_list.clear();
        binding.mSwipeRefreshLayout.setRefreshing(true);
        Log.e("ssa",salescode);
        reference.child("BDSales").child("hierarchy").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()) {
                    for(DataSnapshot ds_t:snapshot.child(ds.getKey()).getChildren()) {
                        if (snapshot.child(ds.getKey()).child(ds_t.getKey()).child(salescode).exists()) {
                            String name = snapshot.child(ds.getKey()).child(ds_t.getKey()).child(salescode).child("name").getValue(String.class);
                            binding.textView4.setText(salescode + ":" + name);
                            break;
                        }
                    }
                }
                data_tcode_list.add(salescode);
                get_the_data();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_the_data() {
        reference_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total=0;
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    long sales_total=0;
                    String date_time=snapshot.child(ds.getKey()).child("date_time").getValue(String.class);
                    String uid=snapshot.child(ds.getKey()).child("uid").getValue(String.class);
                    if(snapshot.child(ds.getKey()).child("package_info").exists()) {
                        for (DataSnapshot ps : snapshot.child(ds.getKey()).child("package_info").getChildren()) {
                            String pacakge = snapshot.child(ds.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                            total = Long.parseLong(pacakge) + total;
                            Log.e("uid ",ds.getKey());
                            sales_total=Long.parseLong(pacakge)+sales_total;
                        }
                    }

                    sales_provider_data sales_provider_data=new sales_provider_data(ds.getKey(),date_time,uid,String.valueOf(sales_total));
                    list.add(sales_provider_data);
                }

                binding.mSwipeRefreshLayout.setRefreshing(false);
                Collections.sort(list, new calendar_data.sortCompare());
                Collections.reverse(list);
                sales_provider_Adapter sales_provider_adapter=new sales_provider_Adapter(list,executive_home.this);
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private static String format() {
        Format f = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        String str_time = f.format(new Date());
        //
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String str_date = formatter.format(new Date());
        String date=str_date + " - " + str_time;
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
    private void get_today_provider() {
        reference.child("BDSales").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int c=0;
                String doj_str=snapshot.child(salescode).child("date_of_joining").getValue(String.class)+"";
                Log.e("doj",doj_str);
                binding.doj.setText("Date Of Joining - "+doj_str);
                for (DataSnapshot ds_phone : snapshot.child(salescode).child("Providers").getChildren()) {
                    if(snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                        if (database_format_date(Objects.requireNonNull(snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                            c++;
                        }
                    }
                }
                binding.textView18.setText(c+"");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void get_today_revenue() {
        reference.child("BDSales").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total=0;
                for (DataSnapshot ds_phone : snapshot.child(salescode).child("Providers").getChildren()) {
                    if (snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                        if (database_format_date(Objects.requireNonNull(snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                            if (snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                for (int p = 0; p < snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                    total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(salescode).child("Providers").child(ds_phone.getKey())
                                            .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                }
                            }
                        }
                    }
                }
                binding.textView17.setText(total+"");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void get_data() {
        reference.child("BDSales").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int c=0;long total=0;
                for (DataSnapshot ds_phone : snapshot.child(salescode).child("Providers").getChildren()) {
                    if (snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                        //total provider
                        c++;
                        //total revenue
                        if (snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                            for (int p = 0; p < snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(salescode).child("Providers").child(ds_phone.getKey())
                                        .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                            }
                        }
                    }
                }
                binding.textView5.setText(String.valueOf(c));
                binding.textView7.setText(String.valueOf(total));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}