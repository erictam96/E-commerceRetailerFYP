package com.ecommerce.merchant.fypproject.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecommerce.merchant.fypproject.R;
import com.ecommerce.merchant.fypproject.SplashActivity;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PackItemAdapter extends RecyclerView.Adapter<PackItemAdapter.ViewHolder>implements OnItemClick {

    private final List<PackItem> dataAdapters;
    private final Context context;
    PackItemAdapter.ViewHolder currentView;
    private FirebaseAuth firebaseAuth;
    private final String ItemUpdatePathOnServer = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    private final String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    //private String PHPURL = "http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";
    private final OnItemClick mCallback;
    private Dialog delayCancelDialog,serialDialog;
    private String uid;
    private ProgressBar progressBar,serialProgressBar,assignedSerialProgressBar;
    private Button confirmCancelBut;
    private int clickedposition=0;
    private CountDownTimer previousTimer;
    private TextView prodName;
    private TextView prodVariant;
    private TextView remainingAssignSerial;
    private ImageButton exitBut;
    private RecyclerView serialRecycle;
    private RecyclerView assignedSerialRecycler;
    private RelativeLayout insertSerialRelative;
    private EditText newSerialText;
    private String orderid;
    private String prodvariant;
    private String prodcode;
    private String retailerid;
    String productName;
    private String qtyOrder;
    private RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    private List<SerialCode> serialList;
    private RecyclerView.Adapter recyclerViewadapter;

    @Override
    public void onClick(String value, String prodvar, String prodcode, String rid) {
        if(value.equalsIgnoreCase("refresh")){
            SERIAL_CODE_CALL();
            ASSIGNED_SERIAL_CODE_CALL();
        }
    }

    public  PackItemAdapter(List<PackItem> getDataAdapter, Context context, OnItemClick listener){
        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        this.mCallback = listener;
    }

    @Override
    public PackItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        PackItemAdapter.ViewHolder viewHolder = new PackItemAdapter.ViewHolder(view);
        delayCancelDialog();
        chooseSerialDialog();
        GetFirebaseAuth();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PackItemAdapter.ViewHolder Viewholder, final int position) {
        final PackItem dataAdapterOBJ = dataAdapters.get(position);
        Viewholder.orderidtxt.setText(dataAdapterOBJ.getOrderId());
        Viewholder.itemtxt.setText(dataAdapterOBJ.getItemName());
        Viewholder.vartxt.setText(dataAdapterOBJ.getVariant());
        Viewholder.qtytxt.setText(dataAdapterOBJ.getQty());
        Viewholder.datetxt.setText(dataAdapterOBJ.getDate());

        Viewholder.donepackbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderid=dataAdapterOBJ.getOrderId();
                prodvariant=dataAdapterOBJ.getVariant();
                prodcode= dataAdapterOBJ.getProdcode();
                retailerid=uid;
                qtyOrder=dataAdapterOBJ.getQty();

                String variant = "Product variant: "+prodvariant;
                String name="Product name: "+dataAdapterOBJ.getItemName();

                prodVariant.setText(variant);
                prodName.setText(name);

                //orderdetail product update to "ready to deliver"
                SERIAL_CODE_CALL();
                ASSIGNED_SERIAL_CODE_CALL();

                serialDialog.show();
            }
        });

        Viewholder.soldoutbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dataAdapterOBJ.getOrderId();
                PopupMenu popup = new PopupMenu(context, Viewholder.soldoutbut);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        //Toast.makeText(context,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                        if(item.getTitle().toString().equalsIgnoreCase("Delete order")){

                            clickedposition=Viewholder.getAdapterPosition();
                            delayCancelDialog.show();
                            progressBar.setVisibility(View.VISIBLE);
                            confirmCancelBut.setEnabled(false);
                            previousTimer=new CountDownTimer(10000, 50) {

                                public void onTick(long millisUntilFinished) {
                                    String confirmCancel = context.getResources().getString(R.string.confirm) + millisUntilFinished / 1000;
                                    confirmCancelBut.setText(confirmCancel);
                                    confirmCancelBut.setTextColor(context.getResources().getColor(R.color.transparentBlack));
                                    Long x=10000-millisUntilFinished;
                                    progressBar.setProgress(x.intValue());
                                }

                                public void onFinish() {
                                   confirmCancelBut.setEnabled(true);
                                   confirmCancelBut.setText(context.getResources().getString(R.string.confirm));
                                   confirmCancelBut.setTextColor(context.getResources().getColor(R.color.colorWhite));
                                   progressBar.setVisibility(View.GONE);
                                }
                            }.start();
                        }
                        return true;
                    }
                });

                popup.show();

            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        final TextView orderidtxt;
        final TextView itemtxt;
        final TextView vartxt;
        final TextView qtytxt;
        final TextView datetxt;
        final ImageButton donepackbut;
        final ImageButton soldoutbut;

        ViewHolder(View itemView) {

            super(itemView);

            orderidtxt=itemView.findViewById(R.id.orderidText);
            itemtxt=itemView.findViewById(R.id.itemText);
            vartxt=itemView.findViewById(R.id.variantText);
            qtytxt=itemView.findViewById(R.id.QtyTxt);
            datetxt=itemView.findViewById(R.id.datetxt);
            donepackbut=itemView.findViewById(R.id.donePackbut);
            soldoutbut=itemView.findViewById(R.id.soldOutBut);

        }
    }

    @Override
    public int getItemCount() {

        return dataAdapters.size();
    }

    private void JSON_UPDATE(final String orderid, final String prodvar, final String prodcode, final String rid){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mCallback.onClick("updating",null,null,null);
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response",response);
                mCallback.onClick("done update",null,null,null);


            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();
                HashMapParams.put("orderid", orderid);
                HashMapParams.put("prodvar",prodvar);
                HashMapParams.put("productcode",prodcode);
                HashMapParams.put("rid",rid);

                return imageProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    private void delayCancelDialog() {
        delayCancelDialog = new Dialog(context, R.style.MaterialDialogSheet);
        delayCancelDialog.setContentView(R.layout.delay_confirm_ui); // your custom view.
        delayCancelDialog.setCancelable(true);
        Objects.requireNonNull(delayCancelDialog.getWindow()).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        delayCancelDialog.getWindow().setGravity(Gravity.CENTER);

        confirmCancelBut = delayCancelDialog.findViewById(R.id.shortsellConfirmBut);
        Button cancelBut = delayCancelDialog.findViewById(R.id.shortsellCancelBut);
        progressBar=delayCancelDialog.findViewById(R.id.shortSellProgressBar);


        cancelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delayCancelDialog.dismiss();
            }
        });
        confirmCancelBut.setEnabled(false);
        confirmCancelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Answers.getInstance().logCustom(new CustomEvent("Pack Item")
                        .putCustomAttribute("Status","Delete Order"));
                String prodvariant=dataAdapters.get(clickedposition).getVariant();
                String prodcode= dataAdapters.get(clickedposition).getProdcode();
                String retailerid=uid;
                mCallback.onClick("cancel",prodvariant,prodcode,retailerid);
                delayCancelDialog.dismiss();
            }
        });
        delayCancelDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                previousTimer.cancel();
            }
        });

    }
    private void GetFirebaseAuth(){
        firebaseAuth=FirebaseAuth.getInstance();//get firebase object
        if(firebaseAuth.getCurrentUser()==null){
            Intent intent = new Intent(context, SplashActivity.class);
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            Toast.makeText(context,context.getResources().getString(R.string.sessionexp),Toast.LENGTH_LONG).show();
        }else uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    private void chooseSerialDialog() {
        serialDialog = new Dialog(context, R.style.MaterialDialogSheet);
        serialDialog.setContentView(R.layout.serial_confirm_dialog); // your custom view.
        serialDialog.setCancelable(true);
        Objects.requireNonNull(serialDialog.getWindow()).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        serialDialog.getWindow().setGravity(Gravity.CENTER);

        prodName = serialDialog.findViewById(R.id.confirmSerialNameLabel);
        prodVariant=serialDialog.findViewById(R.id.confirmSerialVariantLabel);
        exitBut=serialDialog.findViewById(R.id.confirmSerialExit);
        serialRecycle= serialDialog.findViewById(R.id.confirmSerialRecycler);
        assignedSerialRecycler=serialDialog.findViewById(R.id.assignedSerialRecycler);
        Button insertSerial = serialDialog.findViewById(R.id.insertSerialBut);
        Button donePackBut = serialDialog.findViewById(R.id.donePackBut);
        insertSerialRelative= serialDialog.findViewById(R.id.insertSerialRelative);
        newSerialText= serialDialog.findViewById(R.id.serialText);
        Button addSerialBut = serialDialog.findViewById(R.id.confirmNewSerialBut);
        serialProgressBar=serialDialog.findViewById(R.id.serialProgressBar);
        assignedSerialProgressBar=serialDialog.findViewById(R.id.assignedSerialProgressBar);
        remainingAssignSerial=serialDialog.findViewById(R.id.remainingAssignSerial);

        addSerialBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                INSERT_SERIAL_CODE_CALL();
                SERIAL_CODE_CALL();
                ASSIGNED_SERIAL_CODE_CALL();
            }
        });
        insertSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(insertSerialRelative.getVisibility()==View.GONE){
                    insertSerialRelative.setVisibility(View.VISIBLE);
                }else{
                    insertSerialRelative.setVisibility(View.GONE);
                }
            }
        });

        exitBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialDialog.dismiss();
            }
        });

        donePackBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answers.getInstance().logCustom(new CustomEvent("Pack Item")
                .putCustomAttribute("Status","Done Pack"));
                JSON_UPDATE(orderid,prodvariant,prodcode,retailerid);
                serialDialog.dismiss();
            }
        });

        serialRecycle.setHasFixedSize(true);
        layoutManagerOfrecyclerView = new LinearLayoutManager(context);
        serialRecycle.setLayoutManager(layoutManagerOfrecyclerView);

        assignedSerialRecycler.setHasFixedSize(true);
        layoutManagerOfrecyclerView = new LinearLayoutManager(context);
        assignedSerialRecycler.setLayoutManager(layoutManagerOfrecyclerView);

    }

    private void SERIAL_CODE_CALL(){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                //    progressDialog.show();
                serialProgressBar.setVisibility(View.VISIBLE);
                serialRecycle.setVisibility(View.INVISIBLE);
                assignedSerialRecycler.setVisibility(View.INVISIBLE);
                super.onPreExecute();
                // canceldialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Serial response",response);
                try{
                    SERIAL_ParseJSonResponse(response);
                }catch (Exception e){
                    Log.e("Serial response",e.toString());
                }

                // JSON_HTTP_CALL();
                //canceldialog.dismiss();
                //Toast.makeText(OrderConfirmActivity.this,"Done cancel order",Toast.LENGTH_SHORT).show();

            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                Log.d("retailer id",uid);
                HashMapParams.put("fetchSerial",prodcode);

                return imageProcessClass.HttpRequest(ItemUpdatePathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

    private void SERIAL_ParseJSonResponse(String array) throws JSONException {
        // product = new Product();
        JSONArray jarr = new JSONArray(array);//lv 1 array

        serialList=new ArrayList<>();
        for(int a=0;a<jarr.length();a++){
            SerialCode x=new SerialCode();
            JSONObject json;
            json=jarr.getJSONObject(a);

            x.setSerial(json.getString("serial"));
            x.setSerialID(json.getString("serialID"));
            x.setOrderID(orderid);
            x.setProdVariant(prodvariant);

            serialList.add(x);

            Log.e("added list",json.getString("serial"));
        }

        recyclerViewadapter=new SerialCodeAvailableAdapter(serialList,context,this);
        serialRecycle.setAdapter(recyclerViewadapter);
        serialProgressBar.setVisibility(View.INVISIBLE);
        serialRecycle.setVisibility(View.VISIBLE);
        assignedSerialRecycler.setVisibility(View.VISIBLE);
        // progressDialog.dismiss();
    }

    private void ASSIGNED_SERIAL_CODE_CALL(){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                //    progressDialog.show();
                assignedSerialProgressBar.setVisibility(View.VISIBLE);
                serialRecycle.setVisibility(View.INVISIBLE);
                assignedSerialRecycler.setVisibility(View.INVISIBLE);
                super.onPreExecute();
                // canceldialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Serial response",response);
                Answers.getInstance().logCustom(new CustomEvent("Pack Item")
                        .putCustomAttribute("Status","Assigned Serial"));
                try{
                    ASSIGNED_SERIAL_ParseJSonResponse(response);
                }catch (Exception e){
                    Log.e("Serial response",e.toString());
                }

                // JSON_HTTP_CALL();
                //canceldialog.dismiss();
                //Toast.makeText(OrderConfirmActivity.this,"Done cancel order",Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                Log.d("retailer id",uid);
                HashMapParams.put("assignedProdcode",prodcode);
                HashMapParams.put("assignedProdVariant",prodvariant);
                HashMapParams.put("assignedOrderID",orderid);
                return imageProcessClass.HttpRequest(ItemUpdatePathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

    private void ASSIGNED_SERIAL_ParseJSonResponse(String array) throws JSONException {
        // product = new Product();
        JSONArray jarr = new JSONArray(array);//lv 1 array

        serialList=new ArrayList<>();
        for(int a=0;a<jarr.length();a++){
            SerialCode x=new SerialCode();
            JSONObject json;
            json=jarr.getJSONObject(a);

            x.setSerial(json.getString("serial"));
            x.setSerialID(json.getString("serialID"));


            serialList.add(x);

            Log.e("added list",json.getString("serial"));
        }

        recyclerViewadapter=new SerialCodeAssignedAdapter(serialList,context,this);
        assignedSerialRecycler.setAdapter(recyclerViewadapter);
        assignedSerialProgressBar.setVisibility(View.INVISIBLE);
        serialRecycle.setVisibility(View.VISIBLE);
        assignedSerialRecycler.setVisibility(View.VISIBLE);

        // progressDialog.dismiss();

        int remaining;
        remaining=(Integer.parseInt(qtyOrder))-serialList.size();
        if(remaining<=0){
            serialRecycle.setVisibility(View.INVISIBLE);
        }else{
            serialRecycle.setVisibility(View.VISIBLE);
        }
        String remainSerial = context.getResources().getString(R.string.assignRemain)+Integer.toString(remaining);
        remainingAssignSerial.setText(remainSerial);
    }

    private void INSERT_SERIAL_CODE_CALL(){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Toast.makeText(context,response,Toast.LENGTH_SHORT).show();
                newSerialText.setText("");
                newSerialText.setEnabled(true);
                // JSON_HTTP_CALL();
                //canceldialog.dismiss();
                //Toast.makeText(OrderConfirmActivity.this,"Done cancel order",Toast.LENGTH_SHORT).show();

            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                Log.d("retailer id",uid);
                HashMapParams.put("insertSerialProdcode",prodcode);
                HashMapParams.put("insertSerial",newSerialText.getText().toString());
                return imageProcessClass.HttpRequest(ItemUpdatePathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }
}
