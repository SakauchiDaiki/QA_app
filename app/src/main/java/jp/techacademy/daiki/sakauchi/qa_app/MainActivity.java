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
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private Toolbar mToolbar;
    private int mGenre = 0;
    private boolean favoFlag = false;

    protected static List<String> mFavoriteArrayList;

    // 追加
    NavigationView mNavigationView;

    // --- ここから ---
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef, mFavRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    // 追加 Qidとgenre
    protected static Map<String, String> favoriteMap;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
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

            // 追加
            // お気に入り選択時
            if(favoFlag == true) {
                String work = dataSnapshot.getKey();
                for(int i = 0; i< mFavoriteArrayList.size(); i++){
                    // 今の質問がお気に入りに含まれていたら、お気に入り一覧に追加
                    if(work == mFavoriteArrayList.get(i)){
                        Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                        mQuestionArrayList.add(question);
                    }
                }
            }
            // お気に入り選択されていなければ、全て表示
            else {
                Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                mQuestionArrayList.add(question);       // リストに追加
            }

            mAdapter.notifyDataSetChanged();        // 表示

            // ここの処理をmFavoriteListnerのonChildAddの中に書く。他のonhildChangedなどは空欄
            /*

            // この中でお気に入り受け取れる？
            // HashMap favoMap = (HashMap) map.get("favorite");

            // 追加
            HashMap fMap = (HashMap) dataSnapshot.getValue();
            String genre = (String) fMap.get("genre");

            // dataSnapshot.getKey(): そのキーのID(= Qid)が入る？
            favoriteMap.put(dataSnapshot.getKey(), genre);
*/
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
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
    // --- ここまで追加する ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // 追加　お気に入りリスト用
        mFavoriteArrayList = new ArrayList<String>();

        // 追加 ---
        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

       // mNavigationView = (NavigationView) findViewById(R.id.main_navigation_view);
        //mNavigationView.setNavigationItemSelectedListener();
//        mNavigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener)this);

        /*
        // ログインしていなけれお気に入りを非表示に
        if (user == null) {
            //Menu menu = mNavigationView.getMenu();
            mNavigationView = (NavigationView) findViewById(R.id.nav_view);
            mNavigationView.getMenu().findItem(R.id.nav_favorite).setVisible(false);
        }
        // ログイン時はお気に入りを表示し、FavoriteActivityにインテントを投げる
        else{
            mNavigationView = (NavigationView) findViewById(R.id.nav_view);
            mNavigationView.getMenu().findItem(R.id.nav_favorite).setVisible(true);
        }
        */
        //  ---

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                // favoFlagの条件追加
                if (mGenre == 0 && favoFlag == false) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }

            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_hobby) {
                    mToolbar.setTitle("趣味");
                    mGenre = 1;
                    favoFlag = false;
                } else if (id == R.id.nav_life) {
                    mToolbar.setTitle("生活");
                    mGenre = 2;
                    favoFlag = false;
                } else if (id == R.id.nav_health) {
                    mToolbar.setTitle("健康");
                    mGenre = 3;
                    favoFlag = false;
                } else if (id == R.id.nav_computer) {
                    mToolbar.setTitle("コンピューター");
                    mGenre = 4;
                    favoFlag = false;
                }else if (id == R.id.nav_favorite) {
                    favoFlag = true;
                    mGenre = 100;
                    //item.nav_favorite.setVisibility(View.INVISIBLE);
                    //mToolbar.getVisibility();
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mQuestionArrayList.clear();
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);

                // 選択したジャンルにリスナーを登録する
                if (mGenreRef != null) {
                    mGenreRef.removeEventListener(mEventListener);
                }

                // お気に入り表示時
                if(favoFlag == true) {
                //if(mGenre == 100) {

                    FirebaseAuth mAuth;
                    // FirebaseAuthのオブジェクトを取得する
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    //mGenreRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid());

                    Intent intent = new Intent(getApplicationContext(), FavoriteActivity.class);
                    intent.putExtra("GENRE", mGenre);
                    startActivity(intent);

                    //for(int i=0; i < 4; i++){
                     //   DatabaseReference mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(i+1);
                    //    mGenreRef.addChildEventListener(mContentsListener);
                    //}

                    // FloatingButton非表示
                    // FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    // fab.setVisibility(View.INVISIBLE);
                }
                // その他ジャンル表示時
                else {
                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                    mGenreRef.addChildEventListener(mEventListener);

                    // FloatingButton表示
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    fab.setVisibility(View.VISIBLE);
                }

                //mGenreRef.addChildEventListener(mEventListener);
                return true;
            }
        });

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

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

    // 追加　お気に入り用のリスナー
    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            // Mapを用意して、
            HashMap fMap = (HashMap) dataSnapshot.getValue();
            String genre = (String) fMap.get("genre");

            // dataSnapshot.getKey(): そのキーのID(= Qid)が入る
            ///favoriteMap.put(dataSnapshot.getKey(), genre);


            // mFavoriteListにaddしていく
            mFavoriteArrayList.add(dataSnapshot.getKey()); // Qidを入れていく。
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

    // 追加
    @Override
    protected void onResume() {
        super.onResume();

        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // ログインしていなけれお気に入りを非表示に
        if (user == null) {
            mNavigationView = (NavigationView) findViewById(R.id.nav_view);
            mNavigationView.getMenu().findItem(R.id.nav_favorite).setVisible(false);
        }

        // ログイン時はお気に入りを表示し、FavoriteActivityにインテントを投げる
        else {

            mFavoriteArrayList.clear(); // 同じものをaddしないようにclear

            // favorite > UserID の中のイベントリスナーを呼び出し
            mFavRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid());
            mFavRef.addChildEventListener(mFavoriteListener);

            /*
            * FavoriteQidMapを作る
            * mFavoriteListenerを用意、その中でQidMapを作る。onChildAddの中は現87~98行目あたりの処理を入れる。
            * AddChildEvent
            * */

            mNavigationView = (NavigationView) findViewById(R.id.nav_view);
            mNavigationView.getMenu().findItem(R.id.nav_favorite).setVisible(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}