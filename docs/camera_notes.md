
## Summary

We conducted several tests of the Loomo camera hardware to try and capture images of people suitable for face recognition. In the end, we found the hardware insufficient in exposure range, capture speed, and resolution.

## Day 1

On Day 1, images were recorded at maximum resolution (3264x2448), most images captured were too blurry for the model to detect any faces. The few faces that were detected, the model indicated were like too blurry for identification.

![blurry1](day1/0ffadab9-e514-827c-4f4a-23048a847d30.jpg)
![blurry2](day1/3ae5b2e7-51a1-747d-19da-2b1bd79d11a9.jpg)
![blurry3](day1/4f3b306f-3fbb-f659-11aa-36b189e1563a.jpg)
![blurry4](day1/4f89f640-66f1-f088-72e6-22b8e405fc04.jpg)
![blurry5](day1/4faf4188-143f-f1bf-ba5d-9c1d3473e6bb.jpg)
![blurry6](day1/a3d1c338-513d-cd22-b6a5-78a94c4ae2ba.jpg)
![blurry7](day1/a760b0ae-4f63-73fc-cee6-5678ba3caa5b.jpg)
![blurry8](day1/f84946c3-f2e8-6d37-5475-4971244b411a.jpg)

There were a few images where faces could be detected

```json
  "faceRectangle": {
    "top": 534,
    "left": 1528,
    "width": 175,
    "height": 175
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.9
  },
```

![face1](day1/2514140f-6719-ab77-acc0-4fd5bb9bbe71.jpg)

```json
  "faceRectangle": {
    "top": 653,
    "left": 1530,
    "width": 168,
    "height": 168
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.85
  },
```

![face2](day1/9aeffcf7-1b00-64eb-355a-5fa4f8804ea0.jpg)

```json
  "faceRectangle": {
    "top": 801,
    "left": 1479,
    "width": 189,
    "height": 189
  },
  "blur": {
    "blurLevel": "high",
    "value": 1
  },
```

![face3](day1/9d0c968f-0f2c-92aa-f876-7f92f371c200.jpg)

```json
  "faceRectangle": {
    "top": 801,
    "left": 1479,
    "width": 189,
    "height": 189
  },
  "blur": {
    "blurLevel": "high",
    "value": 1
  },
```

![face4](day1/d8a363b0-3e7c-eb33-0e50-d0df71fb6788.jpg)

```json
  "faceRectangle": {
    "top": 832,
    "left": 1517,
    "width": 141,
    "height": 141
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.98
  },
```

![face5](day1/704c43cf-e541-fe18-069f-e4e0aeb63065.jpg)

```json
  "faceRectangle": {
    "top": 778,
    "left": 1751,
    "width": 230,
    "height": 230
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.96
  },
```

## Day 2 & 3

After the first day we hypothesized that the movement of the head due to DTS tracking might be contributing to the blur and also the particular spot was not an area conducive to slow moving people. So we made changes to the code to stop head motion while taking pictures and planned to position Loomo in a better location near the kitchen where people generally congregate or slowed to grab a drink. Again images were recorded at maximum resolution (3264x2448), again many were blurry, however this time about half of the images where faces were detected the blurLevel was medium. OBservationally, this appears to be the set of people that stopped and interacted with Loomo.

