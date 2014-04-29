Scott's Tots TraceMe Writeup

1. Team Members and UTIDs

Name: Matthew Ebeweber
EID : mpe295

Name: Niko Lazaris
EID : njl433

Name: Aaron Villalpando
EID: av9844


2. Brief Instructions on How to Use TraceMe
  1) After logging in the user is presented with a login screen
  2) Register a new account, login (sample login provided below) with username/password or use the FB/Twitter Auth
  3) Once Logged in the User can access the Menu Items
    1) About Goes to a Page About the Project
    2) High Scores takes the user to the current high scores
  4) Click New Game to Play a Game
  5) Select Single Player or Multiplayer
  6) Begin touching the screen whenever and trace
  7) After done tracing select play (don't select save trace)
  8) Hit back because you are done!

  Note: You can also challenge users by username and perform actions on each of the items

** Note: Facebook won't work because you have to explicitly be approved by us to log in to the app with facebook. You can ask us for permission. If you want facebook login to work for all users you have to go through a facebook approval process.


3. List of Features / User Cases Completed
  - Login / Signup Activities Completed
  - Highscore Logic Completed
  - About Page Logic Completed 
  - Game Part Setup
    - Display trace on screen
    - Allow user to trace
    - Give the user a score
    - Drawback of Trace
  - Multiplayer game matching
  - Update of list items
  - Two player play back
  - Scoring between users
  - Push notifications
  - Go to settings to style and change things


4. List of Features / Use Cases from Application Pototype Not Completed
  - Levels
    

5. List of Features / User Cases Added not Part of Prototype
  - None?


6. List of Classes and Major Chunks of Code Obtained from Other Sources
  - Parse : https://parse.com/docs/android_guide
  - Parse Android Anyway Tutorial : https://parse.com/tutorials/anywall-android
	- Specifically, a few of the validation errors/flow and ParseUser Sign In in SignUpActivity.java and LoginNewUserActivity.java
	- Also the initial Parse API set up in DispatchActivity.java and TraceMeApplication.java
  - Android Finger Painting SDK : Comes with the SDK but here's another way to view
	the code http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android-apps/2.2_r1.1/com/example/android/apis/graphics/FingerPaint.java
  - GSON Library, used to save java objects as JSON strings to saved preferences and external storage
  - Android SDK Navigation Drawer Example: http://developer.android.com/training/implementing-navigation/nav-drawer.html
	MainScreen.java was based off of this example.
  - PrettyTime for time formatting
  - PullToRefresh - https://github.com/chrisbanes/ActionBar-PullToRefresh

7. List of Classes and Major Chunks of Code Completed Yourself
  - scotts.tots.traceme
    - LoginMenuActivity
    - LoginNewUserActivity
    - SignUpActivity
    - Splash
  - helperClasses
    - CustomPath
    - DataPoint
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
