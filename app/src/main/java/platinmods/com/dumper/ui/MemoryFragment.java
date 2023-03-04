package platinmods.com.dumper.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import platinmods.com.dumper.BuildConfig;
import platinmods.com.dumper.Core.Dumper;
import platinmods.com.dumper.R;
import platinmods.com.dumper.variable.LogView;
import platinmods.com.dumper.variable.ProcessInfo;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MemoryFragment extends Fragment implements OptionDialog.SaveListener {

    // Main Field Code
    MemoryFragment Instance;

    TextInputEditText process_edit_text, fileName_edit_text, dump_path_edit_text;

    Button button_selectProcess, button_dump, button_option;

    TextView textStatus;

    SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;

    // Field code for Permission Info
    StringBuilder defaultMessage;

    // Field code for Dump Option
    boolean isShowAllProcess, isDumpMaps, isSoFixer, is64Bit, isDumpMetadata;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_memory, container, false);

        InitPreferences();

        setupLayout(view);

        loadDefaultMessage();

        InitOption();

        sendMessage(defaultMessage.toString() + getStatusOption());

        Instance = this;

        return view;
    }

    /**
     * A method for Setup or Init Layout of Fragments
     * @param view A {@link android.view.View} from Inflate layout of this fragment.
     */
    private void setupLayout(View view) {

        process_edit_text = view.findViewById(R.id.process_edit_text);
        fileName_edit_text = view.findViewById(R.id.fileName_edit_text);
        dump_path_edit_text = view.findViewById(R.id.dump_path_edit_text);

        button_selectProcess = view.findViewById(R.id.button_selectProcess);
        button_dump = view.findViewById(R.id.button_dump);
        button_option = view.findViewById(R.id.button_option);
        textStatus = view.findViewById(R.id.textStatus);

        button_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OptionDialog optionDialog = new OptionDialog(getContext(), "Dump Option", MemoryFragment.this);
                optionDialog.setCancelable(false);
                optionDialog.Show(isShowAllProcess, isDumpMaps, isSoFixer, is64Bit, isDumpMetadata);
            }
        });

        button_selectProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(AllPermissionGranted())
                {
                    showListProcess();
                }
            }
        });

        button_dump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Save Preferences for Dump Path
                writeValue("DumpPath", getOutputDirectory());

                loadDefaultMessage();

                if(AllPermissionGranted())
                {
                    StartDump();
                }
            }
        });



        process_edit_text.setText("");

        // Load Preferences for Dump Path
        String outputDirectory = loadValueString("DumpPath", Environment.getExternalStorageDirectory().getPath());

        dump_path_edit_text.setText(outputDirectory);
    }

    /**
     * A method to Start Dumping and the code will run in the background.
     */
    private void StartDump() {

        String processName = process_edit_text.getText().toString();

        if(!processName.isEmpty())
        {
            final String outputDirectory = getOutputDirectory();
            if(outputDirectory.isEmpty()) {
                Toast.makeText(getContext(), "Please set Output Directory first!", Toast.LENGTH_LONG).show();
                return;
            }


            SetStatus("Dumping Started...");
            ControlUI(false);



            // Run Code in background
            new Thread(new Runnable() {
                @Override
                public void run() {

                    // Create a new Looper for the background thread, otherwise this app will crash.
                    Looper.prepare(); // (Needed)

                    // file Name
                    String fileName = fileName_edit_text.getText().toString();

                    if(fileName.isEmpty())
                    {
                        fileName = "libil2cpp.so";
                    }

                    LogView logView = new LogView();

                    logView.appendLine("================================================\n");

                    Dumper dumper = new Dumper(Instance, getContext(), logView);

                    dumper.startDump(processName, fileName, outputDirectory, isDumpMaps, isSoFixer, is64Bit, isDumpMetadata);

                    // Update the UI
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update the UI here
                            sendMessage(defaultMessage + logView.toString());

                            SetStatus("Dumping Done...");

                            ControlUI(true);
                        }
                    });

                    // Start the Looper loop
                    Looper.loop(); // (Needed)
                }
            }).start();
        }
        else
        {
            Toast.makeText(getContext(), "Please select apps first!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * A method to get Output Directory Path
     * @return
     */
    private String getOutputDirectory() {

        return dump_path_edit_text.getText().toString().replaceAll("/+$", "");
    }

    /**
     * A method for Enable State for UI Views to be used later while dumping.
     * @param isEnable True if this ControlUI is enabled, false otherwise.
     */
    private void ControlUI(boolean isEnable) {
        process_edit_text.setEnabled(isEnable);
        fileName_edit_text.setEnabled(isEnable);
        dump_path_edit_text.setEnabled(isEnable);
        button_selectProcess.setEnabled(isEnable);
        button_option.setEnabled(isEnable);
        button_dump.setEnabled(isEnable);
    }

    /**
     * A simple SetStatus for TextView of textStatus to tell you if the dumping is done or not.
     */
    private void SetStatus(String status) {
        textStatus.setText("Status: " + status);
    }

    /**
     * A method to Load Default Message.
     */
    private void loadDefaultMessage() {

        String rootPermission = Boolean.TRUE.equals(Shell.isAppGrantedRoot()) ? "Enabled" : "Disabled";

        String StoragePermission = StoragePermission() ? "Enabled" : "Disabled";

        defaultMessage = new StringBuilder();

        defaultMessage.append("Root Permission: ").append(rootPermission).append("\n\n");

        defaultMessage.append("Storage Permission: ").append(StoragePermission).append("\n\n");

        defaultMessage.append("Output Directory: " + getOutputDirectory()).append("\n\n");
    }

    /**
     * A method to Send Message or Data to another Fragment.
     * @param data A string data to be send.
     */
    private void sendMessage(String data) {


        Bundle result = new Bundle();

        result.putString("logView", data);

        getParentFragmentManager().setFragmentResult("dataDump", result);
    }

    /**
     * A method to Send Message at runtime for Log View in Console
     */
    public void sendRuntimeMessage(LogView logView) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update the UI here
                Bundle result = new Bundle();

                result.putString("logView", defaultMessage + logView.toString());

                getParentFragmentManager().setFragmentResult("dataDump", result);
            }
        });
    }

    /**
     * A method to Set Status at runtime
     */
    public void setRuntimeStatus(String title, String status) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update the UI here
                textStatus.setText("Status: " + title + "\n" + status);
            }
        });
    }

    /**
     * A method for default Dump Option.
     */
    private void InitOption() {
        isShowAllProcess = false;
        isDumpMaps = false;
        isSoFixer = false;
        is64Bit = false;
        isDumpMetadata = false;
    }

    /**
     * A method to get change info on Dump Option.
     */
    private String getStatusOption() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isShowAllProcess: " + isShowAllProcess).append("\n\n");
        stringBuilder.append("isDumpMaps: " + isDumpMaps).append("\n\n");
        stringBuilder.append("isSoFixer: " + isSoFixer).append("\n\n");
        stringBuilder.append("is64Bit: " + is64Bit).append("\n\n");
        stringBuilder.append("isDumpMetadata: " + isDumpMetadata).append("\n\n");

        return stringBuilder.toString();
    }

    /**
     * A method to Display a Alert Dialog containing All Listed Processes to be selected later.
     */
    private void showListProcess() {

        ArrayList<ProcessInfo> list = getAllProcess();

        ArrayList<String> appNames = new ArrayList<>();

        for (ProcessInfo processInfo : list)
        {
            String processName = processInfo.PackageName;

            if(processInfo.AppName.contains("App Name not found"))
            {
                appNames.add(processInfo.PID + " - " + processInfo.PackageName);
            }
            else
            {
                if (processName.contains(":"))
                {
                    appNames.add(processInfo.PID + " - " + processInfo.AppName + " (" + processName.substring(processName.indexOf(":") + 1) + ")");
                }
                else
                {
                    appNames.add(processInfo.PID + " - " + processInfo.AppName + " (" + processInfo.PackageName + ")");
                }
            }
        }

        // Create a dialog to show the list of processes with Single Choice Items.
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select process")
                .setSingleChoiceItems(appNames.toArray(new String[0]), -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        process_edit_text.setText(list.get(which).PackageName);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * A method to get List of All Processes where it has been minimized.
     */
    private ArrayList<ProcessInfo> getAllProcess() {

        ArrayList<ProcessInfo> processList = new ArrayList<>();

        HashMap<Integer, ProcessInfo> createdListProcess = CreateListProcess();

        HashMap<String, ProcessInfo> minimizeList = new HashMap<>();

        // Add Created List to Minimize List
        for (Integer key : createdListProcess.keySet() ) {

            minimizeList.put(createdListProcess.get(key).PackageName, createdListProcess.get(key));
        }

        // Add Minimize List to Main Process List.
        for (String key : minimizeList.keySet() ) {

            processList.add(minimizeList.get(key));
        }
        return processList;
    }

    /**
     * A method to Create List Process of Running Applications.
     */
    private HashMap<Integer, ProcessInfo> CreateListProcess() {

        List<String> listApps;

        if(isShowAllProcess)
        {
            listApps = new ArrayList<>();
        }
        else
        {
            listApps = getInstalledApps();
        }


        HashMap<Integer, ProcessInfo> MakeListProcess = new HashMap<>();

        Shell.Result cmd = Shell.cmd("ps -t").exec();

        if(cmd.isSuccess())
        {
            List<String> output = cmd.getOut();
            for (int i = 0; i < output.size(); i++) {
                String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");
                for (int j = 0; j < results.length; j++) {

                    String processName = results[results.length - 1];
                    if(processName.contains(".") && !processName.contains(BuildConfig.APPLICATION_ID))
                    {
                        int pid = Integer.parseInt(results[1]);

                        if (isShowAllProcess)
                        {
                            // Device is running Android 11 or higher
                            MakeListProcess.put(pid, new ProcessInfo(getContext(), processName, pid));
                        }
                        else
                        {
                            if(isInstalledApps(listApps, processName))
                            {
                                MakeListProcess.put(pid, new ProcessInfo(getContext(), processName, pid));
                            }
                        }
                    }
                }
            }
        }

        cmd = Shell.cmd("ps").exec();
        if(cmd.isSuccess())
        {
            List<String> output = cmd.getOut();
            for (int i = 0; i < output.size(); i++) {
                String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");
                for (int j = 0; j < results.length; j++) {

                    String processName = results[results.length - 1];
                    if(processName.contains(".") && !processName.contains(BuildConfig.APPLICATION_ID))
                    {
                        int pid = Integer.parseInt(results[1]);

                        if (isShowAllProcess)
                        {
                            // Device is running Android 11 or higher
                            MakeListProcess.put(pid, new ProcessInfo(getContext(), processName, pid));
                        }
                        else
                        {
                            if(isInstalledApps(listApps, processName))
                            {
                                MakeListProcess.put(pid, new ProcessInfo(getContext(), processName, pid));
                            }
                        }
                    }
                }
            }
        }

        return MakeListProcess;
    }

    /**
     * A method to check if the Process is Installed Applications or Not by its Process Name.
     * @param listApps A list of All Installed Applications.
     * @param processName A Process Name from Running Applications.
     */
    private boolean isInstalledApps(List<String> listApps, String processName) {
        if(listApps.size() > 0) {
            for(String packageName: listApps) {
                if(packageName.contains(processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * A method to get All Installed Applications.
     */
    private List<String> getInstalledApps() {
        List<ApplicationInfo> packages = getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> ret = new ArrayList<>();

        for (ApplicationInfo s : packages) {
            //Filter system apps and this app
            if (s.sourceDir.startsWith("/data") && !s.sourceDir.contains(BuildConfig.APPLICATION_ID) ) {
                ret.add(s.packageName);
            }
        }
        return ret;
    }

    /**
     * A method to check if All Permission is Granted or Not, but it may not work for Storage Permissions on Android 12 and higher.
     */
    private boolean AllPermissionGranted() {
        if(!StoragePermission()) {
            Toast.makeText(getContext(), "Please Grant Storage Permission first!", Toast.LENGTH_SHORT).show();
        }

        if(!Shell.isAppGrantedRoot()) {
            Toast.makeText(getContext(), "Please Grant Root Permission first!", Toast.LENGTH_SHORT).show();
        }
        if(StoragePermission() && Shell.isAppGrantedRoot()) {
            return true;
        }
        return false;
    }

    /**
     * A method to check Storage Permissions, but it may not work on Android 12 and higher.
     */
    private boolean StoragePermission() {

        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

            if(result == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }
            else
            {
                result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE);

                if(result == PackageManager.PERMISSION_GRANTED)
                {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * This is a method to make new-instance of custom SharedPreferences.
     */
    private void InitPreferences() {
        sharedPreferences = getContext().getSharedPreferences("PMT-Dumper", Context.MODE_PRIVATE);

        // Get an instance of SharedPreferences.Editor
        editor = sharedPreferences.edit();
    }

    /**
     * A method to load value in specific key.
     * @param key Require key to load value in specific key.
     * @param defaultValue the default value that will be used if the key is not found in the SharedPreferences.
     * @return If the key is not found then load the value with default value you set.
     */
    public String loadValueString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * A method to load value in specific key.
     * @param key Require key to load value in specific key.
     * @param defaultValue the default value that will be used if the key is not found in the SharedPreferences.
     * @return If the key is not found then load the value with default value you set.
     */
    public boolean loadValueBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * A method to write new value or edit value.
     * @param key is used to store new value or change value.
     * @param value A value to be stored in the key.
     */
    public void writeValue(String key, String value) {

        // Save the value
        editor.putString(key, value);

        // Commit the changes
        editor.apply();
    }

    /**
     * A method to write new value or edit value.
     * @param key is used to store new value or change value.
     * @param value A value to be stored in the key.
     */
    public void writeValue(String key, boolean value) {

        // Save the value
        editor.putBoolean(key, value);

        // Commit the changes
        editor.apply();
    }


    /**
     * A Implemented Method for onSaveOption from OptionDialog for Dump Options.
     * @param isShowAllProcess This option to show All Running Process.
     * @param isDumpMaps This option is for Dump Maps File from Selected Process.
     * @param isSoFixer This option is for Fixing Dumped SO Files.
     * @param is64Bit This option to use SoFixer 32 Bit or 64 Bit.
     * @param isDumpMetadata This option to Dump File global-metadata.dat
     */
    @Override
    public void onSaveOption(boolean isShowAllProcess, boolean isDumpMaps, boolean isSoFixer, boolean is64Bit, boolean isDumpMetadata) {
        this.isShowAllProcess = isShowAllProcess;
        this.isDumpMaps = isDumpMaps;
        this.isSoFixer = isSoFixer;
        this.is64Bit = is64Bit;
        this.isDumpMetadata = isDumpMetadata;
        loadDefaultMessage();
        sendMessage(defaultMessage.toString() + getStatusOption());
    }
}