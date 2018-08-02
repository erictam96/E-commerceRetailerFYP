package com.ecommerce.merchant.fypproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.widget.Toast;

import com.ecommerce.merchant.fypproject.adapter.OnItemClick;
import com.ecommerce.merchant.fypproject.adapter.OrderConfirm;
import com.ecommerce.merchant.fypproject.adapter.OrderConfirmationAdapter;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
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

public class OrderConfirmActivity extends AppCompatActivity implements OnItemClick{

    private Toolbar toolbar;
    private String uid;
    private final String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    private List<OrderConfirm> orderConfirmList;
    private OrderConfirmationAdapter orderConfirmationAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onClick(String value, String prodvar, String prodcode, String rid) {
        if(value.equalsIgnoreCase("refresh")){
            progressDialog.show();
        }else if(value.equalsIgnoreCase("done refresh")){
            JSON_HTTP_CALL();
            //progressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        toolbar=findViewById(R.id.orderConfirmToolbar);
        swipeRefreshLayout=findViewById(R.id.orderConfirmSwipe);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                JSON_HTTP_CALL();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        GetFirebaseAuth();
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.orderToConfirm));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView=findViewById(R.id.orderConfirmRecycle);
        recyclerView.setHasFixedSize(true);

        layoutManagerOfrecyclerView = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);
        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(getResources().getString(R.string.loading));
        progressDialog.setMessage(getResources().getString(R.string.prepareCustomerOrder));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        JSON_HTTP_CALL();
    }

    private void JSON_HTTP_CALL(){
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

                Log.d("retailer id",uid);
                HashMapParams.put("orderConfirmation",uid);

                return imageProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

    private void ParseJSonResponse(String array) throws JSONException {
        // product = new Product();
        JSONArray jarr = new JSONArray(array);//lv 1 array
        orderConfirmationAdapter=null;
        orderConfirmList=new ArrayList<>();
        for(int a=0;a<jarr.length();a++){
            OrderConfirm x=new OrderConfirm();
            JSONObject json;
            json=jarr.getJSONObject(a);

            x.setCustName(json.getString("customername"));
            x.setItemName(json.getString("itemname"));
            x.setOrderDate(json.getString("orderdate"));
            x.setQty(json.getString("qty"));
            x.setVariant(json.getString("variant"));
            x.setImgurl(json.getString("imgurl"));
            x.setCustid(json.getString("custid"));
            x.setProdcode(json.getString("prodcode"));

            orderConfirmList.add(x);
        }

        orderConfirmationAdapter= new OrderConfirmationAdapter( orderConfirmList, OrderConfirmActivity.this,this);
        recyclerView.setAdapter(orderConfirmationAdapter);

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

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            orderConfirmationAdapter.onActivityResult(requestCode, resultCode, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
