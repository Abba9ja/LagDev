package com.abba9ja.lagdev;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserDetailActivity extends AppCompatActivity {

    //Declcaring
    private ImageView ivUserAvatar;
    private TextView tvUserLogin;
    private TextView tvUserHtmlUrl;
    private Button btnViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        // Fetch views
        ivUserAvatar = (ImageView) findViewById(R.id.ivUserAvatar);
        tvUserLogin = (TextView) findViewById(R.id.tvUserLogin);
        tvUserHtmlUrl= (TextView) findViewById(R.id.tvUserHtmlUrl);
        btnViewProfile = (Button) findViewById(R.id.btnViewProfile);

        // Use the user to populate the data into our views
        MainActivity user = (MainActivity) getIntent().getSerializableExtra(MainActivity.USER_DETAIL_KEY);
        loadUser(user);

        btnViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse(tvUserHtmlUrl.getText().toString()));
                startActivity(myWebLink);

            }
        });
    }

    private void loadUser(MainActivity user) {
        //change activity title
        this.setTitle(user.getLogin());
        // Populate data
        Picasso.with(this).load(Uri.parse(user.getAvatar())).into(ivUserAvatar);
        tvUserLogin.setText(user.getLogin());
        tvUserHtmlUrl.setText(user.getHtmlUrl());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as  specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share) {
            setShareIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setShareIntent() {
        ImageView ivUserImage = (ImageView) findViewById(R.id.ivUserAvatar);
        final TextView tvUsername = (TextView)findViewById(R.id.tvUserLogin);
        final TextView tvProfileUrl = (TextView) findViewById(R.id.tvUserHtmlUrl);
        // Get access to the URI for the bitmap
        Uri bmpUri = getLocalBitmapUri(ivUserImage);
        // Construct a ShareIntent with link to image
        Intent shareIntent = new Intent();
        // Construct a ShareIntent with link to image
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, (String)"Lag Dev App: Check out this awesome developer "+ tvUsername.getText() +" @ " +tvProfileUrl.getText());
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        // Launch share menu
        startActivity(Intent.createChooser(shareIntent, "Share Developer from GitHub"));
    }
    // Returns the URI path to the Bitmap displayed in User imageview
    private Uri getLocalBitmapUri(ImageView ivUserImage) {
        Drawable drawable = ivUserImage.getDrawable();
        Bitmap bmp = null;
        drawable = ivUserImage.getDrawable();
        bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) ivUserImage.getDrawable()).getBitmap();
        } else {
            return null;
        }

        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "Share_Developer_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

}
