package mina.egypt.android.blog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Home");

        mAuth = FirebaseAuth.getInstance();

        // Firebase User authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");

        // Set RecyclerView Props
        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isOnline()) {

            mAuth.addAuthStateListener(mAuthListener);

            // set firebase recyclerAdapter , params ( model, layoutRow, viewHolder, databaseReference)
            FirebaseRecyclerAdapter<mina.egypt.android.blog.Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<mina.egypt.android.blog.Blog, BlogViewHolder>(

                    mina.egypt.android.blog.Blog.class,
                    R.layout.blog_row,
                    BlogViewHolder.class,
                    mDatabase
            ) {
                @Override
                protected void populateViewHolder(BlogViewHolder viewHolder, mina.egypt.android.blog.Blog model, int position) {

                    DatabaseReference postRef = getRef(position);
                    final String postUID = postRef.getKey();

                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setDesc(model.getDesc());
                    viewHolder.setUsername(model.getUsername());
                    viewHolder.setImage(getApplicationContext(), model.getImage());
                    viewHolder.onCardViewLongClick(MainActivity.this, model.getUser_id(), postUID, model.getTitle());
                }
            };

            // add adapter to recyclerView
            mBlogList.setAdapter(firebaseRecyclerAdapter);
        }else{
            Toast.makeText(this,"Please Check your Internet Connection.",Toast.LENGTH_LONG).show();
        }
    }


    // RecyclerView viewHolder
    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public BlogViewHolder(View mView) {
            super(mView);
            this.mView =mView;
        }

        public void setTitle(String title){
            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setLongClickable(false);
            post_title.setText(title);
        }

        public void setDesc(String desc){
            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setLongClickable(false);
            post_desc.setText(desc);
        }

        public void setImage(Context ctx ,String image){
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            post_image.setLongClickable(false);
            Picasso.with(ctx).load(image).into(post_image);

        }

        public void setUsername (String username){
            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setLongClickable(false);
            post_username.setText(username);
        }

        public void onCardViewLongClick(final Context ctx, final String postOwnerUID, final String postUID, final String postTitle){
            CardView post_cardView = (CardView) mView.findViewById(R.id.post_cardView);


            // LongClickListener on recyclerView items
            post_cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // show delete or cancel dialog
                    AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
                    alertDialog.setTitle(postTitle);
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setMessage("Are you sure you want to delete ?");

                    // deleting post only if user is the post owner
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DELETE",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                    if( currentUserID.equals(postOwnerUID)){
                                        DatabaseReference mDatabaseBlogs = FirebaseDatabase.getInstance().getReference().child("Blog");
                                        mDatabaseBlogs.child(postUID).removeValue();
                                        Toast.makeText(ctx, "Successfully Deleted \uD83D\uDE02", Toast.LENGTH_LONG).show();
                                    }else {
                                        Toast.makeText(ctx, "Sorry, only post owners can delete their posts ", Toast.LENGTH_LONG).show();
                                    }

                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    //Toast.makeText(ctx, "Item: " + pos + " clicked " + postUID, Toast.LENGTH_SHORT).show();

                    return false;
                }
            });
        }
    }


    // check connection
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return( netInfo != null && netInfo.isConnectedOrConnecting());
    }



    // inflate option menu ( + add Post) , logout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    // on item selected from Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_add){
            if(isOnline() && (mAuth.getCurrentUser() != null)){
                startActivity(new Intent(MainActivity.this,PostActivity.class));
            }else{
                Toast.makeText(this,"Don't try to scam us Please Login \uD83D\uDE02",Toast.LENGTH_LONG).show();
            }
        }

        if (item.getItemId() == R.id.action_logout){
            logout();
        }


        return super.onOptionsItemSelected(item);
    }


    private void logout() {
        mAuth.addAuthStateListener(mAuthListener);
        mAuth.signOut();
    }
}
