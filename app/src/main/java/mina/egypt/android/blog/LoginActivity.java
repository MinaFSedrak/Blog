package mina.egypt.android.blog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mloginEmail;
    private EditText mloginPassword;
    private Button mloginBtn;
    private Button mloginNewAccBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);

        mloginEmail = (EditText) findViewById(R.id.loginEmailField);
        mloginPassword = (EditText) findViewById(R.id.loginPasswordField);
        mloginBtn = (Button) findViewById(R.id.loginBtn);
        mloginNewAccBtn = (Button) findViewById(R.id.newAccountBtn);

        mloginBtn.setOnClickListener(this);

        mloginNewAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

    }

    private void checkLogin() {
        String email = mloginEmail.getText().toString().trim();
        String password = mloginPassword.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            mProgress.setMessage("Signing in...");
            mProgress.show();
            mProgress.setCancelable(false);
            mProgress.setCanceledOnTouchOutside(false);
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        checkUserExist();


                    }else {
                        Toast.makeText(LoginActivity.this, "Wrong Mail & Password.", Toast.LENGTH_LONG).show();
                        mProgress.dismiss();
                    }
                }
            });


        }
        else{
            Toast.makeText(this, "Empty Fields", Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserExist() {

        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(user_id)){

                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }else {
                    Toast.makeText(LoginActivity.this, "You need to setup your account", Toast.LENGTH_LONG).show();
                }
                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.loginBtn){
            boolean error = false;

            if(mloginEmail.getText().toString().length() == 0){
                mloginEmail.setError("A valid email is required");
                error = true;}

            if (mloginPassword.getText().toString().length() == 0){
                mloginPassword.setError("Password must be at least 6 characters");
                error = true;}

            if(error == false){
                checkLogin();
            }


        }
    }
}
