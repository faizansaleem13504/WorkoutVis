# WorkoutVis
Android Application for fitness geeks to workout without any physical trainers. The app provides valuable feedback to the user regarding their workout by recording realtime video.

**Introduction:**
This repository contains the source code for the android application i.e.  WorkoutVis. We will highlight the major implementation details, frameworks used, and the installation methods within the Readme file.

**Implementation Details**
Following sections highlight the implementation details of the project
**Android Application:**

Andnroid application is developed using JAVA, Kotlin, XML. Java has been used for dynamic interaction of the client, Kotlin has beed used for the preprocessing of the realtime video and feed it to the deep learning model i.e. PoseNET using tensorflow and XML has been used for creating static front end of the application.

**Frameworks and APIs:**
Realtime Video:
**CameraX API ** has been used to record realtime workout and render it through multithreading. The API allows the frames to be processed in separate frames, therefore, increasing the efficiency of the application.

**PoseNET Integration:**
Deep learning model i.e. PoseNET has been integrated inside the mobile device using **TFLite**

**Installation**
The project can be cloned to local directory using following git command

git clone https://github.com/faizansaleem13504/WorkoutVis.git


**GUI:**

![image](https://user-images.githubusercontent.com/50497270/122133599-7af02580-ce56-11eb-965f-8e860b26d4b5.png)


![image](https://user-images.githubusercontent.com/50497270/122133654-922f1300-ce56-11eb-89b5-4da0c2a03aa4.png)


![image](https://user-images.githubusercontent.com/50497270/122133674-9ce9a800-ce56-11eb-931b-9b26bd3f2cd7.png)


**Conclusion:**
The project is open source for now and can be extended in multiple ways i.e. adding the feature of angle correction, installing smart mirrors etc.


