package pl.krzysiek.simplesocial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.*;
import android.widget.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.*;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class Homepage extends AppCompatActivity implements Serializable {

    public static int id;

    TextView loggedAs;
    FloatingActionButton newPostBtn;
    Button refreshBtn;

    ScrollView homeView, menuView, searchView;
    TabLayout navigationTab;
    LinearLayout postsList, searchResults;
    int posts = 0;

    EditText searchBar;
    ImageView avatarUser;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    Server server;
    Helpers helper;
    Activity activity = this;
    Intent imageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        GetFromMain();
        onLoad();
        refreshHomepage();
        changeTab();
    }

    void GetFromMain(){
        id = Main.id;
        settings = Main.settings;
        editor = Main.editor;
    }

    public void AddNewPost(View v){
        Intent intent = new Intent(this, NewPost.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    public void onLoad(){
        postsList = findViewById(R.id.postsList);
        server = new Server();
        helper = new Helpers();
        loggedAs = findViewById(R.id.loggedAsText);
        newPostBtn = findViewById(R.id.button);
        refreshBtn = findViewById(R.id.refreshHomepage);
        avatarUser = findViewById(R.id.avatarUser);

        searchBar = findViewById(R.id.searchET);
        navigationTab = findViewById(R.id.navigationTab);
        searchView = findViewById(R.id.searchView);
        homeView = findViewById(R.id.homeView);
        menuView = findViewById(R.id.menuView);

        searchResults = findViewById(R.id.searchResults);

        ResultSet rs = server.query("username, nazwaWyswietlana, avatar", "users", "id = " + id);
        try {
            while(rs.next()) {
                if(rs.getString(2).matches(""))
                    loggedAs.setText("Witaj,\n" + rs.getString(1));
                else
                    loggedAs.setText("Witaj,\n" + rs.getString(2));

                if(rs.getString(3) != null) {
                    if (!rs.getString(3).matches(""))
                        avatarUser.setImageBitmap(helper.convertToBitmap(rs.getString(3)));
                    else
                        avatarUser.setImageIcon(Icon.createWithResource(this, R.drawable.default_avatar));
                } else
                    avatarUser.setImageIcon(Icon.createWithResource(this, R.drawable.default_avatar));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addMore(View v){
        refreshHomepage();
    }

    public void refresh(View v){
        postsList.removeAllViewsInLayout();
        posts = 0;
        refreshHomepage();
    }

    public void changeAvatar(View v){
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

    @SuppressLint("StaticFieldLeak")
    public void refreshHomepage(){
        /*String[][] publicTable = null;
        new Thread(() -> {
            int size = 0;
            String[][] table = null;
            try {
                ResultSet rs1 = server.query("posts.id_post", "posts");
                if (rs1 != null) {
                    rs1.last();
                    size = rs1.getRow();
                }
                int[] id_posts = new int[size];

                ResultSet rs2 = server.query("posts.id_post", "posts");
                while (rs2.next()) {
                    for (int i = 0; i < size; i++) {
                        rs2.absolute(i + 1);
                        id_posts[i] = rs2.getInt(1);
                    }
                }

                table = new String[size][5];
                for (int i = 0; i < size; i++) {
                    String[][] finalTable = table;
                    int x = i;
                    new Thread(() ->{
                        GetEveryData data = new GetEveryData(id_posts[x], server);
                        finalTable[x][0] = data.giveData()[0];
                        finalTable[x][1] = data.giveData()[1];
                        finalTable[x][2] = data.giveData()[2];
                        finalTable[x][3] = data.giveData()[3];
                        finalTable[x][4] = data.giveData()[4];
                    }).start();
                    runOnUiThread(() -> {
                        helper.GeneratePosts(
                                postsList,
                                id,
                                activity,
                                getBaseContext(),
                                "homepage",
                                finalTable[x][0],
                                finalTable[x][1],
                                finalTable[x][2],
                                finalTable[x][4],
                                finalTable[x][3]
                        );
                    });
                }
                publicTable = table;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }).start();*/
        ResultSet rs = server.query("posts.id_post, users.username, posts.tresc, posts.dataDodania, posts.tytul", "posts, users", "users.id = posts.id_autor",  "ORDER BY id_post DESC LIMIT " + posts + ",5");
        new AsyncTask<ResultSet, Void, String>(){
            @Override
            protected String doInBackground(ResultSet... params){
                        runOnUiThread(() -> {
                            try {
                                while (rs.next()) {
                            helper.GeneratePosts(
                                    postsList,
                                    id,
                                    activity,
                                    getBaseContext(),
                                    "homepage",
                                    params[0].getString(1),
                                    params[0].getString(2),
                                    params[0].getString(3),
                                    params[0].getString(5),
                                    params[0].getString(4)
                            );
                                }
                            } catch (SQLException ex){
                                ex.printStackTrace();
                            }
                        });
                return "";
            }
        }.execute(rs);

        Toast.makeText(this, "Pomyślnie zaktualizowano!", Toast.LENGTH_SHORT).show();
        posts += 5;
    }

    public String[] con(String[] to){ return to;}

    public void goToProfile(View v){
        Intent intent = new Intent(this, Profile.class);
        intent.putExtra("id_user", id);
        startActivity(intent);
        finish();
    }

    public void changeTab(){
        navigationTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 0:
                        homeView.setVisibility(View.VISIBLE);
                        refreshBtn.setVisibility(View.VISIBLE);
                        newPostBtn.setVisibility(View.VISIBLE);
                        break;
                    case 1:

                        break;
                    case 2:
                        searchView.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        menuView.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 0:
                        homeView.setVisibility(View.GONE);
                        refreshBtn.setVisibility(View.GONE);
                        newPostBtn.setVisibility(View.GONE);
                        break;
                    case 1:

                        break;
                    case 2:
                        searchView.setVisibility(View.GONE);
                        break;
                    case 3:
                        menuView.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void logOff(View v){
        editor.putInt("userID", 0);
        editor.apply();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        finish();
    }

    public void editProfile(View v){
        Intent intent = new Intent(this, EditProfile.class);
        intent.putExtra("id_user", id);
        startActivity(intent);
        finish();
    }

    public void editDescription(View v){
        EditText descriptionText = new EditText(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(4,4,4,4);
        descriptionText.setLayoutParams(params);
        descriptionText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        try{
            ResultSet rs = server.query("opis", "users", "id = " + id);

            while(rs.next()){
                descriptionText.setText(rs.getString(1));
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        alert.setTitle("Twój opis");
        alert.setView(descriptionText);

        alert.setPositiveButton("Zapisz", (dialogInterface, i) -> {
            server.update("users", "opis = '" + descriptionText.getText().toString() + "'", "id = " + id);
            Toast.makeText(alert.getContext(), "Opis został zmieniony", Toast.LENGTH_SHORT).show();
        });

        alert.setNegativeButton("Anuluj", (dialogInterface, i) -> {
        });

        alert.show();
    }

    public void search(View v){
        searchResults.removeAllViewsInLayout();
        findInAll(searchBar.getText().toString());
    }

    public void findInAll(String phrase){
        try {
            // Uzytkownicy
            ResultSet usersRS = server.query("username, opis, nazwaWyswietlana, zweryfikowany, avatar, id", "users", "username LIKE '%" + phrase + "%' OR nazwaWyswietlana LIKE '%" + phrase + "%' OR opis LIKE '%" + phrase + "%' LIMIT 3");
            if(usersRS.isBeforeFirst()){
                TextView textOne = new TextView(searchResults.getContext());
                textOne.setId(View.generateViewId());
                LinearLayout.LayoutParams textOneParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textOne.setPadding(8, 8, 8, 8);
                textOne.setLayoutParams(textOneParams);
                textOne.setTextAppearance(R.style.TextAppearance_AppCompat_Medium);
                textOne.setText("Użytkownicy");
                searchResults.addView(textOne);

                while (usersRS.next()) {
                    int id_user = usersRS.getInt(6);

                    TableRow userRow = new TableRow(searchResults.getContext());
                    userRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    userRow.setId(View.generateViewId());
                    userRow.setPadding(8,8,8,8);

                    ImageView avatarUser = new ImageView(userRow.getContext());
                    avatarUser.setId(View.generateViewId());
                    TableRow.LayoutParams avatarParams = new TableRow.LayoutParams((int) getResources().getDisplayMetrics().density * 64, (int) getResources().getDisplayMetrics().density * 64);
                    avatarParams.setMargins(8, 8, 4, 8);
                    avatarUser.setLayoutParams(avatarParams);
                    avatarUser.setAdjustViewBounds(true);
                    avatarUser.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    if (usersRS.getString(5) != null) {
                        avatarUser.setImageBitmap(helper.convertToBitmap(usersRS.getString(5)));
                    } else {
                        avatarUser.setImageIcon(Icon.createWithResource(this, R.drawable.default_avatar));
                    }

                    avatarUser.setOnClickListener(view -> {
                        Intent intent = new Intent(this, Profile.class);
                        intent.putExtra("id_user", id_user);
                        startActivity(intent);
                        finish();
                    });
                    userRow.addView(avatarUser);

                    LinearLayout otherData = new LinearLayout(userRow.getContext());
                    otherData.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    otherData.setOrientation(LinearLayout.VERTICAL);
                    otherData.setId(View.generateViewId());

                    TableRow usernameRow = new TableRow(otherData.getContext());
                    usernameRow.setId(View.generateViewId());
                    usernameRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    if (usersRS.getBoolean(4)) {
                        ImageView verifiedIcon = new ImageView(usernameRow.getContext());
                        verifiedIcon.setId(View.generateViewId());
                        TableRow.LayoutParams verifiedParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
                        verifiedParam.setMargins(8, 8, 4, 4);
                        verifiedIcon.setLayoutParams(verifiedParam);
                        verifiedIcon.setImageIcon(Icon.createWithResource(this, R.drawable.verified_icon));
                        usernameRow.addView(verifiedIcon);
                    }

                    TextView username = new TextView(usernameRow.getContext());
                    username.setId(View.generateViewId());
                    TableRow.LayoutParams usernameParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    usernameParams.setMargins(4, 8, 8, 4);
                    username.setLayoutParams(usernameParams);
                    username.setText(usersRS.getString(1));
                    username.setOnClickListener(view -> {
                        Intent intent = new Intent(this, Profile.class);
                        intent.putExtra("id_user", id_user);
                        startActivity(intent);
                        finish();
                    });

                    TextView description = new TextView(otherData.getContext());
                    description.setId(View.generateViewId());
                    TableRow.LayoutParams descriptionParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    descriptionParams.setMargins(8, 4, 8, 8);
                    description.setLayoutParams(descriptionParams);
                    description.setText(usersRS.getString(2));
                    description.setOnClickListener(view -> {
                        Intent intent = new Intent(this, Profile.class);
                        intent.putExtra("id_user", id_user);
                        startActivity(intent);
                        finish();
                    });

                    usernameRow.addView(username);
                    otherData.addView(usernameRow);
                    otherData.addView(description);
                    userRow.addView(otherData);

                    searchResults.addView(userRow);
                }
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void postManagement(View v) {
        Intent intent = new Intent(this, PostsManagement.class);
        startActivity(intent);
        finish();
    }

    public void commentManagement(View v){
        Intent intent = new Intent(this, CommentsManagement.class);
        intent.putExtra("user", id);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            if(data != null){
                try{
                    imageData = data;
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageData.getData());
                    Toast.makeText(this, helper.getThatPath(data.getData(), this), Toast.LENGTH_SHORT).show();
                    server.update("users", "avatar = '" + helper.convertToBase64(imageData, this) + "'", "id = " + id);
                } catch (IOException ex){
                    ex.printStackTrace();
                }
            }
            avatarUser.setImageBitmap(bitmap);
            Toast.makeText(this, "Zmieniono", Toast.LENGTH_SHORT).show();
        }
    }
}