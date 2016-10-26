import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

/*<applet code="RacingGame" width="480" height="320"></applet>*/
//画像サイズ:2880*1920
//このゲームはフィールドを車が走るものであるが、
//車を常に中心付近に描くことで、カメラが動くような演出を行っているため
//x座標y座標についてフィールドにおける座標、アプレットへの描写にあたっての座標という
//2種類のパラメータを扱う。そこで、それぞれのパラメータをフィールド、アプレットと表現するものとする
class  Car {
    int s=32;//車のサイズ
    int BGsizex=2880;//背景画像の幅
    int BGsizey=1920;//背景画像の高さ
    double x=2500;//車の初期位置のフィールドにおけるx座標
    double y=1600;//
    double vy;//y軸方向の車の速さ
    double vx;//x軸方向の車の速さ
    double spd=0;//車の進行方向における速さ
    double rad = 0;//画面上方向を基準にしたときの時計回り方向への車の角度
    boolean accel,brake,left,right;//アクセル、ブレーキ、左、右のキーが押されているかを確認するフラグ
    boolean re;//リセットボタンについてのフラグ
    Image cimg,shadow;//車の画像イメージと影のイメージ
    Dimension d;//アプレットの画面サイズ
 
    Car(){}//Carクラスの宣言
	
    void cGetImage (Applet ap){//車の画像ファイルを読み込むメソッド
	cimg = ap.getImage(ap.getCodeBase(), "img/car1.png");//car1.imgを読み込み車の画像とする
	shadow = ap.getImage(ap.getCodeBase(), "img/shadow.png");//shadow.imgを読み込み車の影の画像とする
    }
	
    void cDraw (Graphics g,Dimension d){//車を描写するメソッド
	this.d=d;//アプレットサイズを引数としてクラスに取り込む
	int X;//車の速さに応じて進行方向と逆方向に車をずらした際のアプレットでのx座標
	int Y;//上記のy座標
	if(spd>0){//車が前進しているとき
	    X = d.width/2-16-(int)(30*vx);//車の描写x座標の位置を画面の中心より速さに比例して進行方向とは逆にずらす
	    Y = d.height/2-32+(int)(30*vy);//車の描写y座標の位置を画面の中心より速さに比例して進行方向とは逆にずらす
	}
	else{//車が止まっている、または後進しているとき
	    X = d.width/2-16;//車の描写位置は画面の中心である
	    Y = d.height/2-32;//車の描写位置は画面の中心である
	}
	Graphics2D g2 = (Graphics2D) g;//車の回転を扱うため、graphics2Dを導入する
	g2.rotate(Math.toRadians(rad),X+11,Y+37);//車の影を回転させる
	g2.drawImage(shadow,(int)X-5,(int)Y+5,null);//影の描写。なお描写位置は車より左下方向に少しずらしている
	g2.setTransform(new AffineTransform());//画像変形をリセット
	g2.rotate(Math.toRadians(rad),X+16,Y+32);//車を回転させる
	g2.drawImage(cimg,(int)X,(int)Y,null);//車の描写。この順で影と車を描写することで影の位置を正しく描写することができる。
		

    }

    void cReset (){//車の状態をリセットするメソッド
    	x=2500;//車のフィールドでのx座標を初期位置へ
    	y=1600;//車のフィールドでのy座標を初期位置へ
    	rad=0;//車の向きを初期角度へ
    	spd=0;//車の速さを初期速度へ
    }
    
    void move(Rock r,int n){//壁や杭などの障害物に当たりそうな時に呼び出すメソッド
	vy = spd*Math.cos(Math.toRadians(rad));//車の現在の角度、速度から、x軸方向への速度を計算する
	vx = spd*Math.sin(Math.toRadians(rad));//車の現在の角度、速度から、y軸方向への速度を計算する
	if(x+30*vx+vx<=32+d.width/2||x+30*vx+vx>=BGsizex-32-d.width/2||y-30*vy-vy<=32+d.height/2||y-30*vy-vy>=BGsizey-32-d.height/2){//画面外へ飛び出しそうなとき
	    if(spd>0){//車が前進しているとき
		spd-=0.3;//速度を落とす
	    }
	    else{//車が後進しているとき
		spd+=0.3;//速度を上げて止める
	    }
	}
	if((x+30*vx-r.x)*(x+30*vx-r.x)+(y-30*vy-r.y)*(y-30*vy-r.y)<(r.r+32)*(r.r+32)){//フィールドの中心にある杭に当たらないための処理
	    if(spd>0){//車が前進しているとき
		spd-=0.3;//速度を落とす
	    }
	    else{//車が後進しているとき
		spd+=0.3;//速度を上げて止める
	    }
	}
	else{
	    y-=vy;//y軸方向に速さを用いてフィールド上の座標を更新
	    x+=vx;//x軸方向に速さを用いてフィールド上の座標を更新
	}
    }
    
