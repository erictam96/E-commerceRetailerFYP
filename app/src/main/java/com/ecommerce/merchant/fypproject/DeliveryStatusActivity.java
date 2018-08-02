package com.ecommerce.merchant.fypproject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecommerce.merchant.fypproject.adapter.ItemStatus;
import com.ecommerce.merchant.fypproject.adapter.ItemStatusAdapter;
import com.ecommerce.merchant.fypproject.adapter.SalesDetail;
import com.ecommerce.merchant.fypproject.adapter.SalesDetailAdapter;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DeliveryStatusActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText searchText;
    private TextView dateText,emptyText;
    private ImageView searchBut;
    private RelativeLayout calendarBut;
    private RecyclerView recyclerView;
    private DatePickerDialog datePickerDialog;
    private final Calendar c = Calendar.getInstance();

    private final int year = c.get(Calendar.YEAR);
    private final int month = c.get(Calendar.MONTH);
    private final int day = c.get(Calendar.DAY_OF_MONTH);
    private String startDate;
    private String endDate;
    private List<ItemStatus> itemDetailList;
    private ProgressDialog progressDialog;
    private String uid;
    private final String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    private ItemStatusAdapter itemStatusAdapter;
    private boolean isEmpty=false;
    private ImageView emptyImg;

    private CheckBox dateChk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);

        Toolbar myposttoolbar = findViewById(R.id.deliveryStatusToolbar);
        setSupportActionBar(myposttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Item Delivery Status");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        GetFirebaseAuth();
        searchText=findViewById(R.id.searchText);
        dateText=findViewById(R.id.selectedDate);
        searchBut=findViewById(R.id.searctBut);
        calendarBut=findViewById(R.id.dateRelative);
        recyclerView=findViewById(R.id.deliveryStatusRecycler);
        dateChk=findViewById(R.id.dateChk);
        emptyImg=findViewById(R.id.shopCartEmptyImg);
        emptyText=findViewById(R.id.shopCartEmptyText);

        dateChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarBut.setEnabled(dateChk.isChecked());

            }
        });
        calendarBut.setEnabled(dateChk.isChecked());
        progressDialog=new ProgressDialog(DeliveryStatusActivity.this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(getResources().getString(R.string.loading));
        progressDialog.setMessage(getResources().getString(R.string.deliveryStatusMsg));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        String todayDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day);

        dateText.setText(todayDate);
        startDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day);
        endDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day+1);


        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(DeliveryStatusActivity.this),this, year, month, day);

        calendarBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });


        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerOfrecyclerView = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);

        searchBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSON_HTTP_CALL();
            }
        });

        JSON_HTTP_CALL();

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        //month index start from zero, example: March will show 2
        // Toast.makeText(getActivity(),"selected> day:"+Integer.toString(dayOfMonth)+ " month: "+Integer.toString(month)+" year:"+Integer.toString(year),Toast.LENGTH_SHORT).show();
        dateText.setText(Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(dayOfMonth));


        try{
            startDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(dayOfMonth);
            endDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(dayOfMonth+1);

            JSON_HTTP_CALL();

        }catch (Exception ignored){

        }



    }


    private void JSON_HTTP_CALL(){


        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                hideKeyboard(DeliveryStatusActivity.this);
                progressDialog.show();
                super.onPreExecute();
                // canceldialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response....",response);
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

                // JSON_HTTP_CALL();
                //canceldialog.dismiss();
                //Toast.makeText(OrderConfirmActivity.this,"Done cancel order",Toast.LENGTH_SHORT).show();

            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                JSONObject objectDetail = new JSONObject();
                JSONArray array = new JSONArray();
                try {

                    objectDetail.put("startDate", startDate);
                    objectDetail.put("endDate", endDate);
                    objectDetail.put("searchKeyWord",searchText.getText().toString());
                    objectDetail.put("RID", uid);

                    if(dateChk.isChecked()){
                        objectDetail.put("date","1");

                    }else{
                        objectDetail.put("date","0");

                    }

                    array.put(objectDetail);
                    array.toString();
                }catch (Exception ignored){

                }

                //Log.d("retailer id",rid);
                HashMapParams.put("deliveryStatusTracking",array.toString());
                Log.e("send",array.toString());
                return imageProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    private void ParseJSonResponse(String array) throws JSONException {
        // product = new Product();
        JSONArray jarr = new JSONArray(array);//lv 1 array
        itemDetailList=new ArrayList<>();
        double total=0;
        for(int a=0;a<jarr.length();a++) {

            ItemStatus x=new ItemStatus();
            JSONObject json;
            json = jarr.getJSONObject(a);

            x.setCustName(json.getString("custName"));
            x.setOrderID(json.getString("orderID"));
            x.setPicURL(json.getString("picURL"));
            x.setItemName(json.getString("prodName"));
            x.setVariant(json.getString("prodVar"));
            x.setQty(json.getString("prodQty"));
            x.setDateORder(json.getString("orderDate"));
            x.setStatus(json.getString("itemStatus"));
            itemDetailList.add(x);
        }
        itemStatusAdapter=new ItemStatusAdapter(itemDetailList,this);
        RecyclerView.Adapter recyclerViewadapter = itemStatusAdapter;
        recyclerView.setAdapter(recyclerViewadapter);

//        DecimalFormat df2 = new DecimalFormat("0.00");
//
//        String gross = "RM"+df2.format(total);
//        String transaction = "RM"+df2.format((total*0.01));
//        grossText.setText(gross);
//        transactionText.setText(transaction);
        progressDialog.dismiss();
    }

    private void GetFirebaseAuth(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
