package com.abba9ja.lagdev;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class About extends Activity {
    Button btnViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        btnViewProfile = (Button) findViewById(R.id.btnViewProfile);

        btnViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://github.com/Abba9ja"));
                startActivity(myWebLink);

            }
        });

    }
}
