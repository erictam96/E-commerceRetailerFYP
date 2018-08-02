package com.ecommerce.merchant.fypproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.ecommerce.merchant.fypproject.adapter.Product;
import com.ecommerce.merchant.fypproject.adapter.RecyclerViewAdapter;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
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

public class MyPostActivity extends AppCompatActivity {
    private final String MyPostPathOnServer = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php"; //?userID="16WMU10458"
     //String MyPostPathOnServer = "http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";

    private static MyPostActivity myPostActivity;
    private int RecyclerViewItemPosition ;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View view;
    private RequestQueue requestQueue;
    private ProgressBar progressBar;
    private List<Product> ListOfProduct;
    private TextView txtnoresult;
    private final ArrayList<String> ProdCodeArrayListForClick = new ArrayList<>();
    private String uid;
    private boolean doneLoad=false;
    private RecyclerViewAdapter ownRecycle;
    private int loadIndex=0;
    private RelativeLayout layout;
    private Snackbar snackbar;
    private Snackbar snackbar2;
    private EditText searchText;
    private ImageView searchBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);
        GetFirebaseAuth();
        Toolbar myposttoolbar = findViewById(R.id.myposttoolbar);
        setSupportActionBar(myposttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.managePost));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        progressBar = findViewById(R.id.progressBar1);
        txtnoresult = findViewById(R.id.txtNoResult1);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchBut=findViewById(R.id.searctBut);
        searchText=findViewById(R.id.searchText);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchText.clearFocus();
                    hideKeyboard(myPostActivity);
                    loadIndex=0;
                    JSON_HTTP_CALL();

                    return true;
                }
                return false;
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        myPostActivity = this;
        ListOfProduct = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview2);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerOfrecyclerView = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);
        layout=findViewById(R.id.loginlayout);


        snackbar = Snackbar.make(layout, "Loading more...", Snackbar.LENGTH_SHORT);
        (snackbar.getView()).setBackgroundColor(getResources().getColor(R.color.orange));
        snackbar2 = Snackbar.make(layout, "Reach bottom page", Snackbar.LENGTH_LONG);
        (snackbar2.getView()).setBackgroundColor(getResources().getColor(R.color.scarletRed));
        JSON_HTTP_CALL();

        // Implementing Click Listener on RecyclerView.
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            final GestureDetector gestureDetector = new GestureDetector(MyPostActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return true;
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

                view = Recyclerview.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if(view != null && gestureDetector.onTouchEvent(motionEvent)) {

                    //Getting RecyclerView Clicked Item value.
                    RecyclerViewItemPosition = Recyclerview.getChildAdapterPosition(view);


                    String prodCode = ownRecycle.getListData().get(RecyclerViewItemPosition).getProdCode();
                    Toast.makeText(MyPostActivity.this,prodCode,Toast.LENGTH_SHORT).show();
                    itemUpdateActivity(prodCode);

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)&&doneLoad) {

                    //Toast.makeText(FindActivity.this,"LAst",Toast.LENGTH_LONG).show();
                    loadIndex+=6;
                    JSON_HTTP_CALL();

                }
            }
        });

        //Pull-down refresh function
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                loadIndex=0;
                JSON_HTTP_CALL();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        searchBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchText.clearFocus();
                hideKeyboard(myPostActivity);
                loadIndex=0;
                JSON_HTTP_CALL();
            }
        });

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    //Intent to a update item activity
    private void itemUpdateActivity(String prodCode){

        Intent intent = new Intent(MyPostActivity.this,ItemUpdateActivity.class);
        //Pack Data to Send
        intent.putExtra("prodcode_KEY",prodCode);

        //open activity
        startActivity(intent);
    }

    //request update to server
    private void JSON_HTTP_CALL() {
        final String search=searchText.getText().toString();
        doneLoad=false;
        ListOfProduct = new ArrayList<>();
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(loadIndex==0) {
                    progressBar.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                }else{
                    snackbar.show();
                }
                // Showing progress dialog at image upload time.
                //progressDialog = ProgressDialog.show(ItemUpdateActivity.this,"Item is Uploading","Please Wait",false,false);
            }
            @Override
            protected void onPostExecute(String response) {
                Log.d("Responseee",response);
                super.onPostExecute(response);
//                if(response.equalsIgnoreCase("[]")&&ownRecycle!=null){
//                    Toast.makeText(MyPostActivity.this,"Reach max result",Toast.LENGTH_LONG).show();
//                    ownRecycle.refreshList();
//                    recyclerView.setVisibility(View.VISIBLE);
//                    txtnoresult.setVisibility(View.INVISIBLE);
//                    progressBar.setVisibility(View.INVISIBLE);
//                    snackbar.dismiss();
//                    snackbar2.show();
//                }else if(response.equalsIgnoreCase("[]")&&ownRecycle==null){
//                    recyclerView.setVisibility(View.INVISIBLE);
//                    txtnoresult.setVisibility(View.VISIBLE);
//                    progressBar.setVisibility(View.INVISIBLE);
//                }else {
//
//
//                    try {
//                        txtnoresult.setVisibility(View.INVISIBLE);
//                        ParseJSonResponse(response);
//                        progressBar.setVisibility(View.INVISIBLE);
//                        recyclerView.setVisibility(View.VISIBLE);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }



                if(response.equalsIgnoreCase("[]")&&loadIndex!=0){
                    //Toast.makeText(FindActivity.this,"Reach max result",Toast.LENGTH_LONG).show();
                    ownRecycle.refreshList();
                    recyclerView.setVisibility(View.VISIBLE);
                    txtnoresult.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    snackbar.dismiss();
                    snackbar2.show();

                }else if(response.equalsIgnoreCase("[]")&&loadIndex==0){
                    recyclerView.setVisibility(View.INVISIBLE);
                    txtnoresult.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }else{
                    try {
                        txtnoresult.setVisibility(View.INVISIBLE);
                        ParseJSonResponse(response);
                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        // handle your exception here!
                    }
                    Log.d("Response", response.toString());
                }

                // Dismiss the progress dialog after done uploading.
                //progressDialog.dismiss();
            }

            @Override
            protected String doInBackground(Void... voids) {
                UploadProcess imageProcessClass = new UploadProcess();
                HashMap<String, String> HashMapParams = new HashMap<>();
                HashMapParams.put("retailerProd",uid);
                HashMapParams.put("index",Integer.toString(loadIndex));
                HashMapParams.put("searchKey",search);
                return imageProcessClass.HttpRequest(MyPostPathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

    //get response string and set into recyclerView
    private void ParseJSonResponse(String array) throws JSONException {

        JSONArray jarr = new JSONArray(array);
        ListOfProduct.clear();
        ProdCodeArrayListForClick.clear();

        for(int i = 0; i<jarr.length(); i++) {

            Product product=new Product();
            JSONObject json;
            try {
                json = jarr.getJSONObject(i);
                product.setProdCode(json.getString("prodcode"));
                product.setProdName(json.getString("prodname"));
                product.setProdPrice(Double.parseDouble(json.getString("price")));
                product.setShopName(json.getString("shopname"));
                product.setProdDiscount(Integer.parseInt(json.getString("discount")));
                product.setProductURL(json.getString("imageurl"));

                ProdCodeArrayListForClick.add(json.getString("prodcode"));


            } catch (JSONException e) {
                Crashlytics.logException(e);
                // handle your exception here!
                e.printStackTrace();
            }

            ListOfProduct.add(product);
        }
        RecyclerView.Adapter recyclerViewadapter = new RecyclerViewAdapter(ListOfProduct, this);

        doneLoad=true;


        if(loadIndex==0){
            ownRecycle=new RecyclerViewAdapter(ListOfProduct, this);

            recyclerViewadapter = ownRecycle;
            recyclerView.setAdapter(recyclerViewadapter);
        }else{
            //recyclerViewadapter.notifyItemRangeChanged(loadIndex,ListOfProduct.size());
            ownRecycle.addList(ListOfProduct);
        }
        if(snackbar.isShown()){
            snackbar.dismiss();
        }



        progressBar.setVisibility(View.GONE);
       // mSwipeRefreshLayout.setRefreshing(false);
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

    public static MyPostActivity getInstance(){
        return myPostActivity;
    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
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
