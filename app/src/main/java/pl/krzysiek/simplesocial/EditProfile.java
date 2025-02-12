package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class EditProfile extends AppCompatActivity {

    static int id_user;
    Server server;

    TextView username, birthdateBox;
    EditText nameBox, emailBox, phoneBox, actualPassBox, newPassBox, rpPassBox;
    RadioButton maleBox, femaleBox;
    Button deleteBtn;

    DatePickerDialog.OnDateSetListener dateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        onLoad();
    }

    void onLoad(){
        Bundle fromIntent = getIntent().getExtras();
        server = new Server();

        id_user = fromIntent.getInt("id_user");

        loadObjects();
        fillItAll();
    }

    @SuppressLint("SetTextI18n")
    void loadObjects(){
        username = findViewById(R.id.verifiedIcon);
        nameBox = findViewById(R.id.nameBox);
        emailBox = findViewById(R.id.emailBox);
        phoneBox = findViewById(R.id.phoneBox);
        actualPassBox = findViewById(R.id.actualPassBox);
        newPassBox = findViewById(R.id.newPassBox);
        rpPassBox = findViewById(R.id.rpPassBox);
        birthdateBox = findViewById(R.id.birthdateBox);

        maleBox = findViewById(R.id.maleBtn);
        femaleBox = findViewById(R.id.femaleBtn);

        deleteBtn = findViewById(R.id.deleteBtn);

        dateSetListener = (datePicker, i, i1, i2) -> {
                String year, month, day;
                i1++;
                month = (i1 < 10 ? "0" + i1 : Integer.toString(i1));
                day = (i2 < 10 ? "0" + i2 : Integer.toString(i2));
                year = Integer.toString(i);

                birthdateBox.setText(year + "/" + month + "/" + day);
        };
    }

    public void pickADate(View v){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, android.app.AlertDialog.THEME_HOLO_DARK, dateSetListener, year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    void fillItAll(){
        try{
            ResultSet rs = server.query("username, nazwaWyswietlana, email, numerTelefonu, dataUrodzenia, plec", "users", "id = " + id_user);
            while(rs.next()){
                username.setText(rs.getString(1));
                nameBox.setText(rs.getString(2));
                emailBox.setText(rs.getString(3));
                phoneBox.setText(rs.getString(4));
                birthdateBox.setText(rs.getString(5));

                if(rs.getBoolean(6))
                    maleBox.setChecked(true);
                else
                    femaleBox.setChecked(true);
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void exitEdit(View v){
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

    public void updateProfile(View v){
        try{
            ResultSet rs = server.query("id", "users", "password = PASSWORD('" + actualPassBox.getText() + "') AND id = " + id_user);
            if(!actualPassBox.getText().equals("")) {
                while (rs.next()) {
                    if (rs.getInt(1) == id_user) {
                        server.update("users", "nazwaWyswietlana = '" + nameBox.getText() + "', email = '" + emailBox.getText() + "', numerTelefonu = '" + phoneBox.getText() + "', dataUrodzenia = '" + birthdateBox.getText() + "'", "id = " + id_user);

                            if(maleBox.isChecked())
                                server.update("users", "plec = 1", "id = " + id_user);
                            else
                                server.update("users", "plec = 0", "id = " + id_user);

                        Toast.makeText(this, "Profil został zaktualizowany!", Toast.LENGTH_SHORT).show();
                        if (newPassBox.getText() == rpPassBox.getText()) {
                            server.update("users", "password = PASSWORD('" + newPassBox.getText() + "')", "id = " + id_user);
                            Toast.makeText(this, "Hasło zostało zaktualizowane!", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    } else
                        Toast.makeText(this, "Wpisz swoje obecne hasło, aby zaktualizować swój profil!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Wpisz swoje obecne hasło, aby zaktualizować swój profil!", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public void deleteBtnAction(View v){
        areYouSure();
    }

    void areYouSure(){
        AlertDialog.Builder builder = new AlertDialog.Builder(deleteBtn.getContext());
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        server.delete("posts", "id_autor = " + id_user);
                        server.delete("comments", "id_author = " + id_user);
                        Toast.makeText(deleteBtn.getContext(), "Konto zostało usunięte :(", Toast.LENGTH_SHORT).show();
                        server.delete("users", "id = " + id_user);
                        Intent intent = new Intent(deleteBtn.getContext(), Main.class);
                        finish();
                        startActivity(intent);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(deleteBtn.getContext(), "Uff... Fajnie, że zostajesz z nami :D", Toast.LENGTH_SHORT).show();
                        break;
            }
        };

        builder.setMessage("Czy na pewno chcesz usunąć konto? Wszystkie Twoje posty, komentarze oraz inne ślady znikną z serwisu. :/").setPositiveButton("Tak", dialogClickListener).setNegativeButton("Nie", dialogClickListener).show();
    }
}