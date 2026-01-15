# AoC2 Converter - Converts .png to provinces
This command line tool takes a image file as .png and outputs multiple text file describing the contour of the same-colored pixel blobs.

## How to use
The image file needs to be composed of blobs of colors, each blob will be converted into a province.  
The tool doesn't support enclaved provinces, which are inside another province, and 1 pixel big provinces.

The scale increases the distances between the corners. If you have a scale of 2/4, you can use the same image, which you are inputting, as a background, when you enlarge it by 200%/400%/...

The provinces are outputted in the same folder as jar.

The tool can be run by command line, allowing one to see the log:  
java -cp AoC2_Conv_0.1.jar de.timm638.aoc2_converter.Main "Path to image" SCALE

## Notes to algorithm

## Frequently Asked Questions



## Related closed-source tools
- [Official Map Editor](http://www.ageofcivilizationsgame.com/topic/717-map-editor-aoc2/) from [Łukasz Jakowski](https://github.com/jakowskidev)

- ["Map Editor 2.0"](http://www.ageofcivilizationsgame.com/topic/148990-map-editor-20/) from Capodastr

