package com.ecommerce.merchant.fypproject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecommerce.merchant.fypproject.adapter.BoostCartAdapter;
import com.ecommerce.merchant.fypproject.adapter.BoostItem;
import com.ecommerce.merchant.fypproject.adapter.OnItemClick;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.PurchaseEvent;
import com.crashlytics.android.answers.StartCheckoutEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.ipay.Ipay;
import com.ipay.IpayPayment;
import com.ipay.IpayResultDelegate;

public class BoostCartActivity extends AppCompatActivity implements OnItemClick {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressDialog progressDialog,deleteDialog;
    private CheckBox selectAllChkBox;
    private Button finalChkOutBtn;
    private TextView subtotal,emptyText;
    private List<BoostItem> cartItemList;
    private BoostCartAdapter cartAdapter;
    private boolean doneLoading=false;
    private boolean isEmpty=false;
    private ImageView emptyImg;
    private String uid,getsubtotal;
    private String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    private final DecimalFormat df2 = new DecimalFormat("0.00");
    private final String merchantkey="JTKBto70Xe",merchantcode="M03216";
    private ResultDelegate resultDeligate;
    private final int OUT_SUCCESS_IPAY=1,OUT_FAIL_IPAY=2,OUT_CANCEL_IPAY=3;
    public static int ipay_response=0;
    public static String ipay_error_msg="";
    private final String ipayURL="http://ecommercefyp.000webhostapp.com/retailer/ipay88response.php";
    public static String ipay_transid="";
    private FirebaseAuth firebaseAuth;
    private  ProgressDialog placeOrderDialog;
    private Dialog cancelDialog;
    private AlertDialog alertDialog;
    private String orderid;
    private SharedPreferences pref;

    @Override
    public void onClick(String value, String prodvar, String prodcode, String rid) {
        if(value.equalsIgnoreCase("uncheck")){
            selectAllChkBox.setChecked(false);
        }else if(value.equalsIgnoreCase("check")) {
            selectAllChkBox.setChecked(true);
        }else if(value.equalsIgnoreCase("refresh")){
            deleteDialog.show();
        }else if(value.equalsIgnoreCase("done refresh")){
            JSON_HTTP_CALL();
            callToast("item removed");

            Log.d("delete but:","Item removed from cart");
        }else{
            subtotal.setText("RM"+df2.format(Double.parseDouble(value)));
            getsubtotal=df2.format(Double.parseDouble(value));
            if(Double.parseDouble(value)==0){
                finalChkOutBtn.setEnabled(false);
            }else{
                finalChkOutBtn.setEnabled(true);
            }

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boost_cart);

        Toolbar myposttoolbar = findViewById(R.id.boostCartToolbar);
        setSupportActionBar(myposttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.cartChkout));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        GetFirebaseAuth();
        progressDialog=new ProgressDialog(BoostCartActivity.this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(R.string.loading);
        progressDialog.setMessage(getResources().getString(R.string.preparingCart));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        deleteDialog=new ProgressDialog(this);
        deleteDialog.setCancelable(false);
        deleteDialog.setTitle(R.string.deleteItem);
        deleteDialog.setMessage(getResources().getString(R.string.pleaseWait));
        deleteDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        placeOrderDialog=new ProgressDialog(BoostCartActivity.this);
        placeOrderDialog.setCancelable(false);
        placeOrderDialog.setTitle(R.string.processingUrOrder);
        placeOrderDialog.setMessage(getResources().getString(R.string.pleaseWait));
        placeOrderDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        selectAllChkBox=findViewById(R.id.checkAll);
        finalChkOutBtn=findViewById(R.id.payAll);
        subtotal=findViewById(R.id.subtotalText);
        emptyImg=findViewById(R.id.shopCartEmptyImg);
        emptyText=findViewById(R.id.shopCartEmptyText);
        viewPhotoDialog();


        recyclerView=findViewById(R.id.cartRecycler);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerOfrecyclerView = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);

        selectAllChkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cartAdapter==null||!doneLoading){
                    JSON_HTTP_CALL();
                }else {
                    cartAdapter.chkAll(selectAllChkBox.isChecked());
                    subtotal.setText("RM" + df2.format(cartAdapter.calculateTotal()) );
                    if (selectAllChkBox.isChecked() && isEmpty) {
                        finalChkOutBtn.setEnabled(true);
                    } else {
                        finalChkOutBtn.setEnabled(false);
                    }
                }

            }
        });

        finalChkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cartAdapter==null||!doneLoading){
                    JSON_HTTP_CALL();
                }else {
                    boolean[] checkList=cartAdapter.getCheckedList();
                    JSONArray JSONUploadArray=new JSONArray();
                    // cartItemList;
                    for(int a=0;a<checkList.length;a++){
                        if(checkList[a]){  //if checked the item

                            Log.d("cartID", cartItemList.get(a).getRetailerCartID());
                            Log.d("price", cartItemList.get(a).getPrice());
                            Log.d("period",cartItemList.get(a).getPeriod());
                            //get quantity
                            //cartID
                            //product variant
                            JSONObject JSONPlaceOrderProdcut = new JSONObject();
                            try {
                                JSONPlaceOrderProdcut.put("cartID", cartItemList.get(a).getRetailerCartID());
                                JSONPlaceOrderProdcut.put("price", cartItemList.get(a).getPrice());
                                JSONPlaceOrderProdcut.put("period",cartItemList.get(a).getPeriod());
                                JSONPlaceOrderProdcut.put("prodcode",cartItemList.get(a).getProdcode());

                                JSONUploadArray.put(JSONPlaceOrderProdcut);
                                Answers.getInstance().logPurchase(new PurchaseEvent()
                                        .putItemName(cartItemList.get(a).getProdname()+":"+cartItemList.get(a).getProdname())
                                        .putItemPrice(new BigDecimal(cartItemList.get(a).getPrice()))
                                        .putCurrency(Currency.getInstance("MYR")));
                            }catch (JSONException e){
                                Crashlytics.logException(e);
                                // handle your exception here!
                                e.printStackTrace();
                            }
                        }
                    }
                    String orderList=JSONUploadArray.toString();

                    Log.d("pay item list",orderList);
                    SharedPreferences pref = BoostCartActivity.this.getSharedPreferences("MyPref", 0); // 0 - for private mode
                    SharedPreferences.Editor editor = pref.edit();
                    editor.clear();
                    editor.putString("shop_cart_list", orderList); //save the string array in share preference and will use it when user done ipay88 payment
                    editor.putString("total_amount",Double.toString(cartAdapter.calculateTotal()));
                    editor.apply();

//                    Intent addressDelivery = new Intent(BoostCartActivity.this, AddressDeliveryActivity.class);
//                    addressDelivery.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(addressDelivery);


                    if(isOnline()){
                        placeOrderDialog.show();

                        JSONObject objectDetail=new JSONObject();
                        JSONArray array=new JSONArray();

                        try{
                            String addressID;

                            addressID="1";

                            objectDetail.put("addressid",addressID);
                            objectDetail.put("delivery","0");
                            objectDetail.put("paymentmethod","ipay88");
                            objectDetail.put("rid",uid);

                            array.put(objectDetail);
                        }catch (Exception e){
                            Crashlytics.logException(e);
                            // handle your exception here!
                            e.printStackTrace();
                        }
                        CREATE_ORDER_JSON_HTTP_CALL(array.toString());
                    }else{
                        Toast.makeText(BoostCartActivity.this,R.string.pleaseEnableInternet,Toast.LENGTH_SHORT).show();
                    }


                }

            }
        });
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        JSON_HTTP_CALL();

    }

    public void JSON_HTTP_CALL() {

        progressDialog.show();
        selectAllChkBox.setChecked(false);
        finalChkOutBtn.setEnabled(false);

        subtotal.setText("RM0.00");
        cartItemList=new ArrayList<>();
        cartAdapter=null;
        doneLoading=false;

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                try {
                    if(response.equalsIgnoreCase("[]")){
                        isEmpty=false;
                        emptyImg.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                    }else{
                        isEmpty=true;
                        emptyImg.setVisibility(View.INVISIBLE);
                        emptyText.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    ParseJSonResponse(response);
                } catch (JSONException e) {
                    Crashlytics.logException(e);
                    // handle your exception here!
                    e.printStackTrace();
                }
                Log.d("Response", response);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                HashMapParams.put("fetchCart", uid);
                return ProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    //get response string and set into recyclerView
    private void ParseJSonResponse(String array) throws JSONException {
        cartItemList=new ArrayList<>();
        cartAdapter=null;
        JSONArray jarr = new JSONArray(array);//lv 1 array

        int totalQty=0;
        for(int a=0;a<jarr.length();a++){
            BoostItem x=new BoostItem();
            JSONObject json;
            json=jarr.getJSONObject(a);

            x.setPeriod(json.getString("period"));
            x.setPrice(json.getString("price"));
            x.setProdcode(json.getString("prodcode"));
            x.setProdname(json.getString("prodname"));
            x.setRetailerCartID(json.getString("cartid"));
            x.setUrl(json.getString("url"));

            cartItemList.add(x);
        }



        cartAdapter= new BoostCartAdapter(cartItemList, this,this);
        RecyclerView.Adapter recyclerViewadapter = cartAdapter;


        recyclerView.setAdapter(recyclerViewadapter);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }

        });



        progressDialog.dismiss();
        doneLoading=true;
        if(deleteDialog.isShowing()){

            //subtotal.setText(df2.format(cartAdapter.calculateTotal()));
            selectAllChkBox.setChecked(false);
            cartAdapter.notifyItemRangeChanged(0,cartItemList.size());
            deleteDialog.dismiss();
        }

    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
    private void GetFirebaseAuth(){
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(this,getResources().getString(R.string.sessionexp),Toast.LENGTH_LONG).show();
        }else uid = firebaseAuth.getCurrentUser().getUid();
    }

    private void callToast(String x){
        Toast.makeText(BoostCartActivity.this,x,Toast.LENGTH_LONG).show();

    }

    static public class ResultDelegate implements IpayResultDelegate,Serializable {

        private int STATUS=0;
        private final int SUCCESS_IPAY=1;
        private final int FAIL_IPAY=2;
        private final int CANCEL_IPAY=3;




        public void onPaymentSucceeded (String transId, String refNo, String amount, String remarks, String auth)
        {
            STATUS=SUCCESS_IPAY;

            ipay_response=STATUS;
            ipay_transid=transId;

            Log.e("transaction ID",transId);
            Log.e("transaction ID",transId);
            Log.e("transaction ID",transId);
            Log.e("transaction ID",transId);
            Log.e("transaction ID",transId);
            Log.e("transaction ID",transId);
            Log.e("transaction ID",transId);
            Log.e("transaction ID",transId);
        }

        public void onPaymentFailed (String transId, String refNo, String amount, String remarks, String err)
        {

            STATUS=FAIL_IPAY;
            // STATUS=FAIL_IPAY;
            ipay_response=STATUS;
            //  mCallback.onClick("fail ipay");
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            Log.d("ipay fail response:",err);
            ipay_error_msg=err;
        }

        public void onPaymentCanceled (String transId, String refNo, String amount, String remarks, String errDesc)
        {
//

            STATUS=CANCEL_IPAY;
            // STATUS=CANCEL_IPAY;
            // mCallback.onClick("success ipay");
            //  mCallback.onClick("fail ipay");
            ipay_response=STATUS;
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            Log.d("ipay cancel response:",errDesc);
            ipay_error_msg=errDesc;
        }

        public void onRequeryResult (String merchantCode, String refNo, String amount, String result)
        {
            Log.e("ipay requery:",result);
            Log.e("ipay requery:",result);
            Log.e("ipay requery:",result);
            Log.e("ipay requery:",result);
            Log.e("ipay requery:",result);
            Log.e("ipay requery:",result);
            Log.e("ipay requery:",result);

        }
    }

