package jp.techacademy.daiki.sakauchi.qa_app;

/**
 * Created by sakauchidaiki on 2018/02/05.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;
import java.util.HashMap;

public class QuestionDetailActivity extends AppCompatActivity implements OnClickListener {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    // 追加
    private ImageButton favoriteButton;
    private boolean favoriteFlag = false;

    private ChildEventListener mFavoriteListener = new ChildEventListener(){
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            favoriteFlag = true;
            favoriteButton.setImageResource(R.drawable.favorite);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
        // fire
    };

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        // お気に入りボタンの準備（追加）
        favoriteButton = (ImageButton) findViewById(R.id.favorite_button);
        favoriteButton.setOnClickListener(this);

        // ログインしている時のみお気に入りボタンを表示
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            favoriteButton.setVisibility(View.INVISIBLE);
        }
        else{
            // リスナー（favoriteの状態をみるリスナー用意。firebaseの中身問い合わせ）
            DatabaseReference mDataBaseReference;
            mDataBaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference favoRef = mDataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());

            favoRef.addChildEventListener(mFavoriteListener);
            favoriteButton.setVisibility(View.VISIBLE);
        }



        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);

                    // お気に入りボタンも表示させない
                    //favoriteButton.setVisibility(View.INVISIBLE);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // --- ここから ---
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                    // --- ここまで ---
                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

    }

    // お気に入りボタン押下時の処理。追加
    public void onClick(View v) {



        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // ログインしている時だけ
       // if (user == null) {
       // }
       // else {

            // お気に入りじゃない時（firebaseのデータを見て判断）
            if (!favoriteFlag) {
                Map<String, String> data = new HashMap<String, String>();
                // 質問のジャンルが入る
                //data.put("Genre", String.valueOf(mQuestion.getGenre()));
                DatabaseReference mDataBaseReference;
                mDataBaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference favoRef = mDataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());

                //favoRef = mDataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());

                // childとして追加？
                //question.getAnswers().add(answer);
                //HashMap answerMap = (HashMap) map.get("answers");
                //data.put("answers", String.valueOf(mQuestion.getAnswers()));

                // 質問のジャンルを保存したい
                data.put("genre", String.valueOf(mQuestion.getGenre()));

                favoRef.setValue(data);

                favoriteButton.setImageResource(R.drawable.favorite);
                favoriteFlag = !favoriteFlag;

            }
            // すでにお気に入りの時
            else {
                DatabaseReference mDataBaseReference;
                mDataBaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference favoRef = mDataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                favoRef.setValue(null);

                favoriteButton.setImageResource(R.drawable.not_favorite);
                favoriteFlag = !favoriteFlag;
            }
       // }

    }
}