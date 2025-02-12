package pl.krzysiek.simplesocial;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetMeImage extends AsyncTask<String, Void, Bitmap> {
    private final ImageView img;
    Helpers helpers;
    Server server;
    public GetMeImage(ImageView image, Helpers helper, Server srv){
        img = image;
        helpers = helper;
        server = srv;
    }
    @Override
    protected Bitmap doInBackground(String... id_post){
        Bitmap bmp = null;
        try {
            ResultSet rs = server.query("picture", "posts_pictures", "id_post = " + id_post[0]);
            while(rs.next()){
                bmp = helpers.convertToBitmap(rs.getString(1));
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap result){
        super.onPostExecute(result);
        img.setImageBitmap(result);
    }
}
