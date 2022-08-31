package com.cu.gastossales.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cu.gastossales.R;
import com.cu.gastossales.model.package_info_data;

import org.w3c.dom.Text;

import java.util.List;

public class package_info_Adapter extends RecyclerView.Adapter<package_info_Adapter.ViewHolder> {

    List<package_info_data> package_info_dataList;

    public package_info_Adapter(List<package_info_data> package_info_dataList) {
        this.package_info_dataList=package_info_dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_package_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.package_name.setText(package_info_dataList.get(position).getPackage_name());
        holder.amount.setText(package_info_dataList.get(position).getOrder_amount());
        holder.date.setText(package_info_dataList.get(position).getTxn_date());
        holder.time.setText(package_info_dataList.get(position).getTime());
        holder.package_number.setText("Package "+position);

        holder.layout.setOnClickListener(v->{
            if(holder.linearLayout.getVisibility()==View.GONE){
                holder.linearLayout.setVisibility(View.VISIBLE);
                holder.linearLayout2.setVisibility(View.VISIBLE);
                holder.linearLayout3.setVisibility(View.VISIBLE);
                holder.linearlayou4.setVisibility(View.VISIBLE);
                holder.drop.setImageResource(R.drawable.ic_drop_up);
            }
            else{
                holder.linearLayout.setVisibility(View.GONE);
                holder.linearLayout2.setVisibility(View.GONE);
                holder.linearLayout3.setVisibility(View.GONE);
                holder.linearlayou4.setVisibility(View.GONE);
                holder.drop.setImageResource(R.drawable.ic_drop_down);
            }
        });

    }

    @Override
    public int getItemCount() {
        return package_info_dataList.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{

        TextView package_number,package_name,amount,date,time;
        ImageView drop;
        ConstraintLayout layout;
        LinearLayout linearLayout,linearLayout2,linearLayout3,linearlayou4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout=itemView.findViewById(R.id.layout);
            linearLayout=itemView.findViewById(R.id.linearLayout);
            linearLayout2=itemView.findViewById(R.id.linearLayout2);
            linearLayout3=itemView.findViewById(R.id.linearLayout3);
            linearlayou4=itemView.findViewById(R.id.linearlayou4);
            drop=itemView.findViewById(R.id.drop);
            package_number=itemView.findViewById(R.id.textView15);
            package_name=itemView.findViewById(R.id.package_name);
            amount=itemView.findViewById(R.id.amount);
            date=itemView.findViewById(R.id.date);
            time=itemView.findViewById(R.id.time);
        }
    }
}
