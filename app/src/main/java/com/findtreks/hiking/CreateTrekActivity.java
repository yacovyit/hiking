package com.findtreks.hiking;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.findtreks.hiking.model.Trek;
import com.findtreks.hiking.util.TrekUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.findtreks.hiking.Globals.COLLECTION_NAME;


public class CreateTrekActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private static final int PICK_IMAGE_REQUEST = 71;
    @BindView(R.id.spinner_category)
    Spinner mCategorySpinner;

    @BindView(R.id.spinner_region)
    Spinner mRegionSpinner;

    @BindView(R.id.edit_trek_details)
    EditText mTrekDetails;

    @BindView(R.id.edit_trek_whatsapp_group)
    EditText mTrekwhatsappGroup;

    @BindView(R.id.button_trek_date)
    Button mShowDatePickerDialogButton;

    @BindView(R.id.button_create)
    Button mCreateButton;

    @BindView(R.id.image_view_trek)
    ImageView mImageViewTrek;

    private DatePickerDialog mDatePickerDialog;
    private DatePicker mDatePicker;
    private UUID uuidTrekImage;
    private boolean imageUploaded;
    //Firebase
    private FirebaseFirestore mFirestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private Uri filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_trek);

        ButterKnife.bind(this);
        mDatePickerDialog = TrekUtil.dateDialogFactory(this, this);
        setEditListener();
        // Initialize Firestore and the main RecyclerView
        initFirestore();

    }
    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Nullable
    private String getSelectedCategory() {
        String selected = (String) mCategorySpinner.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedRegion() {
        String selected = (String) mRegionSpinner.getSelectedItem();
        if (getString(R.string.value_any_region).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    public String getWhatsappGroup(){
        return mTrekwhatsappGroup.getText().toString();
    }
    private Date getSelectedTrekDate() {
        Date trekDate = TrekUtil.getDateFromDatePicker(this.mDatePicker);
        return trekDate;
    }
    private String getTreDetails() {
        String trekDetails = mTrekDetails.getText().toString();
        return trekDetails;
    }
    public Trek getTrek() {
        Trek trek = new Trek();
            //normalize category and region values to english keys
            TrekApplication trekApplication = (TrekApplication)(this.getApplicationContext());
            String category = trekApplication.getCategoriesTranslationMap().get(getSelectedCategory());
            String region = trekApplication.getRegionsTranslationMap().get(getSelectedRegion());

            trek.setCategory(category);
            trek.setCity(region);
            trek.setTrekStartDate(TrekUtil.getStartOfDate(new Date(getSelectedTrekDate().getTime())).getTime());
            trek.setName(getTreDetails());
            trek.setWhatsappGroup(getWhatsappGroup());
            trek.setPhoto(uuidTrekImage.toString());

        return trek;
    }
    private void setEditListener(){
        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mCreateButton.setEnabled(isEnabled());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mCreateButton.setEnabled(isEnabled());
            }

        };

        mCategorySpinner.setOnItemSelectedListener(onItemSelectedListener);
        mRegionSpinner.setOnItemSelectedListener(onItemSelectedListener);
        mTrekDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                mCreateButton.setEnabled(isEnabled());
            }
        });
    }
    private boolean isEnabled(){
        boolean isEnabled =
                mDatePicker != null &&
                mCategorySpinner.getSelectedItemPosition() > 0 &&
                mRegionSpinner.getSelectedItemPosition() > 0 &&
                mTrekDetails.getText().toString().length() > 0 ;
        return isEnabled;
    }
    @OnClick(R.id.image_view_trek)
    public void onImageViewUploadClick(View view){
        chooseImage();
    }
    @OnClick(R.id.button_create)
    public void onTrekCreateClicked(View view)
    {
        Trek trek = getTrek();
        CollectionReference treks = mFirestore.collection(COLLECTION_NAME);
        treks.add(trek);
        this.finish();
    }
    @OnClick(R.id.button_cancel)
    public void onCancelClicked(View view)
    {
        this.finish();
    }
    @OnClick(R.id.button_trek_date)
    public void onTrekDateClicked(View view)
    {
        mDatePickerDialog.show();
    }
    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        this.mDatePicker = datePicker;
        Date date = TrekUtil.getDateFromDatePicker(datePicker);
        String dateString = new SimpleDateFormat("dd/MM/yyyy").format(date).toString();
        this.mShowDatePickerDialogButton.setText(dateString);
        mCreateButton.setEnabled(isEnabled());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                uploadImage(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }
    private void uploadImage(final Bitmap bitmap) {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getResources().getString(R.string.uploading));
            progressDialog.show();
            uuidTrekImage = UUID.randomUUID();
            StorageReference ref = storageRef.child("images/"+ uuidTrekImage.toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageUploaded = true;
                            ImageViewCompat.setImageTintList( mImageViewTrek,null);
                            mImageViewTrek.setImageBitmap(bitmap);
                            progressDialog.dismiss();
                            Toast.makeText(CreateTrekActivity.this, getResources().getString(R.string.uploaded_success), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uuidTrekImage = null;
                            progressDialog.dismiss();
                            Toast.makeText(CreateTrekActivity.this, getResources().getString(R.string.uploaded_failed), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
}
