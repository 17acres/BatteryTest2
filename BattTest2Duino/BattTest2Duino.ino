//Digital Outputs
#define RCENABLE 2
#define LOADENABLE 3

//VSense Parameters
#define VSENSE 7

//ISense Parameters
#define ISENSE 6

bool oldLoadEnable=0;
bool oldFilterEnable=0;
long wdtTime=0;

void setup() {
  TCCR2B = (TCCR2B & 0b11111000) | 0x01;//2khz pwm
  analogReference(INTERNAL);//1v1 reference
  analogRead(VSENSE);analogRead(VSENSE);analogRead(VSENSE);analogRead(VSENSE);//Update ADC config
  pinMode(RCENABLE, OUTPUT);
  pinMode(LOADENABLE,OUTPUT);
  pinMode(LED_BUILTIN,OUTPUT);
  
  digitalWrite(RCENABLE,LOW);
  digitalWrite(LOADENABLE,LOW);
  digitalWrite(LED_BUILTIN,LOW);

  Serial.begin(250000);
}

void loop() {

  enableFilter(true);
  if(Serial.available()>0){
    String inputString=Serial.readStringUntil('\n');
    if(inputString.indexOf("OPENLOOP")>-1){
      feedWatchdog();
      enableLoad(true);
    }else{
      enableLoad(false);
    }
    
  }
  printStatus();
  checkWatchdog();
  delay(10);
}

void enableFilter(bool enable){
  static bool filterEnable;
  filterEnable=enable;
  if(filterEnable!=oldFilterEnable){
    digitalWrite(RCENABLE,enable);
    oldFilterEnable=filterEnable;
  }
}

void enableLoad(bool enable){
  static bool loadEnable;
  loadEnable=enable;
  if(loadEnable!=oldLoadEnable){
    digitalWrite(LOADENABLE,enable);
    digitalWrite(LED_BUILTIN,enable);
    oldLoadEnable=loadEnable;
  }
}
void printStatus(){
  char buf[18];
  sprintf(buf,"%04d %04d %01d %01d\n",analogRead(VSENSE),analogRead(ISENSE),oldFilterEnable,oldLoadEnable);
  Serial.print(buf);
}

void feedWatchdog(){
  wdtTime=millis();
}
void checkWatchdog(){
  if(millis()-wdtTime>300){
    Serial.println("Watchdog not fed");
    enableLoad(false);
  }
  
}
