package platinmods.com.dumper.variable;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class MapInfo {
    private int PID;
    private String address = "";
    private String perms = "";
    private String offset = "";
    private String dev = "";
    private String inode = "";
    private String path = "";
    private String fileName = "";

    public MapInfo(int PID, String mapData) {
        this.PID = PID;
        mapData = mapData.replaceAll("\\s+", " ");
        String[] dataString = mapData.split(" ");
        for (int index = 0; index < dataString.length; index++) {
            switch (index) {
                case 0:
                    address = dataString[index];
                    break;
                case 1:
                    perms = dataString[index];
                    break;
                case 2:
                    offset = dataString[index];
                    break;
                case 3:
                    dev = dataString[index];
                    break;
                case 4:
                    inode = dataString[index];
                    break;
                case 5:
                    path = this.getFullStringStartingFromSplit(mapData, " ", 5);
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
                    break;
            }
        }
    }

    public int getPID() {
        return PID;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return getEndAddress() - getStartAddress();
    }

    public long getStartAddress() {
        if (address.isEmpty()) return 0L;
        return Long.parseLong(address.split("-")[0], 16);
    }

    public long getEndAddress() {
        if (address.isEmpty()) return 0L;
        return Long.parseLong(address.split("-")[1], 16);
    }

    public String getPerms() {
        return perms;
    }

    public long getOffset() {
        return Long.parseLong(offset, 16);
    }

    public String getDev() {
        return dev;
    }

    public int getInode() {
        return Integer.parseInt(inode);
    }

    public boolean isValid() {
        return getStartAddress() != 0L && getEndAddress() != 0L;
    }

    @NonNull
    @Override
    public String toString() {
        return "MapInfo(address='" + address + "', perms='" + perms + "', offset='" + offset + "', dev='" + dev + "', inode='" + inode + "', path='" + path + "')";
    }


    /**
     * a method to split the string into an array of substrings based on the whitespace delimiter, and then concatenate the substrings starting from the desired index.
     * In this code, we first split the input string into an array of substrings using the `split()` method with the `find` string as delimiter.
     * We then define the index of the substring to start concatenating from, which in this case is 5.
     * @return
     */
    private String getFullStringStartingFromSplit(String input, String find, int startIndex) {

        if(input.contains(find))
        {
            // Split the input string into an array of substrings based on the whitespace delimiter
            String[] substrings = input.split(find);

            // Define the index of the substring to start concatenating from startIndex
            // Concatenate the substrings starting from the startIndex
            return String.join(" ", Arrays.copyOfRange(substrings, startIndex, substrings.length));
        }
        return input;
    }
}
