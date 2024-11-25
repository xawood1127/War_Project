package com.zybooks.warproject;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    // Variables for card deck, players' decks, UI elements, and counters
    private ArrayList<Integer> deck;
    private ArrayList<Integer> topDeck, bottomDeck;
    private final String KEY_TOP_DECK = "topDeck";
    private final String KEY_BOTTOM_DECK = "bottomDeck";
    private ImageView topCard, bottomCard;
    private TextView topCount, bottomCount, topWins, bottomWins;
    private Button dealBTN;
    private ImageView[] topWarCards;
    private ImageView[] bottomWarCards;

    private int topWinCount = 0;
    private final String KEY_TOP_WIN_COUNT = "topWinCount";
    private int bottomWinCount = 0;
    private final String KEY_BOTTOM_WIN_COUNT = "bottomWinCount";
    private Toast currentToast;
    private final Handler handler = new Handler(); // Handler for delays
    private float centerX = 2f;
    private ImageView leftSword, rightSword;
    private float originalLeftSword, originalRightSword;

    private final ArrayList<Integer> cheatSequence = new ArrayList<>();
    private final int[] secretCodePlayer1 = {1, 1, 2, 2, 1, 2}; // Player 1 wins
    private final int[] secretCodePlayer2 = {2, 2, 1, 1, 2, 1}; // Player 2 wins
    private final int[] secretCodeWar = {1, 2, 1, 2, 1, 2}; // Initiates war

    private RadioGroup colorRadioGroup;

    // ----------------------------------------
    // 1. Activity Lifecycle
    // ----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load the theme before setting the content view
        loadTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the saved radio button ID and set it as checked
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int selectedThemeId = prefs.getInt("SelectedThemeId", R.id.radio_dark); // Default to Dark
        colorRadioGroup = findViewById(R.id.color_radio_group);
        colorRadioGroup.check(selectedThemeId); // Set the saved radio button as checked

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

        topWarCards = new ImageView[]{
                findViewById(R.id.top_back1), findViewById(R.id.top_back2), findViewById(R.id.top_back3)
        };
        bottomWarCards = new ImageView[]{
                findViewById(R.id.bottom_back1), findViewById(R.id.bottom_back2), findViewById(R.id.bottom_back3)
        };

        // Load game state or start a new game
        loadGameState();

        colorRadioGroup = findViewById(R.id.color_radio_group);

        colorRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_green) {
                setAppTheme("Green", checkedId);
            } else if (checkedId == R.id.radio_blue) {
                setAppTheme("Blue", checkedId);
            } else if (checkedId == R.id.radio_light) {
                setAppTheme("Light", checkedId);
            } else if (checkedId == R.id.radio_dark) {
                setAppTheme("Dark", checkedId);
            } else if (checkedId == R.id.radio_red) {
                setAppTheme("Red", checkedId);
            }
        });


        dealBTN.setOnClickListener(v -> {
            dismissToast();
            deal();
        });

        topCard.setOnClickListener(v -> checkCheatSequence(1));
        bottomCard.setOnClickListener(v -> checkCheatSequence(2));
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveGameState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveGameState();
    }

    // ----------------------------------------
    // 2. Initialization
    // ----------------------------------------

    private void initializeGame() {
        loadDeck();
        Collections.shuffle(deck);
        topDeck = new ArrayList<>(deck.subList(0, 26));
        bottomDeck = new ArrayList<>(deck.subList(26, 52));
        topCard.setImageResource(R.drawable.back);
        bottomCard.setImageResource(R.drawable.back);
        showWarCards(false);
        topWinCount = 0;
        bottomWinCount = 0;
        updateCounters();
    }

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

    private void loadGameState() {
        SharedPreferences sharedPreferences = getSharedPreferences("GamePreferences", MODE_PRIVATE);

        String topDeckJson = sharedPreferences.getString(KEY_TOP_DECK, null);
        String bottomDeckJson = sharedPreferences.getString(KEY_BOTTOM_DECK, null);

        if (topDeckJson != null && bottomDeckJson != null) {
            topDeck = new Gson().fromJson(topDeckJson, new TypeToken<ArrayList<Integer>>() {}.getType());
            bottomDeck = new Gson().fromJson(bottomDeckJson, new TypeToken<ArrayList<Integer>>() {}.getType());
        } else {
            initializeGame();
        }

        topWinCount = sharedPreferences.getInt(KEY_TOP_WIN_COUNT, 0);
        bottomWinCount = sharedPreferences.getInt(KEY_BOTTOM_WIN_COUNT, 0);

        updateCounters();
    }

    private void saveGameState() {
        SharedPreferences sharedPreferences = getSharedPreferences("GamePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_TOP_DECK, new Gson().toJson(topDeck));
        editor.putString(KEY_BOTTOM_DECK, new Gson().toJson(bottomDeck));
        editor.putInt(KEY_TOP_WIN_COUNT, topWinCount);
        editor.putInt(KEY_BOTTOM_WIN_COUNT, bottomWinCount);

        editor.apply();
    }

    // ----------------------------------------
    // 3. Core Gameplay Logic
    // ----------------------------------------

    private void deal() {
        // Hide extra war cards initially
        showWarCards(false);

        // Check if either deck is empty to determine if game is over
        if (topDeck.isEmpty() || bottomDeck.isEmpty()) {
            String winner = !topDeck.isEmpty() ? getString(R.string.p2_win) : getString(R.string.p1_win);
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
                showToast(getString(R.string.p2_win));
                topDeck.add(card1);
                topDeck.add(card2);
                topWinCount++;
            } else if (card2Value > card1Value) {
                showToast(getString(R.string.p1_win));
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
                    String winner = topDeck.size() < 4 ? getString(R.string.p1_win) : getString(R.string.p2_win);
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

                // Determine the result of the war based on the war cards
                int warCard1Value = getCardValue(warCard1);
                int warCard2Value = getCardValue(warCard2);

                if (warCard1Value > warCard2Value) {
                    showToast(getString(R.string.p2_war));
                    topDeck.addAll(warPot);
                    topWinCount++;
                } else if (warCard2Value > warCard1Value) {
                    showToast(getString(R.string.p1_war));
                    bottomDeck.addAll(warPot);
                    bottomWinCount++;
                } else {
                    // If tied, continue the war
                    showToast(getString(R.string.war_tie));
                    initiateWar(warCard1, warCard2); // Recursive call for new war
                }

                updateCounters(); // Update counts and scores
                dealBTN.setEnabled(true); // Re-enable the deal button
            }
        }, 1000); // Delay for war visibility
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
                showToast(getString(R.string.p1_win));
                topDeck.addAll(bottomDeck);
                bottomDeck.clear();
                updateCounters();
                showGameOverDialog("Player 1");
                dealBTN.setEnabled(false);
                cheatSequence.clear();
            }
            // Activate the cheat if a match is found for Player 2
            else if (isPlayer2Match) {
                showToast(getString(R.string.p2_win));
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

    // ----------------------------------------
    // 4. Utility Methods
    // ----------------------------------------

    private int getCardValue(int resId) {
        String resName = getResources().getResourceEntryName(resId);
        String rankString = resName.replaceAll("\\D+", "");
        return Integer.parseInt(rankString);
    }

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

    private void updateCounters() {
        topCount.setText(String.valueOf(topDeck.size()));
        bottomCount.setText(String.valueOf(bottomDeck.size()));

        topWins.setText("Wins: " + topWinCount);
        bottomWins.setText("Wins: " + bottomWinCount);
    }

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

    // ----------------------------------------
    // 5. UI and Animation
    // ----------------------------------------

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

    private AnimatorSet createSwordAnimation() {
        // Save the original positions of the swords
        originalLeftSword = leftSword.getTranslationX();
        originalRightSword = rightSword.getTranslationX();

        leftSword.setVisibility(View.VISIBLE);
        rightSword.setVisibility(View.VISIBLE);

        // Creates sword animation
        ObjectAnimator leftSwordAnim = ObjectAnimator.ofFloat(leftSword, "translationX", centerX);
        leftSwordAnim.setDuration(2000);
        ObjectAnimator rightSwordAnim = ObjectAnimator.ofFloat(rightSword, "translationX", centerX);
        rightSwordAnim.setDuration(2000);

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

    private void showWarCards(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        for (ImageView card : topWarCards) {
            card.setVisibility(visibility);
        }
        for (ImageView card : bottomWarCards) {
            card.setVisibility(visibility);
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

    private void setAppTheme(String themeName, int radioButtonId) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("AppTheme", themeName);
        editor.putInt("SelectedThemeId", radioButtonId); // Save the radio button ID
        editor.apply();

        // Restart the activity to apply the new theme
        Intent intent = getIntent();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Optional animation
        startActivity(intent);
    }


    private void loadTheme() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String themeName = prefs.getString("AppTheme", "Default");
        switch (themeName) {
            case "Green":
                setTheme(R.style.Theme_WarProject_Table);
                break;
            case "Blue":
                setTheme(R.style.Theme_WarProject_Blue);
                break;
            case "Light":
                setTheme(R.style.Theme_WarProject_Light);
                break;
            case "Red":
                setTheme(R.style.Theme_WarProject_Red);
                break;
            case "Dark":
                setTheme(R.style.Theme_WarProject_Dark);
                break;
            default:
                setTheme(R.style.Theme_WarProject); // Default theme
                break;
        }
    }


    // ----------------------------------------
    // 6. Menu Handling
    // ----------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            showHelpDialog();
            return true;
        } else if (item.getItemId() == R.id.action_home) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.help))
                .setMessage(getString(R.string.help_details))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}
