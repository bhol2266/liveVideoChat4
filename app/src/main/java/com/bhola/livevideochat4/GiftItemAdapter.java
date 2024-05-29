package com.bhola.livevideochat4;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bhola.livevideochat4.Models.GiftItemModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GiftItemAdapter extends RecyclerView.Adapter<GiftItemAdapter.ViewHolder> {

    private List<GiftItemModel> itemList;
    private Context context;
    private int selectedItemPosition = VipMembership.selectedCard[0];

    public GiftItemAdapter(Context context, List<GiftItemModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gift_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GiftItemModel giftItemModel = itemList.get(position);
        String gift = (String) giftItemModel.getGiftName();
        int coin = (int) giftItemModel.getCoin();

        // Load and set the image from the asset folder
        try {
            InputStream ims = context.getAssets().open("gifts/" + gift + ".png");
            Drawable d = Drawable.createFromStream(ims, null);
            holder.giftImage.setImageDrawable(d);
            ims.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.giftName.setText(giftItemModel.getGiftName());
        holder.coin.setText(String.valueOf(coin));


        if (giftItemModel.isSelected()) {
            holder.giftLayout.setBackgroundResource(R.drawable.giftitem_border);
        } else {
            holder.giftLayout.setBackground(null);

        }

        holder.giftLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (GiftItemModel giftItemModel1 : itemList) {
                    giftItemModel1.setSelected(false);
                }

                giftItemModel.setSelected(true);

                notifyDataSetChanged(); // Notify adapter to refresh the UI
                holder.giftLayout.setBackgroundResource(R.drawable.giftitem_border);

                Activity activity = (Activity) context;
                String activityName = activity.getClass().getSimpleName();
                if (activityName.equals("Profile")) {
                    Profile.send.setAlpha(1);
                } else {
                    ChatScreen_User.send.setAlpha(1);

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView giftImage;
        TextView coin, giftName;
        LinearLayout giftLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            giftImage = itemView.findViewById(R.id.giftImage);
            coin = itemView.findViewById(R.id.coin);
            giftLayout = itemView.findViewById(R.id.giftLayout);
            giftName = itemView.findViewById(R.id.giftName);
        }
    }
}
