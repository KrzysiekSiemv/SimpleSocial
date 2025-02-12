package pl.krzysiek.simplesocial;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetEveryData extends AsyncTask<Integer, Void, String[][]> {
    Helpers helper;
    Server server;
    public GetEveryData(Helpers help, Server srv){
        helper = help;
        server = srv;
    }

    @Override
    protected String[][] doInBackground(Integer[] params){
        String[][] table = new String[params.length][5];
        for(int i = 0; i < params.length; i++){
            ResultSet rs = server.query("posts.id_post, users.username, posts.tresc, posts.dataDodania, posts.tytul", "posts, users", "posts.id_post = " + params[i] + " AND users.id = posts.id_autor");
            try{
                while(rs.next()){
                    for(int j = 0; j < 5; j++){
                        table[i][j] = rs.getString(j + 1);
                    }
                }
            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }
        return table;
    }

    @Override
    protected void onPostExecute(String[][] table){
        super.onPostExecute(table);
    }
}