package com.ninetwo.boot.util.pdftowordSprire;

import com.ninetwo.boot.util.FileUtils;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.widget.PdfPageCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class PdfToWord {

	@Autowired
	private FileUtils fileUtils;

	String splitPath = "";
	String docPath = "";

	public boolean pdftoword(String  srcPath) {
		String splitPath = fileUtils.getSplitPath();
		String docPath = fileUtils.getDocPath();
		// 4¡¢×îÖÕÉú³ÉµÄdocËùÔÚµÄÄ¿Â¼£¬Ä¬ÈÏÊÇºÍÒýÈëµÄÒ»¸öµØ·½£¬¿ªÔ´Ê±¶ÔÍâÌá¹©ÏÂÔØµÄ½Ó¿Ú¡£
		String desPath = srcPath.substring(0, srcPath.length()-4)+".docx";
		boolean result = false;
		try {
			// 0¡¢ÅÐ¶ÏÊäÈëµÄÊÇ·ñÊÇpdfÎÄ¼þ
			//µÚÒ»²½£ºÅÐ¶ÏÊäÈëµÄÊÇ·ñºÏ·¨
			boolean flag = isPDFFile(srcPath);
			//µÚ¶þ²½£ºÔÚÊäÈëµÄÂ·¾¶ÏÂÐÂ½¨ÎÄ¼þ¼Ð
			boolean flag1 = create();
			
			if (flag && flag1) {
				// 1¡¢¼ÓÔØpdf
				PdfDocument pdf = new PdfDocument();
				pdf.loadFromFile(srcPath);
				PdfPageCollection num = pdf.getPages();

				//zc  ²âÊÔÏÂÀ´£¬Ã»ÓÐÒ³ÊýÏÞÖÆ£¬ÔÝ²»²ð·Ö¡£ £¨²ð·ÖÇé¿öÏÂ£¬winÉÏÕý³££¬linuxÉÏ´óÓÚ10Ò³»á±¨´í£©
				pdf.saveToFile(desPath, com.spire.pdf.FileFormat.DOCX);


				// 2¡¢Èç¹ûpdfµÄÒ³ÊýÐ¡ÓÚ11£¬ÄÇÃ´Ö±½Ó½øÐÐ×ª»¯
//				if (num.getCount() <= 10) {
//					pdf.saveToFile(desPath, com.spire.pdf.FileFormat.DOCX);
//				}
//				// 3¡¢·ñÔòÊäÈëµÄÒ³Êý±È½Ï¶à£¬¾Í¿ªÊ¼½øÐÐÇÐ·ÖÔÙ×ª»¯
//				else {
//					// µÚÒ»²½£º½«Æä½øÐÐÇÐ·Ö,Ã¿Ò³Ò»ÕÅpdf
//					pdf.split(splitPath+"test{0}.pdf",0);
//
//					// µÚ¶þ²½£º½«ÇÐ·ÖµÄpdf£¬Ò»¸öÒ»¸ö½øÐÐ×ª»»
//					File[] fs = getSplitFiles(splitPath);
//					for(int i=0;i<fs.length;i++) {
//						PdfDocument sonpdf = new PdfDocument();
//						sonpdf.loadFromFile(fs[i].getAbsolutePath());
//						sonpdf.saveToFile(docPath+fs[i].getName().substring(0, fs[i].getName().length()-4)+".docx",FileFormat.DOCX);
//					}
//					//µÚÈý²½£º¶Ô×ª»¯µÄdocÎÄµµ½øÐÐºÏ²¢£¬ºÏ²¢³ÉÒ»¸ö´óµÄword
//					try {
//						result = MergeWordDocument.merge(docPath, desPath);
//						System.out.println(result);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//				}
			} else {
				System.out.println("ÊäÈëµÄ²»ÊÇpdfÎÄ¼þ");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			//4¡¢°Ñ¸Õ¸Õ»º´æµÄsplitºÍdocÉ¾³ý
			if(result==true) {
				new FileDeleteTest().clearFiles(splitPath);
				new FileDeleteTest().clearFiles(docPath);
			}
		}
		return true;
	}


	private  boolean create() {
		File f = new File(splitPath);
		File f1 = new File(docPath);
		if(!f.exists() )  f.mkdirs();
		if(!f.exists() )  f1.mkdirs();
		return true;	    
	}

	// ÅÐ¶ÏÊÇ·ñÊÇpdfÎÄ¼þ
	private  boolean isPDFFile(String srcPath2) {
		File file = new File(srcPath2);
		String filename = file.getName();
		if (filename.endsWith(".pdf")) {
			return true;
		}
		return false;
	}

	// È¡µÃÄ³Ò»Â·¾¶ÏÂËùÓÐµÄpdf
	private  File[] getSplitFiles(String path) {
		File f = new File(path);
		File[] fs = f.listFiles();
		if (fs == null) {
			return null;
		}
		return fs;
	}

}
