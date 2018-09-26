package com.findtreks.hiking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.findtreks.hiking.model.Trek;
import com.findtreks.hiking.util.TrekUtil;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.findtreks.hiking.Globals.COLLECTION_NAME;


public class CreateTrekActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    @BindView(R.id.spinner_category)
    Spinner mCategorySpinner;

    @BindView(R.id.spinner_region)
    Spinner mRegionSpinner;

    @BindView(R.id.edit_trek_details)
    EditText mTrekDetails;

    @BindView(R.id.button_trek_date)
    Button mShowDatePickerDialogButton;

    @BindView(R.id.button_create)
    Button mCreateButton;
    private DatePickerDialog mDatePickerDialog;
    private DatePicker mDatePicker;
    private FirebaseFirestore mFirestore;
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
            trek.setTrekStartDate(getSelectedTrekDate().getTime());
            trek.setName(getTreDetails());

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
        boolean isEnabled = mDatePicker != null &&
                mCategorySpinner.getSelectedItemPosition() > 0 &&
                mRegionSpinner.getSelectedItemPosition() > 0 &&
                mTrekDetails.getText().toString().length() > 0 ;
        return isEnabled;
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
}
