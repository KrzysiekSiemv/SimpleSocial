package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class CommentsManagement extends AppCompatActivity {

    Server server;
    LinearLayout commentsList;
    static int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_management);
        onLoad();
    }

    public void quit(View v){
        Intent homepage = new Intent(this, Homepage.class);
        startActivity(homepage);
        finish();
    }

    public void onLoad(){
        Bundle data = getIntent().getExtras();
        id = data.getInt("user");
        commentsList = findViewById(R.id.commentsList);
        server = new Server();

        refreshComments();
    }

    void refreshComments(){
        ResultSet rs = server.query("*", "comments", "id_author = " + id);
        try{
            while(rs.next()) {
                generateComment(
                        rs.getInt(1),
                        rs.getInt(2),
                        rs.getInt(3),
                        rs.getString(4),
                        rs.getString(5)
                );
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    @SuppressLint("SetTextI18n")
    void generateComment(int id_comment, int id_post, int id_author, String dataUtworzenia, String tresc){
            LinearLayout comment = new LinearLayout(commentsList.getContext());
            LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            commentParams.setMargins(0,4,8,4);
            comment.setLayoutParams(commentParams);
            comment.setId(View.generateViewId());
            comment.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout buttonGroup = new LinearLayout(comment.getContext());
            LinearLayout.LayoutParams btnGroupParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonGroup.setLayoutParams(btnGroupParams);
            buttonGroup.setId(View.generateViewId());
            buttonGroup.setOrientation(LinearLayout.VERTICAL);
            buttonGroup.setGravity(Gravity.CENTER);

            LinearLayout commentContent = new LinearLayout(comment.getContext());
            LinearLayout.LayoutParams commentsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            commentContent.setLayoutParams(commentsParams);
            commentContent.setId(View.generateViewId());
            commentContent.setOrientation(LinearLayout.VERTICAL);

        ContextThemeWrapper theme = new ContextThemeWrapper(buttonGroup.getContext(), R.style.Widget_MaterialComponents_Button);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(8,2,8,2);

        Button editButton = new Button(theme);
        editButton.setLayoutParams(buttonParams);
        editButton.setId(View.generateViewId());
        editButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.description_icon, 0,0,0);
        editButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EditComments.class);
            intent.putExtra("comment", id_comment);
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
                        server.delete("comments", "id_comment = " + id_comment);
                        Toast.makeText(this, "Komentarz został usunięty", Toast.LENGTH_SHORT).show();
                        commentsList.removeAllViewsInLayout();
                        refreshComments();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            alert.setMessage("Czy na pewno chcesz usunąć ten komentarz?").setPositiveButton("Tak", alertDialog).setNegativeButton("Nie", alertDialog).show();
        });

        Button postButton = new Button(theme);
        postButton.setLayoutParams(buttonParams);
        postButton.setId(View.generateViewId());
        postButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gotopost_icon,0,0,0);
        postButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, ThePost.class);
            intent.putExtra("id_user", id);
            intent.putExtra("id_post", id_post);
            intent.putExtra("from", "comment");
            startActivity(intent);
            finish();
        });

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView commentToPost = new TextView(commentContent.getContext());
        commentToPost.setId(View.generateViewId());
        ResultSet rsPost = server.query("tytul", "posts", "id_post = " + id_post);
        try{
            while(rsPost.next())
                commentToPost.setText("Komentarz do posta: \"" + rsPost.getString(1) + "\"");
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        TextView commentText = new TextView(commentContent.getContext());
        commentText.setId(View.generateViewId());
        commentText.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
        commentText.setText(tresc);

        TextView addedText = new TextView(commentContent.getContext());
        addedText.setId(View.generateViewId());
        addedText.setTextAppearance(R.style.TextAppearance_AppCompat_Body2);
        addedText.setText(dataUtworzenia);

        View divider = new View(commentsList.getContext());
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int) getResources().getDisplayMetrics().density);
        dividerParams.setMargins(32, 32, 32, 32);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(getColor(R.color.gray_400));

        buttonGroup.addView(editButton);
        buttonGroup.addView(postButton);
        buttonGroup.addView(deleteButton);
        commentContent.addView(commentToPost);
        commentContent.addView(commentText);
        commentContent.addView(addedText);

        comment.addView(buttonGroup);
        comment.addView(commentContent);

        commentsList.addView(comment);
        commentsList.addView(divider);
    }
}