package com.ecommerce.merchant.fypproject.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ecommerce.merchant.fypproject.R;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;


public class BoostCartAdapter extends RecyclerView.Adapter<BoostCartAdapter.ViewHolder> {

    private final Context context;

    private final List<BoostItem> dataAdapters;
    private final DecimalFormat df2 = new DecimalFormat("0.00");
    private final boolean[] checked;
    private double cartsubtotal=0;
    private boolean triggerValue=false,isChk=false;
    private final OnItemClick mCallback;
    private String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";


    public BoostCartAdapter(List<BoostItem> getDataAdapter, Context context,OnItemClick listener){

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;
        checked=new boolean[dataAdapters.size()];
        this.mCallback = listener;

    }

    @Override
    public BoostCartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkout_card_item, parent, false);


        return new BoostCartAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BoostCartAdapter.ViewHolder Viewholder, final int position) {
             final BoostItem dataAdapterOBJ = dataAdapters.get(position);

        Viewholder.price.setText(dataAdapterOBJ.getPrice());
        // String date = dataAdapterOBJ.getSDate().substring(0,11) ;
        String title = dataAdapterOBJ.getProdname();
        if (title.length()>20) {
            title = title.substring(0,19);
        }

        Viewholder.prodname.setText(title);
        Viewholder.dayText.setText(dataAdapterOBJ.getPeriod());
        Viewholder.addBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentDay=Integer.parseInt(dataAdapters.get(Viewholder.getAdapterPosition()).getPeriod());
                currentDay++;
                Viewholder.dayText.setText(Integer.toString(currentDay));
                dataAdapters.get(Viewholder.getAdapterPosition()).setPeriod(Integer.toString(currentDay));
                mCallback.onClick(Double.toString(calculateTotal()),null,null,null);
            }
        });

        Viewholder.minusBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentDay=Integer.parseInt(dataAdapters.get(Viewholder.getAdapterPosition()).getPeriod());
                if(currentDay>1){
                    currentDay--;
                    Viewholder.dayText.setText(Integer.toString(currentDay));
                    dataAdapters.get(Viewholder.getAdapterPosition()).setPeriod(Integer.toString(currentDay));
                }
                mCallback.onClick(Double.toString(calculateTotal()),null,null,null);

            }
        });

        Viewholder.price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.e("kena b4",Viewholder.price.getText().toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    Double x=Double.parseDouble(Viewholder.price.getText().toString());


                    Log.e("days",Viewholder.dayText.getText().toString());


                    if(x<.1){
                        Viewholder.price.setText("0.10");
                        dataAdapters.get(Viewholder.getAdapterPosition()).setPrice("0.10");
                    }else{
                        dataAdapters.get(Viewholder.getAdapterPosition()).setPrice(Viewholder.price.getText().toString());

                    }




                }catch (Exception e){
                    Viewholder.price.setText("0.10");
                    dataAdapters.get(Viewholder.getAdapterPosition()).setPrice("0.10");


                }

                mCallback.onClick(Double.toString(calculateTotal()),null,null,null);


            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.e("kena after",Viewholder.price.getText().toString());
            }
        });


//

        Viewholder.dayText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try{
                    Integer y=Integer.parseInt(Viewholder.dayText.getText().toString());
                    if(y<1){
                        dataAdapters.get(Viewholder.getAdapterPosition()).setPeriod("1");
                    }else{
                        dataAdapters.get(Viewholder.getAdapterPosition()).setPeriod(Viewholder.dayText.getText().toString());
                    }
                }catch (Exception e){
                    Viewholder.dayText.setText("1");
                    dataAdapters.get(Viewholder.getAdapterPosition()).setPeriod("1");
                }

                mCallback.onClick(Double.toString(calculateTotal()),null,null,null);



            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Glide.with(context)
                .load(dataAdapterOBJ.getUrl()) // image url
                .apply(new RequestOptions()
                        .placeholder(R.drawable.photo) // any placeholder to load at start
                        .error(R.drawable.photo)  // any image in case of error
                        .override(200, 200) // resizing
                        .centerCrop())
                .into(Viewholder.prodimage);

        Viewholder.checkBox.setChecked(checked[Viewholder.getAdapterPosition()]);
        Viewholder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked[Viewholder.getAdapterPosition()] = Viewholder.checkBox.isChecked();

                if(loopChk()){
                    mCallback.onClick("check",null,null,null);
                }else{
                    mCallback.onClick("uncheck",null,null,null);
                }
                mCallback.onClick(Double.toString(calculateTotal()),null,null,null);
                //here adadasdasdasdasdasdasdasdsdasdasdas

            }
        });

        Viewholder.removeBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onClick("refresh",null,null,null);
                JSON_HTTP_CALL(dataAdapters.get(Viewholder.getAdapterPosition()).getRetailerCartID());
            }
        });



    }

    @Override
    public int getItemCount() {

        return dataAdapters.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        final CheckBox checkBox;
        final ImageView prodimage,removeBut;
        final TextView prodname;
        //final Spinner daySpinner;
        final Button addBut,minusBut;
        final EditText price,dayText;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.itemCheckbox);
            prodimage = itemView.findViewById(R.id.prodImage);
            prodname = itemView.findViewById(R.id.prodname);
           // daySpinner = itemView.findViewById(R.id.daySpin);
            price = itemView.findViewById(R.id.boostPriceText);
            dayText=itemView.findViewById(R.id.dayText);
            addBut=itemView.findViewById(R.id.addBut);
            minusBut=itemView.findViewById(R.id.minusBut);
            removeBut=itemView.findViewById(R.id.removeBut);

        }
    }


    public void chkAll(boolean value){

        //   triggerChange=true;
        triggerValue=value;
        isChk=true;

        for(int a=0;a<dataAdapters.size();a++){
            checked[a]=value;
            Log.d("trigger no.",Integer.toString(a)+Boolean.toString(checked[a]));

        }
        notifyItemRangeChanged(0,dataAdapters.size());
       // isChk=false;
        //triggerChange=false;

    }

    private boolean cartIsChecked(int index){
        return checked[index];
    }
    public boolean[] getCheckedList(){
        return checked;
    }
    public List<BoostItem> getCartItemList(){
        return dataAdapters;
    }

    public double calculateTotal(){
        cartsubtotal=0;
        for(int a=0;a<dataAdapters.size();a++){
            if(cartIsChecked(a)){
                cartsubtotal=cartsubtotal+Integer.parseInt(dataAdapters.get(a).getPeriod())*Double.parseDouble(dataAdapters.get(a).getPrice());
            }
        }


        isChk=false;
        Log.d("total",df2.format(cartsubtotal));
        return cartsubtotal;
    }


    private boolean loopChk(){
        for(int a=0;a<dataAdapters.size();a++){
            if(!cartIsChecked(a)){
                return false;
            }
        }
        return true;
    }

    private void JSON_HTTP_CALL(final String cartID) {

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //placeOrderDialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                mCallback.onClick("done refresh",null,null,null);

                Log.d("Response", response);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                HashMapParams.put("removeCart", cartID);
                return ProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();


    }


}
