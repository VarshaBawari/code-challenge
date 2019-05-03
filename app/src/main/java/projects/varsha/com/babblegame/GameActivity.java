package projects.varsha.com.babblegame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;
import java.util.List;
import projects.varsha.com.babblegame.model.SpaWord;
import projects.varsha.com.babblegame.model.WordItem;
import projects.varsha.com.babblegame.util.JsonParserTask;

public class GameActivity extends AppCompatActivity {
    private List<WordItem> mWordItems;
    private JsonParserTask<WordItem> mJsonParserTask;
    private TextView mVersionDescriptionTextView,mFallingWordTranslation;
    private ObjectAnimator objectFallingAnimator,progressAnimation;
    private TextView timerText,correctWords,score;
    private CountDownTimer currentWordTimer;
    private Context mContext;
    private RelativeLayout game_container;
    private ProgressBar mProgressBar;
    private static final int FALLING_WORD_DURATION = 5000;
    private int mCurrentPosition = 0;
    private int gameViewHeight = 0;
    private int correctWordCount = 0;
    private int scoreCounter = 0;
    private int totalScore = 0;
    private float gameViewY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mContext = this;
        mVersionDescriptionTextView = findViewById(R.id.word);
        mFallingWordTranslation = findViewById(R.id.falling_word_trans);
        timerText = findViewById(R.id.timerText);
        correctWords = findViewById(R.id.correctWords);
        timerText = findViewById(R.id.timerText);
        score = findViewById(R.id.score);
        setupButtons();
        setupProgressbar();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateSizeInfo();
    }

    private void updateSizeInfo() {
        game_container = findViewById(R.id.game_container);
        gameViewHeight = game_container.getHeight();
        gameViewY = game_container.getY();
        loadWordTranslator();
    }

    private void setupButtons(){
        findViewById(R.id.action_like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerSelectedByUser(true);
            }
        });
        findViewById(R.id.action_unlike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerSelectedByUser(false);
            }
        });
        findViewById(R.id.action_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNextWord();
            }
        });
    }

    private void setupProgressbar(){
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressAnimation = ObjectAnimator.ofInt(mProgressBar, "progress", 100, 0);
        progressAnimation.setDuration(60000);
        progressAnimation.setInterpolator(new DecelerateInterpolator());
        progressAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                new CountDownTimer(60000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        timerText.setText("00:" + millisUntilFinished / 1000);
                        showGameOverMessage();
                    }
                    public void onFinish() {
                        timerText.setText("00:00");
                        stopFallingAnimation();
                    }
                }.start();
            }
            @Override
            public void onAnimationEnd(Animator animator) {
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    private void showGameOverMessage(){
        new AlertDialog.Builder(mContext)
                .setTitle("Game Over!")
                .setMessage("Your score is " + totalScore)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void loadWordTranslator() {
        mJsonParserTask = new JsonParserTask<>(this).setJson(R.raw.words);
        mJsonParserTask.setJsonParserListener(new JsonParserTask.OnDataListener() {
            @Override
            public void onDecode(JsonParserTask task, List output) {
                mWordItems = output;
                Collections.shuffle(mWordItems);
                if (mWordItems != null && mWordItems.size() > 0) {
                    showWordTranslatorGame();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_data_available), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }).overlay();
    }

    private void answerSelectedByUser(boolean userOptedAnswer) {
        mWordItems.get(mCurrentPosition).isAttempted = true;
        if (currentWordTimer != null) {
            currentWordTimer.cancel();
        }
        mWordItems.get(mCurrentPosition).userOptedAnswer = userOptedAnswer;
        if (userOptedAnswer == mWordItems.get(mCurrentPosition).answerOfThisQues) {
            correctWordCount = correctWordCount + 1;
            correctWords.setText(correctWordCount + "");
            totalScore = totalScore + (5-scoreCounter) * 50;
            score.setText(totalScore + "");
        }
        showNextWord();
    }

    private void stopFallingAnimation() {
        if (objectFallingAnimator != null) {
            objectFallingAnimator.end();

        }
    }

    private void startFallingWordAnimation() {
        float yValueStart = gameViewY;
        int yValueEnd = gameViewHeight;
        objectFallingAnimator = ObjectAnimator.ofFloat(mFallingWordTranslation, View.TRANSLATION_Y, yValueStart, yValueEnd).setDuration(FALLING_WORD_DURATION);
        objectFallingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mCurrentPosition == 0) {
                    progressAnimation.start();
                }
                scoreCounter = 0;
                currentWordTimer = new CountDownTimer(FALLING_WORD_DURATION, 1000) {
                    public void onTick(long millisUntilFinished) {
                        scoreCounter = scoreCounter + 1;
                    }

                    public void onFinish() {
                        scoreCounter = 0;
//                      if (!mWordItems.get(mCurrentPosition).isAttempted){
//                        showNextWord();
//                         }
                    }
                }.start();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }
        });
        startFallingAnimation();

    }

    private void startFallingAnimation() {
        if (objectFallingAnimator != null) {
            objectFallingAnimator.start();
        }
    }

    public void showWordTranslatorGame() {
        showSpanishWordQuestionnaire();
        startFallingWordAnimation();
    }

    private void showSpanishWordQuestionnaire() {
        SpaWord spanishWord = (SpaWord) mWordItems.get(mCurrentPosition);
        mVersionDescriptionTextView.setText(spanishWord.text_eng);
        mFallingWordTranslation.setText(mWordItems.get(mCurrentPosition).fallingTranslationWord);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopFallingAnimation();
        if (mJsonParserTask != null && mJsonParserTask.getStatus() != AsyncTask.Status.FINISHED) {
            mJsonParserTask.cancel(true);
        }
    }

    private void showNextWord() {
        mWordItems.get(mCurrentPosition).isAttempted = true;
        mCurrentPosition = mCurrentPosition + 1;
        if (mCurrentPosition <= mWordItems.size() - 1) {
            showWordTranslatorGame();
        } else {
            progressAnimation.end();
            progressAnimation.cancel();
            showGameOverMessage();
        }
    }
}

