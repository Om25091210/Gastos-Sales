package com.cu.gastossales.SearchProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cu.gastossales.R;
import com.cu.gastossales.SearchCodes.provider_info;
import com.cu.gastossales.databinding.FragmentSearchProviderBinding;
import com.cu.gastossales.model.sales_provider_data;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class search_provider extends Fragment {

    FragmentSearchProviderBinding binding;
    DatabaseReference reference;
    private Context contextNullSafe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentSearchProviderBinding.inflate(inflater, container, false);
        if (contextNullSafe == null) getContextNullSafety();
        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data");
        binding.progressBar2.setVisibility(View.GONE);
        binding.imageView.setOnClickListener(v->back());
        binding.textView5.setOnClickListener(v->{
            if(!binding.search.getText().toString().trim().equals("")){
                search(binding.search.getText().toString().trim());
            }
        });
        return binding.getRoot();

    }

    private void search(String number) {
        binding.progressBar2.setVisibility(View.VISIBLE);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(Objects.equals(snapshot.child(Objects.requireNonNull(ds.getKey())).child("Account_Information").child("phoneNumber").getValue(String.class), number)){
                        String phone_number=snapshot.child(Objects.requireNonNull(ds.getKey())).child("Account_Information").child("phoneNumber").getValue(String.class);
                        String uid= ds.getKey();
                        sales_provider_data sales_provider_data=new sales_provider_data(phone_number,"-",uid,"");
                        binding.progressBar2.setVisibility(View.GONE);
                        Bundle bundle=new Bundle();
                        bundle.putSerializable("provider_data_sending", sales_provider_data);
                        provider_info provider_info=new provider_info();
                        provider_info.setArguments(bundle);
                        ((FragmentActivity) getContextNullSafety()).getSupportFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations( R.anim.enter_from_right, R.anim.exit_to_left,R.anim.enter_from_left, R.anim.exit_to_right)
                                .add(R.id.swipeee,provider_info)
                                .addToBackStack(null)
                                .commit();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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