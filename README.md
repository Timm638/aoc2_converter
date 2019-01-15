# AoC2 Converter - Converts .png to provinces!
This tool converts images to provinces in great masses.

The image file needs to be composed of blobs of colors, each blob will be converted into a province.
The tool doesn't support enclaved provinces, which are inside another province, and 1 pixel big provinces.

The scale increases the distances between the corners. If you have a scale of 2/4, you can use the same image, which you are inputting, as a background, when you enlarge it by 200%/400%/...

The provinces are outputted in the same folder as jar.

The tool can be run by command line, allowing one to see the log:
java -cp AoC2_Conv_0.1.jar Main "Path to image" SCALE

Compiled files is AoC2_Conv_0.25.jar