![blurry](day2-3/13b5744e-d4a9-05ea-51ae-da39598337b4.jpg)
![blurry](day2-3/0f0c9677-af46-a932-aef8-e8bb0a198efc.jpg)
![blurry](day2-3/10d61151-c84f-c428-c1b5-177ec15cfe32.jpg)
![blurry](day2-3/1aceaec8-91f6-8150-b3c5-62d4d3fdecca.jpg)
![blurry](day2-3/35d58b00-da15-96b3-c3d2-a44a0a0d4312.jpg)
![blurry](day2-3/3999d0ac-4fd7-b69c-9961-46dbcec43277.jpg)
![blurry](day2-3/495f538a-e990-b519-ef8e-2ed0ef3a4e7e.jpg)
![blurry](day2-3/020ceb3a-278d-9596-a755-d2cb9904d5fc.jpg)
![blurry](day2-3/5cda0455-527a-7f76-dbc3-3a14212e4831.jpg)
![blurry](day2-3/7d1956cb-c7e5-3f86-bbd6-bdcdcdc90bb2.jpg)
![blurry](day2-3/84347d5e-0558-ede1-53c7-b521c38adef1.jpg)
![blurry](day2-3/85d35a73-a59f-0bdc-4ead-cdd734c4e846.jpg)
![blurry](day2-3/664b0caf-614c-5418-8cbb-3cb5a5311ca8.jpg)
![blurry](day2-3/8804ab7c-26c0-0fb0-35e4-2b99a8b325d7.jpg)
![blurry](day2-3/89085d2c-d4a0-3d6c-1f07-206cc618c7b2.jpg)
![blurry](day2-3/8b5caf5a-28f1-26ed-9148-b69dfb0f4023.jpg)
![blurry](day2-3/8dceced0-1994-bcb4-8ac1-2a0c3d2cd0c7.jpg)
![blurry](day2-3/9ae16df3-e342-8d6f-85fe-d6d772262e1b.jpg)
![blurry](day2-3/a8237e6c-e67e-3243-3c9b-17a3550b9347.jpg)
![blurry](day2-3/59a5bc15-8a14-18b0-3970-60d66d922a24.jpg)
![blurry](day2-3/3fdbfdd0-974f-3f40-4e9c-0a52f2ca80e5.jpg)
![blurry](day2-3/a571495d-df79-ca52-3e8d-ac4eab08a23c.jpg)
![blurry](day2-3/b6e7c314-957a-718f-2e86-d95406f87864.jpg)
![blurry](day2-3/c9ce1014-229e-a4be-20df-fe3d1ee1d065.jpg)
![blurry](day2-3/cf63d8ca-40db-5fe0-5555-39255f546006.jpg)
![blurry](day2-3/d339f1ae-361b-7a61-ba7d-c5b26f03b0f4.jpg)
![blurry](day2-3/da3ae059-c1be-56a7-2b30-54356d136195.jpg)
![blurry](day2-3/eee1ff2e-f5e2-1554-f71d-b9719f9927a0.jpg)
![blurry](day2-3/dff1709f-49b8-998e-59a8-22017c175689.jpg)
![blurry](day2-3/fe643a88-351f-c726-b1ae-ebeacd5d1ace.jpg)
![blurry](day2-3/baf2646e-9a72-8920-b7fc-95237fc3143d.jpg)
![blurry](day2-3/26abcd9a-d6a2-4de0-4480-dedc513e94d8.jpg)
![blurry](day2-3/c21e606f-b31c-ec34-7d5b-0249652b032e.jpg)

Slightlyly more faces were detected.

![face](day2-3/16b43d8d-8651-f31b-b9d1-ca09c36a55d1.jpg)

```json
  "faceRectangle": {
    "top": 1035,
    "left": 1813,
    "width": 166,
    "height": 166
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.62
  },
```

![face](day2-3/27a2573f-31a3-d06a-ed5d-0b041d78cf98.jpg)

```json
  "faceRectangle": {
    "top": 1423,
    "left": 1404,
    "width": 52,
    "height": 52
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.46
  },
```

![face](day2-3/3802f340-7813-3af3-acb5-a05502bcb900.jpg)

```json
  "faceRectangle": {
    "top": 1238,
    "left": 2178,
    "width": 152,
    "height": 152
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.68
  },
```

![face](day2-3/44f8dc45-05ed-986b-726c-b3a83809dacf.jpg)

```json
  "faceRectangle": {
    "top": 1316,
    "left": 1832,
    "width": 263,
    "height": 263
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.7
  },
```

![face](day2-3/726ee731-91e2-b591-cc67-00790afd2c8c.jpg)

```json
  "faceRectangle": {
    "top": 1704,
    "left": 845,
    "width": 120,
    "height": 120
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.92
  },
```

![face](day2-3/72b0baec-6e44-e9ae-5e3a-a056e64d86ec.jpg)

```json
  "faceRectangle": {
    "top": 1587,
    "left": 1679,
    "width": 120,
    "height": 120
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.94
  },
```

![face](day2-3/7e74b109-2b6c-c10c-de28-8e0a28f83316.jpg)

```json
  "faceRectangle": {
    "top": 614,
    "left": 1652,
    "width": 176,
    "height": 176
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.44
  },
```

![face](day2-3/9a4ed4e9-8582-2b00-458a-08b302cffcdb.jpg)

```json
  "faceRectangle": {
    "top": 948,
    "left": 1553,
    "width": 209,
    "height": 209
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.78
  },
```

![face](day2-3/e07ae2b7-881c-9f5f-e3f6-21d17ab6adbd.jpg)

```json
  "faceRectangle": {
    "top": 1230,
    "left": 1509,
    "width": 141,
    "height": 141
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.8
  },
```

## Day 4

Troubleshooting with the Loomo engineers, they indicated that the camera functioned better in more light. So we attached a 215 lumen worklight to the head of the Loomo to direct more light where the camera was looking.

![robot-mod-1](day4/robot-mod-1.jpg)
![robot-mod-2](day4/robot-mod-2.jpg)

Again, images were recorded at maximum resolution (3264x2448).

