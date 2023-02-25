package platinmods.com.dumper.variable;

import androidx.annotation.NonNull;

public class MapInfo {
    private int PID;
    private String address = "";
    private String perms = "";
    private String offset = "";
    private String dev = "";
    private String inode = "";
    private String path = "";

    public MapInfo(int PID, String mapData) {
        this.PID = PID;
        String[] dataString = mapData.replaceAll("\\s+", " ").split(" ");
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
                    path = dataString[index];
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
}
