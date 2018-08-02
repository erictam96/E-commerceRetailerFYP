package com.ecommerce.merchant.fypproject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ecommerce.merchant.fypproject.adapter.CancelItem;
import com.ecommerce.merchant.fypproject.adapter.CancelItemAdapter;
import com.ecommerce.merchant.fypproject.adapter.SalesDetail;
import com.ecommerce.merchant.fypproject.adapter.SalesDetailAdapter;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.firebase.ui.auth.data.model.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CancelReportActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private  RelativeLayout calendar;
    private DatePickerDialog datePickerDialog;
    private TextView selectdate;
    private final String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    private String uid;
    private ProgressDialog progressDialog;
    private List<CancelItem> salesProductList;
    private CancelItemAdapter salesDetailAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    private String startDate,endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_report);

        Toolbar myposttoolbar = findViewById(R.id.cancelReport);
        setSupportActionBar(myposttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.cancelReport));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        GetFirebaseAuth();
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        selectdate=findViewById(R.id.selectReportDate);

        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(CancelReportActivity.this),this, year, month, day);

        calendar=findViewById(R.id.dateRelative);
        recyclerView=findViewById(R.id.cancelReportRecycler);
        recyclerView.setHasFixedSize(true);
        layoutManagerOfrecyclerView = new LinearLayoutManager(CancelReportActivity.this);
        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);




        progressDialog=new ProgressDialog(CancelReportActivity.this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(getResources().getString(R.string.loading));
        progressDialog.setMessage(getResources().getString(R.string.cancelReportMsg));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();

            }
        });

        selectdate.setText(Integer.toString(year)+"-"+Integer.toString(month+1));
        startDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-1";
        endDate=Integer.toString(year) + "-" + Integer.toString(month + 1) + "-" + Integer.toString(c.getActualMaximum(Calendar.DAY_OF_MONTH));
        JSON_HTTP_CALL(startDate, endDate);

    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        //month index start from zero, example: March will show 2
        // Toast.makeText(getActivity(),"selected> day:"+Integer.toString(dayOfMonth)+ " month: "+Integer.toString(month)+" year:"+Integer.toString(year),Toast.LENGTH_SHORT).show();

        String selecteddate = Integer.toString(year)+"-"+Integer.toString(month+1);
        selectdate.setText(selecteddate);

        Toast.makeText(this,selecteddate,Toast.LENGTH_SHORT).show();

        try {
            startDate = Integer.toString(year) + "-" + Integer.toString(month + 1) + "-1";

            String date = startDate;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date convertedDate = dateFormat.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(convertedDate);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));


            endDate = Integer.toString(year) + "-" + Integer.toString(month + 1) + "-" + Integer.toString(c.getActualMaximum(Calendar.DAY_OF_MONTH));


            JSON_HTTP_CALL(startDate, endDate);
//            JSON_HTTP_CALL_3(startDate, endDate);
//            JSON_HTTP_CALL(startDate, endDate);


        } catch (ParseException ignored) {

        }

    }


    private void JSON_HTTP_CALL(final String startDate, final String endDate){


        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                progressDialog.show();
                super.onPreExecute();
                // canceldialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response....",response);
                try{
                    ParseJSonResponse(response);
                }catch (Exception e){
                    Log.e("Response....",e.toString());
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

                    objectDetail.put("salesStartDate", startDate);
                    objectDetail.put("salesEndDate", endDate);
                    objectDetail.put("RID", uid);

                    array.put(objectDetail);
                    array.toString();
                }catch (Exception ignored){

                }

                //Log.d("retailer id",rid);
                HashMapParams.put("cancelReport",array.toString());//salesdetail
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
        salesProductList=new ArrayList<>();
        double total=0;
        for(int a=0;a<jarr.length();a++) {

            CancelItem x=new CancelItem();
            JSONObject json;
            json = jarr.getJSONObject(a);

            x.setStatus(json.getString("status"));
            x.setDesc(json.getString("desc"));
            x.setCount(json.getString("qty"));
            salesProductList.add(x);
        }
        salesDetailAdapter=new CancelItemAdapter(salesProductList,getApplicationContext());
        recyclerView.setAdapter(salesDetailAdapter);

        progressDialog.dismiss();
    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void GetFirebaseAuth(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Intent intent = new Intent(CancelReportActivity.this, SplashActivity.class);
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(CancelReportActivity.this,getResources().getString(R.string.sessionexp),Toast.LENGTH_LONG).show();
        }
        else{
            uid = firebaseAuth.getCurrentUser().getUid();
        }
    }



}
