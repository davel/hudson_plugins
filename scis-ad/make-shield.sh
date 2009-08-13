#!/bin/sh -e
svg2png -w 32 -h 32 < shield.svg > t.png
composite -compose Dst_Over -tile xc:"#4e9a06" t.png shield.gif
rm t.png

