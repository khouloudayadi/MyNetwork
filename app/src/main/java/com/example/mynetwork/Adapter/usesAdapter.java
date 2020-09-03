package com.example.mynetwork.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynetwork.Model.uses;
import com.example.mynetwork.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class usesAdapter extends RecyclerView.Adapter<usesAdapter.UsesViewHolder> {
    List<uses> listeUses;
    Context context;

    public usesAdapter(Context context, List<uses> listeUses) {
        this.context = context;
        this.listeUses = listeUses;
    }

    public UsesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_uses = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_quality_uses,parent,false);
        return new UsesViewHolder(item_uses);
    }

    @Override
    public void onBindViewHolder(@NonNull UsesViewHolder holder, int position) {
        uses currentItem = listeUses.get(position);
        holder.txt_title_uses.setText(currentItem.getTitleUses());
        holder.txt_title_uses.setText(currentItem.getTitleUses());
        holder.img_uses.setImageResource(currentItem.getImgUses());
        holder.img_quality_uses1.setImageResource(currentItem.getQualityUses1());
        holder.img_quality_uses2.setImageResource(currentItem.getQualityUses2());
        holder.img_quality_uses3.setImageResource(currentItem.getQualityUses3());
        holder.img_quality_uses4.setImageResource(currentItem.getQualityUses4());
        holder.img_quality_uses5.setImageResource(currentItem.getQualityUses5());
    }

    @Override
    public int getItemCount() {
        return listeUses.size();
    }

    public static class UsesViewHolder extends RecyclerView.ViewHolder {
       @BindView(R.id.txt_title_uses) TextView txt_title_uses;
       @BindView(R.id.img_uses) ImageView img_uses;
       @BindView(R.id.img_quality_uses1) ImageView img_quality_uses1;
       @BindView(R.id.img_quality_uses2) ImageView img_quality_uses2;
       @BindView(R.id.img_quality_uses3) ImageView img_quality_uses3;
       @BindView(R.id.img_quality_uses4) ImageView img_quality_uses4;
       @BindView(R.id.img_quality_uses5) ImageView img_quality_uses5;

        Unbinder unbinder;
        public UsesViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);

        }
    }

}
