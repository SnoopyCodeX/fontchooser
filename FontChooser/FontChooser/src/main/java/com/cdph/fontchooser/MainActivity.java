package com.cdph.fontchooser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import com.ipaulpro.afilechooser.utils.FileUtils;

import static android.content.SharedPreferences.*;
import static android.view.View.*;
import static android.widget.Toast.*;

public class MainActivity extends Activity implements OnClickListener
{
	public static final String FONT_LOC_DIR = "Android/data/com.cdph.fontchooser/fonts";
	public static final String SAVED_FONTS_KEY = "savedFonts";
	public static final int CHOOSE_FONT_REQUEST = 21;
	
	private SharedPreferences prefs;
	private Editor editor;
	private Button importFont;
	private TextView display;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		importFont = findViewById(R.id.btn_import);
		display = findViewById(R.id.fonts);
		importFont.setOnClickListener(this);
		
		prefs = getSharedPreferences("user_fonts", MODE_PRIVATE);
		editor = prefs.edit();
		listAllSavedFonts();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
			case CHOOSE_FONT_REQUEST:
				if(resultCode == RESULT_OK)
				{
					Uri uri = data.getData();		
					saveFontFile(uri);
					listAllSavedFonts();
				}
			break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onClick(View view)
	{
		Intent intent = FileUtils.createGetContentIntent();
		startActivityForResult(Intent.createChooser(intent, "Pick"), CHOOSE_FONT_REQUEST);
	}
	
	private void saveFontFile(Uri uri)
	{
		try {
			String path = FileUtils.getPath(this, uri);
			File destination = new File(Environment.getExternalStorageDirectory(), FONT_LOC_DIR);
			File origFile = new File(path);
			File copyFile = new File(destination, origFile.getName());
			
			if(!destination.exists())
				destination.mkdirs();
			
			if(copyFile.exists())
				copyFile.delete();
			copyFile.createNewFile();
			
			FileInputStream fis = new FileInputStream(origFile);
			FileOutputStream fos = new FileOutputStream(copyFile);
			byte[] buff = new byte[1024];
			int len = 0;
			
			while((len = fis.read(buff)) != -1)
				fos.write(buff, 0, len);
			
			fos.flush();
			fos.close();
			fis.close();
			
			makeText(this, "Saved on: " + copyFile.getPath(), LENGTH_LONG).show();
			
			String savedfonts = prefs.getString(SAVED_FONTS_KEY, "");
			if(!savedfonts.contains(copyFile.getPath()))
				editor.putString(SAVED_FONTS_KEY, savedfonts += "," + copyFile.getPath()).commit();
		} catch(Exception e) {
			e.printStackTrace();
			makeText(this, e.getMessage(), LENGTH_LONG).show();
		}
	}
	
	private void listAllSavedFonts()
	{
		String savedFonts = prefs.getString(SAVED_FONTS_KEY, "");
		
		if(!savedFonts.isEmpty())
		{
			String[] fontPaths = savedFonts.split(",");
			String names = "";
			
			for(String path : fontPaths)
			{
				File fontFile = new File(path);
				names += fontFile.getName() + "\n";
			}
			
			display.setText("Imported Fonts:\n\n" + names);
		}
		else
			display.setText("Imported Fonts:\n\nNo imported fonts");
	}
}
