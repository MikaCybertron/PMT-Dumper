package platinmods.com.dumper.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import platinmods.com.dumper.R;

public class OptionDialog {

    // Listener class
    public interface SaveListener {

        void onSaveOption(boolean isShowAllProcess, boolean isDumpMaps, boolean isSoFixer, boolean is64Bit, boolean isDumpMetadata);
    }

    // Main Field Code
    private final Context context;

    private final String stringTitle;

    private final SaveListener saveListener;

    public AlertDialog dialog;

    private AlertDialog.Builder alertDialog;

    private TextView dialogTitle;

    private CheckBox isShowAllProcess, isDumpMaps, isSoFixer, isDumpMetadata;

    private Switch is64Bit;


    /**
     * This is a method to make new-instance class of custom dialogs.
     * @param context Require {@link android.content.Context} to setup custom dialog.
     * @param title title for Dialog.
     * @param saveListener a listener for OnSaveOption.
     */
    public OptionDialog(Context context, String title, SaveListener saveListener) {
        this.context = context;
        this.stringTitle = title;
        this.saveListener = saveListener;
        InitViews();
    }

    /**
     * This method to setup or init All Views of custom design dialog from res/layout.
     */
    private void InitViews() {

        View customAlertDialog = LayoutInflater.from(context).inflate(R.layout.custom_dialog_option, null);
        alertDialog = new AlertDialog.Builder(context);

        // set Custom Dialog from layout "custom_dialog"
        alertDialog.setView(customAlertDialog);

        dialogTitle = customAlertDialog.findViewById(R.id.dialog_title);
        Button buttonSave = customAlertDialog.findViewById(R.id.button_positive);
        Button buttonClose = customAlertDialog.findViewById(R.id.button_negative);

        isShowAllProcess = customAlertDialog.findViewById(R.id.isShowAllProcess);
        isDumpMaps = customAlertDialog.findViewById(R.id.isDumpMaps);
        isSoFixer = customAlertDialog.findViewById(R.id.isSoFixer);
        is64Bit = customAlertDialog.findViewById(R.id.is64Bit);
        isDumpMetadata = customAlertDialog.findViewById(R.id.isDumpMetadata);


        isSoFixer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    is64Bit.setEnabled(true);
                    is64Bit.setTextColor(Color.WHITE);
                }
                else
                {
                    is64Bit.setEnabled(false);
                    is64Bit.setTextColor(Color.parseColor("#808080"));
                }
            }
        });


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveListener.onSaveOption(isShowAllProcess.isChecked(), isDumpMaps.isChecked(), isSoFixer.isChecked(), is64Bit.isChecked(), isDumpMetadata.isChecked());
                dialog.cancel();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    /**
     * This method to setup or init dialogs.
     */
    private void InitDialog() {

        dialog = alertDialog.create();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        dialog.getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //int screenWidth = (int)(displayMetrics.widthPixels / 1.5);
        //int screenHeight = (int)(displayMetrics.heightPixels / 2);

        // show dialog with transparent background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // to show dialog
        dialog.show();

        // must make this code below dialog.show() to set width and height of dialog.
        //dialog.getWindow().setLayout(screenWidth, screenHeight);
    }

    /**
     * This method is to Show Dialogs and those arguments is to load previous value of Dump Options.
     */
    public void Show(boolean isShowAllProcess, boolean isDumpMaps, boolean isSoFixer, boolean is64Bit, boolean isDumpMetadata) {

        // Set Title Dialog
        dialogTitle.setText(stringTitle);
        this.isShowAllProcess.setChecked(isShowAllProcess);
        this.isDumpMaps.setChecked(isDumpMaps);
        this.isSoFixer.setChecked(isSoFixer);
        this.is64Bit.setChecked(is64Bit);
        this.isDumpMetadata.setChecked(isDumpMetadata);

        InitDialog();

    }

    /**
     * This method to close dialogs.
     */
    public void close() {
        dialog.cancel();
    }

    /**
     * Sets whether the dialog is cancelable or not. Default is true.
     */
    public void setCancelable(boolean cancelable) {
        alertDialog.setCancelable(cancelable);
        if(dialog != null) {
            dialog.setCancelable(cancelable);
        }
    }
}
