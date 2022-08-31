package com.cu.gastossales.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.cu.gastossales.executive_home;
import com.cu.gastossales.model.sales_code_data;

import java.util.List;

public class Esales_Adapter extends RecyclerView.Adapter<Esales_Adapter.ViewHolder>{

    List<String> today_asm_provier;
    List<String> today_asm_name;
    List<String> today_asm_revenue;
    List<String> sales_code;
    Context context;

    public Esales_Adapter(Context mainActivity, List<String> today_asm_name, List<String> today_asm_provier, List<String> today_asm_revenue,List<String> sales_code) {
        this.context=mainActivity;
        this.today_asm_name=today_asm_name;
        this.today_asm_provier=today_asm_provier;
        this.today_asm_revenue=today_asm_revenue;
        this.sales_code=sales_code;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.executive_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.asm_name.setText(today_asm_name.get(position));
        holder.sales_code.setText(sales_code.get(position));
        holder.today_pro.setText(today_asm_provier.get(position));
        holder.today_revenue.setText(today_asm_revenue.get(position));
        holder.layout.setOnClickListener(v->{
            Intent intent=new Intent(context, executive_home.class);
            intent.putExtra("sales-code",sales_code.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return today_asm_name.size();
    }


    protected static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout layout;
        TextView asm_name,sales_code,today_pro,today_revenue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout=itemView.findViewById(R.id.layout);
            sales_code=itemView.findViewById(R.id.sales_code);
            asm_name=itemView.findViewById(R.id.name);
            today_pro=itemView.findViewById(R.id.total_provider);
            today_revenue=itemView.findViewById(R.id.total_revenue);
        }
    }
}
