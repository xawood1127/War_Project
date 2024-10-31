package com.zybooks.warproject;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout topCardBacks, bottomCardBacks;

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
        deck = new ArrayList<>();
        loadDeck();

        // Shuffle and distribute cards
        Collections.shuffle(deck);
        topDeck = new ArrayList<>(deck.subList(0, 26));
        bottomDeck = new ArrayList<>(deck.subList(26, 52));

        // Initial counters
        updateCounters();
    }

    private void loadDeck() {
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
        showWarCards(false);

        if (topDeck.isEmpty() || bottomDeck.isEmpty()) {
            String winner = topDeck.size() > 0 ? "Player 1 Wins the Game!" : "Player 2 Wins the Game!";
            showToast("Game Over! " + winner);
            dealBTN.setEnabled(false);
            return;
        }

        dealBTN.setEnabled(false);

        int card1 = topDeck.remove(0);
        int card2 = bottomDeck.remove(0);

        topCard.setImageResource(card1);
        bottomCard.setImageResource(card2);

        int card1Value = getCardValue(card1);
        int card2Value = getCardValue(card2);

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
            initiateWar(card1, card2);
        }

        updateCounters();
        handler.postDelayed(() -> dealBTN.setEnabled(true), 500);
    }

    private void initiateWar(int card1, int card2) {
        ArrayList<Integer> warPot = new ArrayList<>();
        warPot.add(card1);
        warPot.add(card2);

        // Show the initial cards being compared
        showWarCards(true);

        topCard.setImageResource(R.drawable.back);
        bottomCard.setImageResource(R.drawable.back);

        // Disable the deal button until the war is resolved
        dealBTN.setEnabled(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (topDeck.size() < 5 || bottomDeck.size() < 5) {
                    String winner = topDeck.size() < 5 ? "Player 2 wins the game!" : "Player 1 wins the game!";
                    showToast(winner);
                    dealBTN.setEnabled(false);
                    return;
                }

                // Draw four additional cards for each player
                for (int i = 0; i < 4; i++) {
                    warPot.add(topDeck.remove(0));
                    warPot.add(bottomDeck.remove(0));
                }

                // Draw the second war card for each player
                int warCard1 = topDeck.remove(0); // First player's war card
                int warCard2 = bottomDeck.remove(0); // Second player's war card
                warPot.add(warCard1);
                warPot.add(warCard2);

                // Show the second war cards
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

    private void showWarCards(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        findViewById(R.id.top_back1).setVisibility(visibility);
        findViewById(R.id.top_back2).setVisibility(visibility);
        findViewById(R.id.top_back3).setVisibility(visibility);
        findViewById(R.id.bottom_back1).setVisibility(visibility);
        findViewById(R.id.bottom_back2).setVisibility(visibility);
        findViewById(R.id.bottom_back3).setVisibility(visibility);
    }

    private int getCardValue(int resId) {
        String resName = getResources().getResourceEntryName(resId);
        String rankString = resName.replaceAll("\\D+", "");
        return Integer.parseInt(rankString);
    }

    private void updateCounters() {
        topCount.setText(String.valueOf(topDeck.size()));
        bottomCount.setText(String.valueOf(bottomDeck.size()));

        topWins.setText("Wins: " + topWinCount);
        bottomWins.setText("Wins: " + bottomWinCount);
    }

    private void showToast(String message) {
        dismissToast();
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void dismissToast() {
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }
}
