// lightSensor.h
#ifndef LIGHTSENSOR_H
#define LIGHTSENSOR_H
enum {  kGetSensorReadingID = 0, // getSensorReading(int *, int *)
		kGetLEDBrightnessID = 1, // getLEDBrightness(int, int *)
		kSetLEDBrightnessID = 2, // setLEDBrightness(int, int, int *)
		kSetLEDFadeID = 3, // setLEDFade(int, int, int, int *)
};
#endif 
