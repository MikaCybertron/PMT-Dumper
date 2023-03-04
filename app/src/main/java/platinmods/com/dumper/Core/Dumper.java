package platinmods.com.dumper.Core;

import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;
import com.topjohnwu.superuser.io.SuFileOutputStream;
import com.topjohnwu.superuser.io.SuRandomAccessFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import platinmods.com.dumper.ui.MemoryFragment;
import platinmods.com.dumper.variable.LogView;
import platinmods.com.dumper.variable.MapInfo;
import platinmods.com.dumper.variable.MemoryInfo;

public class Dumper {

    private final MemoryFragment memoryFragment;

    private final Context context;

    private final LogView logView;

    private HashMap<String, String> listMapsData;

    private HashMap<Integer, List<MemoryInfo>> listBinData;

    public Dumper(MemoryFragment memoryFragment, Context context, LogView logView) {
        this.memoryFragment = memoryFragment;
        this.context = context;
        this.logView = logView;
    }

    private void sendMessage() {
        memoryFragment.sendRuntimeMessage(logView);
    }

    private void setStatus(String title, String status) {
        memoryFragment.setRuntimeStatus(title, status);
    }

    /**
     * A method to Start Dumping Files from Memory.
     * @param processName A selected process.
     * @param fileName A file name to dump from memory.
     * @param outputDirectory A output Directory for Dump File
     * @param isDumpMaps True if you want Dump Maps file.
     * @param isSoFixer True if you Fixed the Dumped ELF Files.
     * @param is64Bit True if you want use 64 Bit and False if you want use 32 Bit for SoFixer.
     * @param isDumpMetadata True if you want to dump global-metadata.dat.
     */
    public void startDump(String processName, String fileName, String outputDirectory, boolean isDumpMaps, boolean isSoFixer, boolean is64Bit, boolean isDumpMetadata) {

        this.listMapsData = new HashMap<>();
        this.listBinData = new HashMap<>();

        List<Integer> listPID = getAllPIDProcess(processName);

        if(listPID.size() == 0) {

            String message = "Failed to attach process, please make sure the game or apps is running.";

            logView.appendError(message);

            sendMessage();

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            return;
        }

        outputDirectory = outputDirectory + "/PlatinmodsDumper/" + processName;

        // Create New Directory if Output Directory not exists.
        CreateDirectory(outputDirectory);

        // Sorting All list Maps
        List<MapInfo> listMaps = getAllMaps(processName, fileName);

        // Dump Maps file
        if(isDumpMaps) {

            for(String fileMaps : listMapsData.keySet()) {

                dumpMapFile(listMapsData.get(fileMaps), fileMaps, outputDirectory);
            }
            logView.appendLine("================================================\n");
            sendMessage();
        }

        boolean isDumpFileSuccess = dumpFile(fileName, outputDirectory, isSoFixer, is64Bit, listMaps);

        boolean isDumpMetadataSuccess = false;

        if(isDumpMetadata)
        {
            if(dumpFile("global-metadata.dat", outputDirectory, isSoFixer, is64Bit, listMaps))
            {
                isDumpMetadataSuccess = true;
            }
        }

        if(isDumpFileSuccess) {
            Toast.makeText(context, "File " + fileName + " has been dumped from memory!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Could not find " + fileName + " in memory!", Toast.LENGTH_LONG).show();
        }

        if(isDumpMetadata) {
            if(isDumpMetadataSuccess) {
                Toast.makeText(context, "File global-metadata.dat has been dumped from memory!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Could not find global-metadata.dat in Memory!", Toast.LENGTH_LONG).show();
            }
        }
        sendMessage();
    }

    /**
     * A method to dump file by finding File Name from maps.
     * @param fileName A file name to dump from memory.
     * @param outputDirectory A path for Output Directory.
     * @param isSoFixer True if you Fixed the Dumped ELF Files.
     * @param is64Bit True if you want use 64 Bit and False if you want use 32 Bit for SoFixer.
     * @param listMaps A list of maps info.
     */
    private boolean dumpFile(String fileName, String outputDirectory, boolean isSoFixer, boolean is64Bit, List<MapInfo> listMaps) {

        List<MemoryInfo> memInfo = parseMap(fileName, listMaps);

        if(memInfo.size() == 0) {
            return false;
        }

        // Get Address Dump Files
        int PID = memInfo.get(0).PID;
        long startAddress = memInfo.get(0).startAddress;
        long endAddress = memInfo.get(memInfo.size() - 1).endAddress;
        long sizeMemory = endAddress - startAddress;

        String memoryAddress = Long.toHexString(startAddress) + "-" + Long.toHexString(endAddress);

        if (startAddress > 1L && endAddress > 1L) {

            // Output Dump File
            String outputFile = outputDirectory + "/" + memoryAddress + "-" + fileName;

            StringBuilder outputShell = new StringBuilder();
            if(dumpMemory(outputFile, PID, startAddress, sizeMemory, false, outputShell))
            {
                logView.appendInfo("File Name: " + fileName);
                logView.appendInfo("File Size: " + Formatter.formatFileSize(context, sizeMemory));
                logView.appendInfo("Start Address: " + Long.toHexString(startAddress));
                logView.appendInfo("End Address: " + Long.toHexString(endAddress));
                logView.appendInfo("Output File: " + outputFile);

                if(isSoFixer && fileName.endsWith(".so")) {

                    SoFixer(outputFile, is64Bit, startAddress);
                }

                logView.appendLine("================================================\n");
                sendMessage();
                return true;
            }
            else
            {
                logView.appendError("File " + fileName + " failed to dump!");
                logView.appendLine("Message Error:");
                logView.appendLine(outputShell.toString());
                logView.appendLine();
                logView.appendError("Could not find " + fileName + " in Memory.");
                logView.appendLine("================================================\n");
                sendMessage();
            }
        }
        return false;
    }