![blurry](day4/58548d25-a8ec-f065-0416-19ddcd2bf900.jpg)
![blurry](day4/70aa1e96-0969-8532-f065-d121f4105c89.jpg)
![blurry](day4/85845dd4-fa46-8fc6-c7c2-a313b0067892.jpg)
![blurry](day4/8c0af2ef-ea1e-7ff7-2d18-114c22c1da70.jpg)
![blurry](day4/9855d6bf-0249-48b9-b4e4-231a45517e79.jpg)
![faces](day4/18b19a56-582b-a28a-4e90-af3fe5e8a80d.jpg)

Images were faces were detected were again less blurry, particularly those where there seemed to be interaction with Loomo

![faces](day4/42638c4c-5da1-ff79-ae84-aca093e15268.jpg)

```json
  "faceRectangle": {
    "top": 957,
    "left": 1584,
    "width": 212,
    "height": 212
  },
  "blur": {
    "blurLevel": "low",
    "value": 0.22
  },
```

![faces](day4/5d7be775-b40c-6268-9068-3fbe5926ebd7.jpg)

```json
  "faceRectangle": {
    "top": 701,
    "left": 1908,
    "width": 234,
    "height": 234
  },
  "blur": {
    "blurLevel": "high",
    "value": 0.77
  },
```

![faces](day4/a1a51a76-f876-ff87-9ba0-a17f6d3f8e49.jpg)

```json
  "faceRectangle": {
    "top": 886,
    "left": 887,
    "width": 239,
    "height": 239
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.54
  },
```

![faces](day4/b73d48e9-1547-a054-6937-c1aac9b52ef0.jpg)

```json
  "faceRectangle": {
    "top": 781,
    "left": 1641,
    "width": 221,
    "height": 221
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.28
  },
```

## Day 5

After looking at the result from Day 4, we determined that we needed to identify the maximum resolution where Loomo could snap clear photos of people while they walked by. We place Loomo is a very high light environment and adjusted the resolution of recorded photos.

### Experiment-1

Images were recorded at a resolution of 640x480

![blurry](day5/exp1/15268a54-7127-046a-52db-14f6ac183346.jpg)
![blurry](day5/exp1/179dc590-ceb4-8914-ec3a-69aef2254eba.jpg)
![blurry](day5/exp1/19e97d92-e15e-04fb-cf06-f8dfbf3cca6f.jpg)
![blurry](day5/exp1/4c062e80-0ba7-3025-743d-301f9cf08e5d.jpg)
![blurry](day5/exp1/533b28bc-fb89-285c-ebcd-4e6b58d01a0e.jpg)
![blurry](day5/exp1/6073a5f5-1374-2992-a336-cf17442e9e1e.jpg)
![blurry](day5/exp1/931d7680-d71e-643a-bada-018d143da6e3.jpg)
![blurry](day5/exp1/95454c34-a645-af7c-8540-7e5da67da044.jpg)
![blurry](day5/exp1/c1b6fdce-ef30-667f-ec72-0d8034d1771b.jpg)
![blurry](day5/exp1/ea1fa996-4ead-f034-16f8-90a692b3977e.jpg)

Detected faces were very sharp, but again only occured when I stopped versus when I was walking past.

![faces](day5/exp1/1cbed163-c919-9c05-8aa1-ffd07e764c56.jpg)

```json
  "faceRectangle": {
    "top": 159,
    "left": 287,
    "width": 49,
    "height": 49
  },
  "blur": {
    "blurLevel": "low",
    "value": 0
  },
```

![faces](day5/exp1/e1f91100-978e-c956-59ee-0745e9dc67a3.jpg)

```json
  "faceRectangle": {
    "top": 83,
    "left": 308,
    "width": 48,
    "height": 48
  },
  "blur": {
    "blurLevel": "low",
    "value": 0.2
  },
```

### Experiment-2

We hypothesized that cognitive services was not detecting faces because they were too small, so we tried increasing the resolution. This time, images were recorded at a resolution of 1920x1080

![blurry](day5/exp2/24102c2b-5529-ceb0-7e61-1ef5439e8373.jpg)
![blurry](day5/exp2/2889a963-4f29-3ae1-b499-fbd622b67e27.jpg)
![blurry](day5/exp2/2abba7e0-e86e-ed89-1a65-bf3de6626816.jpg)
![blurry](day5/exp2/4d9201d9-fbbc-04e3-1868-153e6059224d.jpg)
![blurry](day5/exp2/6369630b-10f3-3e2f-5923-356a72e8b70b.jpg)
![blurry](day5/exp2/6a228073-72a1-e0c8-f820-d28a9cba0ca5.jpg)
![blurry](day5/exp2/adcbcc63-6756-636a-d113-3d162275701f.jpg)
![blurry](day5/exp2/b0b574be-8e04-8240-7505-dad7ab043ac0.jpg)
![blurry](day5/exp2/c3894c85-f40e-fef1-6154-92139c5c4707.jpg)
![blurry](day5/exp2/cdc857af-963a-b6e8-ef65-953e3dc02cb5.jpg)
![blurry](day5/exp2/d09b1661-8b02-30fc-c479-02e1e3d85c08.jpg)

