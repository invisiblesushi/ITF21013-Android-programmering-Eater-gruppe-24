package no.hiof.danielch.eater.ui.dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import no.hiof.danielch.eater.R;

/**
 * https://www.youtube.com/watch?v=LUV_djRHSEY
 */
public class FilterDialogFragment extends DialogFragment {

    private static final String TAG = "FilterDialogFragment";

    private double lat;
    private double lng;


    public interface OnInputSelected{
        void sendInput(String keyword, String location, String radius);
    }

    public OnInputSelected mOnInputSelected;

    private Button button_filter_cancel;
    private Button button_filter_apply;
    private SeekBar seekBarRadius;
    private TextView textViewRadius;
    private EditText editTextKeyword;




    public FilterDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_filter, container, false);

        //Current location
        lat = getArguments().getDouble("lat");
        lng = getArguments().getDouble("lng");

        button_filter_cancel = view.findViewById(R.id.button_filter_cancel);
        button_filter_apply = view.findViewById(R.id.button_filter_apply);
        seekBarRadius = view.findViewById(R.id.seekBarRadius);
        textViewRadius = view.findViewById(R.id.textViewRadius);
        editTextKeyword = view.findViewById(R.id.editTextKeyword);


        textViewRadius.setText((String.valueOf(seekBarRadius.getProgress())) + "m");

        setSeekbarChangeListner();
        setBtnClickListner();

        return view;
    }




    private void setBtnClickListner() {
        button_filter_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDialog().dismiss();
            }
        });

        button_filter_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String radius = String.valueOf(seekBarRadius.getProgress());

                mOnInputSelected.sendInput(editTextKeyword.getText().toString(), "", radius);

                getDialog().dismiss();
            }
        });
    }

    //Gets radius from seekbar value
    private void setSeekbarChangeListner() {
        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewRadius.setText((String.valueOf(seekBarRadius.getProgress())) + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mOnInputSelected = (OnInputSelected) getTargetFragment();
        }
        catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException : " + e.getMessage() );
        }

    }
}
