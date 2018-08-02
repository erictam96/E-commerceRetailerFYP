package com.ecommerce.merchant.fypproject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.ecommerce.merchant.fypproject.adapter.SalesDetail;
import com.ecommerce.merchant.fypproject.adapter.SalesDetailAdapter;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
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


public class OrderFrag extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private View view;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    private SalesDetailAdapter salesDetailAdapter;
    private ProgressDialog progressDialog;
    private String uid;
    private List<SalesDetail> salesProductList;
    private final String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    //private String PHPURL="http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";
    protected RequestQueue requestQueue ;
    private RelativeLayout datePickerBut;
    private DatePickerDialog datePickerDialog;
    private TextView selectdate;
    private TextView transactionText;
    private TextView grossText;
    private final Calendar c = Calendar.getInstance();
    private final int year = c.get(Calendar.YEAR);
    private final int month = c.get(Calendar.MONTH);
    private final int day = c.get(Calendar.DAY_OF_MONTH);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),this, year, month, day);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_order, container, false);
        recyclerView=view.findViewById(R.id.sales_detail_recycle);
        GetFirebaseAuth();
        datePickerBut=view.findViewById(R.id.salesDateRelative);
        selectdate=view.findViewById(R.id.salesSelectReportDate);
        transactionText=view.findViewById(R.id.salesDetailTransactionChrgText);
        grossText=view.findViewById(R.id.salesDetailGrossProfitText);

        datePickerBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        progressDialog.setTitle(getResources().getString(R.string.loading));
        progressDialog.setMessage(getResources().getString(R.string.preparingDailySales));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        recyclerView.setHasFixedSize(true);
        layoutManagerOfrecyclerView = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);
        String todayDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day);
        String todayDate2=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(day+1);

        selectdate.setText(todayDate);
        JSON_HTTP_CALL(todayDate,todayDate2);

        return view;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        //month index start from zero, example: March will show 2

        selectdate.setText(Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(dayOfMonth));


        try{
            String startDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(dayOfMonth);
            String endDate=Integer.toString(year)+"-"+Integer.toString(month+1)+"-"+Integer.toString(dayOfMonth+1);
            JSON_HTTP_CALL(startDate,endDate);

        }catch (Exception ignored){

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
                HashMapParams.put("salesDetail",array.toString());
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

            SalesDetail x=new SalesDetail();
            JSONObject json;
            json = jarr.getJSONObject(a);

            x.setProductName(json.getString("prodName"));
            x.setProductVariant(json.getString("prodVar"));
            x.setProdQty(json.getString("prodQty"));
            x.setProdPrice(json.getString("prodPrice"));
            total=total+(Integer.parseInt(json.getString("prodQty"))*Double.parseDouble(json.getString("prodPrice")));
            salesProductList.add(x);
        }
        salesDetailAdapter=new SalesDetailAdapter(salesProductList,getActivity());
        recyclerView.setAdapter(salesDetailAdapter);

        DecimalFormat df2 = new DecimalFormat("0.00");

        String gross = "RM"+df2.format(total);
        String transaction = "RM"+df2.format((total*0.01));
        grossText.setText(gross);
        transactionText.setText(transaction);
        progressDialog.dismiss();
    }


    private void GetFirebaseAuth(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            Intent intent = new Intent(getActivity(), SplashActivity.class);
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(getActivity(),getResources().getString(R.string.sessionexp),Toast.LENGTH_LONG).show();
        }
        else{
            uid = firebaseAuth.getCurrentUser().getUid();
        }
    }
}
