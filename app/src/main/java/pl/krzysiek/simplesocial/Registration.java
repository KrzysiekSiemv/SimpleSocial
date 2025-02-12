package pl.krzysiek.simplesocial;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import java.sql.*;
import java.util.Calendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.*;

public class Registration extends AppCompatActivity {

    EditText nicknameBox, passwordBox, rpPasswordBox, nameBox, emailBox, phoneBox;
    RadioButton maleBox, femaleBox;
    TextView passwordIdentityText, birthdateBox, tosLink;
    CheckBox acceptToS;
    Button registerBtn;

    String[] firstPartMale = {"Ambitny", "Zabawny", "Kreatywny", "Śmierdzacy", "Stanowczy", "Lojalny", "Dziwny", "Przyzwoity", "Przemądrzały", "Optymistyczny", "Pesymistyczny", "Zwariowany", "Zmarnowany", "Towarzyski", "Szczery", "Latający", "Aktywny", "Bezużyteczny", "Nieśmieszny"};
    String[] secondPartMale = {"Dinozaur", "Robot", "Odkurzacz", "Tygrys", "Nerd", "Gracz", "Przegryw", "Król", "Kujon", "Kierowca", "Drifter", "Aparaciarz", "Zgredek"};
    String[] firstPartFemale = {"Ambitna", "Zabawna", "Kreatywna", "Śmierdzaca", "Stanowcza", "Lojalna", "Dziwna", "Przyzwoita", "Przemądrzała", "Optymistyczna", "Pesymistyczna", "Zwariowana", "Zmarnowana", "Towarzyska", "Szczera", "Latająca", "Aktywna", "Bezużyteczna", "Nieśmieszna"};
    String[] secondPartFemale = {"Dinozaur", "Robot", "Tygrysica", "Nerdini", "Graczka", "Przegrywka", "Królowa", "Kujonka", "Kierowczyni", "Drifterka", "Aparatka", "Zgredka"};

    DatePickerDialog.OnDateSetListener dateSetListener;
    Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        onLoad();
    }

    @SuppressLint("SetTextI18n")
    void onLoad(){
        nicknameBox = findViewById(R.id.usernameBar);
        birthdateBox = findViewById(R.id.birthdateBox);
        passwordBox = findViewById(R.id.passwordBox);
        rpPasswordBox = findViewById(R.id.rpPasswordBox);
        nameBox = findViewById(R.id.nameBox);
        emailBox = findViewById(R.id.emailBox);
        phoneBox = findViewById(R.id.phoneBox);
        tosLink = findViewById(R.id.tosLink);

        tosLink.setMovementMethod(LinkMovementMethod.getInstance());

        server = new Server();

        maleBox = findViewById(R.id.maleBtn);
        femaleBox = findViewById(R.id.femaleBtn);

        passwordIdentityText = findViewById(R.id.identityLogText);
        registerBtn = findViewById(R.id.registerBtn);

        acceptToS = findViewById(R.id.acceptToS);

        dateSetListener = (datePicker, i, i1, i2) -> {
            String year, month, day;
            i1++;
            month = (i1 < 10 ? "0" + i1 : Integer.toString(i1));
            day = (i2 < 10 ? "0" + i2 : Integer.toString(i2));
            year = Integer.toString(i);

            birthdateBox.setText(year + "/" + month + "/" + day);
        };
    }

    public void birthdateEvent(View v){
        Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.THEME_HOLO_LIGHT, dateSetListener, year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void randomMeNickname(View v){
        Random random = new Random();
        StringBuilder x = new StringBuilder();

        if(maleBox.isChecked()) {
            x.append(firstPartMale[random.nextInt(firstPartMale.length)]);
            x.append(secondPartMale[random.nextInt(secondPartMale.length)]);
        } else {
            x.append(firstPartFemale[random.nextInt(firstPartFemale.length)]);
            x.append(secondPartFemale[random.nextInt(secondPartFemale.length)]);
        }
        x.append(random.nextInt(999));

        nicknameBox.setText(x);
    }

    public void registerMe(View v){
        int plec;

        if(maleBox.isChecked())
            plec = 1;
        else
            plec = 0;

        Pattern p = Pattern.compile("[^A-Za-z0-9]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(nicknameBox.getText());

        if(acceptToS.isChecked()) {
            if(!m.find()) {
                if (passwordBox.getText().toString().equals(rpPasswordBox.getText().toString())) {
                    if (!nicknameBox.getText().equals("") && !birthdateBox.getText().equals("")) {
                        if (nicknameBox.getText().length() > 3) {
                            try {
                                server.insert("users", "username, password, nazwaWyswietlana, email, numerTelefonu, dataUrodzenia, plec",
                                        "'" + nicknameBox.getText() + "', PASSWORD('" + passwordBox.getText() + "'), '" + nameBox.getText() + "', '" + emailBox.getText() + "', '" + phoneBox.getText() + "', '" + birthdateBox.getText() + "', " + plec);
                                if (!nameBox.getText().equals("")) {
                                    Toast.makeText(this, "Konto zostało utworzone. Witaj na pokładzie, " + nameBox.getText() + "!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Konto zostało utworzone. Witaj na pokładzie, " + nicknameBox.getText() + "!", Toast.LENGTH_SHORT).show();
                                }

                                ResultSet rs = server.query("id", "users", "username = '" + nicknameBox.getText() + "'");
                                while (rs.next()) {
                                    Main.id = rs.getInt(1);
                                    Intent intent = new Intent(this, Homepage.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        } else Toast.makeText(this, "Za krótka nazwa użytkownika! Minimalnie 4 znaki.", Toast.LENGTH_LONG).show();
                    } else Toast.makeText(this, "Nazwa użytkownika jest wymagana! Jeżeli nie masz pomysłu, to wygeneruj sobie.", Toast.LENGTH_LONG).show();
                } else Toast.makeText(this, "Wpisane hasła nie są identyczne, for blyat sake!", Toast.LENGTH_LONG).show();
            } else Toast.makeText(this, "Nazwa użytkownika nie może zawierać znaków specjalnych. Jedynie litery oraz cyfry zezwolone!", Toast.LENGTH_LONG).show();
        } else Toast.makeText(this, "Aby korzystać z serwisu musisz zakceptować regulamin!", Toast.LENGTH_LONG).show();
    }

    public void cancelRegistration(View v){
        finish();
    }
}