    void cUpdate(Dimension d){//車のアクセル、ブレーキ、ハンドリングの計算メソッド
	if(rad>360)//角度を0~360に収めるための処理
	    {
		rad=0;
	    }
	else if(rad<-360)//角度を0~360に収めるための処理
	    {
		rad=0;
	    }
		
	if(accel){//アクセルが入っているとき
	    if(spd<2.5){//最高速度2.5未満ならば
		spd+=0.007;//速度を上げる
	    }
	}

	if(brake){//ブレーキが入っているとき
	    if(spd>-1){//バックの最高速度-1より大きいならば
		spd-=0.008;//速度を下げる
	    }
	    if(left){//左が入っているとき
		rad-=1.0/4*spd;//速度に反比例して角度数を下げる
	    }
	    if(right){//右が入っているとき
		rad+=1.0/4*spd;//速度に反比例して角度数を上げる
	    }
	}
	else if(!brake && spd<0){//ブレーキが入っておらず、かつスピードがマイナスの時
	    spd=0;//車を止める
	}	
	if(spd>0.3){//スピードがしきい値0.3より大きい場合
	    if(left){//左が入っているとき
		rad-=1/(spd*3);//速度に反比例して角度数を下げる
	    }
	    if(right){//右が入っているとき
		rad+=1/(spd*3);//速度に反比例して角度数を上げる
	    }	
	    spd-=0.001;//アクセル、ブレーキの状態に関わらずスピードを徐々に下げる
	}

	//			System.out.println(rad);
	//			System.out.println(Math.cos(Math.toRadians(rad)));
	//			System.out.println(Math.sin(Math.toRadians(rad)));
	//			System.out.println(spd);
		
    }

}
class Rock extends Object{//Objectクラスを継承した障害物クラス（このゲームでは杭として使用）を定義
    int r=10;
    Rock(){}//Rockクラスを宣言
    Rock(int x,int y){//引数を持つRockクラス
	this.x=x;//フィールドでのx座標を読み込む
	this.y=y;//フィールドでのy座標を読み込む
    }

}
class Object {//フィールドの周りに配置する杭を定義
    int x=-1;//初期位置のフィールドのx座標
    int y=-1;//初期位置のフィールドのy座標
    int leng=(32)*2;//杭の高さ
    int birdeye=(32)*16;//視点の高さ
    Color brown = new Color(63,45,24);//杭の色を定義
	
    Object(){}//引数なしの宣言

    Object(int x,int y){//杭の位置を示す引数ありの宣言
	this.x=x;//杭のx座標
	this.y=y;//杭のy座標
    }
	
    void Draw(Graphics g,double cx,double cy,Dimension d){//描画処理
	Graphics2D g2 = (Graphics2D) g;//杭を、線が太い直線で描くためにGraphics2Dを導入する
	BasicStroke wide = new BasicStroke(10.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);//端が丸く、幅のある線を設定
	g2.setStroke(wide);//上記の設定を適用
	g2.setColor(brown);//杭の色を設定
	g2.draw(new Line2D.Double(x-cx+d.width/2,y-cy+d.height/2,calc((double)x, cx)+x+d.width/2-cx,calc((double)y, cy)+y+d.height/2-cy));//calcメソッドで求めたx,yそれぞれのの長さに従い杭を描写
    }
    double calc(double x,double cx){//アプレット内に杭があり、それを描写する際に、車の頭上から見下ろす1点透視図法に従った向き、長さで描写するための計算メソッド
	int ans;//この答えが杭の地面から杭の高さに当たる部分までの一点透視図法での長さである。
	double X;//計算でのテンプレとする
	X=x;//フィールドにおける杭のx(y)座標を読み込む
	X-=cx;//そこからフィールドにおける車のx(y)座標を引く、これで杭と車の間の距離が残る。
	ans=(int)(leng*X/(birdeye-leng));//（杭の高さ×杭と車の間の距離）÷（視点の高さ-杭の高さ）の答えを求める
	return ans;//この答えを返す。そして杭を描写するときに、杭の地面のx(y)座標からこの答え分足した点へ直線を引けば1点透視図法を踏まえた杭が描けるのである。
    }
}

