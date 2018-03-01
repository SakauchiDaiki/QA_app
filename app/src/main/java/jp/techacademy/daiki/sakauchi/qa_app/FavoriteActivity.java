package jp.techacademy.daiki.sakauchi.qa_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static jp.techacademy.daiki.sakauchi.qa_app.MainActivity.favoriteMap;
import static jp.techacademy.daiki.sakauchi.qa_app.MainActivity.mFavoriteArrayList;

public class FavoriteActivity extends AppCompatActivity {
//public class FavoriteActivity extends MainActivity {

    public HashMap mFavoriteQidMap;

    private Toolbar mToolbar;
    private int mGenre = 0;
    private boolean favoFlag = false;

    // 追加
    NavigationView mNavigationView;
    Intent iGetGenre;
    //int genre = 0;

    // --- ここから ---
    DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    //DatabaseReference getmDatabaseReference;

    private ChildEventListener mContentsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 追加　もしfavoriteMapに質問が含まれていれば
            if(favoriteMap.containsKey(dataSnapshot.getKey())) {

                String title = (String) map.get("title");
                String body = (String) map.get("body");
                String name = (String) map.get("name");
                String uid = (String) map.get("uid");
                String imageString = (String) map.get("image");
                byte[] bytes;
                if (imageString != null) {
                    bytes = Base64.decode(imageString, Base64.DEFAULT);
                } else {
                    bytes = new byte[0];
                }

                ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                HashMap answerMap = (HashMap) map.get("answers");
                if (answerMap != null) {
                    for (Object key : answerMap.keySet()) {
                        HashMap temp = (HashMap) answerMap.get((String) key);
                        String answerBody = (String) temp.get("body");
                        String answerName = (String) temp.get("name");
                        String answerUid = (String) temp.get("uid");
                        Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                        answerArrayList.add(answer);
                    }
                }

                // 追記　mGenre に質問の本来のジャンルを入れる必要あり
                int genre = Integer.parseInt(favoriteMap.get(dataSnapshot.getKey()));

            // 追加
            // お気に入り選択時
            //if(mGenre == 0) {
           // HashMap favGenre = (HashMap) dataSnapshot.getValue();
            //int genre = (int) favGenre.get("genre");

              /*  String work = dataSnapshot.getKey();
                for(int i = 0; i< mFavoriteArrayList.size(); i++){
                    // 今の質問がお気に入りに含まれていたら、お気に入り一覧に追加
                    if(work.equals(mFavoriteArrayList.get(i))){
                */        // これ渡してもしょうがない（選んだナビゲーションバーの種類）
                        //iGetGenre = getIntent();
                        //genre = iGetGenre.getIntExtra("GENRE", genre);


                        // mGenreを変えてやる必要あり！！！

                        Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), genre, bytes, answerArrayList);
                        mQuestionArrayList.add(question);
             /*       }
                }
            }*/
            // お気に入り選択されていなければ、全て表示
           // else {
            //    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            //    mQuestionArrayList.add(question);       // リストに追加
            //}


                mAdapter.notifyDataSetChanged();
            }
        }


        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question : mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
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
        setContentView(R.layout.activity_favorite);

        setTitle("お気に入り");

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mQuestionArrayList.clear();

        for (int i = 0; i < 4; i++) {
            DatabaseReference mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(i + 1));
            mGenreRef.addChildEventListener(mContentsListener);
        }
    }

// --- ここまで追加する ---

}
