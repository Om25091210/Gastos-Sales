package com.cu.gastossales;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cu.gastossales.Adapter.ASM_Adapter;
import com.cu.gastossales.Admin.admin;
import com.cu.gastossales.SearchCodes.search_code;
import com.cu.gastossales.SearchProvider.search_provider;
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

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    List<String> gcode_list=new ArrayList<>();
    List<String> ecode_list=new ArrayList<>();
    List<String> tcode_list=new ArrayList<>();
    List<String> today_asm_provier=new ArrayList<>();
    List<String> today_asm_name=new ArrayList<>();
    List<String> today_asm_revenue=new ArrayList<>();
    DatabaseReference reference,ref_hierachy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data");
        ref_hierachy= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales").child("hierarchy");
        Fresco.initialize(
                MainActivity.this,
                ImagePipelineConfig.newBuilder(MainActivity.this)
                        .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                        .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                        .experiment().setNativeCodeDisabled(true)
                        .build());

        get_data();
        get_today();

        auth=FirebaseAuth.getInstance();

        binding.textView3.setOnClickListener(v->{
            MainActivity.this.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations( R.anim.enter_from_right, R.anim.exit_to_left,R.anim.enter_from_left, R.anim.exit_to_right)
                    .add(R.id.constraint,new search_code())
                    .addToBackStack(null)
                    .commit();
        });

        binding.textView4.setOnClickListener(v->{
            MainActivity.this.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations( R.anim.enter_from_right, R.anim.exit_to_left,R.anim.enter_from_left, R.anim.exit_to_right)
                    .add(R.id.constraint,new search_provider())
                    .addToBackStack(null)
                    .commit();
        });

        LinearLayoutManager mManager = new LinearLayoutManager(MainActivity.this);
        binding.recyclerView.setItemViewCacheSize(500);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.recyclerView.setLayoutManager(mManager);
        set_data_asm();
        binding.mSwipeRefreshLayout.setOnRefreshListener(() -> {
            get_data();
            get_today();
            set_data_asm();
        });
        binding.textView8.setOnClickListener(v->{
            Intent intent=new Intent(MainActivity.this,calendar_data.class);
            intent.putExtra("sales-code","admin");
            startActivity(intent);
        });
        binding.putNumber.setOnClickListener(v->{
            Intent intent=new Intent(MainActivity.this, admin.class);
            startActivity(intent);
        });
        binding.logout.setOnClickListener(v->{
            auth.signOut();
            startActivity(new Intent(MainActivity.this , Splash.class));
            finish();
        });

    }

    private void set_data_asm() {
        today_asm_provier.clear();
        today_asm_name.clear();
        today_asm_revenue.clear();
        binding.mSwipeRefreshLayout.setRefreshing(true);
        reference.child("BDSales").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int c=0;long total=0;
                for(int i=0;i<gcode_list.size();i++) {
                    for (DataSnapshot ds_phone : snapshot.child(gcode_list.get(i)).child("Providers").getChildren()) {
                        if(snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                            if (database_format_date(Objects.requireNonNull(snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                //total provider
                                Log.e("cc",c+"");
                                c++;
                                //total revenue
                                if (snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                    for (int p = 0; p < snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                        total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(gcode_list.get(i)).child("Providers").child(ds_phone.getKey())
                                                .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                    }
                                }
                            }
                        }
                    }
                    for(DataSnapshot ds_t:snapshot.child("hierarchy").child(gcode_list.get(i)).getChildren()) {
                        for (DataSnapshot ds_phone : snapshot.child(ds_t.getKey()).child("Providers").getChildren()) {
                            if (snapshot.child(ds_t.getKey()).child("Providers").child(ds_phone.getKey()).child("date_time").exists()) {
                                if (database_format_date(Objects.requireNonNull(snapshot.child(ds_t.getKey()).child("Providers").child(ds_phone.getKey()).child("date_time").getValue(String.class))).equals(format())) {
                                    //total provider
                                    Log.e("cc", c + "");
                                    c++;
                                    //total revenue
                                    if (snapshot.child(ds_t.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").exists()) {
                                        for (int p = 0; p < snapshot.child(ds_t.getKey()).child("Providers").child(ds_phone.getKey()).child("package_info").getChildrenCount(); p++) {
                                            total = total + Long.parseLong(Objects.requireNonNull(snapshot.child(ds_t.getKey()).child("Providers").child(ds_phone.getKey())
                                                    .child("package_info").child(p + "").child("order_amount").getValue(String.class)));
                                        }
                                    }
                                }
                            }
                        }
                        for(DataSnapshot ds_e:snapshot.child("hierarchy").child(gcode_list.get(i)).child(ds_t.getKey()).getChildren()) {
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
                    }
                    if(!gcode_list.get(i).equals("total")) {
                        String name = snapshot.child(gcode_list.get(i)).child("name").getValue(String.class);
                        today_asm_provier.add(c + "");
                        today_asm_name.add(name);
                        today_asm_revenue.add(total + "");
                    }
                    c=0;total=0;
                }
                gcode_list.remove("total");
                binding.mSwipeRefreshLayout.setRefreshing(false);
                ASM_Adapter adapter=new ASM_Adapter(MainActivity.this,today_asm_name,today_asm_provier,today_asm_revenue,gcode_list);
                adapter.notifyDataSetChanged();
                binding.recyclerView.setAdapter(adapter);

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

    private void get_data() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total=0;
                long total_providers=snapshot.getChildrenCount()-2;
                binding.textView5.setText(String.valueOf(total_providers));

                for(DataSnapshot ds:snapshot.child("BDSales").getChildren()){
                    for(DataSnapshot ds_pr:snapshot.child("BDSales").child(ds.getKey()).child("Providers").getChildren()) {
                        if (snapshot.child("BDSales").child(ds.getKey()).child("Providers").child(ds_pr.getKey()).child("package_info").exists()) {
                            for (DataSnapshot ps : snapshot.child("BDSales").child(ds.getKey()).child("Providers").child(ds_pr.getKey()).child("package_info").getChildren()) {
                                String pacakge = snapshot.child("BDSales").child(ds.getKey()).child("Providers").child(ds_pr.getKey()).child("package_info").child(ps.getKey()).child("order_amount").getValue(String.class);
                                total = Long.parseLong(pacakge) + total;
                            }
                        }
                    }
                }
                binding.textView7.setText(String.valueOf(total));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}