This time more faces were recognized, but more were medium blurry.

![faces](day5/exp2/16af8691-759f-a6a4-9e3b-799c70a63567.jpg)

```json
  "faceRectangle": {
    "top": 458,
    "left": 845,
    "width": 102,
    "height": 102
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.41
  },
```

![faces](day5/exp2/8c0e2a9f-c7c2-a719-c10f-9038d2b7f5c4.jpg)

```json
  "faceRectangle": {
    "top": 152,
    "left": 848,
    "width": 103,
    "height": 103
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.5
  },
```

![faces](day5/exp2/b9e9a65c-cf53-9cf5-ac9a-348147546b94.jpg)

```json
  "faceRectangle": {
    "top": 599,
    "left": 939,
    "width": 109,
    "height": 109
  },
  "blur": {
    "blurLevel": "low",
    "value": 0.16
  },
```

![faces](day5/exp2/d2840c9a-060b-4cc4-6872-db88d6f2786d.jpg)

```json
  "faceRectangle": {
    "top": 512,
    "left": 1164,
    "width": 82,
    "height": 82
  },
  "blur": {
    "blurLevel": "medium",
    "value": 0.43
  },
```

![faces](day5/exp2/ee00b12f-58dc-fe4c-7992-ed223d7b372e.jpg)

```json
  "faceRectangle": {
    "top": 6,
    "left": 574,
    "width": 162,
    "height": 162
  },
  "blur": {
    "blurLevel": "low",
    "value": 0
  },
```

## Hardware

The front facing camera on the Loomo is capable of the following resolutions

```
3264x2448
3264x1836
2560x1920
2560x1440
2048x1536
1600x1200
1920x1080
1280x720
1024x768
720x480
640x480
320x240
```

## Environment

The [National Optical Astronomy Observatory](https://www.noao.edu/) provide a [recommended light levels](https://www.noao.edu/education/QLTkit/ACTIVITY_Documents/Safety/LightLevels_outdoor+indoor.pdf) resource for instructors. Pertinent to our example is this section of one of the tables.

| Activity | Illumination (lux) |
|-|-|
| Working areas where visual tasks are only occasionally performed | 100-150 |
| Warehouses, Homes, Theaters, Archives | 150 |
| Easy Office Work, Classes | 250 |

We tested the Loomo in three locations, 2 well trafficked hallways (103 lux & 214 lux) and by a west facing window in the afternoon sun (2157 lux). So these are not non-standard low light conditions.

![light-meter-location-1](day1/light-meter-location-1.png)
![light-meter-location-2](day2-3/light-meter-location-2.png)
![light-meter-location-3](day5/light-meter-location-3.png)

## Cognitive Services requirements

According to the [documentation](https://docs.microsoft.com/en-us/azure/cognitive-services/face/concepts/face-detection), faces need to be 36x36 to 4096x4096 do be detected. Additionaly, for [recognition](https://westus.dev.cognitive.microsoft.com/docs/services/563879b61984550e40cbbe8d/operations/563879b61984550f30395239) faces should be 200x200 or bigger.

## Conclusions

Given the above for a scenario where Loomo captures faces of occupants and recognizes them, we believe the captures would look something like this.

![face](day2-3/27a2573f-31a3-d06a-ed5d-0b041d78cf98.jpg)

Where the rectangle of the face is 52x52. To get a face of atleast 200x200, the overall resolution would need to be 12554x9416 or 118 Megapixel more than double the current maximum. Not only this but the camera would need to be able to capture at faster shutter speeds in light as low as 100 lux. This seems impractical but there are a few things we can recommend from our learning. 

- The DTS ```PlannerPersonTracking``` was far more accurate with the addition of the 215 lumen LED lamp, we recommend adding a builtin lamp for Loomo to use.
- A higher quality camera with faster response will greatly improve the results.
- Cognitive Services is not ready for this type of scenario and a different model should be used.
- Alternately, a whole different approach might be to have Loomo scan for employee badges when people are detected and snap pictures when people without badges are spotted.

**NOTE:** *All images were post processed to reduce size and add faces rectangles using one of the following imagemagick commands.*

```bash
convert source.jpg -resize 640x640 dest.png
convert source.jpg -fill none -stroke red -strokewidth 3 -draw "rectangle <left>,<top>,<left+width>,<top+height>" -resize 320x320 dest.jpg
```

