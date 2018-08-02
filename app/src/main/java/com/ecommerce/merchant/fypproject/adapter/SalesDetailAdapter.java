package com.ecommerce.merchant.fypproject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ecommerce.merchant.fypproject.R;

import java.util.List;

public class SalesDetailAdapter extends RecyclerView.Adapter<SalesDetailAdapter.ViewHolder>  {

    private final List<SalesDetail> dataAdapters;
    private final Context context;
    PackItemAdapter.ViewHolder currentView;



    public  SalesDetailAdapter(List<SalesDetail> getDataAdapter, Context context){

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;
        //firebaseAuth = FirebaseAuth.getInstance();
        //this.mCallback = listener;

    }

    @Override
    public SalesDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sales_detail_list, parent, false);

        // delayCancelDialog();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SalesDetailAdapter.ViewHolder Viewholder, final int position) {
        final SalesDetail dataAdapterOBJ = dataAdapters.get(position);
        Viewholder.prodNameText.setText(dataAdapterOBJ.getProductName());
        Viewholder.prodVarText.setText(dataAdapterOBJ.getProductVariant());
        Viewholder.prodPriceText.setText(dataAdapterOBJ.getProdPrice());
        Viewholder.prodQtyText.setText(dataAdapterOBJ.getProdQty());

    }

    class ViewHolder extends RecyclerView.ViewHolder{
        final TextView prodNameText;
        final TextView prodVarText;
        final TextView prodPriceText;
        final TextView prodQtyText;
        //ImageButton donepackbut,soldoutbut;

        ViewHolder(View itemView) {

            super(itemView);

            prodNameText=itemView.findViewById(R.id.prodNameList);
            prodVarText=itemView.findViewById(R.id.prodVariantList);
            prodPriceText=itemView.findViewById(R.id.prodPriceList);
            prodQtyText=itemView.findViewById(R.id.prodQtyList);
        }
    }

    @Override
    public int getItemCount() {

        return dataAdapters.size();
    }

}
