package pl.krzysiek.simplesocial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;


public class NewPost extends AppCompatActivity {

    static int id;
    Server server;
    Helpers helper;
    EditText text, title;
    CheckBox nsfwTag;
    ImageView imageToPost;
    Intent imageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        onLoad();
    }

    void onLoad(){
        server = new Server();
        helper = new Helpers();

        id = Homepage.id;
        text = findViewById(R.id.postTextBox);
        title = findViewById(R.id.titleBox);
        imageToPost = findViewById(R.id.imageToPost);
        nsfwTag = findViewById(R.id.nsfwTag);
    }

    public void addPost(View v){
        if(!title.getText().toString().matches("")) {
            String date = helper.GetDate();
            String nsfw = "";

            if (nsfwTag.isChecked())
                nsfw = "1";
            else
                nsfw = "0";

            try {
                int id_post = 0;
                if (imageData != null) {
                    server.insert("posts", "id_autor, dataDodania, tytul, tresc, typPosta, nsfw", id + ", NOW(), \"" + title.getText() + "\", \"" + text.getText() + "\", 1, " + nsfw);
                    ResultSet rsNew = server.query("id_post", "posts", "id_autor = '" + id + "' AND tytul = '" + title.getText() + "' AND tresc = '" + text.getText() + "' AND typPosta = '1';");
                    while (rsNew.next()) {
                        id_post = rsNew.getInt(1);
                    }
                    uploadImage(id, id_post, date, imageData);
                } else
                    server.insert("posts", "id_autor, dataDodania, tytul, tresc, typPosta, nsfw", id + ", \"" + date + "\", \"" + title.getText() + "\", \"" + text.getText() + "\", 0, " + nsfw);

                Toast.makeText(this, "Post został utworzony", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, ThePost.class);
                intent.putExtra("id_post", id_post);
                intent.putExtra("id_user", id);
                intent.putExtra("from", "homepage");
                startActivity(intent);
                finish();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Post musi zawierać tytuł!", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("RestrictedApi")
    public void addImage(View v){
        imgAdd();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> { if (isGranted) { imgAdd(); } else { Toast.makeText(this, "Nie możesz dodać zdjęcia do postu, ponieważ nie zezwoliłeś aplikacji na dostęp do plików.", Toast.LENGTH_LONG).show(); } });

    @SuppressLint("IntentReset")
    public void imgAdd(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            startActivityForResult(pickIntent, 1);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            if(data != null){
                try{
                    imageData = data;
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageData.getData());
                    Toast.makeText(this, helper.getThatPath(data.getData(), this), Toast.LENGTH_SHORT).show();
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
            imageToPost.setImageBitmap(bitmap);
            Toast.makeText(this, "Dodałem się", Toast.LENGTH_SHORT).show();
        }
    }

    void uploadImage(int id_autor, int id_post, String dataDodania, Intent data) {
        server.insert("posts_pictures", "NULL, '" + id_autor + "', '" + id_post + "', '" + helper.convertToBase64(data, this) + "', '" + dataDodania + "'");
    }
}