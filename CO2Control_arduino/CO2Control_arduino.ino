/* Authors: Andriy Fujii
 *          Luiz Zimmermann
 */

#include <WiFi.h>
#include <FirebaseESP32.h>
 
//Firebase and wifi conection configs
#define FIREBASE_HOST "https://co2control-54e55-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "1WpG8rPV25tZtav5gaTmUsCcDK7HznpwcuKP4f2D"
#define WIFI_SSID "TP-Link_3D4B"
#define WIFI_PASSWORD "26071404"

//Pins definition
#define pinPIR 23
#define pinFan 21
#define pinWindow 22
#define pinBuzzer 18
#define pinGas_digital 19
#define pinGas_analog 34

 
//Define FirebaseESP32 data object
FirebaseData firebaseData;
FirebaseJson json;

//Control variables
int automatic_mode = 0;
int co2_limit = 0;
int presence = 0;
int co2_level = 0;


void autonomus_mode(){

    Serial.println("Automatic mode activaded");
    
    Serial.print("Presence: ");
    Serial.println(presence);
    Serial.print("CO2: ");
    Serial.println(co2_level);
    Serial.print("CO2 limit: ");
    Serial.println(co2_limit);
    
    if(presence == 1 and co2_level >= co2_limit){

      int fan, win = 0;
      if(Firebase.getBool(firebaseData, "SensorConfigs/1/boolFan")){
      
        Serial.print("Fan auto: ");
        fan = firebaseData.boolData();
        Serial.println(fan);
    
      }else{
        //Failed, get the error reason from firebaseData
        Serial.print("Error to retrieve information of automatic mode!!:  ");
        Serial.println(firebaseData.errorReason());
      }

      if(Firebase.getBool(firebaseData, "SensorConfigs/1/boolWindow")){
      
        Serial.print("Window auto: ");
        win = firebaseData.boolData();
        Serial.println(win);
    
      }else{
        //Failed, get the error reason from firebaseData
        Serial.print("Error to retrieve information of automatic mode!!:  ");
        Serial.println(firebaseData.errorReason());
      }
      
      Serial.println("Limit reach, actuators activated");
      
      //Activaded in LOW
      if (fan == 1){
        digitalWrite(pinFan, LOW);
      }else{
        digitalWrite(pinFan, HIGH);
      }
      
      //Activaded in LOW
      if(win == 1){
        digitalWrite(pinWindow, LOW);
      }else{
        digitalWrite(pinWindow, HIGH);
      }
      //Actuators activaded
      digitalWrite(pinBuzzer, HIGH);
             
    }else{
       Serial.println("Under limit, actuators deactivated");
       //Actuators desactivaded
       digitalWrite(pinFan, HIGH);
       digitalWrite(pinWindow, HIGH);
       digitalWrite(pinBuzzer, LOW); 
    }
}