class Checkpoint{//車の通るチェックポイントの定義
    int x=-1;//初期位置のフィールドのx座標
    int y=-1;//初期位置のフィールドのy座標
    int r=40;//チェックポイントの半径
    boolean c=false;//チェックポイントを通ったかどうかを判定するフラグ、そして初期化
    Color mada = new Color(255,0,0,100);//チェックポイントがまだ通っていない時の赤色。透過処理を行う
    Color totta = new Color(0,0,255,100);//チェックポイントが通った後の青色。透過処理を行う
    Color point = mada;//チェックポイントを描写する際の色。まだ通っていない時の色に初期化
    Checkpoint(){}//引数なしの宣言
    Checkpoint(int x,int y){//引数ありの宣言
	this.x=x;//x座標を読み込む
	this.y=y;//y座標を読み込む
    }

    void draw(Graphics g,double cx,double cy,Dimension d){//チャックポイントの描写処理
	g.setColor(point);//現在のチェックポイントの色に従い色を設定
	g.fillOval((int)(x-cx+d.width/2-r), (int)(y-cy+d.height/2-r), 2*r, 2*r);//チェックポイントの描写
    }
    void check(double cx,double cy){//チェックポイントを通ったかどうかを判定するメソッド
	if((cx-x)*(cx-x)+(cy-y)*(cy-y)<=52*52){//車がチェックポイントの中に入っているとき（52というのはチェックポイントの半径＋車の幅の半分）
	    c=true;//チェックポイントのフラグを立てる
	    point = totta;//描写の色を切り替える
	}
    }
}
class Button extends Checkpoint{//タイトル画面の難易度を選ぶボタンを定義。丸の処理ということでチェックポイントクラスを継承した
    int r=20;//ボタンの半径
    Color yet,select;//ボタンが選択されていないときの色、選択されているときの色を宣言。ボタンの色は難易度別に分けるため、値は入れない。
    Color button=yet;//ボタンの色を初期化しておく
    boolean push =false;//ボタンが選択されているかどうかを判定するフラグ
    Button(){}//引数なしの宣言
    Button(int x,int y){//引数ありの宣言
	this.x=x;//x座標を読み込む
	this.y=y;//y座標を読み込む
    }
    void draw(Graphics g){//描写処理
	g.setColor(button);//現在の色を設定
	g.fillOval(x-r, y-r, 2*r, 2*r);//円を描く
    }
    void check(Point p){//マウスによる判定の際にマウスがボタンの位置に入っているどうかを判定するメソッド
	if((p.x-x)*(p.x-x)+(p.y-y)*(p.y-y)<=r*r){//引数として読み込んだ点がボタンに入っているとき
	    push= true;//ボタンのフラグを立てる
	}
    }
    void changecolor(){//ボタンの色をフラグに応じて変えるメソッド
	if(push==true){//ボタンフラグが立っているとき
	    button=select;//ボタンの色を選択色に設定
	}
	else{//ボタンが立っていないとき
	    button=yet;//ボタンの色をまだの時の色に戻す
	}
    }
}

public class RacingGame extends Applet implements KeyListener,MouseListener,Runnable{
    private Thread th =null;//スレッドの宣言、初期化
    private Image offImage,BG,title,ui;//画像の宣言。バッファ、背景、タイトル画面、ゲーム画面のメーター画像
    private Graphics buffer;//バッファを宣言
    private Calendar timetmp;//ゲームのタイマー表示をするためのパソコン時間
    private long Timestart;//ゲームの開始時間を入れるlong型を宣言
    private long Timeend;//ゲームの終了時間を入れるlong型を宣言
    private double Passedtime;//ゲームのタイムを表示する際の数値
    int scene=0;//ゲームのタイトル、ゲーム画面、ゲーム終了画面を切り替えるシーン数を宣言、初期化
    Car lambo = new Car();//Car型のランボルギーニを宣言
    Rock[] kui1 = new Rock[1];//画面の中心に配置する杭を宣言（今回は1つのみの使用としているが、配列型で宣言することでゲームバランスを変える際に杭を使うことも可能になる）

    int pointn;//チェックポイントの数
    Checkpoint[] point1= new Checkpoint[20];//チェックポイントを20個宣言
    Object[] saku = new Object[136];//フィールドの外周に配置する杭を136本宣言（周りに配置する数ぴったり）
    Color minimap = new Color(0,0,0,100);//ゲーム画面に描くミニマップの背景色。透過処理を行い、背景を完全には隠さない。
	
    double x=0;//タイトル画面での背景を動かす際のタイトルのフィールド上のx座標
    double y=0;//y座標
    double vx=0.2;//タイトル画面で背景を動かす際のx方向の速さ
    double vy=0.1;//y方向の速さ
	