//create order here, to get order id
    public void CREATE_ORDER_JSON_HTTP_CALL(final String orderDetail) {



        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                try {
                    CREATE_ORDER_ParseJSonResponse(response);
                    // orderid=response;
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    // handle your exception here!
                    e.printStackTrace();
                }
                Log.e("ResponseCreateOrder", response);
                Log.e("ResponseCreateOrder", response);
                Log.e("ResponseCreateOrder", response);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();


                HashMapParams.put("createOrder",orderDetail);
                return ProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    public void CREATE_ORDER_ParseJSonResponse(String array) throws JSONException {

        JSONArray jarr = new JSONArray(array);//lv 1 array
        JSONObject json;
        json=jarr.getJSONObject(0);
        String orderdate=json.getString("orderdate");
        orderid=json.getString("orderid");

        pref = BoostCartActivity.this.getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        //editor.clear();
        editor.putString("currentOrderid", orderid); //save the string array in share preference and will use it when user done ipay88 payment
        editor.apply();

        resultDeligate=new ResultDelegate();
        //ipay88 setting
        final IpayPayment payment = new IpayPayment();
        payment.setMerchantKey (merchantkey);
        payment.setMerchantCode (merchantcode);
        payment.setPaymentId ("2");
        payment.setCurrency ("MYR");

        //total amount here, unlock this when it launch,TQ
        String ipayTotalAmount=pref.getString("total_amount", null);
        Log.e("total amount here:",ipayTotalAmount);
        Log.e("total amount here:",ipayTotalAmount);
        Log.e("total amount here:",ipayTotalAmount);
        Log.e("total amount here:",ipayTotalAmount);

        payment.setAmount ("1");

        //show all item string here
        pref = BoostCartActivity.this.getSharedPreferences("MyPref", 0); // 0 - for private mode
        orderid=pref.getString("currentOrderid",null);

        payment.setProdDesc ("Order: "+orderid);
        payment.setUserName (Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail());
        payment.setUserEmail (firebaseAuth.getCurrentUser().getEmail());
        payment.setUserContact ("test contact");
        payment.setRemark ("retailer pay boost");
        payment.setCountry ("MY");
        payment.setBackendPostURL (ipayURL);
        payment.setRefNo (orderdate+orderid);

        Intent checkoutIntent = Ipay.getInstance().checkout(payment, BoostCartActivity.this, resultDeligate);

        startActivityForResult(checkoutIntent, 223);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            if(requestCode==223){

                switch (ipay_response){
                    case OUT_SUCCESS_IPAY:
                        pref = BoostCartActivity.this.getSharedPreferences("MyPref", 0); // 0 - for private mode
                        orderid=pref.getString("currentOrderid",null);

                        String orderList=pref.getString("shop_cart_list", null);
                        Log.e("success ipay",ipay_transid);
                        JSON_HTTP_SEND("cartList",orderList,orderid,ipay_transid);
                        break;
                    default:
                        CANCEL_ORDER_JSON_HTTP_CALL(orderid);
                        placeOrderDialog.dismiss();
                        Answers.getInstance().logPurchase(new PurchaseEvent()
                                .putSuccess(false)
                                .putCustomAttribute("Failed Reason",ipay_error_msg));
                        Toast.makeText(BoostCartActivity.this,"Transaction fail"+ipay_error_msg,Toast.LENGTH_SHORT).show();
                        finish();
                }
//                if(ipay_response==OUT_SUCCESS_IPAY){ //perform server update only when success payment is made
//
//
//
//                }else{
//                    placeOrderDialog.dismiss();
//                    Toast.makeText(AddressDeliveryActivity.this,"TransactionFailed",Toast.LENGTH_SHORT).show();
//
//
//                }

            }else{
                CANCEL_ORDER_JSON_HTTP_CALL(orderid);
                placeOrderDialog.dismiss();
                Toast.makeText(BoostCartActivity.this,"Unexpected error"+ipay_error_msg,Toast.LENGTH_SHORT).show();
                Answers.getInstance().logPurchase(new PurchaseEvent()
                        .putSuccess(false)
                        .putCustomAttribute("Failed Reason",ipay_error_msg));
                finish();
            }
        }else if(requestCode==124){
            //Toast.makeText(this,"recreate activity",Toast.LENGTH_SHORT).show();

            Intent intent = getIntent();
            finish();
            startActivity(intent);

        }else{
            CANCEL_ORDER_JSON_HTTP_CALL(orderid);
            placeOrderDialog.dismiss();
            Toast.makeText(BoostCartActivity.this,"Transaction fail",Toast.LENGTH_SHORT).show();
            Answers.getInstance().logPurchase(new PurchaseEvent()
                    .putSuccess(false)
                    .putCustomAttribute("Failed Reason","Cancel by user"));
            cancelDialog.show();
        }
    }


    public void CANCEL_ORDER_JSON_HTTP_CALL(final String orderid) {

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.d("Response", response);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                HashMapParams.put("cancelOrder",orderid);
                return ProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }
