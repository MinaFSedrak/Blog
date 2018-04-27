package mina.egypt.android.blog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class PostActivity extends AppCompatActivity implements  View.OnClickListener{

    private ImageButton mSelectImage;
    private EditText mPostTitle;
    private EditText mPostDesc;
    private Button mSubmitBtn;

    private Uri mImageUri = null;

    private StorageReference mStorage;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUser;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;
    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setTitle("Post");

        mStorage = FirebaseStorage.getInstance().getReference();


        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);

        mPostTitle = (EditText) findViewById(R.id.titleField);
        mPostDesc = (EditText) findViewById(R.id.descField);

        mSubmitBtn = (Button)  findViewById(R.id.submitBtn);

        mProgress = new ProgressDialog(this);

        // onImage select handler go to gallery
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });


        mSubmitBtn.setOnClickListener(this);
    }

    // post new blog
    private void startPosting(){

        mProgress.setMessage("Posting to Blog ...");
        final String title_value = mPostTitle.getText().toString().trim();
        final String desc_value = mPostDesc.getText().toString().trim();

        // validation required fields for posting a blog
        if(!TextUtils.isEmpty(title_value) && !TextUtils.isEmpty(desc_value) && mImageUri != null){

            mProgress.show();
            mProgress.setCancelable(false);
            mProgress.setCanceledOnTouchOutside(false);
            StorageReference filepath = mStorage.child("Blog_Images").child(mImageUri.getLastPathSegment());
            Toast.makeText(this, filepath.toString(), Toast.LENGTH_LONG).show();

            // upload image to firebase storage
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    final DatabaseReference newPost = mDatabase.push(); // add record to database with unique name

                    // add given post info to database with owner uid
                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("title").setValue(title_value);
                            newPost.child("desc").setValue(desc_value);
                            newPost.child("image").setValue(downloadUrl.toString());
                            newPost.child("user_id").setValue(mCurrentUser.getUid());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    mProgress.dismiss();
                                    startActivity(new Intent(PostActivity.this, MainActivity.class));

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), "Post isn't Submitted \n" + databaseError, Toast.LENGTH_LONG).show();
                        }
                    });


                    mProgress.dismiss();
                }
            });
        }else {
            Toast.makeText(this, "Empty Fields", Toast.LENGTH_LONG).show();

        }
    }

    // onSelectedImage result handler
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // gallery request
        if( requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            mImageUri = data.getData();

            // give selected image to CropImageActivity
            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(4,3)
                    .start(this);
        }

        // crop image request
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);


            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mSelectImage.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.submitBtn){
            boolean error = false;

            // validation
            if(mPostTitle.getText().toString().length() == 0){
                mPostTitle.setError("Please enter a title");
                error = true;}

            if (mPostDesc.getText().toString().length() == 0){
                mPostDesc.setError("Please enter a description");
                error = true;}

            if(error == false){
                startPosting();
            }
        }
    }
}
