# Commands for recreating the screenshot collections
We use the image maverick command line, and assume the desired images are in the same folder with the naming scheme `l[index].png` (and `d[index].png for the dark equivalent):
```
montage l* -tile [number-columns]x -geometry 1080x2340+4+4 -background #dfdfdf [name].png
montage d* -tile [number-columns]x -geometry 1080x2340+4+4 -background #AAAAAA [name]-dark.png
```
Depending on your device, you might need to adapt the resolution of a single screenshot.