//send order details to own server
    public void JSON_HTTP_SEND(final String name, final String orderList,final String ORDERID,final String ipayid) {

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response.....",response);
                Answers.getInstance().logPurchase(new PurchaseEvent()
                        .putSuccess(true));
                placeOrderDialog.dismiss();
                finish();
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();
                HashMapParams.put(name, orderList);// name=cartList
                // HashMapParams.put("orderobj",orderobj);


                HashMapParams.put("rid",uid);
                HashMapParams.put("orderid",ORDERID);
                HashMapParams.put("ipay_transid",ipayid);
                return ProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }
//cancel order dialog prompt
    private void viewPhotoDialog(){
        cancelDialog = new Dialog(this, R.style.MaterialDialogSheet);
        cancelDialog.setContentView(R.layout.cancel_dialog); // your custom view.
        cancelDialog.setCancelable(true);
        Objects.requireNonNull(cancelDialog.getWindow()).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cancelDialog.getWindow().setGravity(Gravity.CENTER);

        Button helpBut=cancelDialog.findViewById(R.id.cancelHelpBut);
        Button closeHelpBut = cancelDialog.findViewById(R.id.cancelCloseBut);

        closeHelpBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDialog.dismiss();
            }
        });

        helpBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog=new AlertDialog.Builder(BoostCartActivity.this)
                        .setMessage(R.string.internetErrorTip)
                        .setCancelable(false)
                        .setPositiveButton(R.string.close,null)
                        .setNeutralButton(R.string.setting, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelDialog.dismiss();
                                startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS),124);
                                alertDialog.dismiss();
                            }
                        }).show();
            }
        });
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }



}
