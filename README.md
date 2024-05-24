

# PMT-Dumper
[![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/MikaCybertron/PMT-Dumper/total?style=for-the-badge&logo=windows10&link=https%3A%2F%2Fgithub.com%2FMikaCybertron%2FPMT-Dumper%2Freleases)](https://github.com/MikaCybertron/PMT-Dumper/releases)
<p align="left"> <img src="https://komarev.com/ghpvc/?username=PMT-Dumper&label=Total%20views&color=0e75b6&style=flat" alt="PMT-Dumper" /> </p>

This is an Android application that can Dump Files from Android Process Memory based on [**NoxDumper**](https://github.com/zeroKilo/NoxDumper) and [**PADumper**](https://github.com/BryanGIG/PADumper) for Rooted Devices.

for devices with android 12 or higher it may work and some may not work due to permission issue.

# How To Use
- Run the games.
- Download the release version in here [PMT-Dumper.apk](https://github.com/MikaCybertron/PMT-Dumper/releases) and then install it.
- Grant Root Permission and Storage Permission for PMT Dumper.
- Enter process name manually or you can click button `Select Apps` to select running apps.
- Enter the ELF Name or you can leave it with default name `libil2cpp.so`
- [**Optional**] Check `Show All Running Process` if you want to Show All Running Process.
- [**Optional**] Check `Dump Maps file` if you want dump the maps of process memory.
- [**Optional**] Check `ELF Fixer` if you want fix the ELF files.
- [**Optional**] Check `ELF FIXER 32 Bit or 64 Bit` if you want use 64 Bit of ELF Fixer and Uncheck if you want use 32 Bit of ELF Fixer.
- [**Optional**] Check `Dump global-metadata.dat` if you want dump unity metadata from memory.
- Dump and wait until finish.

Custom Output Directory: `/CustomPath/PlatinmodsDumper/[ProcessName]/[startAddress-endAddress-file]`

# Video Preview
[![Video Preview](https://i.imgur.com/A6mWWJW.png)](https://youtu.be/YoW3zylOdZw)


## Credits
- [**NoxDumper**](https://github.com/zeroKilo/NoxDumper)
- [**PADumper**](https://github.com/BryanGIG/PADumper)
- [**libsu**](https://github.com/topjohnwu/libsu)
- [**SoFixer**](https://github.com/F8LEFT/SoFixer)
