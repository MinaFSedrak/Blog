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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;

    private Button mRegisterBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);

        mNameField = (EditText) findViewById(R.id.nameField);
        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);

        mRegisterBtn.setOnClickListener(this);

    }

    private void startRegister() {

        final String name = mNameField.getText().toString().trim();
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                mProgress.setMessage("Signing Up...");
                mProgress.show();
                mProgress.setCancelable(false);
                mProgress.setCanceledOnTouchOutside(false);

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference new_user = mDatabase.child(user_id);
                            new_user.child("name").setValue(name);

                            mProgress.dismiss();

                            Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                        }else{
                            Toast.makeText(RegisterActivity.this, " Error! \n A valid email is required \n Password must be at least 6 characters ", Toast.LENGTH_LONG).show();
                            mProgress.dismiss();

                        }
                    }
                });

        }else {
            Toast.makeText(this, "Empty Fields", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.registerBtn){
            boolean error = false;

            if(mNameField.getText().toString().length() == 0){
                mNameField.setError("Please enter a name");
                error = true;}

            if (mEmailField.getText().toString().length() == 0 ){
                mEmailField.setError("A valid email is required");
                error = true;}

            if (mPasswordField.getText().toString().length() == 0 ){
                mPasswordField.setError("Password must be at least 6 characters");
                error = true;}

            if(error == false){
                startRegister();
            }


        }
    }
}
