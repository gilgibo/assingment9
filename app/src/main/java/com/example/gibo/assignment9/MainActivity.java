package com.example.gibo.assignment9;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    LinearLayout linear1, linear2;
    EditText et;
    DatePicker dp;
    ListView listView;
    TextView tvCount;
    Button btnsave;
    ArrayList<String> stringdata = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    String dateinfo = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linear1 = (LinearLayout) findViewById(R.id.linear1);
        linear2 = (LinearLayout) findViewById(R.id.linear2);
        tvCount = (TextView)findViewById(R.id.tvCount);
        btnsave = (Button)findViewById(R.id.btnsave);
        et = (EditText) findViewById(R.id.et);
        dp = (DatePicker) findViewById(R.id.dp);
        listView = (ListView) findViewById(R.id.listview);

        checkpermission();
        deletAll();

        File file = new File(getExternalPath() + "diary");
        file.mkdir();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringdata);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("삭제").setMessage("삭제 하시겠습니까?").setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(getExternalPath()+ "diary/"+stringdata.get(position)+".txt");
                        file.delete();
                        stringdata.remove(position);
                        sort();
                        setCount();
                    }
                }).setPositiveButton("취소",null).show();
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("저장 내용").setMessage("현재 저장 내용 \n" + getMessage(stringdata.get(position))).setNegativeButton("수정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        linear1.setVisibility(View.INVISIBLE);
                        linear2.setVisibility(View.VISIBLE);
                        btnsave.setText("수정");
                        setDate(stringdata.get(position));
                    }
                }).setPositiveButton("완료", null).show();
            }
        });
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn1) {
            linear1.setVisibility(View.INVISIBLE);
            linear2.setVisibility(View.VISIBLE);
        }
        if (v.getId() == R.id.btnsave) {
            String date = changeFormat(dp.getYear() + "년" + (dp.getMonth() + 1) + "월" + dp.getDayOfMonth() + "일");
            final String path = getExternalPath()+ "diary/" + date + ".memo.txt";
            if (btnsave.getText().toString().equals("저장")) {
                if (stringdata.size() == 0 || !stringdata.contains(date + ".memo")) {
                    setClearWrite(path, et.getText().toString());
                    stringdata.add(date + ".memo");
                }
                else if(stringdata.contains(date + ".memo")){
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);

                    dlg.setMessage("이미 해당 날짜에 "+ getMessage(date+".memo") + "내용이 있습니다. \n 그래도 수정 하시겠습니까?")
                            .setPositiveButton("수정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btnsave.setText("수정");
                            setDate(changeFormat(dp.getYear() + "년" + (dp.getMonth() + 1) + "월" + dp.getDayOfMonth() + "일")+".memo");
                        }
                    }).setNegativeButton("취소",null).show();
                }
            }
            if(btnsave.getText().toString().equals("수정")) {
                if (getDate().equals(changeFormat(dp.getYear() + "년" + (dp.getMonth() + 1) + "월" + dp.getDayOfMonth() + "일")+".memo")) {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);

                    dlg.setTitle("수정 방법 선택").setNegativeButton("덮어 쓰기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setClearWrite(path, et.getText().toString());
                        }
                    }).setPositiveButton("이어 쓰기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setAddWrite(path, et.getText().toString());
                        }
                    }).show();
                }else{
                    if(stringdata.contains(changeFormat(dp.getYear() + "년" + (dp.getMonth() + 1) + "월" + dp.getDayOfMonth() + "일")+".memo")){
                        Toast.makeText(getApplicationContext(),"이미 해당 날짜에 메모가 존재 합니다. 다른 날짜를 선택해 주세요.",Toast.LENGTH_SHORT).show();
                    }else {
                        stringdata.remove(stringdata.indexOf(getDate()));
                        File file = new File(getExternalPath() + "diary/" + getDate() + ".txt");
                        file.delete();
                        setClearWrite(path, et.getText().toString());
                        stringdata.add(date + ".memo");
                        btnsave.setText("저장");
                    }
                }
            }
        }
        if (v.getId() == R.id.btncancel) {
            btnsave.setText("저장");
            sort();
            setCount();
            linear2.setVisibility(View.INVISIBLE);
            linear1.setVisibility(View.VISIBLE);
        }
    }

    Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    public void sort() {
        Collections.sort(stringdata,comparator);
        adapter.notifyDataSetChanged();}

    public void deletAll(){
        File[] files = new File(getExternalPath()+"diary/").listFiles();
        for(File f : files)
            f.delete();
    }

    public void setCount(){
        int cnt = 0;
        File[] files = new File(getExternalPath()+"diary/").listFiles();

        for(File f : files){
            cnt++;
        }

        tvCount.setText("등록된 메모 개수: "+cnt);
    }

    public String getMessage(String date){
        try {
            String path = getExternalPath();
            BufferedReader br = new BufferedReader(new FileReader(path +  "diary/" + date + ".txt"));
            String readStr = "";
            String str = null;
            while ((str = br.readLine()) != null) readStr += str + "\n";
            br.close();
            return readStr;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "File not found";
        } catch (IOException e) {
            e.printStackTrace();
            return "File not found";
        }
    }

    public void setClearWrite(String path, String txt){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path, false));
            bw.write(txt);
            bw.close();
            Toast.makeText(this, "저장완료", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage() + ":" + getFilesDir(), Toast.LENGTH_SHORT).show();
        }
    }

    public void setAddWrite(String path, String txt){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));
            bw.write(txt);
            bw.close();
            Toast.makeText(this, "저장완료", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage() + ":" + getFilesDir(), Toast.LENGTH_SHORT).show();
        }
    }

    public void setDate(String dateinfo){
        this.dateinfo = dateinfo;
    }

    public String getDate(){
        return dateinfo;
    }

    public void checkpermission(){
        int permissioninfo = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permissioninfo == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "SDCard 쓰기  권한 있음", Toast.LENGTH_SHORT).show();
        }
        else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(getApplicationContext(), "권한의 필요성 설명", Toast.LENGTH_SHORT).show();
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults){

        String str = null;
        if(requestCode == 100){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                str = "SD Card 쓰기권한 승인";
            else str = "SD Card 쓰기권한 거부";
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
    }


    public String getExternalPath() {
        String sdPath = "";
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        } else
            sdPath = getFilesDir() + "";
        return sdPath;
    }

    public String changeFormat(String str) {
        SimpleDateFormat origin_format = new SimpleDateFormat("yyyy년M월d일");
        SimpleDateFormat new_format = new SimpleDateFormat("yy-MM-dd");
        try {
            Date original_date = origin_format.parse(str);
            String new_date = new_format.format(original_date);
            return new_date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}