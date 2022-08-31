package com.cu.gastossales.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.cu.gastossales.R;
import com.cu.gastossales.SearchCodes.Sales_history;
import com.cu.gastossales.SearchCodes.provider_info;
import com.cu.gastossales.model.sales_code_data;
import com.cu.gastossales.model.sales_provider_data;

import java.util.List;

public class sales_provider_Adapter extends RecyclerView.Adapter<sales_provider_Adapter.ViewHolder> {

    List<sales_provider_data> list;
    Context context;

    public sales_provider_Adapter(List<sales_provider_data> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_number_sales, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.number.setText(list.get(position).getNumber());
        holder.date_time.setText(list.get(position).getDate());
        holder.amount.setText(list.get(position).getOrder_amount());
        holder.layout.setOnClickListener(v->{
            sales_provider_data sales_provider_data=new sales_provider_data(list.get(position).getNumber(),list.get(position).getDate(),list.get(position).getUid(),list.get(position).getOrder_amount());

            Bundle bundle=new Bundle();
            bundle.putSerializable("provider_data_sending", sales_provider_data);
            provider_info provider_info=new provider_info();
            provider_info.setArguments(bundle);
            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations( R.anim.enter_from_right, R.anim.exit_to_left,R.anim.enter_from_left, R.anim.exit_to_right)
                    .add(R.id.constraint,provider_info)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{

        TextView number,date_time,amount;
        ConstraintLayout layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            number=itemView.findViewById(R.id.number);
            amount=itemView.findViewById(R.id.amount);
            date_time=itemView.findViewById(R.id.date_time);
            layout=itemView.findViewById(R.id.layout);
        }
    }
}
