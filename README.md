# PMT-Dumper
This is an Android application that can Dump Files from Android Process Memory based on [**NoxDumper**](https://github.com/zeroKilo/NoxDumper) and [**PADumper**](https://github.com/BryanGIG/PADumper) for Rooted Devices.

## How To Use
- Run the games.
- Download the release version in here [PMT-Dumper.apk](https://github.com/MikaCybertron/PMT-Dumper/releases) and then install it.
- Grant Root Permission and Storage Permission for PMT Dumper.
- Enter process name manually or you can click button `Select Apps` to select running apps.
- Enter the ELF Name or you can leave it with default name `libil2cpp.so`
- [**Optional**] Check `ELF Fixer` if you want fix the ELF files.
- [**Optional**] Check `ELF Architectures` if you want use 64 Bit of ELF Fixer and Uncheck if you want use 32 Bit of ELF Fixer.
- [**Optional**] Check `Dump global-metadata.dat` if you want dump unity metadata from memory.
- [**Optional**] Check `Dump Maps file` if you want dump the maps of process memory.
- Dump and wait until finish.
- Result will be in `/sdcard/PlatinmodsDumper/[ProcessName]/[startAddress-endAddress-file]`

# Screenshots
![Screenshot](https://i.imgur.com/zKW8apA.png)


## Credits
- [**zeroKilo**](https://github.com/zeroKilo/NoxDumper)
- [**BryanGIG**](https://github.com/BryanGIG/PADumper)
- [**libsu**](https://github.com/topjohnwu/libsu)
- [**SoFixer**](https://github.com/F8LEFT/SoFixer)
