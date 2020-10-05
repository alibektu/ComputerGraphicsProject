# Computer Graphics Final Project Overview

* The goal of this project is to make better step tracking for VR application by using embedded sensor and mimic the [**Matrix movie**](https://en.wikipedia.org/wiki/The_Matrix_(franchise)).

* What I Implemented to Achieve the Goal 
    * Classifying human activities like walking, running, jump (partially done)

      * Walking:

      <img src="https://github.com/alibektu/ComputerGraphicsProject/blob/main/walking.png" width="400">

      * Running:

      <img src="https://github.com/alibektu/ComputerGraphicsProject/blob/main/running.png" width="400">

      * Jumping:

      <img src="https://github.com/alibektu/ComputerGraphicsProject/blob/main/jump.png" width="400">


# Virtual Step Tracking

<img src="https://github.com/alibektu/ComputerGraphicsProject/blob/main/diagram.png" width="700">

# Virtual Environment

<img src="https://github.com/alibektu/ComputerGraphicsProject/blob/main/vr_env.png" width="700">

# Implementation Detail

* Android Phone
  * Rendering Virtual Environment
  * Moving head position when you receive package from Server
  * Handling received packages 
* Server
  * Receive and Send
  * Classification according to the graph
* Sensor Phone
  * Sending sensor values (when its get more than Thold)
