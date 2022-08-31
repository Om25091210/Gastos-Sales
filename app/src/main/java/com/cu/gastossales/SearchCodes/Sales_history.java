package com.cu.gastossales.SearchCodes;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cu.gastossales.Adapter.sales_provider_Adapter;
import com.cu.gastossales.Adapter.search_code_Adapter;
import com.cu.gastossales.R;
import com.cu.gastossales.calendar_data;
import com.cu.gastossales.databinding.FragmentSalesHistoryBinding;
import com.cu.gastossales.model.sales_code_data;
import com.cu.gastossales.model.sales_provider_data;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Sales_history extends Fragment {

    FragmentSalesHistoryBinding binding;
    private Context contextNullSafe;
    DatabaseReference reference;
    sales_code_data sales_code_data;
    sales_provider_Adapter sales_provider_adapter;
    List<sales_provider_data> list=new ArrayList<>();
    List<sales_provider_data> mylist=new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentSalesHistoryBinding.inflate(inflater, container, false);
        if (contextNullSafe == null) getContextNullSafety();

        if(getArguments()!=null){
            sales_code_data= (sales_code_data) getArguments().getSerializable("sales_data_sending");
        }

        binding.search.setText(sales_code_data.getSales_code());
        binding.textView9.setText(sales_code_data.getTotal_providers());
        binding.textView8.setText(sales_code_data.getSales_name());

        LinearLayoutManager mManager = new LinearLayoutManager(getContextNullSafety());
        binding.recyclerView.setItemViewCacheSize(500);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.recyclerView.setLayoutManager(mManager);

        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales").child(sales_code_data.getSales_code()).child("Providers");

        get_data();
        binding.searchMerchant.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s+"");
            }
        });

        binding.mSwipeRefreshLayout.setOnRefreshListener(this::get_data);
        binding.imageView.setOnClickListener(v->back());

        return binding.getRoot();
    }

    private void get_data() {
        binding.mSwipeRefreshLayout.setRefreshing(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        binding.textView14.setText(String.valueOf(total));
                    }

                    sales_provider_data sales_provider_data=new sales_provider_data(ds.getKey(),date_time,uid,String.valueOf(sales_total));
                    list.add(sales_provider_data);
                }

                binding.mSwipeRefreshLayout.setRefreshing(false);
                if(list.size()>1)
                    Collections.sort(list, new sortCompare());
                Collections.reverse(list);
                sales_provider_adapter=new sales_provider_Adapter(list,getContextNullSafety());
                sales_provider_adapter.notifyItemRangeChanged(0,list.size());
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(sales_provider_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void search(String str) {
        mylist.clear();
        for(sales_provider_data object:list) {
            if (object.getNumber().toLowerCase().contains(str.toLowerCase().trim())) {
                mylist.add(object);
            }
        }
        Collections.reverse(mylist);
        sales_provider_adapter=new sales_provider_Adapter(mylist,getContextNullSafety());
        sales_provider_adapter.notifyItemRangeChanged(0,mylist.size());
        if(binding.recyclerView!=null)
            binding.recyclerView.setAdapter(sales_provider_adapter);
    }

    /**CALL THIS IF YOU NEED CONTEXT*/
    public Context getContextNullSafety() {
        if (getContext() != null) return getContext();
        if (getActivity() != null) return getActivity();
        if (contextNullSafe != null) return contextNullSafe;
        if (getView() != null && getView().getContext() != null) return getView().getContext();
        if (requireContext() != null) return requireContext();
        if (requireActivity() != null) return requireActivity();
        if (requireView() != null && requireView().getContext() != null)
            return requireView().getContext();

        return null;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        contextNullSafe = context;
    }

    private void back(){
        FragmentManager fm=((FragmentActivity) getContextNullSafety()).getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        if(fm.getBackStackEntryCount()>0) {
            fm.popBackStack();
        }
        ft.commit();
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
        DateFormat df = new SimpleDateFormat("dd MMMM yyyy - hh:mm:ss aa", Locale.getDefault());
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