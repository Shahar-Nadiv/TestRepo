#include <Servo.h>

Servo myservo1;
Servo myservo2;
Servo myservo3;

int potValx = A1;      // analog pin used to connect the joystick for X-axis
int potValy = A0;      // analog pin used to connect the joystick for Y-axis

int buttonYellowPin = 2;     // digital pin used to connect the Yellow button
int buttonBluePin = 4;
int buttonWhitePin = 7;

int valx;              // variable to read the value from the analog pin for X-axis
int valy;              // variable to read the value from the analog pin for Y-axis


void setup() {
  Serial.begin(9600);
  myservo1.attach(9);  // attaches the servo on pin 3 to the servo object
  myservo2.attach(5);
  myservo3.attach(6);
  pinMode(buttonYellowPin, INPUT);
  pinMode(buttonBluePin, INPUT); 
  pinMode(buttonWhitePin, INPUT); 
}

void loop() {
  
  if(Serial.read() == "boot"){
    bootProcess();
  }
  

  while(Serial.read() != "quit"){ // q stands for quit the 

    valx = analogRead(A0);
    valy = analogRead(A1);
    valx = map(valx, 0, 1023, 0, 170);
    valy = map(valy, 0, 1023, 170, 320);

    

    if(buttonYellowPin == HIGH){
    yellowButton();
    }

    if(buttonBluePin == HIGH){
    blueButton();
    }
  }

  if(Serial.read() == "rotate"){
    blueButton();
  }
  if(Serial.read() == "tilt"){
    yellowButton();
  }

  

 
}


void bootProcess() { // 3 up -> down
  goTo(0,0,0);
  delay(100);
  goTo(45,45,45);
  delay(500);
  goTo(0,0,0);
  delay(100);
  goTo(45,45,45);
  delay(500);
  goTo(0,0,0);
  delay(100);
  goTo(45,45,45);
  delay(500);
}

void yellowButton() { // Each servo goes 45 Degress in turn
  myservo1.write(0);
  myservo2.write(45);
  myservo3.write(45);
  delay(100);
  goTo(45,45,45);
  delay(500);
  myservo1.write(45);
  myservo2.write(0);
  myservo3.write(45);
  delay(100);
  goTo(45,45,45);
  delay(500);
  myservo1.write(45);
  myservo2.write(45);
  myservo3.write(0);
  delay(100);
  goTo(45,45,45);
  delay(500);
}

void blueButton() { // Circular Motion
  myservo1.write(0);
  delay(100);
  myservo1.write(45);
  delay(50);
  myservo2.write(0);
  delay(100);
  myservo2.write(45);
  delay(50);
  myservo3.write(0);
  delay(100);
  myservo3.write(45);
  delay(50);
}

void goTo(int servo1, int servo2, int servo3){ // Point all three servos to same angle;
  myservo1.write(servo1);
  myservo2.write(servo2);
  myservo3.write(servo3);
}