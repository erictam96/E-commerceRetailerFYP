package com.ecommerce.merchant.fypproject.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ecommerce.merchant.fypproject.R;

import java.util.List;

public class CancelItemAdapter extends RecyclerView.Adapter<CancelItemAdapter.ViewHolder>  {
    private final List<CancelItem> dataAdapters;
    private final Context context;
    PackItemAdapter.ViewHolder currentView;



    public  CancelItemAdapter(List<CancelItem> getDataAdapter, Context context){

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;
        //firebaseAuth = FirebaseAuth.getInstance();
        //this.mCallback = listener;

    }

    @Override
    public CancelItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cancel_card, parent, false);

        // delayCancelDialog();
        return new CancelItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CancelItemAdapter.ViewHolder Viewholder, final int position) {
        final CancelItem dataAdapterOBJ = dataAdapters.get(position);

        Viewholder.status.setText(dataAdapterOBJ.getStatus());
        Viewholder.desc.setText(dataAdapterOBJ.getDesc());
        if (dataAdapterOBJ.getDesc().equalsIgnoreCase("")) {

            Viewholder.desc.setText("---NA---");
        }else{
            Viewholder.desc.setText(dataAdapterOBJ.getDesc());
        }

        Viewholder.count.setText(dataAdapterOBJ.getCount());

    }

    class ViewHolder extends RecyclerView.ViewHolder{
        final TextView status;
        final TextView desc;
        final TextView count;

        //ImageButton donepackbut,soldoutbut;

        ViewHolder(View itemView) {

            super(itemView);

            status=itemView.findViewById(R.id.cancelType);
            desc=itemView.findViewById(R.id.cancelDetail);
            count=itemView.findViewById(R.id.count);

        }
    }

    @Override
    public int getItemCount() {

        return dataAdapters.size();
    }
}
