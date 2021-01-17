package com.example.liang;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogFragment extends androidx.fragment.app.DialogFragment {

    private static final String TAG = "DialogFragment";

    private EditText enterName;
    private EditText enterMatric;
    private TextView dialogUpdate;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment,container,false);

        enterName = view.findViewById(R.id.EnterName);
        enterMatric = view.findViewById(R.id.EnterPassword);
        dialogUpdate = view.findViewById(R.id.DialogUpdate);

        dialogUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Update Pressed");
                String InputName = enterName.getText().toString();
                String InputMatric = enterMatric.getText().toString();

                if(!InputName.equals("")&&!InputMatric.equals("")){
                    ((MainActivity)getActivity()).preferenceEdit(InputName,InputMatric);
                    Log.d(TAG, "onClick: Info Updated");
                }
                getDialog().dismiss();
            }
        });

        return view;
    }


}
