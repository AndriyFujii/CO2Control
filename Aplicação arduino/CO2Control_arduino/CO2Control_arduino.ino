/*
 * Firebase ESP32
 * https://www.electroniclinic.com/
 */
 
#include <WiFi.h>
#include <FirebaseESP32.h>
 
 
#define FIREBASE_HOST "https://co2control-54e55-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "1WpG8rPV25tZtav5gaTmUsCcDK7HznpwcuKP4f2D"
#define WIFI_SSID "TP-Link_3D4B"
#define WIFI_PASSWORD "26071404"
 
 
//Define FirebaseESP32 data object
FirebaseData firebaseData;
FirebaseJson json;

//Variaveis de controle
int automatic_mode = 0;
int co2_limit = 300.0;


void setup()
{
 
  Serial.begin(115200);
 
 
 
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
 
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
 
  //Set database read timeout to 1 minute (max 15 minutes)
  Firebase.setReadTimeout(firebaseData, 1000 * 60);
  //tiny, small, medium, large and unlimited.
  //Size and its write timeout e.g. tiny (1s), small (10s), medium (30s) and large (60s).
  Firebase.setwriteSizeLimit(firebaseData, "tiny");
 
  /*
  This option allows get and delete functions (PUT and DELETE HTTP requests) works for device connected behind the
  Firewall that allows only GET and POST requests.
  
  Firebase.enableClassicRequest(firebaseData, true);
  */
 
  //String path = "/data";
  
 
  Serial.println("------------------------------------");
  Serial.println("Connected...");
  
}

void autonomus_mode(){

      Serial.println("Modo automático ativado");
      //verificar co2
      while (Serial.available () == 0)
      {}
      Serial.print("valor co2: ");
      float co2_level = Serial.parseFloat();
      if(co2_level != 0.0){
                if(Firebase.setFloat(firebaseData, "SensorValues/1/co2Value", co2_level)){
        //Success
        //Serial.println("Set int data success");

      }else{
        //Failed?, get the error reason from firebaseData
    
        Serial.print("Error in setfloat, ");
        Serial.println(firebaseData.errorReason());
      }
      }

      
    //verificar presença
    while (Serial.available () == 0)
    {}
    Serial.print("valor presenca: ");
    float presence = Serial.parseFloat();
    if(presence != 0.0){
          if(Firebase.setFloat(firebaseData, "SensorValues/1/testPresence", presence)){
            //Success
            //Serial.println("Set int data success");
      
          }else{
            //Failed?, get the error reason from firebaseData
        
            Serial.print("Error in setfloat, ");
            Serial.println(firebaseData.errorReason());
          }
    }

     //ativar buzzer e envia notificação
    Serial.println("--------------------");
    Serial.println(presence);
    Serial.println(co2_level);
    if(presence == 2.0 and co2_level >= co2_limit){
       Serial.print("Limite atingido, ativando sistema");
    }
    Serial.println("--------------------");
    
   
}

void check_system(){

   //Recupera informação se está no modo automático
   if(Firebase.getBool(firebaseData, "SensorValues/1/boolPresence")){
      Serial.print("sensor: ");
      Serial.println(firebaseData.boolData());
      
      automatic_mode = firebaseData.boolData();

   }else{
      //Failed?, get the error reason from firebaseData

      Serial.print("Error in sensor, ");
      Serial.println(firebaseData.errorReason());
   }

   //Verifica se está no modo automático
   if(automatic_mode == 1){
      autonomus_mode();
   }else{

    
      //verificar co2
      while (Serial.available () == 0)
      {}
      Serial.print("valor: ");
      float valor = Serial.parseFloat();
      if(valor != 0.0){
                if(Firebase.setFloat(firebaseData, "SensorValues/1/co2Value", valor)){
        //Success
        //Serial.println("Set int data success");

      }else{
        //Failed?, get the error reason from firebaseData
    
        Serial.print("Error in setfloat, ");
        Serial.println(firebaseData.errorReason());
      }
      }

      //verificar presença
      
      
      //enviar alarme
      

   }
  
}

 
void loop()
{
   //int Sdata = 200;
   //Serial.println(Sdata); 
   delay(1000); 
   
   //json.set("/co2Value", Sdata);
   //Firebase.updateNode(firebaseData,"/SensorValues/2",json);
   check_system();
 
}
