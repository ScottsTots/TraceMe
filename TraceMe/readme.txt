Scott's Tots TraceMe Writeup

1. Team Members and UTIDs

Name: Matthew Ebeweber
EID : mpe295

Name: Niko Lazaris
EID : 

Name: Aaron Villalpando
EID:


2. Brief Instructions on How to Use TraceMe
  1) After logging in the user is presented with a login screen
  2) Register a new account, login (sample login provided below) with username/password or use the FB/Twitter Auth
  3) Once Logged in the User can access the Menu Items
    1) About Goes to a Page About the Project
    2) High Scores takes the user to the current high scores
  4) Click New Game to Play a Game
  5) Select Single Player (only single player supported)
  6) Begin touching the screen whenever and trace
  7) After done tracing select play (don't select save trace)
  8) Hit back because you are done!


3. List of Features / User Cases Completed
  - Login / Signup Activities Completed
  - Highscore Logic Completed
  - About Page Logic Completed 
  - Game Part Setup
    - Display trace on screen
    - Allow user to trace
    - Give the user a score
    - Signle Player Mode Only
    - Drawback of Trace


4. List of Features / Use Cases from Application Pototype Not Completed
  - Multiplayer
    - Finding other players random and friends
    - Double playback
    - Menu Items not there (no multiplayer)
    - Async games
  - Highscore Styling
  - About Styling
    

5. List of Features / User Cases Added not Part of Prototype
  - None?


6. List of Classes and Major Chunks of Code Obtained from Other Sources
  - Parse : https://parse.com/docs/android_guide
  - Parse Android Anyway Tutorial : https://parse.com/tutorials/anywall-android
  - Android Finger Painting SDK : 
  - GSON Library


7. List of Classes and Major Chunks of Code Completed Yourself
  - scotts.tots.traceme
    - DispatchActivity
    - LoginMenuActivity
    - LoginNewUserActivity
    - MainScreen
    - SignUpActivity
    - Splash
    - TraceMeApplication
  - helperClasses
    - CustomPath
    - DataPOint
    - Game
    - Level
    - ScoreManager
    - TraceFile
  - gamescreens
    - AboutFrag
    - DrawingBoard
    - HighScoreFragment
      - CustomHighScoreListAdapter
      - Score
    - HomeScreenFrag
    - ViewingBoard

** Note: Included in the a4_apk_readme_TraceMe.txt file, but a sample login for use is:
  Username: foobar
  Password: foobar
