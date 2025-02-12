package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EditPost extends AppCompatActivity {

    static Server server;
    static Helpers helper;
    static int id;
    static int id_post;
    boolean changePic;

    EditText title, text;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        onLoad();
    }

    public void onLoad(){
        getIds();
        server = new Server();
        helper = new Helpers();
        setObjects();
        fillFields();
    }

    public void setObjects(){
        title = findViewById(R.id.postTitle);
        text = findViewById(R.id.postText);
        img = findViewById(R.id.postImage);
    }

    public void getIds(){
        Bundle datas = getIntent().getExtras();
        id_post = datas.getInt("post");
        id = datas.getInt("user");
    }

    public void fillFields(){
        ResultSet rs = server.query("tytul, tresc, typPosta", "posts", "id_post = " + id_post);
        try{
            while(rs.next()){
                title.setText(rs.getString(1));
                text.setText(rs.getString(2));
                if(rs.getInt(3) > 0){
                    ResultSet picRs = server.query("picture", "pictures", "pictures.id_post = " + id_post);
                    while(picRs.next()){
                        img.setImageBitmap(helper.convertToBitmap(picRs.getString(1)));
                    }
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void quit(View v){
        Intent intent = new Intent(this, PostsManagement.class);
        intent.putExtra("user", id);
        startActivity(intent);
        finish();
    }

    public void saveChanges(View v) {

    }
}