    Button[] level=new Button[3];//タイトル画面の難易度選択のボタンを3つ宣言
    double[] highscore = {99,99,99};//3つの難易度それぞれのハイスコアを入れる配列
    int selectlevel;//タイトル画面でレベルを選択した際の現在選択している難易度
    boolean pressup;//キーボードから上が押されているかどうかを判定するフラグ
    boolean pressdown;//キーボードから下が押されているかどうかを判定するフラグ
    ////////////////////////////////////////////////////////
    //それぞれの難易度に応じてチェックポイントの位置をx,yの順で配列として宣言している
    //最終的にtxtファイルからこの情報を読み取るつもりだったため、このような不便な置き方をしている。
    //txtファイルから読み込むメソッドもこのソースコードに残してある。
    int L1[] = {450,960, 2430,960, 1440,500, 1440,1320};//簡単なレベル
    int L2[] = {450,960, 2430,960, 1440,600, 1440,1320, 945,400, 1935,400, 945,1520, 1935,1520};//通常レベル
    int L3[] = {1440,310, 1440,1610, 1440,610, 1440,1310, 390,960, 2490,960, 840,960, 2040,960, 740,560, 2140,560, 760,1360, 2140,1360, 1090,760, 1790,1160, 1090,1160, 1790,760};//難しいレベル

    public void init(){//ゲームの初期化
		
        th = new Thread(this);//新しいスレッドの宣言
        th.start();//スレッドの開始
        Dimension d = getSize();//画面サイズの取得
        offImage = createImage(d.width, d.height);//裏バッファをアプレットサイズで作成
        BG = getImage(getCodeBase(), "img/desert.jpg");//背景画像を読み込む
        title = getImage(getCodeBase(), "img/title.png");//タイトル画面の画像を読み込む
        ui = getImage(getCodeBase(), "img/gameui.png");//ゲーム時のメーター表示画像を読み込む
        
    	Color easy = new Color(85,134,28,100);//レベル1の時のボタンの色（選択されていない色）
    	Color easyselect = new Color(85,134,28);//レベル1の選択されているときの色
    	Color normal = new Color(174,158,0,100);//レベル2の時のボタンの色（選択されていない色）
    	Color normalselect = new Color(174,158,0);//レベル2の選択されているときの色
    	Color hard = new Color(157,101,47,100);//レベル3の時のボタンの色（選択されていない色）
    	Color hardselect = new Color(157,101,47);//レベル3の選択されているときの色
        
        level[0] = new Button(160,170);//ボタン1を座標を引数として宣言
        level[0].push=true;//初期状態ではレベル1が選択されているものとする
        level[0].yet=easy;//レベル1でのボタンの色を設定
        level[0].select=easyselect;//レベル1でのボタンの色を設定
        level[0].button=easyselect;//レベル1でのボタンの色の初期設定
        
        level[1] = new Button(160,220);//ボタン2を座標を引数として宣言
        level[1].yet=normal;//レベル2でのボタンの色を設定
        level[1].select=normalselect;//レベル2でのボタンの色を設定
        level[2] = new Button(160,270);//ボタン3を座標を引数として宣言
        level[2].yet=hard;//レベル3でのボタンの色を設定
        level[2].select=hardselect;//レベル2でのボタンの色を設定
        
        for(int i = 0;i<136;i++){//フィールドの周りを囲む杭の初期化
	    saku[i] = new Object();
        }
        int i=0;//フィールドにの上下、左右に杭を等間隔で設定するための杭の数を担う
        for(int x=d.width/2;x<=2880-d.width/2;x+=60){//フィールドの左右に杭を配置する。杭と杭の間は60とする
	    saku[i].x=x;//背景画像よりもアプレットサイズの半分ぶんだけ実際のフィールドを内側にすることで車が端に行っても背景画像が切れることが無くなる
	    saku[i].y=d.height/2;//上記の理由より左側の杭の位置を設定
	    i+=1;
	    saku[i].x=x;
	    saku[i].y=1920-d.height/2;//右側の杭の設定
	    i+=1;
        }
        for(int y=d.height/2;y<=1920-d.height/2;y+=60){//フィールドの上下に位置する杭の設定
	    saku[i].x=d.width/2;//左右の杭を設定する場合と同様
	    saku[i].y=y;
	    i+=1;
	    saku[i].x=2880-d.width/2;
	    saku[i].y=y;
	    i+=1;
        }
        kui1[0]= new Rock(1440,960);//フィールドの中心に配置する障害物としての杭の宣言
        
        lambo.cGetImage(this);//車の画像の読み込み
        addKeyListener(this);//キーリスナー開始
        addMouseListener(this);//マウスリスナー開始
    }


