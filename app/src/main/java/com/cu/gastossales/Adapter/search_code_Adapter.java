package com.cu.gastossales.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.cu.gastossales.R;
import com.cu.gastossales.SearchCodes.Sales_history;
import com.cu.gastossales.model.sales_code_data;

import java.util.List;

public class search_code_Adapter extends RecyclerView.Adapter<search_code_Adapter.ViewHolder> {

    List<sales_code_data> list;
    Context context;

    public search_code_Adapter(Context context,List<sales_code_data> list) {
        this.list = list;
        this.context=context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sales_code, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.sales_code.setText(list.get(position).getSales_code());
        holder.total_provider.setText(list.get(position).getTotal_providers());
        holder.layout.setOnClickListener(v->{
            sales_code_data sales_code_data=new sales_code_data(list.get(position).getSales_code(),list.get(position).getSales_name(),list.get(position).getTotal_providers());

            Bundle bundle=new Bundle();
            bundle.putSerializable("sales_data_sending", sales_code_data);
            Sales_history sales_history=new Sales_history();
            sales_history.setArguments(bundle);
            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations( R.anim.enter_from_right, R.anim.exit_to_left,R.anim.enter_from_left, R.anim.exit_to_right)
                    .add(R.id.constraint,sales_history)
                    .addToBackStack(null)
                    .commit();

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{

        TextView sales_code,total_provider;
        LinearLayout layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            sales_code=itemView.findViewById(R.id.sales_code);
            total_provider=itemView.findViewById(R.id.total_proider);
            layout=itemView.findViewById(R.id.layout);
        }
    }
}
