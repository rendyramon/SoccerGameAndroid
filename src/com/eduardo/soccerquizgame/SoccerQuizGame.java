//SoccerQuizGame
//Main Activity
// Copyright Eduardo Romeiro

package com.eduardo.soccerquizgame;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder; 
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class SoccerQuizGame extends Activity {
    
    //String used when logging error messages
    private static final String TAG = "SoccerQuizGame Activity";
    
    //Instance Variables
    private List<String> fileNameList; // player file names
    private List<String> quizPlayersList; // names of players in quiz
    private String correctAnswer; // current correct answer
    private int totalGuesses; // number of guesses
    private int correctAnswers; // number of correct guesses
    private int guessRows; // number of rows displaying choices
    private Random random; // random number generator
    private Handler handler; // used to delay loading of next player
    private Animation shakeAnimation; // animation for incorrect answers
    
    private TextView answerTextView;
    private TextView questionNumberTextView;
    private ImageView faceImageView;
    private TableLayout buttonTableLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    

   
    fileNameList = new ArrayList<String>(); // list of image file names
    quizPlayersList =  new ArrayList<String>(); // players in quiz
    guessRows = 3; // defaulted to one row of choices
    random = new Random(); // initialize the random number generator
    handler = new Handler(); // used to perform delayed operations
    
    // get references to the GUI components
    questionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
    answerTextView = (TextView) findViewById(R.id.answerTextView);
    faceImageView = (ImageView) findViewById(R.id.faceImageView);
    buttonTableLayout = (TableLayout) findViewById(R.id.buttonTableLayout);
    
    // set questionNumbers Text
    questionNumberTextView.setText(
            getResources().getString(R.string.question) + " 1 " +
            getResources().getString(R.string.of) + " 10");
    
    
    // load the shake animations used to animate incorrect answers
    shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.incorrect_shake);
    shakeAnimation.setRepeatCount(3); // animation repeats 3 times
   
    // start a new quiz
    resetQuiz();
    
  } //end onCreate method
    
    // set up and start the next quiz
    private void resetQuiz(){
        
    	// use the AssetManager to get the player image
    	// file names for the app
    	AssetManager assets = getAssets();
    	fileNameList.clear(); // clear the list

    	// get list of all player names in this region
    	String[] paths = null;
    	try {
    		paths = assets.list("Players");
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		Log.e(TAG, "Error loading ", e);
    	}

    	for(String path : paths)
    		fileNameList.add(path.replace(".jpg", ""));
                
       
        
        correctAnswers = 0; // reset number of correct answers
        totalGuesses= 0; // reset number of guesses
        quizPlayersList.clear(); // clear prior list of quiz countries
        
        // add 10 random file names to the quiz list
        int playerCounter = 1;
        int numberOfPlayers = fileNameList.size();
        
        while(playerCounter <= 10){
            
            int randomIndex = random.nextInt(numberOfPlayers);
            
            //get random file name
            String fileName = fileNameList.get(randomIndex);
            
            //if region is enabled and hasnt been chosen
            if(!quizPlayersList.contains(fileName)){
                quizPlayersList.add(fileName);
                ++playerCounter;
            }
        }
        
        loadNextPlayer(); //start quiz by loading next player
        
        
    }
    
    // after user guesses a correct player, load the next one
    private void loadNextPlayer(){
        
        //get the filename of the next flag and remove it from the list
        String nextImageName = quizPlayersList.remove(0);
        correctAnswer = nextImageName; //update correct answer
        
        answerTextView.setText(""); //clear the answerTextView
        
        //display the number of the current question in the quiz
        questionNumberTextView.setText(
                getResources().getString(R.string.question) + " " +
                (correctAnswers + 1) + " " +
                getResources().getString(R.string.of) + " 10");
        
        //extract the region from the next images name
        String region = "Players"; 
        
        //use AssetManager to load next image from assets folder
        AssetManager assets = getAssets(); // get apps Asset Manager
        InputStream stream; // used to read in player names
        
        try{
            
            //get an InputStream to the asset representing the next flag
            stream = assets.open(region + "/" + nextImageName + ".jpg");
            
            //load the asset as Drawable and display on the flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImageName);
            faceImageView.setImageDrawable(flag);
         }
        catch (IOException e){
            Log.e(TAG, "Error loading " + nextImageName, e);
        }
        
        //clear prior answer buttons from tablerows
        for (int row = 0; row < buttonTableLayout.getChildCount(); row++)
             ((TableRow) buttonTableLayout.getChildAt(row)).removeAllViews();
        
        Collections.shuffle(fileNameList); //shuffle file names
        
        //put the correct answer at the end of the fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));
        
        //get a reference to the LayoutInflator Service
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        // add 3, 6, or 9 answer Buttons based on the value of guessRows
        for (int row = 0; row < guessRows; row++){
            
            TableRow currentTableRow = getTableRow(row);
            
            //place Buttons in currentTableRow
            for (int column = 0; column < 3; column++){
                
                //inflate guess_button.xml to create new Button
                Button newGuessButton = 
                        (Button) inflater.inflate(R.layout.guess_button, null);
                
                //get player name and set it as newGuessButtons text
                String fileName = fileNameList.get((row * 3) + column);
                newGuessButton.setText(getPlayerName(fileName));
                
                //register answerButton listener to respond to clicks
                newGuessButton.setOnClickListener(guessButtonListener);
                currentTableRow.addView(newGuessButton);
            }
                
        }
        
        //randomly replace one Button with the correct answer
        int row = random.nextInt(guessRows);
        int column = random.nextInt(3);
        TableRow randomTableRow = getTableRow(row);
        String playerName = getPlayerName(correctAnswer);
        ((Button) randomTableRow.getChildAt(column)).setText(playerName);
        
    } // end loadNextPlayer method
    
     
    // return the specified TableRow
    private TableRow getTableRow(int row){
        
        return (TableRow) buttonTableLayout.getChildAt(row);
    }
    
    
    // parses the player file name and returns the player name
    private String getPlayerName(String name){
        
        return name.substring(name.indexOf('-') + 1).replace('-', ' ');
    }
    
    
    // method submitGuess called when user selects an answer
    private void submitGuess (Button guessButton){
        
        String guess = guessButton.getText().toString();
        String answer = getPlayerName(correctAnswer);
        ++totalGuesses; //increment the number of guesses made
        
        if (guess.equals(answer)){
            
            ++correctAnswers; // increment number of correct answers
            
            //display Correct answer in answerTextView 
            answerTextView.setText(answer + "!" );
            answerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
            
            disableButtons(); //disable all answer Buttons
            
            // if user has guessed 10 correct players
            if (correctAnswers == 10){
                
                //create new AlertDialog Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.reset_quiz);
                
                //set the AlertDialogs message to display the game results
                builder.setMessage(String.format("%d %s, %.02f%% %s", totalGuesses,
                        getResources().getString(R.string.guesses),
                        (1000 / (double) totalGuesses),
                        getResources().getString(R.string.correct)));
                
                builder.setCancelable(false);
                
                //add reset quiz button
                builder.setPositiveButton(R.string.reset_quiz,
                        new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                
                                resetQuiz();
                            } // end onClick
                        } // end anonymous inner class
                ); //end call to setPositiveButton
                
                // create AlertDialog from the Builder
                AlertDialog resetDialog = builder.create();
                resetDialog.show();
            
            } // end if
            
            else // answer is correct but game isnt over
            {
                //load the next flag after a one second delay
                handler.postDelayed(
                        new Runnable()
                        {
                            @Override
                            public void run(){
                                loadNextPlayer();
                            }
                        }, 1000); // 1000 milliseconds for 1 second delay
                
                
            } // end else
        } // end if
        
        else // answer was incorrect
        {
            //play the animation
            faceImageView.startAnimation(shakeAnimation);
            
            //display "Incorrect" in red
            answerTextView.setText(R.string.incorrect_answer);
            answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
            
            guessButton.setEnabled(false); // disable the incorrect answer
        } 
        
    } // end submitGuess method
    
    
    // method to disable all answer Buttons
    private void disableButtons(){
        
        for (int row = 0; row < buttonTableLayout.getChildCount(); row++){
            TableRow tablerow = (TableRow) buttonTableLayout.getChildAt(row);
            
            for(int i = 0; i < tablerow.getChildCount(); i++){
                tablerow.getChildAt(i).setEnabled(false);
            }
        }
    }
    
    
    // create constants for each menu id
    private final int CHOICES_MENU_ID = Menu.FIRST;
    

    // called when the user accesses the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

    	super.onCreateOptionsMenu(menu);

    	// add options to the menu
    	menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.choices);


    	return true; // display the menu
    }

    // called when the user selects an option from the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	// switch the menu id of the user selected option
    	switch (item.getItemId())
    	{
    	case CHOICES_MENU_ID:
    		//create a list of the possible number of answer choices
    		final String[] possibleChoices = getResources().getStringArray(R.array.guessesList);

    		//create an AlertDialog Builder and set its title
    		AlertDialog.Builder choicesBuilder = new AlertDialog.Builder(this);
    		choicesBuilder.setTitle(R.string.choices);

    		//add possibleChoices items to the Dialog and set the
    		// behavior when one of the items is clicked
    		choicesBuilder.setItems(R.array.guessesList,
    				new DialogInterface.OnClickListener() {

    			@Override
    			public void onClick(DialogInterface dialog, int item) {

    				// update guessRows to reflect user choice
    				guessRows = Integer.parseInt(possibleChoices[item].toString()) / 3;

    				resetQuiz();

    			}
    		});

    		// create AlertDialog from the Builder
    		AlertDialog choicesDialog = choicesBuilder.create();
    		choicesDialog.show();

    		break;
    	} // end switch

    	return super.onOptionsItemSelected(item); 

    }// end method onOptionsItemSelected


    // called when a guess Button is touched
    private OnClickListener guessButtonListener = new OnClickListener(){

    	@Override
    	public void onClick(View v){
    		submitGuess((Button) v); // pass selected Button to submitGuess method
    	}

    };


} // end SoccerQuizGame
