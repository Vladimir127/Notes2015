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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.razrabotkin.notes.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
// Реклама
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.InterstitialAd;

public class Notepadv3 extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int COPY_ID = Menu.FIRST + 2;

    private static final int SET_PASSWORD_DIALOG = 0;
    
    private static String PASSWORD = "password";
    private static String PROTECTED_BY_PASSWORD = "protectedByPassword";
    
    private NotesDbAdapter mDbHelper;
    
    private DialogFragment mDialog;
    
    private static EditText mSetPasswordText;	
    private static EditText mConfirmPasswordText;
    private static TextView mInformationText;
    
    private static SharedPreferences prefs;
    //private boolean showAd = true;
    
    // Реклама
    //InterstitialAd mInterstitialAd;
    
    /** Запускается, когда деятельность впервые запущена */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_list);
        
        // Получаем SharedPreferences для пароля
        prefs = getPreferences(MODE_PRIVATE);
        boolean protectedByPassword = prefs.getBoolean(PROTECTED_BY_PASSWORD, false);
        
        // Реклама
       	//mInterstitialAd = new InterstitialAd(this);
        //mInterstitialAd.setAdUnitId("ca-app-pub-1694320985526388/5201162751");
        //requestNewInterstitial();
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	// Реклама
        //showAd = true; 
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor notesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(notesCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.KEY_BODY, NotesDbAdapter.KEY_DATE};
             
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.text2};

        // Now create a simple cursor adapter and set it to display
        MySimpleCursorAdapter notes = 
            new MySimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        setListAdapter(notes);
    }

    class MySimpleCursorAdapter extends SimpleCursorAdapter {

		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}
    	
		// Этот класс необходим для форматирования даты в списке заметок
		@Override
		public void setViewText(TextView v, String text) {
			// Если это text2, который хранит дату заметки
			if (v.getId() == R.id.text2) {
		        
				// Создаем новый SimpleDateFormat, в котором указываем исходный формат, в котором хранится дата
				SimpleDateFormat format1 = new SimpleDateFormat();
		        format1.applyPattern("yyyy-MM-dd HH:mm:ss.sss");
		        
		        // Создаем новый SimpleDateFormat, в котором указываем формат, в который необходимо преобразовать дату
		        SimpleDateFormat format2 = new SimpleDateFormat();
		        format2.applyPattern("d MMM yyyy"); 
		                
		        Date date1 = null;				// Дата в расширенном формате, как она хранится в БД
		        Date currentDate = new Date();
		        String date2 = text;			// Дата в читабельном формате для отображения в списке
		        
				try {
					date1 = format1.parse(text);	// Преобразуем текст в дату с использованием формата 1
					date2 = format2.format(date1);	// Форматируем полученную дату с использованием формата 2 и преобразуе её обратно в строку
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				// Если удалось распознать в правильном формате дату заметки
				if (date1 != null) {
					
					// Получаем текущую дату и дату заметки в миллисекундах
					long noteDateMilliseconds = date1.getTime();
					long currentDateMilliseconds = currentDate.getTime();
					
					// Выделяем из даты создания день (это нужно для определения Вчера и сегодня)
					GregorianCalendar calendar1 = new GregorianCalendar();
					calendar1.setTime(date1);
					int creatingDay = calendar1.get(Calendar.DATE);
					
					// Выделяем из текущей даты день для этих же целей
					GregorianCalendar calendar2 = new GregorianCalendar();
					calendar2.setTime(currentDate);
					int currentDay = calendar2.get(Calendar.DATE);
					
					// Выделяем из даты создания время для этих же целей
					SimpleDateFormat timeFormat = new SimpleDateFormat();
			        timeFormat.applyPattern("HH:mm");
			        String time = timeFormat.format(date1);
					
					// Вычисляем "возраст" заметки, вычитая из одной величины другую
					int noteAge = (int) (currentDateMilliseconds - noteDateMilliseconds);	
					
					if (noteAge < 0) {																	// Загрушка: если возраст заметки вдруг оказался меньше нуля, 
						text = date2;																	// просто присваиваем дату создания, чтобы не получилось, как ВКонтакте - заметка создана завтра в 0:15 
					} else if (noteAge < 1000 * 5) {													// Если возраст заметки менее 5 секунд, выводим текст "Только что"
						text = (String) getText(R.string.just_now);
					} else if (noteAge < 1000 * 60) {													// Если меньше минуты - 
						text = noteAge / 1000 + " " + (String) getText(R.string.seconds_ago);			// столько-то секунд назад
					} else if (noteAge < 1000 * 60 * 60) {												// Если меньше часа - 
						text = noteAge / (1000 * 60) + " " + (String) getText(R.string.minutes_ago);	// столько-то минут назад
					} else if (noteAge < 1000 * 60 * 60 * 2) {											// Если меньше двух часов, 
						text = (String) getText(R.string.one_hour_ago);									// пишем - час назад
					} else if (noteAge < 1000 * 60 * 60 * 3) {											// Меньше трёх часов - 
						text = (String) getText(R.string.two_hours_ago);								// два часа назад
					} else if (noteAge < 1000 * 60 * 60 * 4) {											// Меньше четырёх -
						text = (String) getText(R.string.three_hours_ago);								// три часа назад
					} else if (creatingDay == currentDay) {												// Если день создания и текущий день совпадают, -
						text = (String) getText(R.string.today_at) + " " + time;						// пишем "Сегодня" и ставим время
					} else if (currentDay - creatingDay == 1) {											// Если день создания меньше текущего дня на один день, -
						text = (String) getText(R.string.yesterday_at) + " " + time;					// пишем "Вчера" и ставим время
					} else {																			// Иначе просто присваиваем дату создания.
						text = date2;	// Присваиваем аргументу text строку с новой датой				  
					}
				}								
			}
			super.setViewText(v, text);
		}
    }
    
    // Используется, чтобы заполнять меню для деятельности. 
    // Показывается, когда пользователь нажимает кнопку меню, и имеет список опций
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.icon_new, menu);
        //inflater.inflate(R.menu.main, menu);
        return true;
    }

    // Используется для обработки событий, вызываемых из меню
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
//        	case R.id.password_protect:
//	        	showDialogFragment(SET_PASSWORD_DIALOG);
//	            return true;
            case R.id.action_new:
            	createNote();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

