package com.zybooks.warproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.animation.Animator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    // Declare variables for card deck, players' decks, UI elements, and counters
    private ArrayList<Integer> deck;
    private ArrayList<Integer> topDeck, bottomDeck;
    private ImageView topCard, bottomCard;
    private TextView topCount, bottomCount, topWins, bottomWins;
    private Button dealBTN;
    private ImageView[] topWarCards;
    private ImageView[] bottomWarCards;

    private int topWinCount = 0;
    private int bottomWinCount = 0;
    private Toast currentToast;
    private final Handler handler = new Handler(); // Handler for delays
    private float centerX = 2f;
    private ImageView leftSword, rightSword;
    private float originalLeftSword, originalRightSword;

    private final ArrayList<Integer> cheatSequence = new ArrayList<>();
    private final int[] secretCodePlayer1 = {1, 1, 2, 2, 1, 2}; // Player 1 wins
    private final int[] secretCodePlayer2 = {2, 2, 1, 1, 2, 1}; // Player 2 wins
    private final int[] secretCodeWar = {1, 2, 1, 2, 1, 2}; // Initiates war


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        topCard = findViewById(R.id.top_card);
        bottomCard = findViewById(R.id.bottom_card);
        topCount = findViewById(R.id.top_card_count);
        bottomCount = findViewById(R.id.bottom_card_count);
        topWins = findViewById(R.id.top_card_wins);
        bottomWins = findViewById(R.id.bottom_card_wins);
        dealBTN = findViewById(R.id.dealBTN);
        leftSword = findViewById(R.id.left_sword);
        rightSword = findViewById(R.id.right_sword);

        // Creates arrays to hold extra cards displayed during a "war"
        topWarCards = new ImageView[]{
                findViewById(R.id.top_back1), findViewById(R.id.top_back2), findViewById(R.id.top_back3)
        };
        bottomWarCards = new ImageView[]{
                findViewById(R.id.bottom_back1), findViewById(R.id.bottom_back2), findViewById(R.id.bottom_back3)
        };

        // Initialize the game setup
        initializeGame();

        // Set up button click listener to play a round
        dealBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissToast(); // Dismiss previous toasts
                deal(); // Deal cards for a round
            }
        });

        // Set up click handlers for cheat sequence
        topCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCheatSequence(1);
            }
        });

        bottomCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCheatSequence(2);
            }
        });
    }

    private void initializeGame() {
        // Load the deck with card images
        loadDeck();

        // Shuffle and split deck between two players
        Collections.shuffle(deck);
        topDeck = new ArrayList<>(deck.subList(0, 26));
        bottomDeck = new ArrayList<>(deck.subList(26, 52));
        topCard.setImageResource(R.drawable.back);
        bottomCard.setImageResource(R.drawable.back);
        showWarCards(false);
        topWinCount = 0;
        bottomWinCount = 0;
        updateCounters(); // Update card counts
    }

    // Add references for each card drawable to deck based on suit and rank
    private void loadDeck() {
        deck = new ArrayList<>();
        String[] suits = {"clubs", "diamonds", "hearts", "spades"};
        for (String suit : suits) {
            for (int rank = 2; rank <= 14; rank++) {
                String cardName = suit + rank; // Construct card name
                int resId = getResources().getIdentifier(cardName, "drawable", getPackageName());
                deck.add(resId); // Add card to deck
            }
        }
    }

    //Sets animation for the cards to be dealt
    private void animateCard(ImageView card, float startY, float endY, long duration) {
        ObjectAnimator translateY = ObjectAnimator.ofFloat(card, "translationY", startY, endY);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 0.5f, 1f); // Scale from 50% to 100%
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 0.5f, 1f); // Scale from 50% to 100%
        ObjectAnimator rotation = ObjectAnimator.ofFloat(card, "rotation", 0f, 360f); // Optional rotation

        // Group animations into a set
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.playTogether(translateY, scaleX, scaleY, rotation);
        animationSet.setDuration(duration);
        animationSet.start();
    }

    private void deal() {
        // Hide extra war cards initially
        showWarCards(false);

        // Check if either deck is empty to determine if game is over
        if (topDeck.isEmpty() || bottomDeck.isEmpty()) {
            String winner = !topDeck.isEmpty() ? "Player 2 Wins the Game!" : "Player 1 Wins the Game!";
            showGameOverDialog(winner);
            dealBTN.setEnabled(false);
            return;
        }

        // Temporarily disable deal button during round processing
        dealBTN.setEnabled(false);

        // Draw the top card from each player's deck
        int card1 = topDeck.remove(0);
        int card2 = bottomDeck.remove(0);
        topCard.setImageResource(card1);
        bottomCard.setImageResource(card2);

        // Get screen height for animation
        float screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Animate the cards
        animateCard(topCard, -screenHeight, 0, 1000); // Fly in from the top
        animateCard(bottomCard, screenHeight, 0, 1000); // Fly in from the bottom

        // Add a delay before comparing the cards
        handler.postDelayed(() -> {
            // Get the numeric values and suit of each drawn card
            int card1Value = getCardValue(card1);
            int card2Value = getCardValue(card2);
            int card1Suit = getSuitValue(card1);
            int card2Suit = getSuitValue(card2);

            // Add values based on suits
            if (card1Suit == 1 && card2Suit == 2) card1Value++;
            else if (card1Suit == 2 && card2Suit == 3) card1Value++;
            else if (card1Suit == 3 && card2Suit == 4) card1Value++;
            else if (card1Suit == 4 && card2Suit == 1) card1Value++;
            else if (card2Suit == 1 && card1Suit == 2) card2Value++;
            else if (card2Suit == 2 && card1Suit == 3) card2Value++;
            else if (card2Suit == 3 && card1Suit == 4) card2Value++;
            else if (card2Suit == 4 && card1Suit == 1) card2Value++;

            // Compare the cards after the delay
            if (card1Value > card2Value) {
                showToast("Player 2 Wins this Round!");
                topDeck.add(card1);
                topDeck.add(card2);
                topWinCount++;
            } else if (card2Value > card1Value) {
                showToast("Player 1 Wins this Round!");
                bottomDeck.add(card1);
                bottomDeck.add(card2);
                bottomWinCount++;
            } else {
                // If tied, add a delay before initiating the war
                handler.postDelayed(() -> {
                    initiateWar(card1, card2);
                }, 1000); // Delay before initiating war (e.g., 1 second)
            }

            updateCounters();
            handler.postDelayed(() -> dealBTN.setEnabled(true), 1000); // Re-enable button after delay

        }, 1000); // Delay before comparing cards
    }

    //Sword animation for initiate war
    private AnimatorSet createSwordAnimation() {
        // Save the original positions of the swords
        originalLeftSword = leftSword.getTranslationX();
        originalRightSword = rightSword.getTranslationX();

        leftSword.setVisibility(View.VISIBLE);
        rightSword.setVisibility(View.VISIBLE);

        // Creates sword animation
        ObjectAnimator leftSwordAnim = ObjectAnimator.ofFloat(leftSword, "translationX", centerX);
        leftSwordAnim.setDuration(1000);
        ObjectAnimator rightSwordAnim = ObjectAnimator.ofFloat(rightSword, "translationX", centerX);
        rightSwordAnim.setDuration(1000);

        AnimatorSet swordAnimationSet = new AnimatorSet();
        swordAnimationSet.playTogether(leftSwordAnim, rightSwordAnim);

        // Adds an animation listener to reset the visibility after the animation ends
        swordAnimationSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //add flying animation on start
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                leftSword.setTranslationX(originalLeftSword);
                leftSword.setVisibility(View.GONE);
                rightSword.setTranslationX(originalRightSword);
                rightSword.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        return swordAnimationSet;
    }

    private void initiateWar(int card1, int card2) {
        // Create a "war pot" to hold the disputed cards
        ArrayList<Integer> warPot = new ArrayList<>();
        warPot.add(card1);
        warPot.add(card2);

        // Show face-down war cards and set images to card back
        showWarCards(true);
        topCard.setImageResource(R.drawable.back);
        bottomCard.setImageResource(R.drawable.back);

        // Disable the deal button during war
        dealBTN.setEnabled(false);

        // Trigger sword animation when war is declared
        AnimatorSet swordAnimationSet = createSwordAnimation();
        swordAnimationSet.start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if a player has fewer than 4 cards to play a war
                if (topDeck.size() < 4 || bottomDeck.size() < 4) {
                    String winner = topDeck.size() < 4 ? "Player 1 wins the game!" : "Player 2 wins the game!";
                    showToast(winner);
                    dealBTN.setEnabled(false);
                    return;
                }

                // Add three hidden war cards from each player's deck to the pot
                for (int i = 0; i < 3; i++) {
                    warPot.add(topDeck.remove(0));
                    warPot.add(bottomDeck.remove(0));
                }

                // Draw the fourth war card from each player's deck
                int warCard1 = topDeck.remove(0);
                int warCard2 = bottomDeck.remove(0);
                warPot.add(warCard1);
                warPot.add(warCard2);

                // Show the final war cards for comparison
                topCard.setImageResource(warCard1);
                bottomCard.setImageResource(warCard2);

                // Get screen height for animation
                float screenHeight = getResources().getDisplayMetrics().heightPixels;

                // Animate the cards
                animateCard(topCard, -screenHeight, 0, 1000); // Fly in from the top
                animateCard(bottomCard, screenHeight, 0, 1000); // Fly in from the bottom

                // Determine the result of the war based on the war cards
                int warCard1Value = getCardValue(warCard1);
                int warCard2Value = getCardValue(warCard2);

                if (warCard1Value > warCard2Value) {
                    showToast("Player 2 wins the war!");
                    topDeck.addAll(warPot);
                    topWinCount += warPot.size() / 2;
                } else if (warCard2Value > warCard1Value) {
                    showToast("Player 1 wins the war!");
                    bottomDeck.addAll(warPot);
                    bottomWinCount += warPot.size() / 2;
                } else {
                    // If tied, continue the war
                    showToast("It's a tie! War continues!");
                    initiateWar(warCard1, warCard2); // Recursive call for new war
                }

                updateCounters(); // Update counts and scores
                dealBTN.setEnabled(true); // Re-enable the deal button
            }
        }, 1000); // Delay for war visibility
    }

    // Method to toggle the visibility of war stacks
    private void showWarCards(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        for (ImageView card : topWarCards) {
            card.setVisibility(visibility);
        }
        for (ImageView card : bottomWarCards) {
            card.setVisibility(visibility);
        }
    }

    // Extracts the rank value from the card's resource ID
    private int getCardValue(int resId) {
        String resName = getResources().getResourceEntryName(resId);
        String rankString = resName.replaceAll("\\D+", "");
        return Integer.parseInt(rankString);
    }

    //Extracts the suit value from the card's resource ID
    private int getSuitValue(int resId) {
        String resName = getResources().getResourceEntryName(resId);
        String suitString = resName.replaceAll("\\d+", "");
        switch (suitString) {
            case "clubs":
                return 1;
            case "diamonds":
                return 2;
            case "hearts":
                return 3;
            case "spades":
                return 4;
            default:
                return 0;
        }
    }

    // Updates player win counts and deck counts after each round
    private void updateCounters() {
        topCount.setText(String.valueOf(topDeck.size()));
        bottomCount.setText(String.valueOf(bottomDeck.size()));

        topWins.setText("Wins: " + topWinCount);
        bottomWins.setText("Wins: " + bottomWinCount);
    }

    // Shows a toast message, ensuring only one toast is visible at a time
    private void showToast(String message) {
        if (currentToast == null) {
            currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            currentToast.setText(message);
        }
        currentToast.show();
    }

    // Dismiss the current toast if it exists
    private void dismissToast() {
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }

    private void checkCheatSequence(int input) {
        cheatSequence.add(input);

        // Limit the sequence length to the maximum code length
        if (cheatSequence.size() > secretCodePlayer1.length) {
            cheatSequence.remove(0); // Remove the oldest input if the sequence is too long
        }

        // Check for Player 1's and Player 2's cheat codes
        if (cheatSequence.size() == secretCodePlayer1.length) {
            boolean isPlayer1Match = true;
            boolean isPlayer2Match = true;
            boolean isWarMatch = true;

            // Check Player 1's cheat code
            for (int i = 0; i < secretCodePlayer1.length; i++) {
                if (cheatSequence.get(i) != secretCodePlayer1[i]) {
                    isPlayer1Match = false;
                }
                if (cheatSequence.get(i) != secretCodePlayer2[i]) {
                    isPlayer2Match = false;
                }
                if (cheatSequence.get(i) != secretCodeWar[i]) {
                    isWarMatch = false;
                }
            }

            // Activate the cheat if a match is found for Player 1
            if (isPlayer1Match) {
                showToast("Cheat Activated! Player 1 Wins!");
                topDeck.addAll(bottomDeck);
                bottomDeck.clear();
                updateCounters();
                showGameOverDialog("Player 1");
                dealBTN.setEnabled(false);
                cheatSequence.clear();
            }
            // Activate the cheat if a match is found for Player 2
            else if (isPlayer2Match) {
                showToast("Cheat Activated! Player 2 Wins!");
                bottomDeck.addAll(topDeck);
                topDeck.clear();
                updateCounters();
                showGameOverDialog("Player 2");
                dealBTN.setEnabled(false);
                cheatSequence.clear();
            }
            // Activate the cheat for initiating war
            else if (isWarMatch) {
                showToast("Cheat Activated! Immediate War!");
                if (!topDeck.isEmpty() && !bottomDeck.isEmpty()) {
                    int card1 = topDeck.remove(0);
                    int card2 = bottomDeck.remove(0);
                    initiateWar(card1, card2);
                } else {
                    showToast("Not enough cards to start a war!");
                }
                cheatSequence.clear(); // Reset the sequence
            }
        }
    }

    private void showGameOverDialog(String winner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Game Over")
                .setMessage(winner + " has won the game!")
                .setCancelable(false) // Prevent closing the dialog without choosing an option
                .setPositiveButton("New Game", (dialog, which) -> {
                    initializeGame(); // Restart the game
                    dealBTN.setEnabled(true); // Re-enable the deal button
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish(); // Close MainActivity
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Create help menu on app bar
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
    }

    //Show help Popup on click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
                // Show the help dialog when the Help button is clicked
                showHelpDialog();
                return true;
        }
        return false;
    }
    
    //Create Help Text Popup
    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage("How to play.\n\n1. Click the Deal Button to start game.\n2. Click Deal again to play one round of War.\n3.The highest Card will Win.\n4.Face cards are worth their order value.\n(ie. Jacks = 11, Kings = 13, etc).\n5.Ace cards are worth 14.\n6.The Card's Suit will add 1 to its value when compared against certain Suits.\n7.Clubs beat Diamonds. Diamonds beat Hearts. Hearts beat Spades. Spades beat Clubs.\n8.Winning a Round adds your opponent's card to your deck.\n9.Keep Playing until one player has every card in their deck.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // Close button
                .setCancelable(true)  // Allows dismissing the dialog by tapping outside
                .show();
    }
    
}