    public void update(Graphics g){//ペイントメソッドの更新処理
	paint(g);//ペイント
    }
    public void paint(Graphics g){//ペイントメソッド

	Dimension d = getSize();//画面サイズを取得
	buffer = offImage.getGraphics();//裏バッファを読み込む
	if(scene==0){//タイトル画面を示すシーン0
	    buffer.setColor(Color.black);
	    buffer.fillRect(0, 0, d.width, d.height);//まずアプレットを黒で塗りつぶす
	    buffer.drawImage(BG, (int)(0-x),(int)(0-y),this);//タイトル画面で動かす背景画像を描写
	    x+=vx;//背景画像を移動
	    y+=vy;
	    if(x>2880-d.width || x<0){//背景画像が端まで移動したら反転
		vx *=(-1);
	    }
	    if(y>1920-d.height || y<0){//背景画像が端まで移動したら反転
		vy*=(-1);
	    }
	    buffer.drawImage(title, 0, 0,d.width,d.height,this);//タイトル画像を描写
			
	    for(int i=0;i<3;i++){//レベル選択ボタンの色の更新と描写
		level[i].changecolor();
		level[i].draw(buffer);
	    }
	    Font howto = new Font("Dialog",Font.BOLD,15);//操作説明のフォント設定
	    buffer.setFont(howto);
	    buffer.setColor(Color.black);
	    buffer.drawString("Z = accel / select  X = brake", 130, 107);//操作説明
	    buffer.drawString("LEFT&RIGHT KEY = handling", 130, 120);
			
	    Font time = new Font("Dialog",Font.ITALIC,30);//ハイスコアタイムのフォント設定
	    buffer.setFont(time);

	    buffer.drawString(+highscore[0]+"", 350, 185);//ハイスコア表示
	    buffer.drawString(+highscore[1]+"", 350, 235);
	    buffer.drawString(+highscore[2]+"", 350, 285);
			
	}		

	else if(scene==1){//ゲーム画面を示すシーン1
	    buffer.setColor(Color.black);
	    buffer.fillRect(0, 0, d.width, d.height);//アプレットを黒で塗りつぶす
	    buffer.drawImage(BG, (int)(240-lambo.x-lambo.vx),(int)(160-lambo.y+lambo.vy),this);//背景を車の動きとは逆に動かして描写
	    for(int i=0;i<pointn;i++){//チェックポイントを描写
		point1[i].draw(buffer, lambo.x, lambo.y,d);//現在のフィールドにおける車の位置を引数として渡す
	    }

	    kui1[0].Draw(buffer, lambo.x, lambo.y, d);//フィールド中心に配置した障害物の描写
	    for(int i=0;i<136;i++){
		saku[i].Draw(buffer, lambo.x, lambo.y, d);//フィールドまわりを囲む柵の描写
	    }
	    for(int i=0;i<pointn;i++){
		point1[i].draw(buffer, lambo.x, lambo.y, d);//チェックポイントの描写
	    }
	    lambo.cDraw(buffer,d);//車の描写
	    if(lambo.re){//リセットフラグが立っているとき
		lambo.cReset();//車の状態をリセット
		lambo.re=false;//リセットフラグを下ろす
	    }

	    drawdirection(buffer, d);//残っているチェックポイントの方向を示す矢印を描写

	    paintmap(buffer,point1,pointn,kui1,1);//車のとチェックポイント、そして杭の位置をミニマップとして描写する

	    paintmeter(buffer, lambo.spd,d);//速度をメーター表示

	    int pass=0;//既に通ったチェックポイントの数
	    for(int i=0;i<pointn;i++){
		point1[i].check(lambo.x, lambo.y);//チェックポイントに車が入っているかどうかを判断するメソッド
		if(point1[i].c==true){
		    pass++;//既にチェックポイントを通った後ならば、数える
		}
	    }
	    if(pass==pointn){//チェックポイントをすべて廻ったならば
				
		if(lambo.spd>0){//スピードが出ていたら
		    lambo.spd-=0.05;//急ブレーキをかける
		}
		else{//車が後進または止まっているならば
		    lambo.spd=0;//スピードを0にし
		    timerend();//タイマーを止める
		}
	    }
	    drawscore(buffer,pass,selectlevel);//スコアを表示する
	    lambo.cUpdate(d);//車の位置を移動
	    lambo.move(kui1[0], 1);//柵や障害物のあたり判定を行う

	}
	else if(scene==2){//ゲームクリア画面を示すシーン2
	    Font finish1 = new Font("Dialog",Font.ITALIC,60);
	    Font finish2 = new Font("Dialog",Font.BOLD,25);
	    buffer.setFont(finish1);
	    buffer.setColor(Color.red);
	    buffer.drawString("Finish!!", 160, 150);//「フィニッシュ」と表示
	    if(Passedtime<highscore[selectlevel]){//今回のタイムがハイスコアを更新していたら
		highscore[selectlevel]=Passedtime;//今回のスコアをハイスコアとする
		buffer.drawString("New Record!!", 80, 200);//ハイスコアを更新した場合は「New Record!!」と表示
	    }
	    buffer.setFont(finish2);
	    buffer.setColor(Color.black);
	    buffer.drawString("SPACE = RETRY", 140, 240);//リトライとタイトルの操作を示す
	    buffer.drawString("ENTER = TITLE", 140, 260);
	}
	g.drawImage(offImage,0,0,this);
    }

