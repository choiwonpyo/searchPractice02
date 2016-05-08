package com.example.wp.searchpractice02;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
//this is my love song
//im' jnkgu
//hello baby this is practice
//fucking git
public class MainActivity extends AppCompatActivity {

    private String mFileName;
    private ListView lvFileControl;
    private Context mContext=this;

    private List<String> IItem=null;
    private List<String> IPath=null;
    private String mRoot= Environment.getExternalStorageDirectory().getAbsolutePath();
    private String mTemp;
    private TextView mPath;
    private static final int REQUEST_CODE_EXTERNAL_READ=0;
    public EditText tempPath;
    private Button button1;
    private LinearLayout list;
    private int round=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mPath = (TextView) findViewById(R.id.tvPath);
        lvFileControl = (ListView) findViewById(R.id.lvFileControl);
        tempPath=(EditText)findViewById(R.id.editText);
        button1=(Button)findViewById(R.id.button);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(this,"Read/Write external storage",Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_EXTERNAL_READ);
            }
        }

        //명령어 받는 곳곳
       button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(round==0&&tempPath.getText().toString().equals("watch")){
                    getDir(mRoot);
                    round=1;
                }else if(round==1&&tempPath.getText().toString().equals("clear")){
                    IItem.clear();
                    lvFileControl.deferNotifyDataSetChanged();
                    mPath.setText("입력하세요");
                    round=0;
                }else if(round==1){
                    mTemp=mPath.getText().toString();
                    String a=mPath.getText()+"/"+tempPath.getText();
                    getDir(a);
                }

            }
        });


        lvFileControl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = new File(IPath.get(position));

                if (file.isDirectory()) {
                    if (file.canRead())
                        getDir(IPath.get(position));
                    else {
                        Toast.makeText(mContext, "No files in this folder.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mFileName = file.getName();
                    Log.i("Test", "ext: " + mFileName.substring(mFileName.lastIndexOf('.') + 1, mFileName.length()));

                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_CODE_EXTERNAL_READ:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){//permission Success
                    getDir(mRoot);
                }else{
                    Toast.makeText(this,"permissionDeny",Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    private void getDir(String dirPath){
        mPath.setText(dirPath);

        IItem=new ArrayList<String>();
        IPath=new ArrayList<String>();
        File f=new File(dirPath);
        File[] files=f.listFiles();

        if(files==null){
            mPath.setText(mTemp);
            f=new File(mTemp);
            files=f.listFiles();
            if(!mTemp.equals(mRoot)){
                //item.add(root);//to root.
                //path.add(root);
                IItem.add("../");//to parent folder
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
            lvFileControl.setAdapter(fileList);

            Toast.makeText(this,"그런 디렉토리 없습니다.",Toast.LENGTH_SHORT).show();
        }else{
            if(!dirPath.equals(mRoot)){
                //item.add(root);//to root.
                //path.add(root);
                IItem.add("../");//to parent folder
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
            lvFileControl.setAdapter(fileList);
        }
    }
}
