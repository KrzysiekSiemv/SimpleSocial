package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PostsManagement extends AppCompatActivity {

    LinearLayout postsList;
    Server server;
    public static int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_management);
        server = new Server();

        onLoad();
    }

    void onLoad(){
        postsList = findViewById(R.id.postList);
        id = Homepage.id;
        refreshPosts();
    }

    void refreshPosts(){
        ResultSet res = server.query("id_post, tresc, tytul, dataDodania", "posts", "id_autor = " + id, "ORDER BY id_post DESC");
        try{
            while(res.next()){
                showPosts(
                        res.getInt(1),
                        res.getString(2),
                        res.getString(3),
                        res.getString(4)
                );
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    public void showPosts(int id_post, String tresc, String title, String dataUtworzenia){
        LinearLayout post = new LinearLayout(postsList.getContext());
        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        commentParams.setMargins(0,4,8,4);
        post.setLayoutParams(commentParams);
        post.setId(View.generateViewId());
        post.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout buttonGroup = new LinearLayout(post.getContext());
        LinearLayout.LayoutParams btnGroupParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonGroup.setLayoutParams(btnGroupParams);
        buttonGroup.setId(View.generateViewId());
        buttonGroup.setOrientation(LinearLayout.VERTICAL);
        buttonGroup.setGravity(Gravity.CENTER);

        LinearLayout postContent = new LinearLayout(post.getContext());
        LinearLayout.LayoutParams commentsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        postContent.setLayoutParams(commentsParams);
        postContent.setId(View.generateViewId());
        postContent.setOrientation(LinearLayout.VERTICAL);

        ContextThemeWrapper theme = new ContextThemeWrapper(buttonGroup.getContext(), R.style.Widget_MaterialComponents_Button);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(8,2,8,2);

        Button editButton = new Button(theme);
        editButton.setLayoutParams(buttonParams);
        editButton.setId(View.generateViewId());
        editButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.description_icon, 0,0,0);
        editButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditPost.class);
            intent.putExtra("post", id_post);
            intent.putExtra("user", id);
            startActivity(intent);
            finish();
        });

        Button deleteButton = new Button(theme);
        deleteButton.setLayoutParams(buttonParams);
        deleteButton.setId(View.generateViewId());
        deleteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.delete_icon,0,0,0);
        deleteButton.setOnClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            DialogInterface.OnClickListener alertDialog = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        server.delete("comments", "id_post = " + id_post);
                        server.delete("posts_likes", "id_post = " + id_post);
                        server.delete("posts_pictures", "id_post = " + id_post);
                        server.delete("posts", "id_post = " + id_post);
                        Toast.makeText(this, "Post został usunięty", Toast.LENGTH_SHORT).show();
                        postsList.removeAllViewsInLayout();
                        refreshPosts();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            alert.setMessage("Czy na pewno chcesz usunąć ten post?").setPositiveButton("Tak", alertDialog).setNegativeButton("Nie", alertDialog).show();
        });

        Button postButton = new Button(theme);
        postButton.setLayoutParams(buttonParams);
        postButton.setId(View.generateViewId());
        postButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gotopost_icon,0,0,0);
        postButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, ThePost.class);
            intent.putExtra("id_user", id);
            intent.putExtra("id_post", id_post);
            intent.putExtra("from", "edit");
            startActivity(intent);
            finish();
        });

        TextView postTitle = new TextView(postContent.getContext());
        postTitle.setId(View.generateViewId());
        postTitle.setText(title);

        TextView postText = new TextView(postContent.getContext());
        postText.setId(View.generateViewId());
        postText.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
        postText.setText(tresc);

        TextView addedText = new TextView(postContent.getContext());
        addedText.setId(View.generateViewId());
        addedText.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        addedText.setText(dataUtworzenia);

        View divider = new View(postsList.getContext());
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int) getResources().getDisplayMetrics().density);
        dividerParams.setMargins(32, 24, 32, 24);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(getColor(R.color.gray_400));

        buttonGroup.addView(editButton);
        buttonGroup.addView(postButton);
        buttonGroup.addView(deleteButton);
        postContent.addView(postTitle);
        postContent.addView(postText);
        postContent.addView(addedText);

        post.addView(buttonGroup);
        post.addView(postContent);

        postsList.addView(post);
        postsList.addView(divider);
    }

    public void quit(View v){
        Intent homepage = new Intent(this, Homepage.class);
        startActivity(homepage);
        finish();
    }

    @Override
    public void onBackPressed(){
        Intent homepage = new Intent(this, Homepage.class);
        startActivity(homepage);
        finish();
    }
}