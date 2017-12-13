package com.witsi.setting.hardwaretest;

import java.io.File;
import java.util.ArrayList;

import com.witsi.adapter.MyAdapter;
import com.witsi.setting1.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class FileExplorerActivity extends ListActivity {
    private static final String ROOT_PATH = "/mnt/sdcard";  
    
    private ArrayList<String> names = null;  
    private ArrayList<String> paths = null;

    /** Called when the activity is first created. */  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.hardware_file_explorer_activity);  
        showFileDir(ROOT_PATH);  
    }
    
    private void showFileDir(String path){  
        names = new ArrayList<String>();  
        paths = new ArrayList<String>();  
        
        File file = new File(path);  
        File[] files = file.listFiles();  
          
        if (!ROOT_PATH.equals(path)){  
            names.add("@1");  
            paths.add(ROOT_PATH);  
              
            names.add("@2");  
            paths.add(file.getParent());  
        }  

        for (File f : files){  
        	if(f.isDirectory() || f.isFile()) { // 只显示文件与目录,不显示链接
        		names.add(f.getName());  
        		paths.add(f.getPath());
        	}
        }  
        this.setListAdapter(new MyAdapter(this,names, paths));  
    }  
    
    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {  
        String path = paths.get(position);  
        File file = new File(path);  

        if (file.exists() && file.canRead()){  
            if (file.isDirectory()){  
                showFileDir(path);  
            }  
            else{  
            	Intent intent = new Intent(FileExplorerActivity.this, EntryActivity.class);
            	intent.putExtra("path", file.getPath());
            	setResult(RESULT_OK, intent);
            	finish();
            }  
        }  
        else{  
            Resources res = getResources();  
            new AlertDialog.Builder(this).setTitle("Message")  
            .setMessage(res.getString(R.string.no_permission))  
            .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialog, int which) {  
                      
                }  
            }).show();  
        }  
        super.onListItemClick(l, v, position, id);  
    }  
}	
	
	
	

