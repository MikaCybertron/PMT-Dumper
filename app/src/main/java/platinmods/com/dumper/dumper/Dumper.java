package platinmods.com.dumper.dumper;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import platinmods.com.dumper.variable.LogView;
import platinmods.com.dumper.variable.MapInfo;

public class Dumper {

    private Context context;

    private String DEFAULT_DIR;

    private String packageName;

    private String fileName;

    HashMap<String, String> listMapsData;

    private int PID;

    private LogView logView;

    public Dumper(Context context, String packageName, LogView logView) {
        this.packageName = packageName;
        this.context = context;
        this.logView = logView;
    }

    public void dumpFile(String fileName, boolean isFixELF, boolean is64Bit, boolean isDumpMaps) {

        this.listMapsData = new HashMap<String, String>();

        this.fileName = fileName;

        DEFAULT_DIR = Environment.getExternalStorageDirectory().getPath() + "/PlatinmodsDumper/" + packageName;

        File outputDir = new File(DEFAULT_DIR);

        List<Integer> listPID = getPID();

        if(listPID.size() == 0) {

            String message = "Failed to attach process, please make sure the game or apps is running.";

            logView.appendError(message);

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            return;
        }

        List<MapInfo> listMaps = getMaps(listPID);

        List<Long> listAddress = parseMaps(listMaps);

        if(listAddress.size() == 0) {

            String message = "Failed to dump file, because file not found in memory.";

            logView.appendError(message);

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            return;
        }


        if(!outputDir.exists()) {

            outputDir.mkdirs();
        }


        long startAddress = listAddress.get(0);
        long endAddress = listAddress.get(listAddress.size() - 1);
        long sizeMemory = endAddress - startAddress;



        if (startAddress > 1L && endAddress > 1L) {

            File outputFile = new File(outputDir.getAbsolutePath() + "/" + Long.toHexString(startAddress) + "-" + Long.toHexString(endAddress) + "-" + fileName);

            Shell.Result cmd = Shell.cmd("dd if=/proc/" + PID + "/mem of=" + outputFile + " bs=1024 count=" + (sizeMemory / 1024) + " skip=" + (startAddress / 1024)).exec();

            if(cmd.isSuccess()) {

                logView.appendInfo("File Name: " + fileName);
                logView.appendInfo("File Size: " + Formatter.formatFileSize(context, sizeMemory));
                logView.appendInfo("Start Address: " + startAddress);
                logView.appendInfo("End Address: " + endAddress);

                logView.appendInfo("Dumped File: " + outputFile.getAbsolutePath());

                if(isFixELF && fileName.endsWith(".so")) {

                    SoFixer(outputFile, is64Bit, startAddress);
                }

                if(isDumpMaps) {

                    for(String fileMaps : listMapsData.keySet()) {

                        dumpMapsFile(listMapsData.get(fileMaps), fileMaps);
                    }
                }

                Toast.makeText(context, "File " + fileName + " successfully dumped!", Toast.LENGTH_LONG).show();
            }
            else {
                logView.appendError("File " + fileName + " failed to dump!");

                Toast.makeText(context, "File " + fileName + " failed to dump!", Toast.LENGTH_LONG).show();
            }
        }
        logView.appendLine("================================================\n");
    }

