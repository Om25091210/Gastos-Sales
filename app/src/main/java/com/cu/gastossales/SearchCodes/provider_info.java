package com.cu.gastossales.SearchCodes;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cu.gastossales.Adapter.package_info_Adapter;
import com.cu.gastossales.R;
import com.cu.gastossales.databinding.FragmentProviderInfoBinding;
import com.cu.gastossales.model.Account_Information;
import com.cu.gastossales.model.Shop_Information;
import com.cu.gastossales.model.package_info_data;
import com.cu.gastossales.model.sales_provider_data;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class provider_info extends Fragment {

    sales_provider_data sales_provider_data;
    FragmentProviderInfoBinding binding;
    DatabaseReference reference;
    private Context contextNullSafe;
    List<package_info_data> package_info_dataList=new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentProviderInfoBinding.inflate(inflater, container, false);
        if (contextNullSafe == null) getContextNullSafety();
        if(getArguments()!=null){
            sales_provider_data= (sales_provider_data) getArguments().getSerializable("provider_data_sending");
        }

        binding.search.setText(sales_provider_data.getNumber());
        binding.phone.setText(sales_provider_data.getNumber());

        reference= FirebaseDatabase.getInstance().getReference().child("Merchant_data");

        get_information();

        binding.dis1.setOnClickListener(v->{
            if(binding.linearLayout.getVisibility()==View.GONE) {
                binding.drop1.setImageResource(R.drawable.ic_drop_up);
                binding.linearLayout.setVisibility(View.VISIBLE);
                binding.linearLayout7.setVisibility(View.VISIBLE);
            }
            else{
                binding.drop1.setImageResource(R.drawable.ic_drop_down);
                binding.linearLayout.setVisibility(View.GONE);
                binding.linearLayout7.setVisibility(View.GONE);
            }
        });

        binding.dis1.setVisibility(View.GONE);
        binding.dis2.setVisibility(View.GONE);
        binding.dis3.setVisibility(View.GONE);

        binding.textView18.setVisibility(View.GONE);

        binding.dis2.setOnClickListener(v->{
            if(binding.linearLayout0.getVisibility()==View.GONE) {
                binding.drop2.setImageResource(R.drawable.ic_drop_up);
                binding.linearLayout0.setVisibility(View.VISIBLE);
                binding.linearLayout8.setVisibility(View.VISIBLE);
            }
            else{
                binding.drop2.setImageResource(R.drawable.ic_drop_down);
                binding.linearLayout0.setVisibility(View.GONE);
                binding.linearLayout8.setVisibility(View.GONE);
            }
        });

        binding.dis3.setOnClickListener(v->{
            if(binding.linearLayout1.getVisibility()==View.GONE){
                binding.drop2.setImageResource(R.drawable.ic_drop_up);
                binding.linearLayout1.setVisibility(View.VISIBLE);
                binding.linearLayout9.setVisibility(View.VISIBLE);
            }
            else{
                binding.drop2.setImageResource(R.drawable.ic_drop_up);
                binding.linearLayout1.setVisibility(View.GONE);
                binding.linearLayout9.setVisibility(View.GONE);
            }
        });

        LinearLayoutManager mManager = new LinearLayoutManager(getContextNullSafety());
        binding.packageRecycler.setItemViewCacheSize(500);
        binding.packageRecycler.setDrawingCacheEnabled(true);
        binding.packageRecycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.packageRecycler.setLayoutManager(mManager);

        binding.imageView.setOnClickListener(v->back());

        return binding.getRoot();
    }

    private void get_information() {
        if (sales_provider_data.getUid() != null) {
            reference.child(sales_provider_data.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Account_Information account_information = snapshot.child("Account_Information").getValue(Account_Information.class);
                    Shop_Information shop_information = snapshot.child("Shop_Information").getValue(Shop_Information.class);
                    String expiry_membership = snapshot.child("membership").child("expiry").getValue(String.class);
                    if (snapshot.child("package_info").exists()) {
                        binding.textView18.setVisibility(View.VISIBLE);
                        for (DataSnapshot ds : snapshot.child("package_info").getChildren()) {
                            package_info_dataList.add(snapshot.child("package_info").child(ds.getKey()).getValue(package_info_data.class));
                        }
                    }
                    package_info_Adapter package_info_adapter = new package_info_Adapter(package_info_dataList);
                    package_info_adapter.notifyDataSetChanged();
                    if (binding.packageRecycler != null)
                        binding.packageRecycler.setAdapter(package_info_adapter);

                    set_shop_information(shop_information);
                    set_acc_info(account_information);
                    set_membership(expiry_membership);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }
    private void set_membership(String expiry) {

        binding.expiry.setText(expiry);
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date date1 = sdf.parse(expiry+"");
            Date date2 = sdf.parse(sdf.format(date));
            if(date1.compareTo(date2) > 0)
            {
                binding.status.setText("Active");
                binding.status.setTextColor(Color.parseColor("#16A34A"));
            }
            binding.status.setText("Inactive");
            binding.status.setTextColor(Color.parseColor("#FF5C5C"));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void set_acc_info(Account_Information account_information) {

        binding.accOwnerName.setText(account_information.getOwnerName());
        binding.emailAddress.setText(account_information.getEmailAddress());
        binding.regTime.setText(sales_provider_data.getDate());
        binding.salesCode.setText(account_information.getSalesCode());
    }

    private void set_shop_information(Shop_Information shop_information) {

        Uri uri = Uri.parse(shop_information.getShopImageUri());
        binding.myImageView.setImageURI(uri);
        binding.shopName.setText(shop_information.getShopName());
        binding.category.setText(shop_information.getCategory());
        binding.shopAddress.setText(shop_information.getShopAddress());
        binding.shopArea.setText(shop_information.getShopArea());
        binding.shopDistrict.setText(shop_information.getShopDistrict());
        binding.shopState.setText(shop_information.getShopDistrict());

        if(shop_information.getDiscounts().size()==1) {
            binding.dis1.setVisibility(View.VISIBLE);
            binding.disPer1.setText(shop_information.getDiscounts().get(0).getDiscountPercentage()+"");
            binding.minDis1.setText(shop_information.getDiscounts().get(0).getMinBillAmount()+"");
        }
        if(shop_information.getDiscounts().size()==2) {
            binding.dis2.setVisibility(View.VISIBLE);
            binding.disPer2.setText(shop_information.getDiscounts().get(1).getDiscountPercentage()+"");
            binding.minDis2.setText(shop_information.getDiscounts().get(1).getMinBillAmount()+"");
        }
        if(shop_information.getDiscounts().size()==3) {
            binding.dis3.setVisibility(View.VISIBLE);
            binding.disPer3.setText(shop_information.getDiscounts().get(2).getDiscountPercentage()+"");
            binding.minDis3.setText(shop_information.getDiscounts().get(2).getMinBillAmount()+"");
        }
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
    private void back() {
        FragmentManager fm=((FragmentActivity) getContextNullSafety()).getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        if(fm.getBackStackEntryCount()>0) {
            fm.popBackStack();
        }
        ft.commit();
    }
}