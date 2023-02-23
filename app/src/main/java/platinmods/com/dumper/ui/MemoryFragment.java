package platinmods.com.dumper.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import platinmods.com.dumper.BuildConfig;
import platinmods.com.dumper.R;
import platinmods.com.dumper.dumper.Dumper;
import platinmods.com.dumper.variable.LogView;
import platinmods.com.dumper.variable.ProcessInfo;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MemoryFragment extends Fragment {

    TextInputEditText process_edit_text, fileName_edit_text;
    Button button_selectApp, button_dump;

    CheckBox elfFixer, dumpMetadata, dumpMaps;

    Switch architectures64Bit;


    StringBuilder permissionMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_memory, container, false);

        setupLayout(view);

        loadFirstMessage();

        sendMessage(permissionMessage.toString());

        return view;
    }

    private void loadFirstMessage() {

        String rootPermission = Shell.isAppGrantedRoot() == true ? "Enabled" : "Disabled";

        String StoragePermission = StoragePermission() == true ? "Enabled" : "Disabled";

        permissionMessage = new StringBuilder();

        permissionMessage.append("Root Permission: " + rootPermission + "\n\n");

        permissionMessage.append("Storage Permission: " + StoragePermission + "\n\n");
    }

    private void setupLayout(View view) {

        process_edit_text = view.findViewById(R.id.process_edit_text);
        fileName_edit_text = view.findViewById(R.id.fileName_edit_text);

        button_selectApp = view.findViewById(R.id.button_selectApp);
        button_dump = view.findViewById(R.id.button_dump);


        elfFixer = view.findViewById(R.id.elfFixer);
        dumpMetadata = view.findViewById(R.id.dumpMetadata);
        dumpMaps = view.findViewById(R.id.dumpMaps);
        architectures64Bit = view.findViewById(R.id.architectures64Bit);

        process_edit_text.setText("");

        button_selectApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(AllPermissionGranted()) {
                    showListProcess();
                }
            }
        });

        button_dump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(AllPermissionGranted()) {

                    String processName = process_edit_text.getText().toString();

                    if(!processName.isEmpty()) {

                        String fileName = fileName_edit_text.getText().toString();

                        if(fileName.isEmpty()) {
                            fileName = "libil2cpp.so";
                        }

                        LogView logView = new LogView();

                        Dumper dumper = new Dumper(getContext(), processName, logView);

                        dumper.dumpFile(fileName, elfFixer.isChecked(), architectures64Bit.isChecked(), dumpMaps.isChecked());

                        if(dumpMetadata.isChecked()) {
                            dumper.dumpFile("global-metadata.dat", false, false, false);
                        }

                        sendMessage(permissionMessage + logView.toString());
                    }
                    else {
                        Toast.makeText(getContext(), "Please select apps first!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean AllPermissionGranted() {
        boolean result = false;
        if(StoragePermission()) {
            result = true;
        }
        else {
            Toast.makeText(getContext(), "Please Grant Storage Permission first!", Toast.LENGTH_SHORT).show();
            result = false;
        }

        if(Shell.isAppGrantedRoot()) {
            result = true;
        }
        else {
            Toast.makeText(getContext(), "Please Grant Root Permission first!", Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }

    private void sendMessage(String data) {
        Bundle result = new Bundle();

        result.putString("logView", data);

        getParentFragmentManager().setFragmentResult("dataDump", result);
    }

    private void showListProcess() {

        List<String> listApps = getInstalledApps();

        ArrayList<ProcessInfo> list = getAllProcess(listApps);

        ArrayList<String> appNames = new ArrayList<>();
        for (ProcessInfo processInfo : list) {

            String processName = processInfo.PackageName;

            if(processInfo.AppName.contains("App Name not found")) {
                appNames.add(processInfo.PID + " - " + processInfo.PackageName);
            }
            else {
                if (processName.contains(":")) {
                    appNames.add(processInfo.PID + " - " + processInfo.AppName + " (" + processName.substring(processName.indexOf(":") + 1) + ")");
                } else {
                    appNames.add(processInfo.PID + " - " + processInfo.AppName + " (" + processInfo.PackageName + ")");
                }
            }
        }

        // Create a dialog to show the list of processes
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


    private ArrayList<ProcessInfo> getAllProcess(List<String> listApps) {

        HashMap<Integer, ProcessInfo> MakeListProcess = CreateListProcess(listApps);
        ArrayList<ProcessInfo> processList = new ArrayList<>();
        processList.clear();


        for (Integer key : MakeListProcess.keySet() ) {

            processList.add(MakeListProcess.get(key));
        }

        return processList;
    }

    private HashMap<Integer, ProcessInfo> CreateListProcess(List<String> listApps) {

        HashMap<Integer, ProcessInfo> MakeListProcess = new HashMap<Integer, ProcessInfo>();


        List<String> output;

        Shell.Result cmd = Shell.cmd("ps -t").exec();
        if(cmd.isSuccess())
        {
            output = cmd.getOut();
            for (int i = 0; i < output.size(); i++) {
                String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");
                for (int j = 0; j < results.length; j++) {

                    String processName = results[results.length - 1];
                    if(processName.contains(".") && findInstalledApps(listApps, processName))
                    {
                        int pid = Integer.parseInt(results[1]);
                        MakeListProcess.put(pid, new ProcessInfo(getContext(), processName, pid));
                    }
                }
            }
        }

        cmd = Shell.cmd("ps").exec();
        if(cmd.isSuccess())
        {
            output = cmd.getOut();
            for (int i = 0; i < output.size(); i++) {
                String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");
                for (int j = 0; j < results.length; j++) {

                    String processName = results[results.length - 1];
                    if(processName.contains(".") && findInstalledApps(listApps, processName))
                    {
                        int pid = Integer.parseInt(results[1]);
                        MakeListProcess.put(pid, new ProcessInfo(getContext(), processName, pid));
                    }
                }

            }
        }

        return MakeListProcess;
    }

    private boolean findInstalledApps(List<String> listApps, String processName) {
        if(listApps.size() > 0) {
            for(String packageName: listApps) {
                if(packageName.contains(processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getInstalledApps() {
        List<ApplicationInfo> packages = getContext().getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> ret = new ArrayList<String>();

        for (ApplicationInfo s : packages) {
            //Filter system apps and this app
            if (s.sourceDir.startsWith("/data") && !s.sourceDir.contains(BuildConfig.APPLICATION_ID) ) {
                ret.add(s.packageName);
            }
        }
        return ret;
    }

    private boolean StoragePermission() {
        int result= ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }
}