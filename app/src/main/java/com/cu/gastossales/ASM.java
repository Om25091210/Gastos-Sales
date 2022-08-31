package com.cu.gastossales;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cu.gastossales.Adapter.ASM_Adapter;
import com.cu.gastossales.Adapter.TL_Adapter;
import com.cu.gastossales.SearchCodes.search_code;
import com.cu.gastossales.SearchProvider.search_provider;
import com.cu.gastossales.databinding.ActivityAsmBinding;
import com.cu.gastossales.databinding.ActivityMainBinding;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ASM extends AppCompatActivity {

    ActivityAsmBinding binding;
    List<String> gcode_list=new ArrayList<>();
    List<String> ecode_list=new ArrayList<>();
    List<String> tcode_list=new ArrayList<>();
    List<String> data_tcode_list=new ArrayList<>();
    List<String> today_asm_provier=new ArrayList<>();
    List<String> today_asm_name=new ArrayList<>();
    List<String> today_asm_revenue=new ArrayList<>();
    String salescode;
    FirebaseAuth auth;
    DatabaseReference reference,ref_hierachy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAsmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data");
        ref_hierachy= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales").child("hierarchy");
        Fresco.initialize(
                ASM.this,
                ImagePipelineConfig.newBuilder(ASM.this)
                        .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                        .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                        .experiment().setNativeCodeDisabled(true)
                        .build());
        salescode=getIntent().getStringExtra("sales-code");
        if(salescode==null){
            salescode=getSharedPreferences("saving_code",MODE_PRIVATE)
                    .getString("the_code_is","");
        }
        get_today();
        auth=FirebaseAuth.getInstance();
        LinearLayoutManager mManager = new LinearLayoutManager(ASM.this);
        binding.recyclerView.setItemViewCacheSize(500);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.recyclerView.setLayoutManager(mManager);
        binding.salesCode.setText(salescode);
        set_data_asm();
        binding.mSwipeRefreshLayout.setOnRefreshListener(() -> {
            get_today();
            set_data_asm();
        });
        binding.logout.setOnClickListener(v->{
            auth.signOut();
            startActivity(new Intent(ASM.this , Splash.class));
            finish();
        });
        binding.textView81.setOnClickListener(v->{
            Intent intent=new Intent(ASM.this,calendar_data.class);
            intent.putExtra("sales-code",salescode);
            startActivity(intent);
        });
        binding.logout.setVisibility(View.GONE);
        String hide_or_not=getIntent().getStringExtra("hide back?");
        if(hide_or_not!=null) {
            if (hide_or_not.equals("hide")) {
                binding.imageView4.setVisibility(View.GONE);
                binding.logout.setVisibility(View.VISIBLE);
            }
        }
        binding.imageView4.setOnClickListener(v->{
            finish();
        });
        binding.sales.setOnClickListener(v->{
            Intent intent=new Intent(ASM.this,only_asm.class);
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
        reference.child("BDSales").child("hierarchy").child(salescode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()) {
                    String name=snapshot.child("name").getValue(String.class);
                    binding.textView4.setText(salescode+":"+name);
                    binding.name.setText(name);
                    if(!ds.getKey().equals("total") && !ds.getKey().equals("name") && !ds.getKey().equals("phone"))
                        data_tcode_list.add(ds.getKey());
                }

                get_the_data(data_tcode_list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void get_the_data(List<String> tcode_list) {
        reference.child("BDSales").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int c=0;long total=0;
                String doj_str=snapshot.child(salescode).child("date_of_joining").getValue(String.class)+"";
                Log.e("doj",doj_str);
                binding.doj.setText("Date Of Joining - "+doj_str);
                for (int i = 0; i < tcode_list.size(); i++) {
                    for (DataSnapshot ds_phone : snapshot.child(tcode_list.get(i)).child("Providers").getChildren()) {
                        if (snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                //total provider
                                c++;
                                //total revenue
                                if (snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (int p = 0; p < snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                        total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey())
                                                .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                    }
                                }
                            }
                        }
                    }
                    for(DataSnapshot ds_e:snapshot.child("hierarchy").child(salescode).child(tcode_list.get(i)).getChildren()) {
                        for (DataSnapshot ds_phone : snapshot.child(ds_e.getKey()).child("Providers").getChildren()) {
                            if (snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                                if (database_format_date(Objects.requireNonNull(snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                    //total provider
                                    Log.e("cc", c + "");
                                    c++;
                                    //total revenue
                                    if (snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                        for (int p = 0; p < snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                            total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey())
                                                    .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String name = snapshot.child(tcode_list.get(i)).child("name").getValue(String.class);
                    today_asm_provier.add(c + "");
                    today_asm_name.add(name);
                    today_asm_revenue.add(total + "");
                    c=0;total=0;
                }
                get_totals(tcode_list);
                binding.mSwipeRefreshLayout.setRefreshing(false);
                TL_Adapter adapter=new TL_Adapter(ASM.this,today_asm_name,today_asm_provier,today_asm_revenue,tcode_list);
                adapter.notifyDataSetChanged();
                binding.recyclerView.setAdapter(adapter);
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

    private void get_totals(List<String> tcode_list) {
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
                binding.totalRevenue.setText(String.valueOf(total));
                binding.totalProvider.setText(String.valueOf(c));
                for (int i = 0; i < tcode_list.size(); i++) {
                    for (DataSnapshot ds_phone : snapshot.child(tcode_list.get(i)).child("Providers").getChildren()) {
                        if (snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            //total provider
                            c++;
                            //total revenue
                            if (snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                for (int p = 0; p < snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                    total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey())
                                            .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                }
                            }
                        }
                    }
                    Log.e("provider",c+"");
                    Log.e("pro",salescode+"");
                    for(DataSnapshot ds_e:snapshot.child("hierarchy").child(salescode).child(tcode_list.get(i)).getChildren()) {
                        for (DataSnapshot ds_phone : snapshot.child(ds_e.getKey()).child("Providers").getChildren()) {
                            if (snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                                //total provider
                                Log.e("cc", c + "");
                                c++;
                                //total revenue
                                if (snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (int p = 0; p < snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                        total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(ds_e.getKey()).child("Providers").child(ds_phone.getKey())
                                                .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                    }
                                }
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

    private void get_today() {
        gcode_list.clear();
        tcode_list.clear();
        ecode_list.clear();
        ref_hierachy.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds_g:snapshot.getChildren()){
                    gcode_list.add(ds_g.getKey());
                    for(DataSnapshot ds_t:snapshot.child(ds_g.getKey()).getChildren()){
                        tcode_list.add(ds_t.getKey());
                        for(DataSnapshot ds_e:snapshot.child(ds_g.getKey()).child(ds_t.getKey()).getChildren()){
                            ecode_list.add(ds_e.getKey());
                        }
                    }
                }
                get_today_provider();
                get_today_revenue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void get_today_provider() {
        Log.e("gcode",gcode_list+"");
        Log.e("tcode",tcode_list+"");
        Log.e("ecode",ecode_list+"");
        reference.child("BDSales").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int c=0;
                for(int i=0;i<gcode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(gcode_list.get(i)).child("Providers").getChildren()) {
                        if(snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                c++;
                            }
                        }
                    }
                }
                for(int i=0;i<tcode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(tcode_list.get(i)).child("Providers").getChildren()) {
                        if(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                c++;
                            }
                        }
                    }
                }
                for(int i=0;i<ecode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(ecode_list.get(i)).child("Providers").getChildren()) {
                        if(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                c++;
                            }
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
                for(int i=0;i<gcode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(gcode_list.get(i)).child("Providers").getChildren()) {
                        if(snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                if (snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (int p = 0; p < snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                        total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey())
                                                .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                    }
                                }
                            }
                        }
                    }
                }
                for(int i=0;i<tcode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(tcode_list.get(i)).child("Providers").getChildren()) {
                        if (snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                if (snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (int p = 0; p < snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                        total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(tcode_list.get(i)).child("Providers").child(ds_phone.getKey())
                                                .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                    }
                                }
                            }
                        }
                    }
                }
                for(int i=0;i<ecode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(ecode_list.get(i)).child("Providers").getChildren()) {
                        if (snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                if (snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (int p = 0; p < snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                        total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(ecode_list.get(i)).child("Providers").child(ds_phone.getKey())
                                                .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                    }
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
}