//    private void showDialogFragment(int dialogID) {
//		
//    	switch (dialogID) {
//		case SET_PASSWORD_DIALOG:
//			// Create a new AlertDialogFragment
//			mDialog = AlertDialogFragment.newInstance();
//
//			// Show AlertDialogFragment
//			mDialog.show(getFragmentManager(), "Alert");
//			break;
//
//		default:
//			break;
//		}
//	}
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        //super.onCreateContextMenu(menu, v, menuInfo);
        //menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	switch(item.getItemId()) {
            case DELETE_ID:
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
    	//showAd = false;
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	//showAd = false;
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
//    public static class AlertDialogFragment extends DialogFragment {
//
//    	public static AlertDialogFragment newInstance() {
//    		return new AlertDialogFragment();
//    	}
//
//    	// Build AlertDialog using AlertDialog.Builder
//    	@Override
//    	public Dialog onCreateDialog(Bundle savedInstanceState) {
//    		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//    								
//    				// Получаем layout inflater
//    		        LayoutInflater inflater = getActivity().getLayoutInflater();
//    		
//    		        // Заполняем (Inflate) и устанавливаем layout для диалога
//    		        // Передаем null как родительский элемент потому что он идет в диалоговом layout
//    		        final View layout = inflater.inflate(R.layout.dialog_set_password, null);
//    		        builder.setView(layout)	
//    		        .setTitle(R.string.password_protect)
//    				// Set up No Button
//    				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//    	                   @Override
//    	                   public void onClick(DialogInterface dialog, int id) {
//    	                	   mSetPasswordText = (EditText) layout.findViewById(R.id.setPassword);
//    	                	   mConfirmPasswordText = (EditText) layout.findViewById(R.id.confirmPassword);
//    	                	   mInformationText = (TextView) layout.findViewById(R.id.passwordInformation);
//    	                	   
//    	                	   String setPassword = mSetPasswordText.getText().toString();
//    	                	   String confirmPassword = mSetPasswordText.getText().toString();
//    	                	   
//    	                	   if (setPassword.equals(confirmPassword)) {
//    	                		   SharedPreferences.Editor editor = prefs.edit();
//	    	       					editor.putString(PASSWORD, setPassword);
//	    	       					editor.putBoolean(PROTECTED_BY_PASSWORD, true);
//	    	       					editor.commit();
//    	                		   
//								
//								} else {
//									mInformationText.setText("Пароли не совпадают");
//									mInformationText.setTextColor(Color.RED);
//									
//								}
//    	                   }
//    	               })
//    	            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//    	                   public void onClick(DialogInterface dialog, int id) {
//    	                       //LoginDialogFragment.this.getDialog().cancel();
//    	                   }
//    	               });
//    		        return builder.create();
//    	}
//    }
    
    @Override
    public void onBackPressed(){
    	super.onBackPressed();
    	// Объявление будет показано в том случае, если будет нажата кнопка Назад
//    	if (mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//        }
    }
    
    // Реклама
//	private void requestNewInterstitial() {
//		AdRequest adRequest = new AdRequest.Builder()
//        .addTestDevice("YOUR_DEVICE_HASH")
//        .build();
//
//		mInterstitialAd.loadAd(adRequest);
//	}
    
}


