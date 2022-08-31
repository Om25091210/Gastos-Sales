package com.cu.gastossales.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cu.gastossales.ASM;
import com.cu.gastossales.R;
import com.cu.gastossales.TL;

import java.util.List;

public class TL_Adapter extends RecyclerView.Adapter<TL_Adapter.ViewHolder> {

    List<String> today_asm_provier;
    List<String> today_asm_name;
    List<String> today_asm_revenue;
    List<String> sales_code;
    Context context;

    public TL_Adapter(Context mainActivity, List<String> today_asm_name, List<String> today_asm_provier, List<String> today_asm_revenue, List<String> sales_code) {
        this.context = mainActivity;
        this.today_asm_name = today_asm_name;
        this.today_asm_provier = today_asm_provier;
        this.today_asm_revenue = today_asm_revenue;
        this.sales_code = sales_code;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tl, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.asm_name.setText(today_asm_name.get(position));
        holder.today_pro.setText(today_asm_provier.get(position));
        holder.today_revenue.setText(today_asm_revenue.get(position));
        holder.layout.setOnClickListener(view -> {
            Intent intent = new Intent(context, TL.class);
            intent.putExtra("sales-code", sales_code.get(holder.getAdapterPosition()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return today_asm_name.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout layout;
        TextView asm_name, today_pro, today_revenue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layout);
            asm_name = itemView.findViewById(R.id.asm_name);
            today_pro = itemView.findViewById(R.id.textView21);
            today_revenue = itemView.findViewById(R.id.textView22);
        }
    }
}