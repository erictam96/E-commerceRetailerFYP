package com.ecommerce.merchant.fypproject.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.ecommerce.merchant.fypproject.R;

import java.util.HashMap;
import java.util.List;

public class SerialCodeAssignedAdapter extends RecyclerView.Adapter<SerialCodeAssignedAdapter.ViewHolder> {
    private final List<SerialCode> dataAdapter;
    private final Context context;
    private final OnItemClick mCallback;
    private final String HTTP_URL = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    private ProgressDialog assignDialog;

    //String NotificationPathOnServer = "http://10.0.2.2/cashierbookPHP/Eric/customer_manage_user.php";

    public  SerialCodeAssignedAdapter(List<SerialCode> getDataAdapter, Context context, OnItemClick listener){
        super();
        this.dataAdapter = getDataAdapter;
        this.context = context;
        mCallback=listener;
    }
    @Override
    public SerialCodeAssignedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.serial_list, parent, false);

        SerialCodeAssignedAdapter.ViewHolder viewHolder = new SerialCodeAssignedAdapter.ViewHolder(view);

        assignDialog=new ProgressDialog(context);
        assignDialog.setCancelable(true);
        assignDialog.setTitle(R.string.loading);
        assignDialog.setMessage(context.getResources().getString(R.string.assigningSerial));
        assignDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        return viewHolder;
    }
    @Override
    public void onBindViewHolder(final SerialCodeAssignedAdapter.ViewHolder Viewholder, final int position) {

        // checked=new boolean[dataAdapters.size()];
        final SerialCode dataAdapterOBJ = dataAdapter.get(position);
        Viewholder.serialCode.setText(dataAdapterOBJ.getSerial());
        Viewholder.serialCode.setClickable(false);

        Viewholder.removeBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSON_HTTP_CALL(Viewholder.getAdapterPosition());
            }
        });
    }


    private void JSON_HTTP_CALL(final int clickedPosition) {
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.d("Response", response);
                assignDialog.dismiss();
                mCallback.onClick("refresh",null,null,null);

            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();
                HashMapParams.put("removeAssignedSerial", dataAdapter.get(clickedPosition).getSerialID());


                return ProcessClass.HttpRequest(HTTP_URL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

    @Override
    public int getItemCount() {
        return dataAdapter.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        final Button serialCode;
        final ImageButton removeBut;

        ViewHolder(View itemView) {
            super(itemView);
            serialCode=itemView.findViewById(R.id.serialCodeBut);
            removeBut=itemView.findViewById(R.id.removeSerialBut);

        }
    }


}
