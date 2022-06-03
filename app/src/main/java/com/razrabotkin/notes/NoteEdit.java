/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.razrabotkin.notes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.razrabotkin.notes.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NoteEdit extends Activity {

    private EditText mBodyText;			// Текст заметки
    private TextView mDateText;			// Дата заметки
    private static Long mRowId;			// Id заметки
    private NotesDbAdapter mDbHelper;	
    
    // Эти переменные нужны для сравнения текста заметок и определения, 
    // вносились ли изменения
    private String firstText;			// Начальное значение текста заметки
    private String lastText;			// Конечное значение текста заметки
    
    // Это для диалогового окна о подтверждении удаления заметок
    private static final int ALERTTAG = 0;	
    private DialogFragment mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        
        setContentView(R.layout.note_edit);
        
        mBodyText = (EditText) findViewById(R.id.body);
        mDateText = (TextView) findViewById(R.id.date);

        mRowId = (savedInstanceState == null) ? null : 
        	(Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
			
			// Если заметка новая, программно показываем клавиатуру сразу же после создания заметки
			InputMethodManager imm = (InputMethodManager)
			        getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mBodyText, 0);
		}
        
        populateFields();
        
        firstText = mBodyText.getText().toString();	//Запоминаем начальный текст, чтобы потом проверить, был ли он изменён.
        if (firstText.equals("")) {
        	// Если заметка новая, программно показываем клавиатуру сразу же после создания заметки
			InputMethodManager imm = (InputMethodManager)
			        getSystemService(this.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mBodyText, 0);
		}
        
        // Присваиваем вновь созданной заметке текущую дату
        Date currentDate = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm");
    	mDateText.setText(sdf.format(currentDate));
         
    	// Принудительный вызов экранной клавиатуры для новой заметки (не работает)
    	InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
    	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    
    // Добавляем на ActionBar иконку для удаления
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.icon_remove, menu);
        return true;
    }
    
    // Удаление заметки
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_delete:
            	
            	if (mBodyText.getText().toString().equals("")) {
            		finish();
        		}
            	else {
            		showDialogFragment(ALERTTAG);
					return true;
            	}
            	
        }
        return super.onMenuItemSelected(featureId, item);
    }
    
    void showDialogFragment(int dialogID) {

		switch (dialogID) {

		// Show AlertDialog
		case ALERTTAG:

			// Create a new AlertDialogFragment
			mDialog = AlertDialogFragment.newInstance();

			// Show AlertDialogFragment
			mDialog.show(getFragmentManager(), "Alert");

			break;
		
		}
	}
    
    public static class AlertDialogFragment extends DialogFragment {

		public static AlertDialogFragment newInstance() {
			return new AlertDialogFragment();
		}

		// Build AlertDialog using AlertDialog.Builder
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setMessage(R.string.alert_delete)
					
					// User cannot dismiss dialog by hitting back button
					.setCancelable(true)
					
					// Set up No Button
					.setNegativeButton(R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									
								}
							})
							
					// Set up Yes Button
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog, int id) {
									if (mRowId == null) {
										((NoteEdit) getActivity()).mBodyText.setText("");
									} else {
										((NoteEdit) getActivity()).mDbHelper.deleteNote(mRowId);
									}
									((NoteEdit) getActivity()).finish();	
								}
							}).create();
		}
	}
    
    private void populateFields() {
    	if (mRowId != null) {
			Cursor note = mDbHelper.fetchNote(mRowId);
			startManagingCursor(note);
			mBodyText.setText(note.getString(
					note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
			
			mDateText.setText(note.getString(
					note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE)));
			
			// Создаем новый SimpleDateFormat, в котором указываем исходный формат, в котором хранится дата
			SimpleDateFormat format1 = new SimpleDateFormat();
	        format1.applyPattern("yyyy-MM-dd HH:mm:ss.sss");
	        
	        // Создаем новый SimpleDateFormat, в котором указываем формат, в который необходимо преобразовать дату
	        SimpleDateFormat format2 = new SimpleDateFormat();
	        format2.applyPattern("d MMM yyyy HH:mm"); 
	                
	        Date date1;
			try {
				date1 = format1.parse(note.getString(
						note.getColumnIndexOrThrow(NotesDbAdapter.KEY_DATE)));		// Преобразуем значение из базы данных в дату с использованием формата 1
				String date2 = format2.format(date1);								// Форматируем полученную дату с использованием формата 2 и преобразуе её обратно в строку
				mDateText.setText(date2);											// Присваиваем элементу mDateTtext строку с новой датой
			} catch (ParseException e) {
				e.printStackTrace();
			}	
		}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	lastText = mBodyText.getText().toString();
    	if (!firstText.equals(lastText)) {
    		saveState();
		}
    	outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	lastText = mBodyText.getText().toString();
    	if (!firstText.equals(lastText)) {
    		saveState();
		}	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	populateFields();
    }
    
    private void saveState() {
    	String body = mBodyText.getText().toString();
    	String date;
			 
    	Date currentDate = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
    	date = sdf.format(currentDate);
    	
    	if (mRowId == null) {
			long id = mDbHelper.createNote(body, date);
			if (id > 0) {
				mRowId = id;
			}
		} else {
			mDbHelper.updateNote(mRowId, body, date);
		}
    }
    
    
}
