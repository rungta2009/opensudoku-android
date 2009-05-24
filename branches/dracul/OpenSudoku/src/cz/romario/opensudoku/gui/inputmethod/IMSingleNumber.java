package cz.romario.opensudoku.gui.inputmethod;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuCellNote;

/**
 * This class represents following type of number input workflow: Number buttons are displayed
 * in the sidebar, user selects one number and then fill values by tapping the cells.
 * 
 * @author romario
 *
 */
public class IMSingleNumber extends InputMethod {

	private static final int MODE_EDIT_VALUE = 0;
	private static final int MODE_EDIT_NOTE = 1;
	
	private Handler mGuiHandler;
	private Map<Integer,Button> mNumberButtons;
	private ImageButton mSwitchNumNoteButton;

	private int mSelectedNumber = 1;
	private int mEditMode = MODE_EDIT_VALUE;
	
	public IMSingleNumber() {
		super();
		
		mGuiHandler = new Handler();
	}
	
	@Override
	public int getNameResID() {
		return R.string.single_number;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_single_number_hint;
	}
	
	@Override
	public String getAbbrName() {
		return mContext.getString(R.string.single_number_abbr);
	}
	

	@Override
	protected View createControlPanel() {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_single_number, null);
		
		mNumberButtons = new HashMap<Integer, Button>(); 
		mNumberButtons.put(1, (Button)controlPanel.findViewById(R.id.button_1));
		mNumberButtons.put(2, (Button)controlPanel.findViewById(R.id.button_2));
		mNumberButtons.put(3, (Button)controlPanel.findViewById(R.id.button_3));
		mNumberButtons.put(4, (Button)controlPanel.findViewById(R.id.button_4));
		mNumberButtons.put(5, (Button)controlPanel.findViewById(R.id.button_5));
		mNumberButtons.put(6, (Button)controlPanel.findViewById(R.id.button_6));
		mNumberButtons.put(7, (Button)controlPanel.findViewById(R.id.button_7));
		mNumberButtons.put(8, (Button)controlPanel.findViewById(R.id.button_8));
		mNumberButtons.put(9, (Button)controlPanel.findViewById(R.id.button_9));
		mNumberButtons.put(0, (Button)controlPanel.findViewById(R.id.button_clear));
		
		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(mNumberButtonClicked);
		}
		
		mSwitchNumNoteButton = (ImageButton)controlPanel.findViewById(R.id.switch_num_note);
		mSwitchNumNoteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE : MODE_EDIT_VALUE;
				update();
			}
			
		});
		
		return controlPanel;
	}
	
	private OnClickListener mNumberButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mSelectedNumber = (Integer)v.getTag();
			
			update();
		}
	};
	
	private void update() {
		switch (mEditMode) {
		case MODE_EDIT_NOTE:
			mSwitchNumNoteButton.setImageResource(R.drawable.pencil);
			break;
		case MODE_EDIT_VALUE:
			mSwitchNumNoteButton.setImageResource(R.drawable.pencil_disabled);
			break;
		}
		
		// TODO: sometimes I change background too early and button stays in pressed state
		// this is just ugly workaround
		mGuiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (Button b : mNumberButtons.values()) {
					if (b.getTag().equals(mSelectedNumber)) {
						b.setTextAppearance(mContext, android.R.style.TextAppearance_Large_Inverse);
						// TODO: add color to resources
						b.getBackground().setColorFilter(new LightingColorFilter(Color.rgb(240, 179, 42), 0));
					} else {
						b.setTextAppearance(mContext, android.R.style.TextAppearance_Widget_Button);
						b.getBackground().setColorFilter(null);
					}
				}
			}
		}, 100);
		
	}

	@Override
	protected void onActivated() {
		update();
	}
	
	@Override
	protected void onCellTapped(SudokuCell cell) {
		int selNumber = mSelectedNumber;
		
		switch (mEditMode) {
		case MODE_EDIT_NOTE:
			if (selNumber == 0) {
				mGame.setCellNote(cell, null);
				mBoard.postInvalidate();
				
			} else if (selNumber > 0 && selNumber <= 9) {
				SudokuCellNote newNote = cell.getNote().clone();
				newNote.toggleNumber(selNumber);
				mGame.setCellNote(cell, newNote);
				// TODO: board should know when data changes on itself
				mBoard.postInvalidate();
			}
			break;
		case MODE_EDIT_VALUE:
			if (selNumber >= 0 && selNumber <= 9) {
				if (selNumber == cell.getValue()) {
					selNumber = 0;
				}
				mGame.setCellValue(cell, selNumber);
				mBoard.postInvalidate();
			}
			break;
		}
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(getInputMethodName() + ".sel_number", mSelectedNumber);
		outState.putInt(getInputMethodName() + ".edit_mode", mEditMode);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		
		mSelectedNumber = savedInstanceState.getInt(getInputMethodName() + ".sel_number");
		mEditMode = savedInstanceState.getInt(getInputMethodName() + ".edit_mode");
		if (isControlPanelCreated()) {
			update();
		}
	}

}
