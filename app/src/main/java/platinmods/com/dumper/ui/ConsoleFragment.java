package platinmods.com.dumper.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import platinmods.com.dumper.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ConsoleFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_console, container, false);

        setupLayout(view);

        return view;
    }

    private void setupLayout(View view) {

        TextView textView = view.findViewById(R.id.textview_log);

        getParentFragmentManager().setFragmentResultListener("dataDump", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String data = result.getString("logView");

                textView.setText(data);
            }
        });
    }
}