    private void dumpMapsFile(String data, String filename) {
        try
        {
            File file = new File(DEFAULT_DIR + "/" + filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();

            if(file.exists()) {
                logView.appendInfo("Dumped Maps File: " + file.getAbsolutePath());
            }
        }
        catch (IOException e) {
            Log.e("Exception", "Write file failed: " + e.toString());
            e.printStackTrace();
        }
    }

    private void SoFixer(File pathDumpFile, boolean is64Bit, long startAddress) {

        if(extractSoFixer()) {

            String fixerPath = context.getApplicationInfo().nativeLibraryDir + "/SoFixer32";
            if(is64Bit) {
                fixerPath = context.getApplicationInfo().nativeLibraryDir + "/SoFixer64";
            }
            SuFile soFixerPath = new SuFile(fixerPath);

            File pathDumpFixed = new File(pathDumpFile.getAbsolutePath().replace(".so", "-fixer.so"));

            StringBuilder commandBuilder = new StringBuilder();

            commandBuilder.append(soFixerPath.getAbsolutePath());
            commandBuilder.append(" -s ");
            commandBuilder.append(pathDumpFile);
            commandBuilder.append(" -o ");
            commandBuilder.append(pathDumpFixed.getAbsolutePath());
            commandBuilder.append(" -m ");
            commandBuilder.append(" 0x" + Long.toHexString(startAddress));


            Shell.Result cmd = Shell.cmd(commandBuilder.toString()).exec();

            if(cmd.isSuccess() && pathDumpFixed.exists()) {

                logView.appendInfo("Fixed Dumped File: " + pathDumpFixed.getAbsolutePath());
            }
            else
            {
                logView.appendError("ELF Fixer failed:");

                getOutputShell(cmd.getOut());

                logView.appendLine();
            }
        }
    }

    public boolean extractSoFixer() {

        try {

            boolean result = false;
            String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;

            String[] libs = context.getAssets().list("SoFixer");

            for (String lib : libs) {

                SuFile outputFile = new SuFile(nativeLibraryDir, lib);

                InputStream inputStream = context.getAssets().open("SoFixer/" + lib);

                OutputStream outputStream = SuFileOutputStream.open(outputFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                if(outputFile.exists()) {

                    Shell.Result cmd = Shell.cmd("chmod 777 " + outputFile.getAbsolutePath()).exec();

                    if(cmd.isSuccess()) {
                        result = true;

                        Log.d("Dumper", lib + " extracted to " + outputFile.getAbsolutePath());
                    }
                }
                else {
                    logView.appendError("Failed to create SoFixer files: " + lib);
                }
            }
            return result;

        } catch (IOException e) {

            logView.appendError("Failed to create SoFixer: " + e.getMessage());

            e.printStackTrace();
        }
        return false;
    }

    private List<Long> parseMaps(List<MapInfo> listMaps) {

        List<Long> result = new ArrayList<>();

        for(MapInfo map : listMaps) {
            if (map.getPath().contains(fileName)) {

                PID = map.getPID();

                result.add(map.getStartAddress());

                result.add(map.getEndAddress());
            }
        }
        return result;
    }

    public List<MapInfo> getMaps(List<Integer> listPid) {

        List<MapInfo> listMaps = new ArrayList<>();

        for(int pid : listPid) {

            String mapsString = "";

            Shell.Result cmd = Shell.cmd("cat /proc/" + pid + "/maps").exec();
            if(cmd.isSuccess()) {

                List<String> output = cmd.getOut();

                for (int i = 0; i < output.size(); i++) {

                    String lines = output.get(i);

                    mapsString += lines.replaceAll("\\s+", " ") + "\n";

                    listMaps.add(new MapInfo(pid, lines));
                }
            }

            listMapsData.put("Maps-" + pid + ".txt", mapsString);

        }

        return listMaps;
    }

    private List<Integer> getPID() {

        //List<String> output = new ArrayList<>();

        List<Integer> listPID = new ArrayList<>();

        HashMap<Integer, Integer> CreateListProcess = new HashMap<Integer, Integer>();

        Shell.Result cmd = Shell.cmd("ps -t").exec();
        if(cmd.isSuccess())
        {
            List<String> output = cmd.getOut();

            for (int i = 0; i < output.size(); i++) {

                String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");

                for (int j = 0; j < results.length; j++) {

                    String getPackageName = results[results.length - 1];

                    if(getPackageName.contains(".") && getPackageName.contains(packageName))
                    {
                        int pid = Integer.parseInt(results[1]);

                        CreateListProcess.put(pid, pid);
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

                    String getPackageName = results[results.length - 1];

                    if(getPackageName.contains(".") && getPackageName.contains(packageName))
                    {
                        int pid = Integer.parseInt(results[1]);

                        CreateListProcess.put(pid, pid);
                    }
                }
            }
        }

        for (Integer pid : CreateListProcess.keySet()) {
            listPID.add(pid);
        }

        return listPID;
    }

    private void getOutputShell(List<String> outputData) {
        if(outputData.size() > 0) {
            for(String lines : outputData) {
                logView.appendLine(lines);
            }
        }
    }

}
