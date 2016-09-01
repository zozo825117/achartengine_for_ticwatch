package com.example.achartmotion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import org.achartengine.chartdemo.demo.R;

/**
 * 描述：文件存储
 * 作者：Andy.Liu
 * 时间： 2012-7-9  上午08:06:23
 **/
public class FileStoreTools {
    /**保存字符串的key*/
    public static final String STRING_KEY ="string_key";
    /**保存字符串的的名字*/
    public static final String STRING_NAME ="string_name";

    public static final String FILE_NAME = "hello.txt";  //保存的文件名

    Context mContext = null;

    EditText edSave,edRead;
    Button btnSave,btnRead;

    private static final String TAG = "FileStoreTools";

    private static int writeFilePtr = 0;

/*    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = FileStoreActivity.this;
        initView();
    }*/

/*    private void initView(){
        setContentView(R.layout.store_layout);
        edSave = (EditText) findViewById(R.id.ed_edit);
        edRead = (EditText) findViewById(R.id.ed_show);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);
        btnRead = (Button) findViewById(R.id.btn_read);
        btnRead.setOnClickListener(this);
    }*/
/*    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_save:
                FileStoreTools.saveFile(edSave.getText().toString(), FileStoreTools.getSDPath()+File.separator+FILE_NAME);
                break;
            case R.id.btn_read:
                edRead.setText(FileStoreTools.readFile(FileStoreTools.getSDPath()+File.separator+FILE_NAME));
                break;

        }
    }*/

    //public class FileStoreTools{

        /**
         *
         *TODO：保存文件 根目录
         *Author：Andy.Liu
         *Create Time：2012-7-10 上午08:54:14
         *TAG：@return
         *Return：String
         */
        public static String getSDPath(){
            boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            if(hasSDCard){
                return Environment.getExternalStorageDirectory().toString();
            }else
                return Environment.getDownloadCacheDirectory().toString();
        }

        /**
         *
         *TODO：保存文件
         *Author：Andy.Liu
         *Create Time：2012-7-10 上午08:42:40
         *TAG：@param str 文件的内容
         *TAG：@param filePath 保存路径
         *Return：void
         */
        public static void saveFile(String str, String filePath ,String fileName){
            FileOutputStream fos = null;
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yy-M-dd-HH-mm");
                Calendar calendar = Calendar.getInstance();
                formatter.format(calendar.getTime());
                fileName = formatter.format(calendar.getTime())+ fileName;
                File file = new File(filePath,fileName);
                if(!file.exists())
                {
                    Log.i(TAG, "file Path = " +file.getPath());
                    //file.mkdirs();
                    file.createNewFile();
                    //writeFilePtr = 0;
                }

                OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(file, true),
                        "UTF-8");
                BufferedWriter fbw = new BufferedWriter(writer);
                fbw.write(str);
                fbw.newLine();
                fbw.flush();
                fbw.close();

                Log.i(TAG, "file lenght" + "" + file.length());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }finally{
                try {
                    if(null!=fos)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         *
         *TODO：读取文件
         *Author：Andy.Liu
         *Create Time：2012-7-10 上午08:48:40
         *TAG：@param filePath
         *TAG：@return
         *Return：String
         */
        public String readFile(String filePath){
            FileInputStream fis = null;
            byte[] mByte = new byte[512];
            try {
                fis = new FileInputStream(new File(filePath));
                fis.read(mByte);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    if(null!=fis)
                        fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return new String(mByte).toString();
        }
    //}
}
