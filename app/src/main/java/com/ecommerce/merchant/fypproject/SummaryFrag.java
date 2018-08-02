package com.ecommerce.merchant.fypproject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


public class SummaryFrag extends DialogFragment implements DatePickerDialog.OnDateSetListener {
   // Dialog datePickerDialog;
   private View view;
    private RelativeLayout datePickerBut;
    private DatePickerDialog datePickerDialog;
    private GraphView monthGraph;
    private final String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    //private String PHPURL="http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";
    private String uid;
    private final LineGraphSeries<DataPoint> netProfitLine = new LineGraphSeries<>(new DataPoint[] {});
    private final LineGraphSeries<DataPoint> transactionChrgLine = new LineGraphSeries<>(new DataPoint[] {});
    private ProgressDialog progressDialog;
    private final Date date = new Date();
    private TextView selectdate;
    private TextView totalNetSales;
    private TextView transactionChrgText;
    private TextView totalTransactionCountText;
    private TextView itemSoldText;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),this, year, month, day);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_summary, container, false);
        GetFirebaseAuth();
        datePickerBut=view.findViewById(R.id.dateRelative);
        monthGraph=view.findViewById(R.id.monthGraph);
        selectdate=view.findViewById(R.id.selectReportDate);
        totalNetSales=view.findViewById(R.id.monthly_section_GrossSalesText);
        transactionChrgText=view.findViewById(R.id.transactionChargeText);
        totalTransactionCountText=view.findViewById(R.id.totalTransactionCountText);
        itemSoldText=view.findViewById(R.id.itemSoldText);


        datePickerBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        progressDialog.setTitle(getResources().getString(R.string.loading));
        progressDialog.setMessage(getResources().getString(R.string.prepareingSalesReport));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        netProfitLine.setTitle(getResources().getString(R.string.grossprofit));
        netProfitLine.setColor(Color.RED);
        netProfitLine.setDataPointsRadius(10);
        netProfitLine.setDrawDataPoints(true);
        netProfitLine.setAnimated(true);
       // netProfitLine.setCustomPaint(paint);
       // netProfitLine.setThickness(1);
        transactionChrgLine.setTitle(getResources().getString(R.string.transChrg));
        transactionChrgLine.setColor(Color.GREEN);
        transactionChrgLine.setDataPointsRadius(10);
        transactionChrgLine.setDrawDataPoints(true);
        transactionChrgLine.setAnimated(true);

        monthGraph.getGridLabelRenderer().setHorizontalAxisTitle(getResources().getString(R.string.week));
        monthGraph.getGridLabelRenderer().setVerticalAxisTitle("RM");


        monthGraph.addSeries(transactionChrgLine);
        monthGraph.addSeries(netProfitLine);
       // monthGraph.setTitle("monthly sales");
        monthGraph.getLegendRenderer().setVisible(true);
        monthGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        String startDate;
        String endDate;
        try {
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
            String convertedDate = dateFormat.format(date);

            DateFormat dateFormat1=new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
            String currentDate=dateFormat1.format(date);
            startDate=convertedDate+"-1";

            selectdate.setText(convertedDate);
            String dateBuffer = startDate;
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date convertedDate2 = dateFormat.parse(dateBuffer);
            Calendar c = Calendar.getInstance();
            c.setTime(convertedDate2);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));

            endDate=Integer.toString(c.getActualMaximum(Calendar.DAY_OF_MONTH))+"-"+convertedDate;
            JSON_HTTP_CALL_2(startDate,endDate);
            JSON_HTTP_CALL_3(startDate,endDate);
            JSON_HTTP_CALL(startDate,endDate);

        }catch (Exception ignored){

        }
        return view;
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        //month index start from zero, example: March will show 2
       // Toast.makeText(getActivity(),"selected> day:"+Integer.toString(dayOfMonth)+ " month: "+Integer.toString(month)+" year:"+Integer.toString(year),Toast.LENGTH_SHORT).show();

        String selecteddate = Integer.toString(year)+"-"+Integer.toString(month+1);
        selectdate.setText(selecteddate);


        try {
            String startDate = Integer.toString(year) + "-" + Integer.toString(month + 1) + "-1";

            String date = startDate;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date convertedDate = dateFormat.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(convertedDate);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));


            String endDate = Integer.toString(year) + "-" + Integer.toString(month + 1) + "-" + Integer.toString(c.getActualMaximum(Calendar.DAY_OF_MONTH));


            JSON_HTTP_CALL_2(startDate, endDate);
            JSON_HTTP_CALL_3(startDate, endDate);
            JSON_HTTP_CALL(startDate, endDate);


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
                    if(response.equalsIgnoreCase("[]")){
                        progressDialog.dismiss();
                        Toast.makeText(getContext(),"Current month Monthly Sales is empty", Toast.LENGTH_LONG).show();
                    }
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

                    objectDetail.put("reportStartDate", startDate);
                    objectDetail.put("reportEndDate", endDate);
                    objectDetail.put("RID", uid);


                    array.put(objectDetail);
                    array.toString();
                }catch (Exception ignored){

                }

                //Log.d("retailer id",rid);
                HashMapParams.put("overallSummary",array.toString());
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


        DataPoint[] value=new DataPoint[jarr.length()+1];
        DataPoint[] transactionChargeValue=new DataPoint[jarr.length()+1];
        String[] xaxisLabel=new String[jarr.length()+1];
        double netTotalProfit=0;
        double currentMax=0;
        for(int a=0;a<jarr.length();a++){

            JSONObject json;
            json=jarr.getJSONObject(a);

            value[a]=new DataPoint(a+1,Double.parseDouble(json.getString("salesFigure")));
            transactionChargeValue[a]=new DataPoint(a+1,Double.parseDouble(json.getString("salesFigure"))*0.01);

            netTotalProfit+=Double.parseDouble(json.getString("salesFigure"));


            xaxisLabel[a]=json.getString("figureDate").substring(0,10);


            if(Double.parseDouble(json.getString("salesFigure"))>currentMax){
                currentMax=Double.parseDouble(json.getString("salesFigure"));
            }

        }
        value[jarr.length()]=new DataPoint(jarr.length(),0.00);
        transactionChargeValue[jarr.length()]=new DataPoint(jarr.length(),0.00);
        xaxisLabel[jarr.length()]="";
        //line setting
