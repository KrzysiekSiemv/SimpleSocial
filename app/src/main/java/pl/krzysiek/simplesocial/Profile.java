package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Profile extends AppCompatActivity {

    int id;
    int id_user;
    Server server;
    Helpers helper;
    TextView nameText, usernameText, observedByText, descriptionText;
    ImageView verifiedIcon, avatarImage;
    Button observationBtn;
    TabLayout navigationTab;

    LinearLayout posts, comments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        onLoad();
        changeTab();
    }

    void onLoad(){
        nameText = findViewById(R.id.nameText);
        usernameText = findViewById(R.id.usernameText);
        observedByText = findViewById(R.id.observedByText);
        verifiedIcon = findViewById(R.id.verifiedIcon);
        observationBtn = findViewById(R.id.observationBtn);
        navigationTab = findViewById(R.id.navigationTab);
        descriptionText = findViewById(R.id.descriptionText);
        avatarImage = findViewById(R.id.avatarImage);

        comments = findViewById(R.id.comments);
        posts = findViewById(R.id.posts);

        id = Homepage.id;
        server = new Server();
        helper = new Helpers();

        Bundle fromSomewhere = getIntent().getExtras();
        id_user = fromSomewhere.getInt("id_user");

        giveMeAll();
    }

    public void quitTheProfile(View v){
        Intent intent = new Intent(this, Homepage.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, Homepage.class);
        startActivity(intent);
        finish();
    }

    public void changeTab(){
        navigationTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 0:
                        posts.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        comments.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 0:
                        posts.setVisibility(View.GONE);
                        break;
                    case 1:
                        comments.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    void giveMeAll(){
        try{
            ResultSet rsComments = server.query("DISTINCT users.username, comments.tresc, comments.dataUtworzenia, comments.id_post, posts.id_autor, comments.id_post", "comments, users, posts", "comments.id_author = " + id_user + " AND users.id = comments.id_author AND comments.id_post = posts.id_post", "ORDER BY comments.dataUtworzenia DESC");
            while(rsComments.next()) {
                String username = rsComments.getString(1);
                String tresc = rsComments.getString(2);
                String dataUtworzenia = rsComments.getString(3);
                int id_post = rsComments.getInt(4);
                ResultSet rsAuthor = server.query("users.username", "users", "users.id = " + rsComments.getInt(5));
                while(rsAuthor.next())
                    showComments(
                        id_post,
                        username,
                        tresc,
                        dataUtworzenia,
                        rsAuthor.getString(1)
                    );
            }
            ResultSet rsPost = server.query("posts.id_post, users.username, posts.tresc, posts.dataDodania", "posts, users", "users.id = posts.id_autor AND posts.id_autor = " + id_user, "ORDER BY id_post DESC");

            while(rsPost.next())
                showPosts(
                        Integer.parseInt(rsPost.getString(1)),
                        rsPost.getString(2),
                        rsPost.getString(3),
                        rsPost.getString(4)
                );

            ResultSet rsUser = server.query("username, nazwaWyswietlana, zweryfikowany, opis, avatar", "users", "id = " + id_user);

            while(rsUser.next()){
                usernameText.setText(rsUser.getString(1));
                nameText.setText(rsUser.getString(2));
                descriptionText.setText(rsUser.getString(4));
                if(rsUser.getInt(3) == 1)
                    verifiedIcon.setImageIcon(Icon.createWithResource(this, R.drawable.verified_icon));
                else
                    verifiedIcon.setImageIcon(Icon.createWithResource(this, R.drawable.normaluser_icon));

                if(rsUser.getString(5) != null) {
                    if(!rsUser.getString(5).matches(""))
                        avatarImage.setImageBitmap(helper.convertToBitmap(rsUser.getString(5)));
                    else
                        avatarImage.setImageIcon(Icon.createWithResource(this, R.drawable.default_avatar));
                }else
                    avatarImage.setImageIcon(Icon.createWithResource(this, R.drawable.default_avatar));
            }

            ResultSet rsFollowers = server.query("COUNT(*)", "users_observations", "id_observedProfile = " + id_user);
            while(rsFollowers.next())
                observedByText.setText(rsFollowers.getInt(1) + " obserwujących");

            if(id_user == id)
                observationBtn.setVisibility(View.GONE);
            else {
                ResultSet rsObs = server.query("id_observation", "users_observations", "id_observedProfile = " + id_user + " AND id_observedBy = " + id);

                if (rsObs.next()){
                    observationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.observed_icon, 0,0,0);
                } else
                    observationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.observe_icon, 0,0,0);
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void observation(View v){
        try{
            ResultSet rs = server.query("id_observation", "users_observations", "id_observedProfile = " + id_user + " AND id_observedBy = " + id);

            if(!rs.isBeforeFirst()){
                server.insert("users_observations", "NULL, " + id_user + ", " + id_user + ", '" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'");
                Toast.makeText(this, "Zaobserwowano profil", Toast.LENGTH_SHORT).show();
                observationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.observed_icon, 0,0,0);

                ResultSet rsFollowers = server.query("COUNT(*)", "users_observations", "id_observedProfile = " + id_user);
                while(rsFollowers.next())
                    observedByText.setText(rsFollowers.getInt(1) + " obserwujących");
            } else {
                server.delete("users_observations", "id_observedProfile = " + id_user + " AND id_observedBy = " + id);
                Toast.makeText(this, "Już nie obserwujesz tego profilu", Toast.LENGTH_SHORT).show();
                observationBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.observe_icon, 0,0,0);

                ResultSet rsFollowers = server.query("COUNT(*)", "users_observations", "id_observedProfile = " + id_user);
                while(rsFollowers.next())
                    observedByText.setText(rsFollowers.getInt(1) + " obserwujących");
            }
        } catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    public void showPosts(int id_post, String autor, String text, String datetime){
        TableLayout table = new TableLayout(posts.getContext());
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        table.setId(View.generateViewId());
        table.setLayoutParams(tableParams);

        TableRow authorRow = new TableRow(table.getContext());
        authorRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        TextView authorLabel = new TextView(authorRow.getContext());
        authorLabel.setPadding(16,16,16,16);
        authorLabel.setText(autor + " \nDodano: " + datetime);

        /*                          */

        TableRow textRow = new TableRow(table.getContext());
        textRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        TextView textLabel = new TextView(textRow.getContext());
        textLabel.setPadding(16,16,16,16);
        textLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textLabel.setMaxLines(5);
        textLabel.setTextSize(20f);
        textLabel.setText(text);

        /*                          */

        TableRow likesRow = new TableRow(table.getContext());
        likesRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.MATCH_PARENT));

        Button goToPost = new Button(likesRow.getContext());
        goToPost.setId(View.generateViewId());
        TableRow.LayoutParams redirectParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 96, 1f);
        redirectParams.setMargins(16, 4, 16, 4);
        goToPost.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        goToPost.setTextColor(Color.WHITE);
        goToPost.setLayoutParams(redirectParams);
        goToPost.setText("Pokaż cały post");
        goToPost.setOnClickListener(
                view -> {
                    Intent thePostIntent = new Intent(this, ThePost.class);
                    thePostIntent.putExtra("id_user", id);
                    thePostIntent.putExtra("id_post", id_post);
                    startActivity(thePostIntent);
                }
        );

        View divider = new View(posts.getContext());
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int) getResources().getDisplayMetrics().density);
        dividerParams.setMargins(32, 32, 32, 32);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_400));

        likesRow.addView(goToPost);
        textRow.addView(textLabel);
        authorRow.addView(authorLabel);

        table.addView(authorRow);
        table.addView(textRow);
        table.addView(likesRow);
        posts.addView(table);
        posts.addView(divider);
    }

    @SuppressLint("SetTextI18n")
    void showComments(int id_post, String autor, String tresc, String dataUtworzenia, String autorPosta){
        LinearLayout comment = new LinearLayout(comments.getContext());
        LinearLayout.LayoutParams linParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        comment.setOrientation(LinearLayout.VERTICAL);
        comment.setId(View.generateViewId());
        comment.setLayoutParams(linParams);

        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow authorRow = new TableRow(comment.getContext());
        authorRow.setId(View.generateViewId());
        authorRow.setLayoutParams(rowParams);

        TextView authorText = new TextView(authorRow.getContext());
        authorText.setId(View.generateViewId());
        authorText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        authorText.setPadding(16, 16, 16, 4);
        authorText.setText(autor + " odpowiedział " + autorPosta);

        TableRow content = new TableRow(comment.getContext());
        content.setId(View.generateViewId());
        content.setLayoutParams(rowParams);

        TextView contentText = new TextView(authorRow.getContext());
        contentText.setId(View.generateViewId());
        contentText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        contentText.setPadding(16, 4, 16, 4);
        contentText.setAutoLinkMask(Linkify.ALL);
        contentText.setMovementMethod(LinkMovementMethod.getInstance());
        contentText.setText(tresc);

        TableRow added = new TableRow(comment.getContext());
        added.setId(View.generateViewId());
        added.setLayoutParams(rowParams);

        TextView addedText = new TextView(authorRow.getContext());
        addedText.setId(View.generateViewId());
        addedText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        addedText.setPadding(16, 4, 16, 16);
        addedText.setText("Dodano " + dataUtworzenia);

        Button goToPost = new Button(comments.getContext());
        goToPost.setId(View.generateViewId());
        TableRow.LayoutParams redirectParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 96, 1f);
        redirectParams.setMargins(16, 4, 16, 4);
        goToPost.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        goToPost.setTextColor(Color.WHITE);
        goToPost.setLayoutParams(redirectParams);
        goToPost.setText("Pokaż cały post");
        goToPost.setOnClickListener(
                view -> {
                    Intent thePostIntent = new Intent(this, ThePost.class);
                    thePostIntent.putExtra("id_user", id);
                    thePostIntent.putExtra("id_post", id_post);
                    startActivity(thePostIntent);
                }
        );

        View divider = new View(comments.getContext());
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int) getResources().getDisplayMetrics().density);
        dividerParams.setMargins(32, 32, 32, 32);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_400));

        authorRow.addView(authorText);
        content.addView(contentText);
        added.addView(addedText);

        comment.addView(authorRow);
        comment.addView(content);
        comment.addView(added);

        comments.addView(comment);
        comments.addView(goToPost);
        comments.addView(divider);
    }
}