    public void readtxt(String s){//txtファイルからステージ情報を読み取るメソッド（今回は用いていない）
        int j;
        for(j = 0;j<20;j++){//チェックポイントの初期化
	    point1[j] = new Checkpoint();
        }      
        j=0;
        try{
	    File file = new File("txt/"+s+".txt");//引数としてtxtのファイル名を受け取り読み込むファイルを決める
	    BufferedReader br = new BufferedReader(new FileReader(file));//txtファイルを読み込む準備

	    String str = br.readLine();//ストリング型のstrにtxtファイルの中の最初の行を入れる
	    while(str != null){//文字列がある限り

		point1[j].x=Integer.valueOf(str);//チェックポイントの配列のx座標にstrをint型に変換して入れる		
		str = br.readLine();//次の行の文字列を読み込む
		point1[j].y=Integer.valueOf(str);//チェックポイントの配列のｙ座標にstrをint型に変換して入れる	
		str = br.readLine();//次の行の文字列を読み込む

		j++;//チェックポイントの配列を次にする
	    }

	    br.close();//txtファイルを閉じる
	}catch(FileNotFoundException e){//例外処理
	    System.out.println(e);
	}catch(IOException e){
	    System.out.println(e);
	}
        pointn=j;//txtファイルに含まれている座標の数をチェックポイントの数にする
        
    }
	
    public void setpoint(int n,Checkpoint[] c){//txtファイルを使わない場合のレベルに応じてチェックポイントを配置するメソッド
	int j;
        for(j = 0;j<20;j++){//チェックポイントの初期化
	    point1[j] = new Checkpoint();
        }   
        j=0;
//	System.out.println(n);
	int[] l;//レベル選択した際のチェックポイント座標の配列を入れる。
	if(n==0){//レベルが1の時
	    j=4;//要素数
	    l=L1;//L1を入れる
	}
	else if(n==1){//レベルが2の時
	    j=8;
	    l=L2;//L2をいれる
	}
	else {
	    j=16;
	    l=L3;//L3を入れる
	}
	int m=0;
	for(int i=0;i<j;i++){//要素数分だけチェックポイントの配列の座標に入れていく
	    c[i].x=l[m];//x座標
	    m++;
	    c[i].y=l[m];//y座標
	    m++;
	}
			
	pointn=j;//要素数をチェックポイントの数に入れる		
    }
	
    public void drawdirection(Graphics g,Dimension d){//残っているチェックポイントを示す矢印を描くメソッド
	Graphics2D g2 = (Graphics2D)g;//アプレットの位置からチェックポイントまでの向きを示す際に角度を用いるためGraphics2Dを導入する

	BasicStroke arrow = new BasicStroke(3.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER);//矢印の線の太さ、形を設定
	g2.setColor(Color.red);
	g2.setStroke(arrow);
	for(int i=0;i<pointn;i++){//1つずつチェックポイントを見ていく
	    if(point1[i].c==false){//チェックポイントがまだ通っていないならば
			double rad=0;//チェックポイントへの角度をリセット
			g2.setTransform(new AffineTransform());//Graphics2Dをリセット
			rad = Math.atan((point1[i].y-lambo.y)/(point1[i].x-lambo.x));//車の座標とチェックポイントの座標からアークタンジェントを用いて角度を求める
			if(point1[i].x <= lambo.x){//上で求めた角度は-90から90までしかないので車からみてチェックポイントがマイナス方向にある場合はパイを足して補正をする
				rad+=Math.PI;
			}
			g2.rotate(rad,d.width/2,d.height/2);//チェックポイントとの角度分、描写を曲げる
			g2.drawLine(300,160 , 320, 160);//矢印の描写
	    }
	}
    }

