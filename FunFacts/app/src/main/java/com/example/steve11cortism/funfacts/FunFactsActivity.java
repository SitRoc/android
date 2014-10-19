package com.example.steve11cortism.funfacts;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


public class FunFactsActivity extends Activity {

    public static final String TAG = FunFactsActivity.class.getSimpleName();

    private FactBook mFactBook = new FactBook();

    private ColorWheel mColorWheel = new ColorWheel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_facts);

        //Start Added Code

        // Declare our View Variables
        final TextView factLabel;
        final Button showFactButton;
        final RelativeLayout relativeLayout;

        // Assign the variables the views from the layout file.
        factLabel = (TextView) findViewById(R.id.funFact);
        showFactButton = (Button) findViewById(R.id.showFactButton);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        // Declare OnClickListener
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fact = mFactBook.getFact();

                int color = mColorWheel.getColor();

                relativeLayout.setBackgroundColor(color);

                showFactButton.setTextColor(color);

                factLabel.setText(fact);
            }
        };

        // On click listener listens to a tap or click.
        showFactButton.setOnClickListener(listener);


        //Toast.makeText(this, "Yeyy! Our activity was created!", Toast.LENGTH_LONG).show();
        Log.d(TAG, "We're Logging from the onCreate() method!");
        //End Added Code End
    }
}
