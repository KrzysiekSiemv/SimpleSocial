package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Main extends AppCompatActivity {

    public static int id;
    boolean automaticLogIn = false;

    EditText usernameText;
    EditText passwordText;
    Switch showPassword;
    public static Server server;

    public static SharedPreferences settings;
    public static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onLoad();
        passwordText.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_GO){
                if(!usernameText.getText().toString().matches("") && !passwordText.getText().toString().matches(""))
                    LogIn();
                else
                    Toast.makeText(this, "Nie możesz się zalogować bez podania danych do logowania!", Toast.LENGTH_LONG).show();
            }

            return true;
        });

        showPassword.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
                passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            else {
                passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    public void onLoad(){
        setObjects();
        policySet();
        server = new Server();
        showPassword = findViewById(R.id.showPassword);

        settings = getApplicationContext().getSharedPreferences("userID", MODE_PRIVATE);
        editor = settings.edit();

        if(settings.getInt("userID", MODE_PRIVATE) == 0) {
            automaticLogIn = false;
        }else{
            automaticLogIn = true;
            LogIn();
        }
    }

    @Override
    public void onBackPressed(){
        server.close();
        finish();
    }

    void setObjects(){
        usernameText = findViewById(R.id.usernameBar);
        passwordText = findViewById(R.id.passwordBar);
    }

    // Wymagane, aby mógł się połączyć z siecią
    public void policySet(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void AddToTable(View v){
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }

    public void LogInBtn(View v){
        if(!usernameText.getText().toString().matches("") && !passwordText.getText().toString().matches(""))
            LogIn();
        else
            Toast.makeText(this, "Nie możesz się zalogować bez podania danych do logowania!", Toast.LENGTH_LONG).show();
    }

    void LogIn(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener dialogClickListener;
        try{
            if(!automaticLogIn){
                ResultSet rs = server.query("id, username, nazwaWyswietlana", "users", "username = '" + usernameText.getText() + "' OR numerTelefonu = '" + usernameText.getText() + "' OR email = '" + usernameText.getText() + "' AND password = PASSWORD('" + passwordText.getText() + "')");

                if(rs.isBeforeFirst()){
                    while (rs.next()) {
                        id = rs.getInt(1);
                        String username = rs.getString(2);
                        String displayname = rs.getString(3);
                        dialogClickListener = (dialogInterface, i) -> {
                            switch (i) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    editor.putInt("userID", id);
                                    editor.apply();
                                    TheThirdLogIn(username, displayname);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    TheThirdLogIn(username, displayname);
                                    break;
                            }
                        };
                        builder.setMessage("Czy chcesz zachować zalogowane konto na urządzeniu?").setPositiveButton("Tak", dialogClickListener).setNegativeButton("Nie", dialogClickListener).show();
                    }
                } else {
                    Toast.makeText(this, "Nie poprawne dane logowania", Toast.LENGTH_LONG).show();
                }
            } else {
                id = settings.getInt("userID", 0);
                ResultSet rs = server.query("username, nazwaWyswietlana", "users", "id = " + id);

                while(rs.next()){
                    TheThirdLogIn(rs.getString(1), rs.getString(2));
                }
            }
        } catch(SQLException ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void GoToHomepage(){
        Intent intent = new Intent(this, Homepage.class);
        startActivity(intent);
        finish();
    }

    void TheThirdLogIn(String username, String displayname){
        if (displayname == null)
            Toast.makeText(Main.this, "Witaj ponownie, " + username + "!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(Main.this, "Witaj ponownie, " + displayname + "!", Toast.LENGTH_LONG).show();
        GoToHomepage();
    }
}