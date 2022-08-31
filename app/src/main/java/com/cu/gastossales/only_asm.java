package com.cu.gastossales;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cu.gastossales.Adapter.sales_provider_Adapter;
import com.cu.gastossales.databinding.ActivityOnlyAsmBinding;
import com.cu.gastossales.databinding.ActivityOnlyTlBinding;
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

public class only_asm extends AppCompatActivity {

    ActivityOnlyAsmBinding binding;
    FirebaseAuth auth;
    String gcode="";
    List<sales_provider_data> list=new ArrayList<>();
    String salescode;
    DatabaseReference reference,ref_hierachy,reference_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityOnlyAsmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data");
        ref_hierachy= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales").child("hierarchy");
        Fresco.initialize(
                only_asm.this,
                ImagePipelineConfig.newBuilder(only_asm.this)
                        .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                        .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                        .experiment().setNativeCodeDisabled(true)
                        .build());
        salescode=getIntent().getStringExtra("sales-code");
        if(salescode==null){
            salescode=getSharedPreferences("saving_code",MODE_PRIVATE)
                    .getString("the_code_is","");
        }
        get_today_provider();
        auth=FirebaseAuth.getInstance();
        reference_ref= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales").child(salescode).child("Providers");
        LinearLayoutManager mManager = new LinearLayoutManager(only_asm.this);
        binding.recyclerView.setItemViewCacheSize(500);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.recyclerView.setLayoutManager(mManager);
        set_data_asm();
        binding.mSwipeRefreshLayout.setOnRefreshListener(() -> {
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
            startActivity(new Intent(only_asm.this , Splash.class));
            finish();
        });

        binding.textView81.setOnClickListener(v->{
            Intent intent=new Intent(only_asm.this,calendar_data.class);
            intent.putExtra("sales-code",salescode);
            intent.putExtra("specific-tl","yes");
            startActivity(intent);
        });
    }
    private void set_data_asm() {
        binding.mSwipeRefreshLayout.setRefreshing(true);
        reference.child("BDSales").child(salescode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                binding.textView4.setText(salescode + ":" + name);
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
                get_totals();
                binding.mSwipeRefreshLayout.setRefreshing(false);
                Collections.sort(list, new calendar_data.sortCompare());
                Collections.reverse(list);
                sales_provider_Adapter sales_provider_adapter=new sales_provider_Adapter(list,only_asm.this);
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_totals() {
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
        Log.e("gcode",gcode+"");
        Log.e("tcode",salescode+"");
        reference.child("BDSales").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int c=0;long total=0;
                for (DataSnapshot ds_phone : snapshot.child(salescode).child("Providers").getChildren()) {
                    if (snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                        if (database_format_date(Objects.requireNonNull(snapshot.child(salescode).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
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
                }
                binding.textView18.setText(c+"");
                binding.textView17.setText(total+"");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}