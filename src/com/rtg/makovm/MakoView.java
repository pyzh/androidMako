package com.rtg.makovm;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.view.KeyEvent;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;

import android.graphics.*;

public class MakoView extends View {

	private static final int FRAME_RATE = 1000/60;
	private MakoVM vm;

	public MakoView(Context c, AttributeSet a) {
		super(c, a);
		if(isInEditMode())
			return;
		try {
			int[] rom = loadRom(c.getAssets().open("Loko.rom"), null);
			vm = new MakoVM(rom);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static int[] loadRom(InputStream i, int[] prev) {
		try {
			int romSize = i.available();
			
			// Allocate a byteBuffer, this will be used later to convert the bytes
			// into an integer array
			ByteBuffer buffer = ByteBuffer.allocate(romSize);
			buffer.clear();
			byte[] page = new byte[4096];
			int read = 0;
			int totalRead = 0;
			while(i.available()>0)
			{
				read = i.read(page, 0, 4096);
				totalRead += read;
				buffer.put(page,0, read);
			}
			int[] rom = new int[totalRead/4];
			buffer.rewind();
			IntBuffer intBuf = buffer.asIntBuffer();
			intBuf.get(rom);
			buffer.clear();
			i.close();
			System.out.println("Restored from save file!");
			return rom;
		}
		catch(IOException ioe) {
			System.out.println("Unable to load rom!");
			return prev;
		}
	}

	int keyTimeout = 0;
	public void setKeys(int mask) {
		vm.keys = mask;
		keyTimeout = 1;
	}

	public void keyTyped(int character) {
		vm.keyQueue.add(character);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
				keyTyped(8);
			}
			else {
				keyTyped(event.getUnicodeChar());
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		heightMeasureSpec = MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(widthMeasureSpec)/4)*3, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onDraw(Canvas c) {
		super.onDraw(c);
		if(isInEditMode())
		{
			c.drawColor(0xffff8800);
			return;
		}
		c.save();
		c.scale(2.5f, 2.5f);
		c.drawBitmap(vm.p, 0, 320, 0, 0, 320, 240, false, null);
		c.restore();
		
		vm.run();

		if (keyTimeout == 0) { vm.keys = 0; }
		else { keyTimeout--; }

		postInvalidateDelayed(FRAME_RATE);
	}
}