    /**
     * A method to dump file from memory.
     * @param outputFile A path for Output Dumped File.
     * @param pid A PID Process from Selected Process.
     * @param startAddress A Start Address from memory to dump file.
     * @param sizeMemory A size of dumped files from memory.
     * @param isCHMOD True to give permission access, False to not give permission access.
     * @param outputShell True to print output shell LogView.
     */
    private boolean dumpMemory(String outputFile, int pid, long startAddress, long sizeMemory, boolean isCHMOD, StringBuilder outputShell) {

        Shell.Result cmd = Shell.cmd("dd if=/proc/" + pid + "/mem of=" + outputFile + " bs=1024 count=" + (sizeMemory / 1024) + " skip=" + (startAddress / 1024)).exec();

        if(!cmd.isSuccess()) {

            if(outputShell != null)
            {
                for(String log : cmd.getOut())
                {
                    outputShell.append(log).append("\n");
                }
            }

            return false;
        }

        if(isCHMOD)
        {
            cmd = Shell.cmd("chmod 777 " + outputFile).exec();
            if(outputShell != null)
            {
                for(String log : cmd.getOut())
                {
                    outputShell.append(log).append("\n");
                }
            }
        }
        return true;
    }

    /**
     * A method to Dump Maps File.
     * @param data A string Data of Maps Info
     * @param filename A file name of dumped maps file.
     * @param outputDirectory A Output Directory for dumped maps file.
     */
    private void dumpMapFile(String data, String filename, String outputDirectory) {
        try
        {
            SuFile file = new SuFile(outputDirectory + "/" + filename);

            // File output streams
            OutputStream fileOutputStream = SuFileOutputStream.open(file);

            // Create output streams
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(data.getBytes());
            bufferedOutputStream.close();

            if(file.exists()) {
                logView.appendInfo("Dumped Maps File: " + file.getAbsolutePath());
                logView.appendLine();
            }
            else {
                logView.appendInfo("Dumped Maps File failed.");
            }

        }
        catch (IOException e) {
            Log.e("Exception", "Crash while dump maps file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * A method to run SoFixer to fix dumped ELF files.
     * @param pathDumpFile path of dumped file.
     * @param is64Bit True if you want use 64 Bit and False if you want use 32 Bit for SoFixer.
     * @param startAddress A Start Address of dumped file.
     */
    private void SoFixer(String pathDumpFile, boolean is64Bit, long startAddress) {

        if(extractSoFixer())
        {
            String fixerPath = context.getApplicationInfo().nativeLibraryDir + "/SoFixer32";

            if(is64Bit)
            {
                fixerPath = context.getApplicationInfo().nativeLibraryDir + "/SoFixer64";
            }
            SuFile soFixerPath = new SuFile(fixerPath);

            String pathDumpFixed = pathDumpFile.replace(".so", "-fixed.so");

            String commandBuilder = soFixerPath.getAbsolutePath() + " -s " + pathDumpFile + " -o " + pathDumpFixed + " -m " + " 0x" + Long.toHexString(startAddress);

            Shell.Result cmd = Shell.cmd(commandBuilder).exec();

            if(cmd.isSuccess())
            {
                SuFile dumpFile = new SuFile(pathDumpFixed);
                if(dumpFile.exists())
                {
                    logView.appendInfo("Fixed Dumped File: " + dumpFile.getAbsolutePath());
                }
            }
            else
            {
                logView.appendError("ELF Fixer failed:");

                logView.appendLine(getOutputShell(cmd.getOut()));

                logView.appendLine();
            }
        }
    }

    /**
     * A method to Extract SoFixer from assets folder.
     */
    private boolean extractSoFixer() {

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


    /**
     * A method to Parse List Maps Info to be used for finding address of file from memory.
     * @param fileName a file name to be searched in maps.
     * @param listMaps a list maps info.
     */
    private List<MemoryInfo> parseMap(String fileName, List<MapInfo> listMaps) {

        List<MemoryInfo> result = new ArrayList<>();

        for(MapInfo map : listMaps)
        {
            if (map.getPath().contains(fileName))
            {
                int pid = map.getPID();

                result.add(new MemoryInfo(pid, map.getPath(), map.getStartAddress(), map.getEndAddress()));
            }
        }
        return result;
    }

    /**
     * a method to get List Maps Info from Selected Process.
     * @param processName a process name from selected process.
     * @param fileName a file name to be searched in maps.
     */
    private List<MapInfo> getAllMaps(String processName, String fileName) {

        List<MapInfo> listMaps = new ArrayList<>();

        int PID = -1;
        for(int getPID : getAllPIDProcess(processName)) {

            StringBuilder mapsString = new StringBuilder();

            Shell.Result cmd = Shell.cmd("cat /proc/" + getPID + "/maps").exec();
            if(cmd.isSuccess()) {

                List<MapInfo> createListMaps = new ArrayList<>();

                List<String> output = cmd.getOut();

                for (int i = 0; i < (output.size() - 5); i++) {

                    String lines = output.get(i).replaceAll("\\s+", " ");

                    if(!lines.contains("USER") && !lines.contains("PID") && !lines.contains("PPID") && !lines.contains("NAME")) {

                        mapsString.append(lines).append("\n");

                        createListMaps.add(new MapInfo(getPID, lines));
                    }
                }

                if (mapsString.toString().contains(fileName))
                {
                    if(PID == -1)
                    {
                        PID = getPID;
                    }

                    List<MemoryInfo> listMemoryInfo = new ArrayList<>();
                    for(MapInfo mapInfo : createListMaps)
                    {
                        if(mapInfo.getPID() == PID)
                        {
                            listMaps.add(mapInfo);
                        }
                        listMemoryInfo.add(new MemoryInfo(mapInfo.getPID(), mapInfo.getPath(), mapInfo.getStartAddress(), mapInfo.getEndAddress()));
                    }

                    listBinData.put(PID, listMemoryInfo);
                    listMapsData.put("Maps-" + getPID + ".txt", mapsString.toString());
                }
            }
        }
        return listMaps;
    }

    /**
     * A method to get All PID Process from Selected Process.
     * @param processName a process name from selected process.
     */
    private List<Integer> getAllPIDProcess(String processName) {

        HashMap<Integer, Integer> CreateListProcess = new HashMap<>();

        Shell.Result cmd = Shell.cmd("ps -t | grep \"" + processName + "\"").exec();
        if(cmd.isSuccess())
        {
            List<String> output = cmd.getOut();

            for (int i = 0; i < output.size(); i++) {

                String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");

                for (int j = 0; j < results.length; j++) {

                    String getPackageName = results[results.length - 1];

                    if(getPackageName.contains(".") && getPackageName.contains(processName))
                    {
                        int pid = Integer.parseInt(results[1]);

                        CreateListProcess.put(pid, pid);
                    }
                }
            }
        }

        cmd = Shell.cmd("ps | grep \"" + processName + "\"").exec();
        if(cmd.isSuccess())
        {
            List<String> output = cmd.getOut();

            for (int i = 0; i < output.size(); i++) {

                String[] results = output.get(i).trim().replaceAll("( )+", ",").replaceAll("(\n)+", ",").split(",");

                for (int j = 0; j < results.length; j++) {

                    String getPackageName = results[results.length - 1];

                    if(getPackageName.contains(".") && getPackageName.contains(processName))
                    {
                        int pid = Integer.parseInt(results[1]);

                        CreateListProcess.put(pid, pid);
                    }
                }
            }
        }

        return new ArrayList<>(CreateListProcess.keySet());
    }

    private List<Integer> getPIDProcess(String processName) {

        HashMap<Integer, Integer> CreateListProcess = new HashMap<>();

        Shell.Result cmd = Shell.cmd("pidof " + processName).exec();
        if(cmd.isSuccess())
        {
            List<String> output = cmd.getOut();

            for (int i = 0; i < output.size(); i++) {

                String lines = output.get(i);
                if(lines.contains(" "))
                {
                    String[] listPID = lines.split(" ");
                    for(int j = 0; j < listPID.length; j++)
                    {
                        try
                        {
                            int pid = Integer.parseInt(listPID[j]);

                            Log.d("GetPID", "PID: " + pid);

                            CreateListProcess.put(pid, pid);
                            break;

                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return new ArrayList<>(CreateListProcess.keySet());
    }

    /**
     * A method to get Output Shell from Executing Command in {@link Shell.Result}
     * @param outputData A data from Output Shell.
     */
    private String getOutputShell(List<String> outputData) {

        if(outputData.size() > 0) {

            StringBuilder result = new StringBuilder();

            for(String lines : outputData)
            {
                result.append(lines).append("\n");
            }
            return result.toString();
        }
        return "";
    }

    /**
     * A method to Create Directory and also it can be used to Check if Directory exists or not.
     * @param path a path of directory.
     */
    private boolean CreateDirectory(String path) {
        SuFile directory = new SuFile(path);

        return directory.mkdirs();
    }
}
