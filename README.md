# AoC2 Converter - Converts .png to provinces
This command line tool takes a image file as .png and outputs multiple text file describing the contour of the same-colored pixel blobs.

## How to use
The image file needs to be composed of blobs of colors, each blob will be converted into a province. 
Multiple blobs of the same color will be converted to one province. Enclaved provinces are supported in this version. 

By default, the provinces are outputted in the same folder as jar. If this is not desire, use `-o ./folderForOutput` to 
redirect these files. 

A linear scaling can be applied to the output with `-s <NUMBER>`.

Adding `-v` adds additional messages for the progress of the converter.

The tool can be run by command line, allowing one to see the log:  
```
java -jar ./aoc2-converter-v0.4.0.jar -v ./map.png 
```

```
java -jar ./aoc2-converter-<version>.jar <OPTIONS> <PATH TO IMAGE>
```
The numbered files can be then moved to the folder with the custom map you create. 

## Notes to algorithm

## Frequently Asked Questions
- **Q**: 
  - **A**: 
- **Q**:
    - **A**:
- **Q**:
    - **A**:

## Related closed-source tools
- [Official Map Editor](http://www.ageofcivilizationsgame.com/topic/717-map-editor-aoc2/) from [Łukasz Jakowski](https://github.com/jakowskidev)

- ["Map Editor 2.0"](http://www.ageofcivilizationsgame.com/topic/148990-map-editor-20/) from Capodastr

