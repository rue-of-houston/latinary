package com.ameivasoft.latinary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

/**
 * Author Rue
 * Date   2019-05-04
 */
public class AboutScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about_screen);

        ImageButton imgBtn = findViewById(R.id.linkedInBtn);

        if (imgBtn != null)
        {
            imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent linkedIn = new Intent(Intent.ACTION_VIEW);
                    linkedIn.setData(Uri.parse(getString(R.string.linkedin_url)));
                    startActivity(linkedIn);

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
