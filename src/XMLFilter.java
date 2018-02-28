import java.io.File;

import javax.swing.filechooser.FileFilter;


public class XMLFilter extends FileFilter{
	public boolean accept(File f){
		if (f.isDirectory()) {
			return true;
		}
	
		String extension = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 &&  i < s.length() - 1) {
			extension = s.substring(i+1).toLowerCase();
		}
		if (extension != null && extension.equals("xml")){
			return true;
		}
		return false;
	}
	
	public String getDescription(){
		return "XML Files";
	}
}
