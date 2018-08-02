package com.ecommerce.merchant.fypproject.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ecommerce.merchant.fypproject.R;


import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Eric on 02-Nov-17.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>  {

    private final Context context;

    private final List<Product> dataAdapters;


    private final DecimalFormat df2 = new DecimalFormat("0.00");

    public RecyclerViewAdapter(List<Product> getDataAdapter, Context context){

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder Viewholder, int position) {


        Product dataAdapterOBJ = dataAdapters.get(position);


       // String date = dataAdapterOBJ.getSDate().substring(0,11) ;
        String title = dataAdapterOBJ.getProdName();
        if (title.length()>20) {
            title = title.substring(0,19);
        }
        Viewholder.itemCardTitle.setText(title);
        String itemCardPrice = "RM"+df2.format(dataAdapterOBJ.getProdPrice());
        Viewholder.itemCardPrice.setText(itemCardPrice);

        if(dataAdapterOBJ.getProdDiscount()>0) {
            Viewholder.itemCardPrice.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            Viewholder.itemCardPrice.setVisibility(View.VISIBLE);
            Viewholder.itemCardPromotion.setVisibility(View.VISIBLE);
        }else{
            Viewholder.itemCardPrice.setPaintFlags(0);
            Viewholder.itemCardPrice.setVisibility(View.INVISIBLE);
            Viewholder.itemCardPromotion.setVisibility(View.INVISIBLE);
        }
        String itemCardPromo = "-"+Integer.toString(dataAdapterOBJ.getProdDiscount())+"%";
        Viewholder.itemCardSeller.setText(dataAdapterOBJ.getShopName());
        Viewholder.itemCardPromotion.setText(itemCardPromo);
        Viewholder.itemCardPromotion.bringToFront();
        double promo=dataAdapterOBJ.getProdPrice()*(100-dataAdapterOBJ.getProdDiscount())/100;
        String promotionPrice = "RM"+df2.format(promo);
        Viewholder.promoPrice.setText(promotionPrice);

        Glide.with(context)
                .load(dataAdapterOBJ.getProductURL()) // image url
                .apply(new RequestOptions()
                        .placeholder(R.drawable.photo) // any placeholder to load at start
                        .error(R.drawable.photo)  // any image in case of error
                        .override(200, 200) // resizing
                        .centerCrop())
                .into(Viewholder.itemCardImage);

    }

    @Override
    public int getItemCount() {

        return dataAdapters.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        final TextView itemCardPromotion;
        final TextView itemCardTitle;
        final TextView itemCardPrice;
        final TextView itemCardSeller;
        final TextView promoPrice;
        final ImageView itemCardImage ;

        ViewHolder(View itemView) {
            super(itemView);
            itemCardPromotion = itemView.findViewById(R.id.itemCardPromotion);
            itemCardTitle = itemView.findViewById(R.id.itemCardTitle);
            itemCardPrice = itemView.findViewById(R.id.itemCardPrice);
            itemCardSeller = itemView.findViewById(R.id.itemCardSeller);
            itemCardImage = itemView.findViewById(R.id.itemCardImage);
            promoPrice=itemView.findViewById(R.id.itemCardPromoPrice);
        }
    }

    public void addList(List<Product> dataAdapter){
        dataAdapters.addAll(dataAdapter);
        notifyItemRangeChanged(0,dataAdapters.size());
    }
    public void refreshList(){
        notifyItemRangeChanged(0,dataAdapters.size());
    }
    public List<Product>getListData(){
        return dataAdapters;
    }

}
