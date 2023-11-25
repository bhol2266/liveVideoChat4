package com.bhola.livevideochat4;

import android.app.Activity;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ProductDetails;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class Vip_CustomAdapter extends RecyclerView.Adapter<Vip_CustomAdapter.ViewHolder> {

    VipMembership context;
    List<ProductDetails> productDetailsList;
    List<ProductDetails> mlist_offer;
    LayoutInflater inflater;
    BillingClient billingClient;
    LinearLayout progressBar;
    String offer;

    public Vip_CustomAdapter(VipMembership ctx, List<ProductDetails> productDetailsList, BillingClient billingClient, LinearLayout progressBar, String offer, ArrayList<ProductDetails> mlist_offer) {
        this.context = ctx;
        this.productDetailsList = productDetailsList;
        this.mlist_offer = mlist_offer;
        this.billingClient = billingClient;
        inflater = LayoutInflater.from(ctx);
        this.progressBar = progressBar;
        this.offer = offer;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.vip_grid_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        TextView coinsCount = holder.itemView.findViewById(R.id.coinsCount);
        TextView price = holder.itemView.findViewById(R.id.price);
        TextView mrp = holder.itemView.findViewById(R.id.mrp);
        TextView discountPercent = holder.itemView.findViewById(R.id.discountPercent);

        ProductDetails productDetails = productDetailsList.get(position);

        if (offer.equals("with offer")) {
            productDetails = mlist_offer.get(position);
            discountPercent.setVisibility(View.VISIBLE);
        }

        if (position == 0) {
            coinsCount.setText("200");
        } else if (position == 1) {
            coinsCount.setText("500");
        } else if (position == 2) {
            coinsCount.setText("3000");
        }

        Log.d(MyApplication.TAG, "position: " + productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());

        price.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
        if (offer.equals("with offer")) {
            ProductDetails productDetails1 = productDetailsList.get(position);
            mrp.setVisibility(View.VISIBLE);
            mrp.setText(productDetails1.getOneTimePurchaseOfferDetails().getFormattedPrice().replace(".00", ""));
            mrp.setPaintFlags(mrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

    }

    @Override
    public int getItemCount() {
        return productDetailsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