    public void paintmap(Graphics g,Checkpoint[] c,int cn,Rock[] k,int kn){//ミニマップの描写。チェックポイントと障害物の配列とその個数をそれぞれ引数として渡す
	Graphics2D g2 = (Graphics2D)g;//前の処理でGraphics2Dを用いているので、リセットするために導入
	int r=3;//位置を示す点の半径
	g2.setTransform(new AffineTransform());//Graphics2Dの設定をリセット
	g2.setColor(minimap);//ミニマップの背景色をセット
	g2.fillRect(330, 220, 120, 80);//ミニマップの背景を描写
	for(int i=0;i<cn;i++){//
	    if(c[i].point==c[i].mada){//チェックポイントが通る前ならば
		g2.setColor(Color.red);//色を赤に
	    }
	    else{//通った後ならば
		g2.setColor(Color.blue);//色を青に
	    }
	    g2.fillOval((int)(330+(double)c[i].x/2880*120-r), (int)(220+(double)c[i].y/1920*80-r), 2*r, 2*r);//チェックポイントの位置をミニマップに表示
	}
	g2.setColor(Color.green);//ミニマップの中の車を描写する色を指定
	g2.fillOval((int)(330+lambo.x/2880*120-r), (int)(220+lambo.y/1920*80-r), 2*r, 2*r);//車の位置を描写
	for(int i=0;i<kn;i++){//障害物の数だけ描写。今回は1つ
	    r=2;//位置を示す点の半径
	    g2.setColor(Color.black);//障害物の位置を指定
	    g2.fillOval((int)(330+(double)k[i].x/2880*120-r), (int)(220+(double)k[i].y/1920*80-r), 2*r, 2*r);//障害物の位置を描写
	}
    }
    public void paintmeter(Graphics g,double spd,Dimension d){//アプレットの左下に表示する車の速度メーターの描写
	Graphics2D g2 = (Graphics2D) g;//メーターの針で角度を用いるためにGraphics2Dを導入
	g2.drawImage(ui, 0, 0, d.width, d.height, this);//まずメーターの盤面を描いた画像を描写する
	Color darkred = new Color(200,0,0);//メーターの針の色を宣言
	g2.setColor(darkred);//色を適用
	g2.rotate(Math.toRadians(spd*40)-0.8,80,300);//メータの角度を調整し、設定。
	g2.drawLine(80, 300, 80, 260);//メーターの針を描写
		
    }
    public void run(){//実行メソッド
	try {
	    while(true){
		repaint();//updateを呼び出す
		Thread.sleep(4L);//画像の回転、移動を使っているため、標準的なゲームスピードではカクカクになってしまう。そこで車の動きをスムーズにするため、処理のスピードを上げた。しかし、パソコンのスペックによってゲームスピードの変化が出やすくなってしまった。
				
	    }
	}
	catch (Exception e) {
	}
    }

    public void drawscore(Graphics g,int p,int l){//スコアの表示メソッド
	Graphics2D g2 = (Graphics2D)g;//前の処理でGraphics2Dを用いているので、リセットするために導入
	g2.setTransform(new AffineTransform());//描画設定のリセット
	timetmp = Calendar.getInstance();//タイムスコアの基準としてその瞬間のパソコン時間を読み込む
	Timeend = timetmp.getTimeInMillis();//ミリ秒に変換
	Passedtime = (double)(Timeend-Timestart)/1000;//ゲームスタートの時間と現在の時間の差を出しスコアタイムとし、千分の一をして秒単位にする。
	Font time = new Font("Dialog",Font.BOLD,30);//スコア表示諸々のフォント設定
	Font time2 = new Font("Dialog",Font.BOLD,20);
	Font score = new Font("Serif",Font.BOLD,60);
        g2.setColor(Color.white);
	g2.setFont(time);
	g2.drawString("Time:"+Passedtime,20,30);//スコアタイムの描写
	g2.setFont(time2);
	g2.drawString("Highscore"+highscore[l], 20, 60);//選択されているレベルでのハイスコアタイムの表示
	g2.setFont(score);
	g2.drawString(p +"/"+pointn, 340, 60);//チェックポイントの総数と通過済みのチェックポイントの数を描写
 		
    }
    public void timerstart(){//ゲーム開始時のパソコン時間を読み込むメソッド
	timetmp = Calendar.getInstance();
	Timestart = timetmp.getTimeInMillis();//ミリ秒に変換
    }
 	
    public void titlekeycheck(){//タイトル画面でのキーボード操作によるレベル選択
	if(pressup==true){//上が押されたとき
	    selectlevel=(selectlevel+2)%3;//０，２，１，０，２・・・の順でレベルを変えていく
 			
	}
	if(pressdown==true){//下が押されたとき
	    selectlevel=(selectlevel+1)%3;//０，１，２，０，１・・・の順でレベルを変えていく
	}
	level[selectlevel%3].push=true;//現時点での選択レベルのボタンのフラグを立てる
	level[(selectlevel+1)%3].push=false;//それ以外のレベルのフラグは下げる
	level[(selectlevel+2)%3].push=false;//同上
	pressup=false;//キーフラグを下げる
	pressdown=false;//同上

    }
 	
