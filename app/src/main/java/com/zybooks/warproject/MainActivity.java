package com.zybooks.warproject;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

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
    private Handler handler = new Handler();

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

        //Creates one array for the top and one for the bottom
        topWarCards = new ImageView[]{
                findViewById(R.id.top_back1), findViewById(R.id.top_back2), findViewById(R.id.top_back3)
        };
        bottomWarCards = new ImageView[]{
                findViewById(R.id.bottom_back1), findViewById(R.id.bottom_back2), findViewById(R.id.bottom_back3)
        };

        // Initialize the game
        initializeGame();

        // Set up button click listener for each round
        dealBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissToast();
                deal();
            }
        });
    }

    private void initializeGame() {
        // Load deck of card images
        loadDeck();

        //Shuffle and divide deck for both players
        Collections.shuffle(deck);
        topDeck = new ArrayList<>(deck.subList(0, 26));
        bottomDeck = new ArrayList<>(deck.subList(26, 52));
        updateCounters();
    }

    //Add each card drawable reference to deck by finding suit + rank
    private void loadDeck() {
        deck = new ArrayList<>();
        String[] suits = {"clubs", "diamonds", "hearts", "spades"};
        for (String suit : suits) {
            for (int rank = 2; rank <= 14; rank++) {
                String cardName = suit + rank;
                int resId = getResources().getIdentifier(cardName, "drawable", getPackageName());
                deck.add(resId);
            }
        }
    }

    private void deal() {
        //Hides cards shown in war
        showWarCards(false);

        if (topDeck.isEmpty() || bottomDeck.isEmpty()) {
            String winner = !topDeck.isEmpty() ? "Player 1 Wins the Game!" : "Player 2 Wins the Game!";
            showToast("Game Over! " + winner);
            dealBTN.setEnabled(false);
            return;
        }

        //Disables button until value comparisons are made
        dealBTN.setEnabled(false);

        //Pulls card from the top of each player's deck and displays them
        int card1 = topDeck.remove(0);
        int card2 = bottomDeck.remove(0);
        topCard.setImageResource(card1);
        bottomCard.setImageResource(card2);

        //Get the value of each card and initializes them to variables
        int card1Value = getCardValue(card1);
        int card2Value = getCardValue(card2);

        //Compares card values and updates toast, win count, and deck count
        if (card1Value > card2Value) {
            showToast("Player 1 Wins this Round!");
            topDeck.add(card1);
            topDeck.add(card2);
            topWinCount++;
        } else if (card2Value > card1Value) {
            showToast("Player 2 Wins this Round!");
            bottomDeck.add(card1);
            bottomDeck.add(card2);
            bottomWinCount++;
        } else {
            //If values are the same, calls initiateWar method
            initiateWar(card1, card2);
        }

        updateCounters();
        handler.postDelayed(() -> dealBTN.setEnabled(true), 1000);
    }

    private void initiateWar(int card1, int card2) {
        //Creates a "warPot" to store all cards that may be lost in war
        ArrayList<Integer> warPot = new ArrayList<>();
        warPot.add(card1);
        warPot.add(card2);

        //Shows the hidden card stacks and sets image to the back until revealed
        showWarCards(true);
        topCard.setImageResource(R.drawable.back);
        bottomCard.setImageResource(R.drawable.back);

        // Disable the deal button until the war is resolved
        dealBTN.setEnabled(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //If a player doesn't have enough cards for war, they lose and game ends
                if (topDeck.size() < 4 || bottomDeck.size() < 4) {
                    String winner = topDeck.size() < 4 ? "Player 2 wins the game!" : "Player 1 wins the game!";
                    showToast(winner);
                    dealBTN.setEnabled(false);
                    return;
                }

                // Draw three hidden cards for each player
                for (int i = 0; i < 3; i++) {
                    warPot.add(topDeck.remove(0));
                    warPot.add(bottomDeck.remove(0));
                }

                // Draw the fourth war card for each player to compare
                int warCard1 = topDeck.remove(0);
                int warCard2 = bottomDeck.remove(0);
                warPot.add(warCard1);
                warPot.add(warCard2);

                // Show the war cards
                topCard.setImageResource(warCard1);
                bottomCard.setImageResource(warCard2);

                // Determine the winner of the war
                int warCard1Value = getCardValue(warCard1);
                int warCard2Value = getCardValue(warCard2);

                if (warCard1Value > warCard2Value) {
                    showToast("Player 1 wins the war!");
                    topDeck.addAll(warPot);
                    topWinCount += warPot.size() / 2;
                } else if (warCard2Value > warCard1Value) {
                    showToast("Player 2 wins the war!");
                    bottomDeck.addAll(warPot);
                    bottomWinCount += warPot.size() / 2;
                } else {
                    showToast("It's a tie! War continues!");
                    initiateWar(warCard1, warCard2); // Continue the war
                }

                updateCounters();
                dealBTN.setEnabled(true); // Re-enable the deal button after the war is resolved
            }
        }, 1000); // Delay between war steps for visibility
    }

    //Method to update the visibility of the war stack
    private void showWarCards(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        for (ImageView card : topWarCards) {
            card.setVisibility(visibility);
        }
        for (ImageView card : bottomWarCards) {
            card.setVisibility(visibility);
        }
    }

    //Gets the resId saved from the card and replaces all non  digit characters with "" leaving with value
    private int getCardValue(int resId) {
        String resName = getResources().getResourceEntryName(resId);
        String rankString = resName.replaceAll("\\D+", "");
        return Integer.parseInt(rankString);
    }

    //Method to update the win count and card counts for each player after every round
    private void updateCounters() {
        topCount.setText(String.valueOf(topDeck.size()));
        bottomCount.setText(String.valueOf(bottomDeck.size()));

        topWins.setText("Wins: " + topWinCount);
        bottomWins.setText("Wins: " + bottomWinCount);
    }

    //When a toast is shown it calls the dismissToast method to eliminate a toast queue
    private void showToast(String message) {
        if (currentToast == null) {
            currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            currentToast.setText(message);
        }
        currentToast.show();
    }

    private void dismissToast() {
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }
}
