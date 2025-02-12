package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ThePost extends AppCompatActivity {

    int id_user;
    int id_post;

    int id_author;
    String from;

    ImageView postImage, avatarView;
    Server server;
    Helpers helper;

    TextView author, createDate, contentOfPost, likes, title;
    EditText commentBox;
    Button likeBtn, observationBtn;

    LinearLayout linearLayout;
    LinearLayout comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thepost);
        onLoad();
        updateThePost();
        refreshComments();
    }

    void onLoad(){
        Bundle homepage = getIntent().getExtras();
        id_user = homepage.getInt("id_user");
        id_post = homepage.getInt("id_post");
        from = homepage.getString("from");
        server = new Server();
        helper = new Helpers();

        author = findViewById(R.id.userText);
        createDate = findViewById(R.id.createDateText);
        observationBtn = findViewById(R.id.observeBtn);

        postImage = findViewById(R.id.postImage);

        contentOfPost = findViewById(R.id.contentText);
        contentOfPost.setMovementMethod(LinkMovementMethod.getInstance());
        title = findViewById(R.id.titleText);
        avatarView = findViewById(R.id.avatarView);

        likes = findViewById(R.id.likesText);
        likeBtn = findViewById(R.id.likeBtn);
        commentBox = findViewById(R.id.commentBox);
        comments = findViewById(R.id.comments);
        linearLayout = findViewById(R.id.postLayout);
    }

    @SuppressLint("SetTextI18n")
    void updateThePost(){
        int typ = 0;
        try{
            ResultSet rsPost = server.query("users.username, posts.dataDodania, posts.tresc, posts.id_autor, posts.tytul, posts.typPosta, users.avatar", "posts, users", "posts.id_post = " + id_post + " AND users.id = posts.id_autor");

            while(rsPost.next()){
                author.setText(rsPost.getString(1));
                createDate.setText("Dodano " + rsPost.getString(2));
                contentOfPost.setText(rsPost.getString(3));
                id_author = rsPost.getInt(4);
                title.setText(rsPost.getString(5));
                typ = rsPost.getInt(6);

                if(rsPost.getString(7) != null) {
                    if(!rsPost.getString(7).matches(""))
                        avatarView.setImageBitmap(helper.convertToBitmap(rsPost.getString(7)));
                    else
                        avatarView.setImageIcon(Icon.createWithResource(this, R.drawable.default_avatar));
                } else
                    avatarView.setImageIcon(Icon.createWithResource(this, R.drawable.default_avatar));
            }

            ResultSet rsLikes = server.query("COUNT(*)", "posts_likes", "id_post = " + id_post);
            while(rsLikes.next()){
                likes.setText(rsLikes.getString(1));
            }

            if(typ > 0){
                ResultSet rsImage = server.query("picture", "posts_pictures", "id_post = " + id_post + " AND id_autor = " + id_author);
                while(rsImage.next()) {
                    postImage.setImageBitmap(helper.convertToBitmap(rsImage.getString(1)));
                }
            }

            if(id_author == id_user)
                observationBtn.setVisibility(View.GONE);
            else {
                ResultSet rsObs = server.query("id_observation", "users_observations", "id_observedProfile = " + id_author + " AND id_observedBy = " + id_user);

                if(!rsObs.isBeforeFirst())
                    observationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.observe_icon, 0,0,0);
                else
                    observationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.observed_icon, 0,0,0);
            }

            ResultSet rs = server.query("id_like", "posts_likes", "id_post = " + id_post + " AND id_user = " + id_user + ";");

            if(!rs.next())
                likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon, 0, 0, 0);
            else
                likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_icon, 0, 0, 0);

        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void goToProfile(View v){
        Intent intent = new Intent(this, Profile.class);
        intent.putExtra("id_user", id_author);
        startActivity(intent);
    }

    public void giveAComment(View v){
        if(!title.getText().toString().matches("")) {
            server.insert("comments", "NULL, " + id_post + ", " + id_user + ", NOW(), '" + commentBox.getText() + "', 0");
            commentBox.setText("");
            refreshComments();
            Toast.makeText(this, "Komentarz dodany!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Nie można dodawać pustych komentarzy", Toast.LENGTH_SHORT).show();
        }
    }

    void refreshComments(){
        try{
            ResultSet rs = server.query("users.username, comments.tresc, comments.dataUtworzenia", "comments, users", "comments.id_post = " + id_post + " AND users.id = comments.id_author", "ORDER BY comments.dataUtworzenia DESC");
            comments.removeAllViewsInLayout();
            while(rs.next())
                helper.GenerateComments(
                        comments,
                        rs.getString(1),
                        getBaseContext(),
                        rs.getString(2),
                        rs.getString(3),
                        author
                );
            Toast.makeText(this, "Pomyślnie zaktualizowano!", Toast.LENGTH_SHORT).show();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void giveALike(View v){
        try{
            ResultSet rs = server.query("id_like", "posts_likes", "id_post = " + id_post + " AND id_user = " + id_user);

            if(!rs.isBeforeFirst()){
                server.insert("posts_likes", "NULL, " + id_post + ", " + id_user + ", '" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'");
                likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_icon, 0, 0, 0);
            } else {
                server.delete("posts_likes", "id_post = " + id_post + " AND id_user = " + id_user + ";");
                likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon, 0, 0, 0);
            }

            ResultSet rsLikes = server.query("COUNT(*)", "posts_likes", "id_post = " + id_post);
            while(rsLikes.next()){
                likes.setText(rsLikes.getString(1));
            }
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public void observation(View v){
        try{
            ResultSet rs = server.query("id_observation", "users_observations", "id_observedProfile = " + id_author + " AND id_observedBy = " + id_user);

            if(!rs.isBeforeFirst()){
                server.insert("users_observations", "NULL, " + id_author + ", " + id_user + ", '" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'");
                Toast.makeText(this, "Zaobserwowano profil", Toast.LENGTH_SHORT).show();
                observationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.observed_icon, 0,0,0);
            } else {
                server.delete("users_observations", "id_observedProfile = " + id_author + " AND id_observedBy = " + id_user);
                Toast.makeText(this, "Już nie obserwujesz tego profilu", Toast.LENGTH_SHORT).show();
                observationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.observe_icon, 0,0,0);
            }
        } catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    public void exitPost(View v){
        Intent parent = null;
        switch (from) {
            case "homepage":
                parent = new Intent(this, Homepage.class);
                break;
            case "user":
                parent = new Intent(this, Profile.class);
                parent.putExtra("id_user", id_author);
                break;
            case "edit":
                parent = new Intent(this, PostsManagement.class);
                break;
            case "comment":
                parent = new Intent(this, CommentsManagement.class);
                parent.putExtra("user", id_user);
                break;
        }

        startActivity(parent);
        finish();
    }
}