    public void timerend(){//チェックポイントを全て通過した時に呼ばれるメソッド
	timetmp = Calendar.getInstance();
	Timeend = timetmp.getTimeInMillis();//現在のパソコンをミリ秒に変換
	Passedtime = (double)(Timeend-Timestart)/1000;//ゲーム開始時との差を出し、クリアタイムとして数値を入れる

	scene=2;//ゲームクリア画面へ移行するためシーン数を進める
    }
    private void reset(){//ゲームの状態をリセットするメソッド
	lambo.re=true;//車のフィールドにおける位置をリセット
	for(int i=0;i<pointn;i++){//チェックポイントの通過状態をリセット
	    point1[i].c=false;	
	    point1[i].point=point1[i].mada;
	}
	scene=1;//ゲーム画面へ移行するためシーン数をリセット
	timerstart();//スコアタイマーをこの時点から開始させる
		
    }
 	/////////////////////////////////////////////////キーボード操作
    public void keyTyped(KeyEvent e){
    }
    public void keyPressed(KeyEvent ke) {
        int cd = ke.getKeyCode();//キーボード入力をint型で受け取る
        switch (cd) {
        case KeyEvent.VK_Z: // Zキーが押されたら
	    lambo.accel = true;   // 車のアクセルフラグを立てる
	    if(scene==0){//タイトル画面の場合は
		setpoint(selectlevel, point1);//選択されているレベルに応じてチェックポイントを再配置
		reset();//ゲーム状態をリセットしゲームに移行する
	    }
	    break;
        
        case KeyEvent.VK_X: // Xキーが押されたら
	    lambo.brake = true;   // 車のブレーキフラグを立てる
	    break;
	    
        case KeyEvent.VK_LEFT: // 左キーが押されたら
	    lambo.left = true;   // 左ハンドルフラグを立てる
        break;
        
        case KeyEvent.VK_RIGHT: // 右キーが押されたら
	    lambo.right = true;   // 右ハンドルフラグを立てる
	    break;
	    
	case KeyEvent.VK_SPACE: // スペースキーが押されたら
	    if(scene==2||scene==1){//ゲーム画面かクリア画面の時ならば
		reset();//ゲームをリセットしリスタート
	    }
	    break;
    	
        case KeyEvent.VK_ENTER://エンターキーが押されたら
	    scene=0;//タイトル画面に戻る
	    break;
	    
        case KeyEvent.VK_UP://上キーが押されたら
	    if(scene==0){//タイトル画面の場合は
		pressup = true;//上キーフラグを立て
		titlekeycheck();//レベルの選択判定を行う
	    }
	    break;
	    
        case KeyEvent.VK_DOWN://下キーが押されたら
	    if(scene==0){//タイトル画面の場合は
		pressdown = true;//下キーフラグを立て
		titlekeycheck();//レベルの選択判定を行う
	    }
	    break;
        }
    }
    public void keyReleased(KeyEvent ke) {
	int cd = ke.getKeyCode();
	switch (cd) {
	case KeyEvent.VK_Z: // Zキーが離されたら
	    lambo.accel = false;   // アクセルフラグを下げる
	    break;
    
	case KeyEvent.VK_X: // Xキーが離されたら
	    lambo.brake = false;   // ブレーキフラグを下げる
	    break;
	    
        case KeyEvent.VK_LEFT: // 左キーが離されたら
	    lambo.left = false;   // 左ハンドルフラグを下げる        
	    break;
        
        case KeyEvent.VK_RIGHT: // 右キーが離されたら
	    lambo.right = false;   // 右ハンドルフラグを下げる        
	    break;
	}
    }

    @Override
	public void mouseClicked(MouseEvent e) {
    }
    @Override
	public void mousePressed(MouseEvent e) {
	Point point = e.getPoint();//マウスが押された時のポインタの位置を取得
	if(scene==0){//タイトル画面の時
	    for(int i=0;i<3;i++){//レベル選択ボタンをそれぞれチェックする
		level[i].check(point);	//マウスがボタンを示しているかどうかそれぞれ判定する
		if(level[i].push==true){//押されていたら
		    selectlevel=i;//それを選択レベルとする
		}
		level[(selectlevel+1)%3].push=false;//それ以外は選択を外す
		level[(selectlevel+2)%3].push=false;
	    }
	}
    }
    @Override
	public void mouseReleased(MouseEvent e){	
    }
    @Override
	public void mouseEntered(MouseEvent e) {	
    }
    @Override
	public void mouseExited(MouseEvent e) {
    }
}
