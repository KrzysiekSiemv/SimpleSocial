package pl.krzysiek.simplesocial;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.Debug;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.IOException;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

public class Helpers extends AppCompatActivity {

    final Server server = new Server();
    Bitmap bmp;

    public Bitmap convertToBitmap(String base64) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public String getThatPath(Uri uri, Context activity){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        String yourRealPath = null;
        Cursor cursor = activity.getContentResolver().query(uri, filePathColumn, null, null, null);
        if(cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            yourRealPath = cursor.getString(columnIndex);
        } else
            Debug.logStack("Błąd!", "Coś wyszło nie tak przy getThatPath. Linia 30 w Helpers", 1);

        cursor.close();
        return yourRealPath;
    }

    public String convertToBase64(Intent data, Context activity) {
        String x = "";
        try {
            byte[] fileContent = org.apache.commons.io.FileUtils.readFileToByteArray(new File(getThatPath(data.getData(), activity)));
            x = Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return x;
    }

    public String GetDate(){
        LocalDateTime dateTime = LocalDateTime.now();
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "UseCompatLoadingForDrawables", "StaticFieldLeak"})
    public void GeneratePosts(LinearLayout postsList, int id_user, Activity activity, Context context, String from, String id_post, String autor, String text, String title, String datetime){
        boolean nsfw = false;

        TableLayout table = new TableLayout(postsList.getContext());
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        table.setId(View.generateViewId());
        table.setLayoutParams(tableParams);

        TableRow authorRow = new TableRow(table.getContext());
        authorRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        TextView authorLabel = new TextView(authorRow.getContext());
        authorLabel.setPadding(16,16,16,16);
        authorLabel.setText(autor + " \nDodano: " + datetime);

        /*                          */

        TableRow titleRow = new TableRow(table.getContext());
        titleRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        TextView titleLabel = new TextView(titleRow.getContext());
        titleLabel.setPadding(16,16,16,4);
        titleLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        titleLabel.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        titleLabel.setMaxLines(2);
        titleLabel.setTextSize(20f);
        titleLabel.setText(title);
        titleLabel.setOnClickListener(view -> {
            Intent thePostIntent = new Intent(context, ThePost.class);
            thePostIntent.putExtra("id_user", id_user);
            thePostIntent.putExtra("id_post", id_post);
            thePostIntent.putExtra("from", from);
            activity.startActivity(thePostIntent);
        });

        /*                          */

        TableRow textRow = new TableRow(table.getContext());
        textRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));

        TextView textLabel = new TextView(textRow.getContext());
        textLabel.setPadding(16,4,16,16);
        textLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textLabel.setMaxLines(5);
        textLabel.setTextSize(20f);
        textLabel.setText(text);
        textLabel.setOnClickListener(view -> {
            Intent thePostIntent = new Intent(context, ThePost.class);
            thePostIntent.putExtra("id_user", id_user);
            thePostIntent.putExtra("id_post", id_post);
            thePostIntent.putExtra("from", from);
            activity.startActivity(thePostIntent);
        });

        ImageView image = new ImageView(table.getContext());
        try{
            ResultSet haveImage = server.query("typPosta", "posts", "id_post = " + id_post);
            while(haveImage.next()){
                if(haveImage.getBoolean(1)){
                    TableLayout.LayoutParams layout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    layout.setMargins(0, 16, 0, 32);
                    image.setLayoutParams(layout);
                    image.setAdjustViewBounds(true);
                    image.setId(View.generateViewId());
                    image.setMaxHeight(1920);
                    GetMeImage gmi = new GetMeImage(image, this, server);
                    gmi.doInBackground(id_post);
                    WeakReference<ImageView> imageViewWeakReference = new WeakReference<>(image);
                    new AsyncTask<String, Void, Bitmap>(){
                        @Override
                        protected Bitmap doInBackground(String... strings){
                            Bitmap bitmap = null;
                            try {
                                ResultSet rs = server.query("picture", "posts_pictures", "id_post = " + id_post);
                                while(rs.next()){
                                    bitmap = convertToBitmap(rs.getString(1));
                                }
                            } catch (SQLException ex){
                                ex.printStackTrace();
                            }
                            return bitmap;
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap){
                            super.onPostExecute(bitmap);
                            if(bitmap != null){
                                ImageView weak = imageViewWeakReference.get();
                                if(weak != null)
                                    weak.setImageBitmap(bitmap);
                            }
                        }
                    }.execute();
                            /*ResultSet getImage = server.query("picture", "posts_pictures", "id_post = " + id_post);
                            while (getImage.next()) {
                                image.setImageBitmap(convertToBitmap(getImage.getString(1)));*/

                }
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        TableRow nsfwTag = new TableRow(table.getContext());
        nsfwTag.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        nsfwTag.setGravity(Gravity.START);

        ImageView nsfwIcon = new ImageView(nsfwTag.getContext());
        nsfwIcon.setImageDrawable(context.getDrawable(R.drawable.warning));
        nsfwIcon.setId(View.generateViewId());
        TableRow.LayoutParams iconLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        iconLayout.setMargins(16,16,4,16);
        nsfwIcon.setLayoutParams(iconLayout);
        nsfwIcon.setBackgroundColor(Color.BLACK);
        nsfwIcon.setMaxWidth(32);

        TextView nsfwText = new TextView(nsfwTag.getContext());
        nsfwText.setText("NSFW");
        TableRow.LayoutParams textLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        textLayout.setMargins(4,16,16,16);
        nsfwText.setLayoutParams(textLayout);

        nsfwTag.addView(nsfwIcon);
        nsfwTag.addView(nsfwText);

        /*                          */

        TableRow likesRow = new TableRow(table.getContext());
        likesRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        ConstraintLayout inLikesRow = new ConstraintLayout(likesRow.getContext());
        inLikesRow.setId(View.generateViewId());
        TableRow.LayoutParams inLikesRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.0f);
        inLikesRow.setLayoutParams(inLikesRowParams);

        ContextThemeWrapper newContext = new ContextThemeWrapper(context, R.style.Widget_MaterialComponents_Button);

        Button likesBtn = new Button(newContext);
        likesBtn.setId(View.generateViewId());
        ConstraintLayout.LayoutParams likeParams = new ConstraintLayout.LayoutParams((int) context.getResources().getDisplayMetrics().density * 70, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        likeParams.setMarginStart(8);
        likeParams.startToStart = 0;
        likeParams.topToTop = 0;
        likeParams.bottomToBottom = 0;
        likesBtn.setGravity(Gravity.CENTER);

        TextView likesCount = new TextView(inLikesRow.getContext());
        ConstraintLayout.LayoutParams likesCountParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        likesCountParams.startToEnd = likesBtn.getId();
        likesCountParams.bottomToBottom = 0;
        likesCountParams.topToTop = 0;
        likesCountParams.setMarginStart(8);
        likesCount.setGravity(Gravity.CENTER);
        likesCount.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        likesCount.setId(View.generateViewId());
        likesCount.setLayoutParams(likesCountParams);
        try {
            ResultSet rs = server.query("COUNT(*)", "posts_likes", "id_post = " + id_post);
            while(rs.next())
                likesCount.setText(rs.getString(1));

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        likesBtn.setLayoutParams(likeParams);
        try{
            ResultSet rs = server.query("id_like", "posts_likes", "id_post = " + id_post + " AND id_user = " + id_user);
            if(!rs.isBeforeFirst())
                likesBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon, 0, 0, 0);
            else
                likesBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_icon, 0,0,0);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        likesBtn.setOnClickListener(view -> {
            try{
                ResultSet rs = server.query("id_like", "posts_likes", "id_post = " + id_post + " AND id_user = " + id_user);
                if(!rs.isBeforeFirst()) {
                    server.insert("posts_likes", "NULL, " + id_post + ", " + id_user + ", '" + GetDate() + "'");
                    likesBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_icon, 0,0,0);
                } else {
                    server.delete("posts_likes", "id_post = " + id_post + " AND id_user = " + id_user);
                    likesBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon, 0, 0, 0);
                }
                ResultSet rsLikes = server.query("COUNT(*)", "posts_likes", "id_post = " + id_post);
                while(rsLikes.next())
                    likesCount.setText(rsLikes.getString(1));

            } catch (SQLException ex){
                ex.printStackTrace();
            }
        });

        View divider = new View(inLikesRow.getContext());
        divider.setId(View.generateViewId());
        ConstraintLayout.LayoutParams dividerParams = new ConstraintLayout.LayoutParams((int) context.getResources().getDisplayMetrics().density, ConstraintLayout.LayoutParams.MATCH_PARENT);
        dividerParams.startToEnd = likesCount.getId();
        dividerParams.topToTop = 0;
        dividerParams.bottomToBottom = 0;
        dividerParams.setMargins(16, 0, 0, 0);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(context.getColor(R.color.gray_600));

        Button commentsBtn = new Button(newContext);
        commentsBtn.setId(View.generateViewId());
        ConstraintLayout.LayoutParams commentsParams = new ConstraintLayout.LayoutParams((int) context.getResources().getDisplayMetrics().density * 70, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        commentsParams.setMarginStart(16);
        commentsParams.startToEnd = divider.getId();
        commentsParams.topToTop = 0;
        commentsParams.bottomToBottom = 0;
        commentsBtn.setGravity(Gravity.CENTER);
        commentsBtn.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.comment_icon,0);
        commentsBtn.setLayoutParams(commentsParams);
        commentsBtn.setOnClickListener(view ->{
            Intent thePostIntent = new Intent(context, ThePost.class);
            thePostIntent.putExtra("id_user", id_user);
            thePostIntent.putExtra("id_post", id_post);
            thePostIntent.putExtra("from", from);
            activity.startActivity(thePostIntent);
        });

        View divider2 = new View(postsList.getContext());
        TableRow.LayoutParams divider2Params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int) context.getResources().getDisplayMetrics().density);
        divider2Params.setMargins(32, 32, 32, 32);
        divider2.setLayoutParams(divider2Params);
        divider2.setBackgroundColor(context.getColor(R.color.gray_400));

        try{
            ResultSet checkNsfw = server.query("nsfw", "posts", "id_post = " + id_post);
            while(checkNsfw.next()){
                nsfw = checkNsfw.getBoolean(1);
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        titleRow.addView(titleLabel);
        inLikesRow.addView(likesBtn);
        inLikesRow.addView(likesCount);
        inLikesRow.addView(divider);
        inLikesRow.addView(commentsBtn);
        likesRow.addView(inLikesRow);
        textRow.addView(textLabel);
        authorRow.addView(authorLabel);

        table.addView(authorRow);
        table.addView(titleRow);

        if(!nsfw) {
            table.addView(textRow);
            table.addView(image);
        } else {
            table.addView(nsfwTag);
        }
        table.addView(likesRow);
        postsList.addView(table);
        postsList.addView(divider2);
    }

    @SuppressLint("SetTextI18n")
    void GenerateComments(LinearLayout commentList, String autor, Context context, String tresc, String dataUtworzenia, TextView author){
        LinearLayout comment = new LinearLayout(commentList.getContext());
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
        authorText.setText(autor + " odpowiedział " + author.getText());

        TableRow content = new TableRow(comment.getContext());
        content.setId(View.generateViewId());
        content.setLayoutParams(rowParams);

        TextView contentText = new TextView(author.getContext());
        contentText.setId(View.generateViewId());
        contentText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        contentText.setPadding(16, 4, 16, 4);
        contentText.setAutoLinkMask(Linkify.ALL);
        contentText.setMovementMethod(LinkMovementMethod.getInstance());
        contentText.setText(tresc);

        TableRow added = new TableRow(comment.getContext());
        added.setId(View.generateViewId());
        added.setLayoutParams(rowParams);

        TextView addedText = new TextView(author.getContext());
        addedText.setId(View.generateViewId());
        addedText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        addedText.setPadding(16, 4, 16, 16);
        addedText.setText("Dodano " + dataUtworzenia);

        View divider = new View(commentList.getContext());
        TableRow.LayoutParams dividerParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int) context.getResources().getDisplayMetrics().density);
        dividerParams.setMargins(32, 32, 32, 32);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(context.getColor(R.color.gray_400));

        authorRow.addView(authorText);
        content.addView(contentText);
        added.addView(addedText);

        comment.addView(authorRow);
        comment.addView(content);
        comment.addView(added);

        commentList.addView(comment);
        commentList.addView(divider);
    }
}
