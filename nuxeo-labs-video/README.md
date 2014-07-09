## Media Info Helper

An helper class (MediaInfoHelper) to call mediainfo -i on video blobs and parse the result.
Main methods are also exposed is 2 automation operations.

### mediainfo -i Structure
A call to mediainfo -i gives a back a String List looking like :

$ mediainfo -i elephantsdream-160-mpeg4-su-ac3.avi 

General

Complete name                            : elephantsdream-160-mpeg4-su-ac3.avi

Format                                   : AVI

Format/Info                              : Audio Video Interleave

File size                                : 6.20 MiB

Duration                                 : 10mn 53s

Overall bit rate                         : 79.5 Kbps

Writing application                      : Lavf52.31.0


Video

ID                                       : 0

Format                                   : MPEG-4 Visual

Format profile                           : Simple@L1

Format settings, BVOP                    : No

Format settings, QPel                    : No

Format settings, GMC                     : No warppoints

Format settings, Matrix                  : Default (H.263)

Codec ID                                 : FMP4

Duration                                 : 10mn 53s

Bit rate                                 : 34.7 Kbps

Width                                    : 160 pixels

Height                                   : 100 pixels

Display aspect ratio                     : 16:9

Frame rate                               : 24.000 fps

Color space                              : YUV

Chroma subsampling                       : 4:2:0

Bit depth                                : 8 bits

Scan type                                : Progressive

Compression mode                         : Lossy

Bits/(Pixel*Frame)                       : 0.090

Stream size                              : 2.71 MiB (44%)

Writing library                          : Lavc52.20.0


Audio

ID                                       : 1

Format                                   : MPEG Audio

Format version                           : Version 1

Format profile                           : Layer 2

Codec ID                                 : 50

Duration                                 : 10mn 53s

Bit rate mode                            : Constant

Bit rate                                 : 32.0 Kbps

Channel(s)                               : 1 channel

Sampling rate                            : 48.0 KHz

Compression mode                         : Lossy

Stream size                              : 2.49 MiB (40%)

Alignment                                : Aligned on interleaves

Interleave, duration                     : 24 ms (0.58 video frame)

Interleave, preload duration             : 24 ms


3 categories are returned (General, Audio, Video), and each category is a list of strings containing a key and a value separated by a ":" and some useless white spaces. Each Category is separated by a blank line.

## Main methods available

Considering the usual ouput of mediainfo -i a map of maps seems the simplest solution to use the result.

Two main methods are available : 

- getProcessedMediaInfo gets all information for the mediainfo in a processed form (not the raw String List).

a call to getProcessMediaInfo(Blob video) gives back the full map of maps, for example : 

{General={Format=MPEG-4, Complete name=test.mp4}, Audio={Format profile=Layer 2, Format=MPEG Audio, Delay relative to video=83ms, ID=0}, Video={Format profile=Baseline@L2.1, Format=AVC, Format/Info=Advanced Video Codec, ID=1, Height=288 pixels, Width=512 pixels}}

- getSpecificMediaInfo returns a string corresponding to the info requested. Two keys are necessary, the first one is the category (General, Audio, Video), and the second is the item in the category (Width, Format...). 

a few examples : 

- getSpecificMediaInfo("General","Format", myVideoBlob) could return : "MPEG-4"
- getSpecificMediaInfo("Video","Height", myVideoBlob) could return : "288 pixels"

Please be aware that the result of getSpecificMediaInfo is always the output String of mediainfo and may need extra parsing, for example for the Height the result could be "512 pixels" and will not be 512.

## Miscellaneous 
- A command line contribution is deployed to make the call to mediainfo available.

- The available automation operation are :
+ GetInfoFromMediaInfo that calls getProcessedMediaInfo
+ GetSpecificInformationFromMediaInfo that calls getSpecificMediaInfo
The operations set the result into a context variable.


## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software
platform for enterprise content management] [5] and packaged applications
for [document management] [6], [digital asset management] [7] and
[case management] [8]. Designed by developers for developers, the Nuxeo
platform offers a modern architecture, a powerful plug-in model and
extensive packaging capabilities for building content applications.

[5]: http://www.nuxeo.com/en/products/ep
[6]: http://www.nuxeo.com/en/products/document-management
[7]: http://www.nuxeo.com/en/products/dam
[8]: http://www.nuxeo.com/en/products/case-management

More information on: <http://www.nuxeo.com/>
