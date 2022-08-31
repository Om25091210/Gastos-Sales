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

import com.cu.gastossales.Adapter.search_code_Adapter;
import com.cu.gastossales.databinding.FragmentSearchCodeBinding;
import com.cu.gastossales.model.sales_code_data;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class search_code extends Fragment {

    FragmentSearchCodeBinding binding;
    private Context contextNullSafe;
    DatabaseReference reference;
    List<sales_code_data> list=new ArrayList<>();
    List<sales_code_data> mylist=new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentSearchCodeBinding.inflate(inflater, container, false);
        if (contextNullSafe == null) getContextNullSafety();

        LinearLayoutManager mManager = new LinearLayoutManager(getContextNullSafety());
        binding.recyclerView.setItemViewCacheSize(500);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.recyclerView.setLayoutManager(mManager);

        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data").child("BDSales");

        get_data();
        binding.search.addTextChangedListener(new TextWatcher() {

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
        list.clear();
        binding.mSwipeRefreshLayout.setRefreshing(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    String provider_count=snapshot.child(ds.getKey()).child("Providers").getChildrenCount()+"";
                    String sales_name=snapshot.child(ds.getKey()).child("name").getValue(String.class);
                    sales_code_data sales_code_data=new sales_code_data(ds.getKey(),sales_name,provider_count);
                    list.add(sales_code_data);
                }
                binding.mSwipeRefreshLayout.setRefreshing(false);

                Collections.reverse(list);
                search_code_Adapter search_code_adapter=new search_code_Adapter(getContextNullSafety(),list);
                search_code_adapter.notifyDataSetChanged();
                if(binding.recyclerView!=null)
                    binding.recyclerView.setAdapter(search_code_adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void search(String str) {
        mylist.clear();
        for(sales_code_data object:list) {
            if (object.getSales_code().toLowerCase().contains(str.toLowerCase().trim())) {
                mylist.add(object);
            }
        }
        Collections.reverse(mylist);
        search_code_Adapter search_code_adapter=new search_code_Adapter(getContextNullSafety(),mylist);
        search_code_adapter.notifyDataSetChanged();
        if(binding.recyclerView!=null)
            binding.recyclerView.setAdapter(search_code_adapter);
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
}