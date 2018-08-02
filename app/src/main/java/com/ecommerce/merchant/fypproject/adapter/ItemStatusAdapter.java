package com.ecommerce.merchant.fypproject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ecommerce.merchant.fypproject.R;

import java.util.List;

public class ItemStatusAdapter extends RecyclerView.Adapter<ItemStatusAdapter.ViewHolder>{
    private final List<ItemStatus> dataAdapters;
    private final Context context;

    public  ItemStatusAdapter(List<ItemStatus> getDataAdapter, Context context){

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;

        boolean[] checked = new boolean[dataAdapters.size()];
        if(dataAdapters.size()>0){
            checked[0]=true;

        }

    }
    @Override
    public ItemStatusAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_card, parent, false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemStatusAdapter.ViewHolder Viewholder, final int position) {

        final ItemStatus dataAdapterOBJ = dataAdapters.get(position);
        Viewholder.custName.setText(dataAdapterOBJ.getCustName());
        Viewholder.orderID.setText(dataAdapterOBJ.getOrderID());
        Viewholder.itemName.setText(dataAdapterOBJ.getItemName());
       Viewholder.variant.setText(dataAdapterOBJ.getVariant());
       Viewholder.qty.setText(dataAdapterOBJ.getQty());
       Viewholder.date.setText(dataAdapterOBJ.getDateORder());
       Viewholder.status.setText(dataAdapterOBJ.getStatus());

        Glide.with(context)
                .load(dataAdapterOBJ.getPicURL()) // image url
                .apply(new RequestOptions()
                        .placeholder(R.drawable.photo) // any placeholder to load at start
                        .error(R.drawable.photo)  // any image in case of error
                        .override(500, 500) // resizing
                        .centerCrop())
                .into(Viewholder.prodImage);  // imageview object

    }
    class ViewHolder extends RecyclerView.ViewHolder{

        final ImageView prodImage;
        final TextView orderID;
        final TextView custName;
        final TextView itemName;
        final TextView variant;
        final TextView qty;
        final TextView date;
        final TextView status;

        ViewHolder(View itemView) {

            super(itemView);

            prodImage=itemView.findViewById(R.id.prodImage);
            orderID=itemView.findViewById(R.id.orderidText);
            custName=itemView.findViewById(R.id.custName);
            itemName=itemView.findViewById(R.id.itemText);
            variant=itemView.findViewById(R.id.variantText);
            qty=itemView.findViewById(R.id.QtyTxt);
            date=itemView.findViewById(R.id.datetxt);
            status=itemView.findViewById(R.id.itemStatus);



        }
    }
    @Override
    public int getItemCount() {

        return dataAdapters.size();
    }
}