void check_system(){
    Serial.println("-----------------------------------------------");
    //------------------------------------------------------------------
    //Retrive automode status
    if(Firebase.getBool(firebaseData, "SensorConfigs/1/boolAutomatic")){
      
      Serial.print("Auto mode: ");
      automatic_mode = firebaseData.boolData();
      Serial.println(automatic_mode);
    
    }else{
      //Failed, get the error reason from firebaseData
      Serial.print("Error to retrieve information of automatic mode!!:  ");
      Serial.println(firebaseData.errorReason());
    }
    //------------------------------------------------------------------

    //------------------------------------------------------------------
    //Retrieve co2 limit data
    if(Firebase.getInt(firebaseData, "SensorConfigs/1/co2Threshold")){
      
      Serial.print("Co2 limit: ");
      co2_limit = firebaseData.intData();
      Serial.println(co2_limit);
    
    
    }else{
      //Failed, get the error reason from firebaseData
      Serial.print("Error to retrieve information of automatic mode!!:  ");
      Serial.println(firebaseData.errorReason());
    }
    //------------------------------------------------------------------

    //------------------------------------------------------------------
    //Set presence status
    if(Firebase.setInt(firebaseData, "SensorValues/1/boolPresence", presence)){
      //Success
      //Serial.println("Set presence data success");
    }else{
      //Failed, get the error reason from firebaseData
      
      Serial.print("Error in setInt presence: ");
      Serial.println(firebaseData.errorReason());
    }
    
    //------------------------------------------------------------------
    
    
    //------------------------------------------------------------------
    //Set co2 value
    co2_level = analogRead(pinGas_analog);
    if(Firebase.setInt(firebaseData, "SensorValues/1/co2Value", co2_level)){
      //Success
      //Serial.println("Set co2_level data success");
    }else{
      //Failed, get the error reason from firebaseData
      
      Serial.print("Error in co2_level sent: ");
      Serial.println(firebaseData.errorReason());
    }
    //------------------------------------------------------------------

    //------------------------------------------------------------------
    //Check auto mode status
    if(automatic_mode == 1){
      autonomus_mode();
    }else{
      int fan, win = 0;
      
      //Retriving information about actuators
      if(Firebase.getBool(firebaseData, "SensorConfigs/1/boolFan")){
      
        Serial.print("Fan: ");
        fan = firebaseData.boolData();
        Serial.println(fan);
    
      }else{
        //Failed, get the error reason from firebaseData
        Serial.print("Error to retrieve information of automatic mode!!:  ");
        Serial.println(firebaseData.errorReason());
      }

      if(Firebase.getBool(firebaseData, "SensorConfigs/1/boolWindow")){
      
        Serial.print("Window: ");
        win = firebaseData.boolData();
        Serial.println(win);
    
      }else{
        //Failed, get the error reason from firebaseData
        Serial.print("Error to retrieve information of automatic mode!!:  ");
        Serial.println(firebaseData.errorReason());
      }
       
      //On/off actuators
      if (fan == 1){
        digitalWrite(pinFan, LOW);
      }else{
        digitalWrite(pinFan, HIGH);
      }

      if(win == 1){
        digitalWrite(pinWindow, LOW);
      }else{
        digitalWrite(pinWindow, HIGH);
      }
      
    }
    //------------------------------------------------------------------
       
}

//Function to be called for the interruption
void IRAM_ATTR sensor_movimento(){

    Serial.println("Presença: ");
    Serial.println(digitalRead(pinPIR));
    presence = digitalRead(pinPIR);
    
}

//Initial configs
void setup()
{
 
    Serial.begin(115200);

    //Trying to connect to wifi
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to Wi-Fi");
    while (WiFi.status() != WL_CONNECTED)
    {
      Serial.print(".");
      delay(300);
    }
    Serial.println();
    Serial.print("Connected with IP: ");
    Serial.println(WiFi.localIP());
    Serial.println();

    //Firebase configs
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
    Firebase.reconnectWiFi(true);
    //Set database read timeout to 1 minute (max 15 minutes)
    Firebase.setReadTimeout(firebaseData, 1000 * 60);
    //tiny, small, medium, large and unlimited.
    //Size and its write timeout e.g. tiny (1s), small (10s), medium (30s) and large (60s).
    Firebase.setwriteSizeLimit(firebaseData, "tiny");

    //Pins modes
    pinMode(pinPIR, INPUT_PULLUP);
    pinMode(pinFan, OUTPUT);
    pinMode(pinWindow, OUTPUT);
    pinMode(pinBuzzer, OUTPUT);
    pinMode(pinGas_digital, INPUT);
    pinMode(pinGas_analog, INPUT);

    //Pins set to low
    //Active in low
    digitalWrite(pinFan, HIGH);
    digitalWrite(pinWindow, HIGH);
    //Active in high
    digitalWrite(pinBuzzer, LOW);

    //Attach a fuction to an interruption activaded for change on the pinPIR port 
    attachInterrupt(digitalPinToInterrupt(pinPIR), sensor_movimento, CHANGE);
   
    /*
    This option allows get and delete functions (PUT and DELETE HTTP requests) works for device connected behind the
    Firewall that allows only GET and POST requests.
    
    Firebase.enableClassicRequest(firebaseData, true);
    */  
   
    Serial.println("------------------------------------");
    Serial.println("Connected...");
  
}

int refreshTime = 5000;

void loop()
{
   
   check_system();
   delay(refreshTime);
  
}
