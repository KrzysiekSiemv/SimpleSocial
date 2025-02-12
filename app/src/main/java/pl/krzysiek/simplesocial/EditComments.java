package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EditComments extends AppCompatActivity {

    Server server;
    Helpers helper;

    EditText commentBox;
    TextView postAuthor, postTitle, postText, postDate;
    ImageView postImage;

    int id_comment, id_post, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comments);

        onLoad();
    }

    public void onLoad(){
        server = new Server();
        helper = new Helpers();

        commentBox = findViewById(R.id.commentBox);
        postAuthor = findViewById(R.id.postAuthor);
        postTitle = findViewById(R.id.postTitle);
        postText = findViewById(R.id.postText);
        postDate = findViewById(R.id.postDate);

        postImage = findViewById(R.id.postImage);

        Bundle datas = getIntent().getExtras();
        id_comment = datas.getInt("comment");
        id = datas.getInt("user");

        ResultSet rs = server.query("posts.id_post, comments.tresc, users.username, posts.tytul, posts.tresc, posts.typPosta, posts.dataDodania", "comments, posts, users", "id_comment = " + id_comment + " AND posts.id_post = comments.id_post AND users.id = posts.id_autor");
        try{
            while(rs.next()){
                id_post = rs.getInt(1);
                commentBox.setText(rs.getString(2));
                postAuthor.setText("Post użytkownika " + rs.getString(3));
                postTitle.setText(rs.getString(4));
                postText.setText(rs.getString(5));
                if(rs.getInt(6) > 0){
                    ResultSet rsImage = server.query("picture", "pictures", "id_post = " + id_post);
                    while(rsImage.next())
                        postImage.setImageBitmap(helper.convertToBitmap(rsImage.getString(1)));
                }
                postDate.setText("Dodano " + rs.getString(7));
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void updateComment(View v){
        server.update("comments", "tresc = '" + commentBox.getText() + "'", "id_comment = " + id_comment);
        Toast.makeText(this, "Komentarz został zaktualizowany", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ThePost.class);
        intent.putExtra("id_user", id);
        intent.putExtra("id_post", id_post);
        intent.putExtra("from", "comment");
        startActivity(intent);
        finish();
    }

    public void quit(View v){
        Intent intent = new Intent(this, CommentsManagement.class);
        intent.putExtra("user", id);
        startActivity(intent);
        finish();
    }
}