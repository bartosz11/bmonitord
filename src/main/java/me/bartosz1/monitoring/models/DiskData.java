package me.bartosz1.monitoring.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//represents 1 disk
public class DiskData {

    private String mountpoint;
    private float usage;
    private long usedBytes;
    private long totalBytes;

    //expects a string in format
    // mountpoint,usage,totalBytes,usedBytes
    public static DiskData fromString(String s) {
        String[] split = s.split(",");
        DiskData diskData = new DiskData();
        diskData.setMountpoint(split[0]);
        diskData.setUsage(Float.parseFloat(split[1]));
        diskData.setTotalBytes(Long.parseLong(split[2]));
        diskData.setUsedBytes(Long.parseLong(split[3]));
        return diskData;
    }

    //expects a string <diskData format from above method>;<some disk again>;...;...
    public static List<DiskData> multipleFromString(String s) {
        List<String> disksString = Arrays.asList(s.split(";"));
        List<DiskData> disks = new ArrayList<>();
        disksString.forEach(diskString -> disks.add(fromString(diskString)));
        return disks;
    }

    public static String listToString(List<DiskData> list) {
        StringBuilder builder = new StringBuilder();
        for (DiskData diskData : list) {
            builder.append(diskData);
            builder.append(";");
        }
        return builder.toString();
    }

    public String getMountpoint() {
        return mountpoint;
    }

    public DiskData setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
        return this;
    }

    public float getUsage() {
        return usage;
    }

    public DiskData setUsage(float usage) {
        this.usage = usage;
        return this;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public DiskData setUsedBytes(long usedBytes) {
        this.usedBytes = usedBytes;
        return this;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public DiskData setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s,%f,%d,%d", getMountpoint(), getUsage(), getTotalBytes(), getUsedBytes());
    }
}