//        netProfitLine.setTitle("Net Profit");
//        netProfitLine.setColor(Color.RED);
//        transactionChrgLine.setTitle("transaction Charge");
//        transactionChrgLine.setColor(Color.GREEN);

       // netProfitLine=new LineGraphSeries<>(value);
       // transactionChrgLine=new LineGraphSeries<>(transactionChargeValue);

        DecimalFormat df2 = new DecimalFormat("0.00");

        netProfitLine.resetData(value);
        transactionChrgLine.resetData(transactionChargeValue);
        String totalSales = "RM"+df2.format(netTotalProfit);
        String transChrg = "RM"+df2.format(netTotalProfit*0.01);
        totalNetSales.setText(totalSales);
        transactionChrgText.setText(transChrg);



        //graph setting
//        monthGraph.getGridLabelRenderer().setHorizontalAxisTitle("Day");
//        monthGraph.getGridLabelRenderer().setVerticalAxisTitle("RM");
//        monthGraph.setTitle("monthly sales");
//        monthGraph.getLegendRenderer().setVisible(true);
//        monthGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(monthGraph);
        staticLabelsFormatter.setHorizontalLabels(xaxisLabel);


//        monthGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        monthGraph.getGridLabelRenderer().setHorizontalLabelsAngle(135);
        monthGraph.getGridLabelRenderer().setLabelHorizontalHeight(220);
        monthGraph.getGridLabelRenderer().setNumHorizontalLabels(jarr.length()+1);







        monthGraph.getGridLabelRenderer().setLabelVerticalWidth(150);
        monthGraph.getViewport().setYAxisBoundsManual(true);
        monthGraph.getViewport().setXAxisBoundsManual(true);
        monthGraph.getViewport().setMaxY(currentMax+1000);
        monthGraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        monthGraph.getViewport().setMinX(1);
        monthGraph.getViewport().setMaxX(jarr.length()+1);



//        monthGraph.getGridLabelRenderer().setPadding(100);

        monthGraph.animate();


        //plot the line
//        monthGraph.addSeries(netProfitLine);
//        monthGraph.addSeries(transactionChrgLine);




        progressDialog.dismiss();


    }


    private void JSON_HTTP_CALL_2(final String startDate, final String endDate){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response....",response);
                try{
                    ParseJSonResponse_2(response);
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

                    objectDetail.put("reportStartDate", startDate);
                    objectDetail.put("reportEndDate", endDate);
                    objectDetail.put("RID", uid);


                    array.put(objectDetail);
                    array.toString();
                }catch (Exception ignored){

                }

                //Log.d("retailer id",rid);
                HashMapParams.put("fetchTranscount",array.toString());
                Log.e("send2",array.toString());
                return imageProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    private void ParseJSonResponse_2(String array) throws JSONException {

        JSONArray jarr = new JSONArray(array);//lv 1 array
        JSONObject json;
        json=jarr.getJSONObject(0);
        totalTransactionCountText.setText(json.getString("transactionCount"));

    }

    private void JSON_HTTP_CALL_3(final String startDate, final String endDate){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response....",response);
                try{
                    ParseJSonResponse_3(response);
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

                    objectDetail.put("reportStartDate", startDate);
                    objectDetail.put("reportEndDate", endDate);
                    objectDetail.put("RID", uid);


                    array.put(objectDetail);
                    array.toString();
                }catch (Exception ignored){

                }

                //Log.d("retailer id",rid);
                HashMapParams.put("fetchSoldcount",array.toString());
                Log.e("send3",array.toString());
                return imageProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    private void ParseJSonResponse_3(String array) throws JSONException {

        JSONArray jarr = new JSONArray(array);//lv 1 array
        JSONObject json;
        json=jarr.getJSONObject(0);
        itemSoldText.setText(json.getString("itemSoldCount"));

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
        }else uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }
}
