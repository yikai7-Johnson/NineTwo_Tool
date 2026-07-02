package com.ninetwo.boot.util.pdftowordSprire;

import java.io.File;

public class FileDeleteTest {
	//É¾³ýÎÄ¼þºÍÄ¿Â¼
	public void clearFiles(String workspaceRootPath){
	     File file = new File(workspaceRootPath);
	     if(file.exists()){
	          deleteFile(file);
	     }
	}
	public void deleteFile(File file){
	     if(file.isDirectory()){
	          File[] files = file.listFiles();
	          for(int i=0; i<files.length; i++){
	               deleteFile(files[i]);
	          }
	     }
	     file.delete();
	}
}
