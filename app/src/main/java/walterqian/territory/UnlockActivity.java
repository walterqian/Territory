package walterqian.territory;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class UnlockActivity extends Activity {

    Button submit;
    String url;
    ImageView image;
    TextView congrats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        image = (ImageView) findViewById(R.id.completed_photo);

        congrats = (TextView) findViewById(R.id.discovered);
        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAction();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        url = sharedPreferences.getString("url","failed to get url");

        String title = sharedPreferences.getString("title","failed to get title");
        congrats.setText("Discovered " + title + "!");

        new DownloadImageTask(image)
                .execute(url);

    }

    public void submitAction(){
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
        finish();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null, scaled = null;

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 2;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in, null, o);
                scaled = Bitmap.createScaledBitmap(mIcon11, 240, 240, true);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return scaled;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}


