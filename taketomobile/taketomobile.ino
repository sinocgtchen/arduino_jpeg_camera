//Author : Dennis Chen 
//email : sinocgtchen@gmail.com
// This is a basic snapshot sketch using the VC0706 library.
// descriptions:  receives control code from android phone via bluetooth
//                 code 'a': image size: 320x240
//                 code 'b' : image size: 640x480
#include <Adafruit_VC0706.h>
//#include <SD.h>

// comment out this line if using Arduino V23 or earlier
#include <SoftwareSerial.h>         

// SD card chip select line varies among boards/shields:
// Adafruit SD shields and modules: pin 10
// Arduino Ethernet shield: pin 4
// Sparkfun SD shield: pin 8
// Arduino Mega w/hardware SPI: pin 53
// Teensy 2.0: pin 0
// Teensy++ 2.0: pin 20
#define chipSelect 10


// On Uno: camera TX connected to pin 2, camera RX to pin 3:
SoftwareSerial cameraconnection = SoftwareSerial(2, 3);

Adafruit_VC0706 cam = Adafruit_VC0706(&cameraconnection);

// Using hardware serial on Mega: camera TX conn. to RX1,
// camera RX to TX1, no SoftwareSerial object is required:
//Adafruit_VC0706 cam = Adafruit_VC0706(&Serial1);
int i;
char val;

void setup() {

  Serial.begin(57600);
  Serial.println("VC0706 Camera snapshot test");
  
  // see if the card is present and can be initialized:
//  if (!SD.begin(chipSelect)) {
//    Serial.println("Card failed, or not present");
//    // don't do anything more:
//    return;
//  }  
  
  // Set the picture size - you can choose one of 640x480, 320x240 or 160x120 mode
  // Remember that bigger pictures take longer to transmit!

   pinMode(13,OUTPUT);
   digitalWrite(13, HIGH);
}

void loop()
{
  if(Serial.available())
  {
    val = Serial.read();
    switch(val)
    {
      case 'a':    // image size: 320x240
            {
             digitalWrite(13, LOW);      
             // Try to locate the camera
             cam.begin();
               delay(500);
               cam.setDownsize(VC0706_320x240);
               //cam.setImageSize(VC0706_640x480);        // biggest
              //cam.setImageSize(VC0706_320x240);        // medium
              //cam.setImageSize(VC0706_160x120);          // small
          
 //              Serial.println("Snap in 3 secs...");
               delay(3000);
         
               cam.takePicture();
               // Get the size of the image (frame) taken  
               uint16_t jpglen = cam.frameLength();
               while (jpglen > 0) {
                  // read 32 bytes at a time;
                  uint8_t *buffer;
                  uint8_t bytesToRead = min(32, jpglen); // change 32 to 64 for a speedup but may not work with all setups!
                  buffer = cam.readPicture(bytesToRead);
//                  imgFile.write(buffer, bytesToRead);
                  for(i=0; i<bytesToRead;i++)
                       Serial.write(buffer[i]);
                       
                  jpglen -= bytesToRead;
                }
              digitalWrite(13, HIGH); 
            }  
              break;
      case 'b':            // image size: 640x480
             { digitalWrite(13, LOW);      
             // Try to locate the camera
             cam.begin();
               delay(500);
               cam.setDownsize(VC0706_640x480);
//              Serial.println("Snap in 2 secs...");
               delay(2000);
         
               cam.takePicture();
               // Get the size of the image (frame) taken  
               uint16_t jpglen = cam.frameLength();
               while (jpglen > 0) {
                  // read 32 bytes at a time;
                  uint8_t *buffer;
                  uint8_t bytesToRead = min(32, jpglen); // change 32 to 64 for a speedup but may not work with all setups!
                  buffer = cam.readPicture(bytesToRead);
//                  imgFile.write(buffer, bytesToRead);
                  for(i=0; i<bytesToRead;i++)
                       Serial.write(buffer[i]);
                       
                  jpglen -= bytesToRead;
                }

                digitalWrite(13, HIGH);
             }
      }
      
  }
  
}

