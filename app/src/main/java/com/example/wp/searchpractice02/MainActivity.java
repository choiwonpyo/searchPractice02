package com.example.wp.searchpractice02;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//hello baby this is practice
//fucking git
public class MainActivity extends AppCompatActivity {

    private class forSimilarFile{
        public File similarFile;
        public int how;
    }
    private forSimilarFile[] similar;
    int similarCount;
    //onCreate와 다른 함수들에서 사용하기 위해 전역변수(?)로 선언한다.
    private String mFileName;//파일을 클릭했을시, 파일의 이름을 저장하는 String 변수.
    private ListView lvFileControl;
    private Context mContext=this;

    private List<String> IItem=null;
    private List<String> IPath=null;
    private String mRoot= Environment.getExternalStorageDirectory().getAbsolutePath();//루트 디렉토리의 주소를 얻어서 mRoot에 저장해준다.
    private String mTemp;//mPath의 값을 잠시 맡아서, 존재하지 않는 디렉토리 탐색을 시도할시, 이 mTemp를 이용해 돌아온다.
    public EditText tempPath;//입력 받는 부분이다. SUI를 사용하기 애매할때를 대비한... 입력을 받는 곳이다.

    private TextView mPath;//파일 탐색기에서, 탐색을 위한 Path를 저장하는 곳.

    //퍼미션 획득을 위한 변수 선언.
    private static final int REQUEST_CODE_EXTERNAL_READ=0;
    private static final int REQUEST_CODE_RECORD_AUDIO=1;

    private Button button1;
    private int round=0;
    private String[] voiceCheck;

    private String[] orderList={"보기","닫기","실행","이동","뒤로"};
    private String[] order;
    //
    private String[] object;

    Intent i;//시스템 콜을 요청하기위한, Intent 객체를 선언한다.
    SpeechRecognizer mRecognizer;//음성인식을 위한 SpeechRecognizer 객체를 선언한다.



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//가장 중요한 함수, 이 아래 부분에서 초기화 하는 View(화면에 보이는 요소들)을 acitivity_main.xml(layout의)과 연동시키기 위해선...
                                               //이 함수가 activity_main 에 있는 View 성분들을 불러오기 때문에, 이 함수가 실행되기 전에 초기화하면 아무 의미가 없다.

        //화면에 보여주는 역할을 담당하는, xml의 구성 요소들을 변수에 옮겨 담는 역할을 한다.(setContentView로 View를 담고있는 xml을 가져왔다.)
        mPath = (TextView) findViewById(R.id.tvPath);//TextView인, tvPath에서 보여주는 값을 지정한다. 일단 경로 값을 담당하고, 현재 어떤 경로를 탐색하고 있는지 보여주는 역할이다.
        lvFileControl = (ListView) findViewById(R.id.lvFileControl);
        tempPath=(EditText)findViewById(R.id.editText);
        button1=(Button)findViewById(R.id.button);//여기까지, xml에서 화면에 보여주는 성분들(View)을 변수로 옮겨 담아왔다.


