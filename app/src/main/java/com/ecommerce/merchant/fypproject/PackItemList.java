package com.ecommerce.merchant.fypproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class PackItemList extends AppCompatActivity {
    private Intent intent;
    private String title;
    private Button checkoutBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pack_item);


        intent=getIntent();
        title= Objects.requireNonNull(intent.getExtras()).getString("invoiceNumber");

        Toolbar myposttoolbar = findViewById(R.id.packItemtoolbar);
        setSupportActionBar(myposttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);


        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


      //  setTitle(title);
        intent=new Intent(PackItemList.this,MainActivity.class);
        intent.putExtra("status","done");
        checkoutBut= findViewById(R.id.checkOutButton);





        checkoutBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences("STATUS", MODE_PRIVATE).edit();
                editor.putBoolean("PACKSTATUS", true);
                editor.apply();
                //packItemBut.setVisibility(View.INVISIBLE);
                finish();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                //this.onBackPressed();
                startActivity(intent);
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
        //return super.onOptionsItemSelected(item);
    }
}
