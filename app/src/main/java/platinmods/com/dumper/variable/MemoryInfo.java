package platinmods.com.dumper.variable;

public class MemoryInfo {

    public int PID;

    public String path;

    public String fileName;

    public long startAddress;

    public long endAddress;

    public MemoryInfo(int PID, String path, long startAddress, long endAddress) {
        this.PID = PID;
        this.path = path;
        this.startAddress = startAddress;
        this.endAddress = endAddress;

        if(path.contains("[")) {
            fileName = "";
        }
        else
        {
            if(path.contains(":"))
            {
                fileName = "";
            }
            else
            {
                if(path.contains(" ")) {

                    String[] stringData = path.split(" ");
                    if(path.contains("/")) {

                        String[] stringData2 = stringData[0].split("/");
                        fileName = stringData2[stringData2.length - 1];
                    }
                    else
                    {
                        fileName = "";
                    }
                }
                else
                {
                    if(path.contains("/")) {
                        fileName = path.split("/")[path.split("/").length - 1];
                    }
                    else
                    {
                        fileName = "";
                    }
                }
            }
        }
    }

    private String[] getSplitString(String input, String regex) {

        String[] stringData = input.split(regex);

        return input.split(regex);
    }
}