        //---------------------------음성인식
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);//이 시스템 콜은, ACTION_RECOGNIZE_SPEECH 즉 음성인식을 요청한다는 것을 알려준다
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());//그리그 이 i라는 시스템콜에 함께 전해줄 정보들을 추가한다.(Extra 정보라고 한다.)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");//언어 설정은 한국어

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);//음성인식 상황시에 처리해야할 함수를 설정한다. listener는 함수다.

        //-----------------------------

        similar=new forSimilarFile[100];
        similarCount=0;

        for(int i=0;i<similar.length;i++){
            similar[i]=new forSimilarFile();
        }//객체 초기화.

        //어플이 실행될때, 저장공간을 읽는 퍼미션을 얻는다.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(this,"Read/Write external storage",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},REQUEST_CODE_EXTERNAL_READ);
            }
        }

        //명령어 받는 곳 (버튼 1이 클릭 됐을때 실행되는 함수)
       button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempPath.setText(voiceCheck[0]);

                order=new String[voiceCheck.length];
                object=new String[voiceCheck.length];
                isThereOrder();
                if(round==0&&order[0].equals(orderList[0])){//현재 완성된 상황에선, 인식된 사운드의 텍스트 변환 값을 tempPath에 전달한다.
                    getDir(mRoot);                                         //tempPath(eidtText)에 직접 입력을 하거나, 사운드로 tempPath에 값을 전달하고... 버튼을 클릭하면?
                    round=1;
                }else if(round==1&&order[0].equals(orderList[1])){//.equals("  ")함수로 텍스트 값과 비교한다.//닫기인 경우.
                    IItem.clear();
                    lvFileControl.deferNotifyDataSetChanged();
                    mPath.setText("입력하세요");
                    round=0;
                    //보기와 닫기는, 명령어 사이에 '공백'이 없다는 것을 알 수 있다.
                }else if(round==1&&(!itHasOrder())) {//round=1 일땐, 현재 폴더의 내용물을 보고 있는 상태다.(처음 시작은 당연히 Root) 이 상황에서 입력을 더 받는다면....
                    //round=1이고, 명렁어랑 상관 없는 놈이 들어왔다?!
                    File file = null; //File 클래스는, "위치"와 "파일 이름"(혹은 폴더 이름)으로 할당 가능.
                    int MaxCount=0;
                    boolean find=false;
                    for (int i = 0; i < voiceCheck.length; i++) {
                        tempPath.setText(voiceCheck[i]);
                        file = new File(mPath.getText().toString(), tempPath.getText().toString());

                        if (file.isDirectory() || file.isFile()) {//file이거나 디렉토리이면 빠져나오게...
                            find=true;
                            break;
                        }
                        //마지막까지 못찾았다면, find는 계속 false다.
                    }
                    if(find==false){//확실한 이름의 파일도 아니고, 디렉토리도 아니다? 그러면 비슷한거라도 찾아보자.
                        similarFinding2(voiceCheck,mPath.getText().toString());
                        if(similarCount!=0){
                            MaxCount=similar[0].how;
                            for(int i=0;i<similarCount-1;i++){
                                if(similar[i+1].how>similar[i].how){
                                    MaxCount=similar[i+1].how;
                                }//최대로 비슷한 파일들을 찾는다.
                            }
                        }

                        for(int i=0;i<similarCount;i++){
                            if(similar[i].how==MaxCount){//가장 비슷한 놈중 첫번째를 찾아 바로 지정해주고.
                                file=similar[i].similarFile;
                                similarCount=0;//다시 0으로 만들어주자.
                                break;
                            }
                        }
                    }
                    fileConnection(file);
                }else if(round==1 &&order[0].equals(orderList[4])){
                    File file=null;
                    file=new File(mPath.getText().toString());
                    if(file.getAbsolutePath()!=mRoot){
                        getDir(file.getParent());
                    }else{
                        Toast.makeText(mContext,"이미 루트 디렉토리입니다.",Toast.LENGTH_SHORT).show();
                    }
                }else if(order[0].equals(orderList[3])){//명령어가 "이동"인 경우를 일단 구현해 보자.
                    round=1;
                    File file=null;
                    String a=mRoot+"/"+object[0];
                    for(int i=0;i<object.length;i++){
                        if(object[i]!=null){
                            a=mRoot+"/"+object[i];
                            file=new File(a);
                            if(file.isDirectory()){
                                break;
                            }
                        }else{
                            break;
                        }
                    }
                    if (file.isDirectory()) {
                        if (file.canRead())
                            getDir(a);
                    }else{
                        Toast.makeText(mContext, "디렉토리가 아닙니다.", Toast.LENGTH_SHORT).show();
                    }
                }else if(order[0].equals(orderList[2])){//명령어가 "실행"인 경우.
                    File file=similarFinding(object,mRoot);
                    int MaxCount=0;
                    if(file==null) {//완전히 동일한 파일이 없다면, 비슷한 이름의 파일을 찾아본다.
                        similarFinding2(object,mRoot);
                    }
                    if(similarCount!=0){
                        MaxCount=similar[0].how;
                        for(int i=0;i<similarCount-1;i++){
                            if(similar[i+1].how>similar[i].how){
                                MaxCount=similar[i+1].how;
                            }//최대로 비슷한 파일들을 찾는다.
                        }
                    }

                    for(int i=0;i<similarCount;i++){
                        if(similar[i].how==MaxCount){//가장 비슷한 놈중 첫번째를 찾아 바로 지정해주고.
                            file=similar[i].similarFile;
                            similarCount=0;//다시 0으로 만들어주자.
                            break;
                        }
                    }
                    if(file==null){
                        return;
                    }
                    fileConnection(file);
                }else{
                    tempPath.setText(voiceCheck[0]);
                    return;
                }

            }
        });

        lvFileControl.setOnItemClickListener(new AdapterView.OnItemClickListener() { //사실 이부분은, ListView에서 보여주고 있는 ArrayList의 원소중 하나를 클릭하면 반응하는 함수다.
            @Override                                                                //SUI와 CUI 기반으로 만드는 코드이므로, 나중에 삭제될 여지가 있는 부분이다.(터치해서 반응하면 그건 GUI)
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = new File(IPath.get(position));

                if (file.isDirectory()) {
                    if (file.canRead())
                        getDir(IPath.get(position));//IItem가 같은 배열 위치(position)에 있는 실제의 경로를 가지고 있는 IPath로부터 경로 값을 가져온다. 그리고 그 경로값으로 다시 getDir()함수를 실행한다.
                    else {
                        Toast.makeText(mContext, "No files in this folder.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    fileConnection(file);
                }
            }
        });

    }//여기까지가 전부 onCreate 함수 내에서 정의되는 것들.

    //명령어 관련 함수들..


    public boolean itHasOrder(){
        for(int i=0;i<order.length;i++){
            for(int j=0;j<orderList.length;j++){
                if(order[i]==null){
                    break;
                }
                if(order[i].equals(orderList[j])){
                    return true;
                }
            }
        }
        return false;
    }
    public void isThereOrder(){
        //1. voiceCheck[]들을 subString해서 명령어가 있는지 파악해보자.
        String imsiOrder=null;
        String imsiObject=null;
        int index;


        //명령어가 있으면, order와 명령어를 구분한다.

        for(int i=0;i<voiceCheck.length;i++){
            if(i>4){
                break;
            }
            index=voiceCheck[i].indexOf(' ');

            if(index!=-1){//공백이 있으면, 앞부분을 명령어, 뒷부분을 대상이라고 생각한다.
                imsiOrder=voiceCheck[i].substring(0,index);
                imsiObject=voiceCheck[i].substring(index+1);
                for(int j=0;j<orderList.length;j++){
                    if(imsiOrder.equals(orderList[j])){
                        order[i]=imsiOrder;
                        object[i]=imsiObject;
                    }
                }
                if(order[i]==null){//공백이 있는데, 명령어에 해당이 안될 경우.
                    order[i]=voiceCheck[i];
                    object[i]=voiceCheck[i];
                }
            }else{
                order[i]=voiceCheck[i];//공백이 없으면, 그냥 그 자체로 명령어라 생각한다.
                object[i]=voiceCheck[i];
            }
        }
    }



    //==========================================파일 관련.
    public static String getExtension(String fileStr) {
        return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
    }

    public File similarFinding(String []object,String dirPath){//완벽히 일치하는 파일을 찾는다.
        File file=new File(dirPath);//처음엔 Root로 설정을 한 뒤에 탐색...
        File getFile=null;
        File [] files=file.listFiles();
        String fileName;

        for(int i=0;i<files.length;i++){
            if(getFile!=null){
                return getFile;
            }
            if(files[i].isDirectory()) {//파일이 폴더면 일단 들어가자.
                getFile=similarFinding(object,files[i].getAbsolutePath());
            }else if(files[i].isFile()) {
                int index=files[i].getName().lastIndexOf('.');
                if(index<=0){//이런 썩을놈의....
                    break;
                }
                fileName=files[i].getName().substring(0,index);
                if(object[0].equals(fileName)){
                    return files[i];
                }
            }
        }
        return null;
    }

    public void similarFinding2(String []object,String dirPath){
        File file=new File(dirPath);
        File[] files=file.listFiles();
        String fileName;
        int how;
        for(int i=0;i<files.length;i++){
            if(files[i].isDirectory()){
                similarFinding2(object,files[i].getAbsolutePath());
            }else if(files[i].isFile()){
                int index=files[i].getName().lastIndexOf('.');
                if(index<=0){
                    break;
                }
                fileName=files[i].getName().substring(0,index);
                if(0<(how=similarThing(fileName,object[0]))){
                    try{
                        similar[similarCount].how=how;
                        similar[similarCount++].similarFile=files[i];
                    }catch(ArrayIndexOutOfBoundsException e){

                        return;//함수 강제 종료.
                    }

                }
            }
        }
    }
    private int similarThing(String first,String second){//첫번째 문자열과 두번째 문자열의 비슷함을 파악해서 실행.
        String [] stringBottle=new String[second.length()];//문자의 갯수만큼 string배열을 만든다.
        String firstChanged=first.toLowerCase();
        int count=0;
        int subCount=second.length()/2;
        for(;count<stringBottle.length;count++){
            stringBottle[count]=second.substring(0,count+1);
        }

        for(int i=count-1;i>=0;i--){
            if(firstChanged.contains(stringBottle[i])){
                return i+1;
            }
            subCount--;
            if(subCount<0){//반 이상 틀리면 의미가 없어. 걍 삭제해 버린다.
                break;
            }
        }

        return 0;

    }
    private void fileConnection(File file){//나도 뭔 원리인줄 모르겠...나중에 다시 보자.

            if (file.isDirectory()) {//일단 할당된 것이 "폴더"일 경우.
                if (file.canRead()){
                    mTemp=mPath.getText().toString();//mTemp에 mPath(원래의 디렉토리 경로)를 저장해 놓고.
                    String a=mPath.getText()+"/"+tempPath.getText();//원래의 디렉토리에, 입력한(이동하려는) 폴더 이름을 추가한후.
                    getDir(a);//목록을 불러오는 함수를 실행한다.
                }
                else {
                    Toast.makeText(mContext, "No files in this folder.", Toast.LENGTH_SHORT).show();
                }
            } else if(file.isFile()){
                mFileName = file.getName();
                // TODO Auto-generated method stub
                Intent fileLinkIntent = new Intent(Intent.ACTION_VIEW);
                fileLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);//카테고리를 사용할 거란것을 명시.
                fileLinkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//새로운 Task에 Activity를 띄우겠다...
                String fileExtend =file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")+1);
                // 파일 확장자 별로 mime type 지정해 준다.
                if (fileExtend.equalsIgnoreCase("mp3")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file), "audio/*");//audio 카테고리로 설정.
                } else if (fileExtend.equalsIgnoreCase("mp4")
                        ||fileExtend.equalsIgnoreCase("avi")
                        ||fileExtend.equalsIgnoreCase("wmv")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file), "vidio/*");//video파일인 경우,video 카테고리 추가.
                } else if (fileExtend.equalsIgnoreCase("jpg")
                        || fileExtend.equalsIgnoreCase("jpeg")
                        || fileExtend.equalsIgnoreCase("gif")
                        || fileExtend.equalsIgnoreCase("png")
                        || fileExtend.equalsIgnoreCase("bmp")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file), "image/*");
                } else if (fileExtend.equalsIgnoreCase("txt")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file), "text/*");
                } else if (fileExtend.equalsIgnoreCase("doc")
                        || fileExtend.equalsIgnoreCase("docx")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/msword");
                } else if (fileExtend.equalsIgnoreCase("xls")
                        || fileExtend.equalsIgnoreCase("xlsx")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file),
                            "application/vnd.ms-excel");
                } else if (fileExtend.equalsIgnoreCase("ppt")
                        || fileExtend.equalsIgnoreCase("pptx")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file),
                            "application/vnd.ms-powerpoint");
                } else if (fileExtend.equalsIgnoreCase("pdf")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
                } else if (fileExtend.equalsIgnoreCase("hwp")) {
                    fileLinkIntent.setDataAndType(Uri.fromFile(file),
                            "application/haansofthwp");
                }else{//없는 형식의 파일이면 그냥 함수를 강제 종료 시켜버리자/
                    Toast.makeText(this,"인식 불가능한 파일 형식입니다.",Toast.LENGTH_SHORT);
                    return;
                }
                PackageManager pm = getApplicationContext().getPackageManager();
                List<ResolveInfo> list = pm.queryIntentActivities(fileLinkIntent,
                        PackageManager.GET_META_DATA);
                //PackageMamager의 queryIntentActivities(intent,?);는 intent의 action과 category에 해당하는 모든 APP 목록을 얻어온다.
                if (list.size() == 0) {
                    Toast.makeText(getApplicationContext(), mFileName + "을 확인할 수 있는 앱이 설치되지 않았습니다.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    tempPath.setText(voiceCheck[0]);
                    getApplicationContext().startActivity(fileLinkIntent);
                }
            }else{
                tempPath.setText(voiceCheck[0]);//실행에 실패했을 때, 그래도 가장 가까운걸 보여준다.
                Toast.makeText(getApplicationContext(),"이런건 아예 없습니다.",Toast.LENGTH_SHORT).show();
            }

    }


    //dirPath로 넘겨진 경로값을 기준으로,File(Directory)의 이름과 경로에 관련된 ArrayList를 만들어 ListView에 삽입해 보여주는 함수.
    private void getDir(String dirPath){
        mPath.setText(dirPath);
        IItem=new ArrayList<String>();//ArrayList를 만든다. IItem 에는, 파일들의 이름이 차례대로 들어간다. ex)(폴더 이름: myDirectory)(배열의 3번째 값)
        IPath=new ArrayList<String>();//ArrayList를 만든다. IPath 에는, 파일들의 경로가 차례대로 들어간다. ex)(경로:root/~/~/myDirectory)(배열의 3번째 값)
        File f=new File(dirPath);
        File[] files=f.listFiles();

        if(order[0].equals("이동")&&files==null){
            mPath.setText(mRoot);
            return;
        }
        if(files==null){//getDir(String dirPath)에서 건내지는 dirPath의 값이 존재하지 않을때... files==null이 된다.
            mPath.setText(mTemp);//mTemp엔, mPath가 변하기 전의 값이 저장돼 있다. mPath를 다시 전값으로 돌려준다.
            f=new File(mTemp);
            files=f.listFiles();
            if(!mTemp.equals(mRoot)){
                IItem.add("../");//만약 mRoot(루트 디렉토리)가 아니라면, 루트 디렉토리까지 돌아갈 수 있게 "../"를 목록에 추가한다.
                IPath.add(f.getParent());//IItem에 값이 추가될 때마다, IPath에는 IItem과 같은 위치에 IItem의 실제 경로값을 저장한다.
                                         //만약 "../"이 배열의 첫번째 원소라면, 이 "../"를 클릭했을때, 클릭에 대한 액션 리스너는 IPath의 값을 토대로 반응하고, "../"이 뜻하는 경로를 얻을 수 있다.
                                         //즉, f.getParent(), 현재 디렉토리의 부모 디렉토리 값을 전달받는다.
            }
            for(int i=0;i<files.length;i++){
                File file=files[i];
                IPath.add(file.getAbsolutePath());
                if(file.isDirectory())
                    IItem.add(file.getName()+"/");
                else
                    IItem.add(file.getName());
            }
            ArrayAdapter<String> fileList=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,IItem); //ArrayList(IItem)를 담기위한, Array Adapter, fileList를 만들어준다.
            lvFileControl.setAdapter(fileList);//ListView인 lvFileControl,에 이 ArrayAdapter를 setAdapter()시켜준다.
            Toast.makeText(this,"그런 디렉토리 없습니다.",Toast.LENGTH_SHORT).show();
        }else{
            if(!dirPath.equals(mRoot)){//아래의 코드는 위와 같다. 단, mPath는 잘못된 값이 아니므로, 다시 설정해줄 필요가 없다.
                IItem.add("../");      //mPath가 잘못된 값이 아니므로, FILE f도 FILE[] files도 잘못된 값을 입력받지 않아 다시 설정해줄 필요가 없다.
                IPath.add(f.getParent());
            }
            for(int i=0;i<files.length;i++){
                File file=files[i];
                IPath.add(file.getAbsolutePath());
                if(file.isDirectory())
                    IItem.add(file.getName()+"/");
                else
                    IItem.add(file.getName());
            }
            ArrayAdapter<String> fileList=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,IItem);
            lvFileControl.setAdapter(fileList); //ListView에 .setAdapter 시켜준다.
        }
    }



    //===================================================================음성인식에 관련된 함수들.
    public void onButton2Clicked(View v){//이 onButton2Clicked가 음성인식 이미지 버튼에 연동되는 함수다.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {//RECORD_AUDIO의 권한을 얻는다. 한번 얻으면, if문이 걸러진다.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {//퍼미션 설명을 해주고
                Toast.makeText(this, "for RECORD_AUDO permission needed", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO);//requestPermissions를 해준다.
                //전달 값 중 String[]부분은, 요구하는 접근권한의 목록을 담는 배열이다.(동시에 여러개의 Permission을 요구할수 있다.)
                //여기선, String[]에 RECORD_AUDIO에 관한 퍼미션만 요구하고, 요구할 때의 상황코드를 전달한다.(이 전달 값은 위에서 constant 변수로 선언해 놓았다.)
            }
        }
        mRecognizer.startListening(i);
    }


    private RecognitionListener listener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {//굳이 필요 없을듯해서 비웠다.
        }
        @Override
        public void onBeginningOfSpeech() {//버튼을 누르고 음성 인식을 받고 있을때 실행되는 함수다
                                           //처음에 "입력하세요" 라고 Toast 문자열을 띄웠는데.... 이 "입력하세요"가 떴을때즈음 이미 음성을 말했어야 해서 그냥 없앴다.
                                          //조금 늦게 뜨더라.
        }
        @Override
        public void onRmsChanged(float rmsdB) {//이 rmsdB값은 일단 정해져 있다고 한다, 이게 뭐냐면 인식이 끝났다고 생각하는 최저 음역??
                                               //즉 목소리가 어느정도 안들린다 싶으면, "아 입력이 끝났구나" 라는걸 생각한다고 한다....
                                               //이걸 수정하면 잡음을 무시하고, 입력만 받게 할 수 있겠지만....굳이 수정 안해도 일단은 잘된다.

        }
        @Override
        public void onBufferReceived(byte[] buffer) {
            Toast.makeText(getApplicationContext(),"뭐지이건",Toast.LENGTH_SHORT).show();//뭔지 모르겠다.
        }
        @Override
        public void onEndOfSpeech() {//음성인식을 끝냈을때...
            Toast.makeText(getApplicationContext(),"입력 완료",Toast.LENGTH_SHORT).show();
            //Toast는 말 그대로, 토스트처럼 쉽게 만들어서 띄우는 함수다.
            //Toast.makeText 함수로, getApplicationContext()를 하면, 현재의 화면을 의미하고
            //"입력 완료" 부분은 잠시 띄울 문자열을 입력받고
            //Toast.LENGTH_SHORT는 문자열을 띄울 시간을 설정한다. 저건 이미 정해진 값이고, 직접 숫자를 입력해도 된다.
            //마지막에 .show() 함수로 이 간단히(Toast처럼)만든 객체를 보여준다. -->"입력 완료" 라고 뜨게 된다.

        }
        @Override
        public void onError(int error) {//에러 뜰때만 나올줄 알았는데, 매번 에러 난다고 한다. 그래서 그냥 비웠다.
        }
        @Override
        public void onResults(Bundle results) {//결과 값을 뱉을때 작동하는 함수이다.
            String key = "";//일단 "key"를 초기화하고(아무 값으로)
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);//mResult라는 <String> 리스트에다 음성인식 결과 값들을 담는다.
            voiceCheck = new String[mResult.size()];//이번엔 진짜 String, 문자열 배열을 음성인식 결과의 길이(만약 헬로! 라고 말했으면, 헬로 핼로 할로 이렇게 세개가 비슷하다고 하면..mResult의 길이는 3이다.)
            mResult.toArray(voiceCheck);                     //만큼 만들고... mResult(음성인식 결과를 담은 ArrayList)의 값들을 이 길이만큼 생성한 rs, String 배열에 넣어준다.

            tempPath.setText("입력 완료");              //이 헬로 핼로 할로 중 가장 근접하다 생각되는 것일수록 앞쪽 배열에 입력 되기 때문에, rs[0]이라 하면 가장 근접하다 생각되는 값이다.
                                                     //이 rs[0]값으로 tempPath값을 설정한다.
        }
        @Override
        public void onPartialResults(Bundle partialResults) {

        }
        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };



    //퍼미션 관련


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {//퍼미션을 요구한뒤, 사용자의 응답이 전해졌을때의 반응이다.
        switch(requestCode){
            case REQUEST_CODE_EXTERNAL_READ://처음 실행될 때, 저장소를 읽는 권한을 얻는 경우.
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){//permission Success
                    break;//일단 허락을 받았으니 아무 상관 없다.
                }else{
                    Toast.makeText(this,"permissionDeny",Toast.LENGTH_SHORT).show();//허락 못받으면 일단 못받았다고 알려는 준다.
                    break;
                }
            case REQUEST_CODE_RECORD_AUDIO://음성인식 버튼을 눌렀을 때, 이 어플에서 음성인식을 처음 시도할때....
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){//허락을 받았으니 아무 상관 없다.
                    break;
                }else{
                    Toast.makeText(this,"permissionDeny",Toast.LENGTH_SHORT).show();//못 받았으면 못받았다고 말한다.
                    break;
                }
        }
    }
}
